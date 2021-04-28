package vmware.CPBU.controller;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.api.model.OwnerReference;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import vmware.CPBU.crd.contentlibrary.ContentLibrary;
import vmware.CPBU.crd.contentlibrary.ContentLibraryList;
import vmware.CPBU.crd.contentlibrary.ContentLibraryStatus;
import vmware.CPBU.crd.contentlibrary.DoneableContentLibrary;
import vmware.CPBU.crd.vmimage.*;
import vmware.CPBU.services.ContentLibraryService;
import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.VMService;
import vmware.CPBU.utils.Constants;
import vmware.CPBU.utils.KubernetesHelper;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ContentLibraryController implements Runnable{

    private LoginService loginService;
    private ContentLibraryService clService;
    private VMService vmService;

    private BlockingQueue<String> workqueue;
    private SharedInformer<ContentLibrary> contentLibraryInformer;
    private SharedInformer<VirtualMachineImage> vmImageInformer;
    private Lister<ContentLibrary> contentLibraryLister;
    private Lister<VirtualMachineImage> vmImageLister;
    private KubernetesClient k8sCoreClient;
    private MixedOperation<
            ContentLibrary,
            ContentLibraryList,
            DoneableContentLibrary,
            Resource<ContentLibrary,DoneableContentLibrary>> contentLibraryClient;

    private MixedOperation<
            VirtualMachineImage,
            VirtualMachineImageList,
            DoneableVirtualMachineImage,
            Resource<VirtualMachineImage,DoneableVirtualMachineImage>> vmImageClient;

    public static Logger logger = Logger.getLogger(ContentLibraryController.class.getName());

    public ContentLibraryController(
            KubernetesClient k8sClient,
            MixedOperation<
                    ContentLibrary,
                    ContentLibraryList,
                    DoneableContentLibrary,
                    Resource<ContentLibrary,DoneableContentLibrary>> contentLibraryClient,
            SharedIndexInformer<ContentLibrary> contentLibraryInformer,
            MixedOperation<
                    VirtualMachineImage,
                    VirtualMachineImageList,
                    DoneableVirtualMachineImage,
                    Resource<VirtualMachineImage,DoneableVirtualMachineImage>> vmImageClient,
            SharedIndexInformer<VirtualMachineImage> vmImageInformer) throws Exception {

        this.k8sCoreClient = k8sClient;
        this.contentLibraryClient = contentLibraryClient;
        this.contentLibraryInformer = contentLibraryInformer;
        this.contentLibraryLister = new Lister<>(contentLibraryInformer.getIndexer());
        this.vmImageClient = vmImageClient;
        this.vmImageInformer = vmImageInformer;
        this.vmImageLister = new Lister<>(vmImageInformer.getIndexer());
        this.workqueue = new ArrayBlockingQueue<>(1024);

        String namespace = k8sClient.getNamespace();
        if(namespace == null) namespace = Constants.NAMESPACE_NAME;
        String vcurl = KubernetesHelper.getStringValueFromSecret(
                k8sClient,
                namespace,
                Constants.SECRET_NAME,
                "vcurl");
        String username = KubernetesHelper.getStringValueFromSecret(
                k8sClient,
                namespace,
                Constants.SECRET_NAME,
                "username");
        String passwd = KubernetesHelper.getStringValueFromSecret(
                k8sClient,
                namespace,
                Constants.SECRET_NAME,
                "password");

        this.loginService = new LoginService().setServer(vcurl)
                .setUsername(username).setPassword(passwd);
        this.loginService.login();
        this.clService = new ContentLibraryService(loginService);
        this.vmService = new VMService(loginService);
    }

    public void init() {
    contentLibraryInformer.addEventHandler(
        new ResourceEventHandler<ContentLibrary>() {
          @Override
          public void onAdd(ContentLibrary contentLibrary) {
              logger.log(Level.INFO, "Add Content library Event: " +
                      contentLibrary.getMetadata().getName());
              enqueueContentLibrary(contentLibrary);
          }

          @Override
          public void onUpdate(ContentLibrary oldContentLibrary, ContentLibrary newContentLibrary) {
              logger.log(Level.INFO, "Add Content library Event: " +
                      newContentLibrary.getMetadata().getName());
              String oldObjectVersion = oldContentLibrary.getMetadata().getResourceVersion();
              String newObjectVersion = newContentLibrary.getMetadata().getResourceVersion();
              if (oldObjectVersion.equals(newObjectVersion)) {
                  logger.log(Level.INFO, "This Is Not a Really Content Library Update Event, Ignored ");
                  return;
            }
              enqueueContentLibrary(newContentLibrary);
          }

          @Override
          public void onDelete(ContentLibrary contentLibrary, boolean b) {
            String name = contentLibrary.getMetadata().getName();
            logger.log(Level.WARNING, "ContentLibrary " + name + " is Deleted!");
          }
        });
    }

    private void enqueueContentLibrary(ContentLibrary cl) {
        String key = Cache.metaNamespaceKeyFunc(cl);
        if(key != null && !key.isEmpty()) {
            workqueue.add(key);
        }
    }

    public void run() {
        logger.log(Level.INFO, "Waiting ContentLibraryInformer to be Synchronized");
        while (!contentLibraryInformer.hasSynced());
        logger.log(Level.INFO, "Waiting VirtualMachineImageInformer to be Synchronized");
        while (!vmImageInformer.hasSynced());

        while (true) {
            String key = null;
            try {
                key = workqueue.take();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE,"Content Library Controller interrupted!");
            }
            if (key == null || key.isEmpty()) {
                logger.log(Level.WARNING,"Invalid Resource Key!");
                continue;
            }

            ContentLibrary cl = contentLibraryLister.get(key);
            if (cl == null) {
                logger.log(Level.SEVERE,"ContentLibrary: "+key+" in workqueue no longer exists!");
                return;
            }
            reconcile(cl);
        }
    }

    private void reconcile(ContentLibrary cl) {
        logger.log(Level.INFO, "Inside ContentLibrary Reconcile Process.");
        String clName = cl.getMetadata().getName();
        logger.log(Level.INFO, "The Resource Name of the ContentLibrary: "+ clName);
        String libraryName = cl.getSpec().getLibraryName();
        logger.log(Level.INFO, "The Name of the ContentLibrary: "+ libraryName);

        // get the uuid of the content library
        String cl_uuid = clService.getLibraryIdByName(libraryName);
        if(cl_uuid == null) {
            logger.log(Level.WARNING, "Failed to find ContentLibrary: "+ libraryName);
            return;
        }


        Set<String> ovfFromLibrary = new HashSet<>();
        Set<String> vmTemplateFromLibrary = new HashSet<>();
        Set<String> ovfFromVmImageResource = new HashSet<>();
        Set<String> vmTemplateFromVmImageResource = new HashSet<>();
        Set<String> ovfAddList;
        Set<String> ovfRemoveList;
        Set<String> vmTemplateAddList;
        Set<String> vmTemplateRemoveList;

        //search the ovf and vm templates from the content library
        List<String> uuids = clService.getAllItemsId(libraryName);

        for (String uuid : uuids) {
            Map<String,String> props = clService.getItemPropsById(uuid);
            String itemName = (String) props.get("itemName");
            String itemType = (String) props.get("itemType");

            if (itemType.equals("ovf")) {
                ovfFromLibrary.add(itemName);
            }

            if(itemType.equals("vm-template")) {
                vmTemplateFromLibrary.add(itemName);
            }

        }


        //get all vmimages resources with owner reference to this ContentLibrary
        String clUid = cl.getMetadata().getUid();
        List<VirtualMachineImage> vmImages = this.vmImageLister.list();
        for (VirtualMachineImage vmImage: vmImages) {
            if(getControllerOf(vmImage).getUid().equals(clUid)) {
                if (vmImage.getSpec().getType().equals("ovf"))
                    ovfFromVmImageResource.add(vmImage.getMetadata().getName());
                if (vmImage.getSpec().getType().equals("vm-template"))
                    vmTemplateFromVmImageResource.add(vmImage.getMetadata().getName());
            }
        }

        //create the addList and removeList
        Set<String> intersectOvf = new HashSet<>(ovfFromLibrary);
        intersectOvf.retainAll(ovfFromVmImageResource);

        ovfAddList = new HashSet<>(ovfFromLibrary);
        ovfAddList.removeAll(intersectOvf);

        ovfRemoveList = new HashSet<>(ovfFromVmImageResource);
        ovfRemoveList.removeAll(intersectOvf);

        Set<String> intersectVmTemplate = new HashSet<>(vmTemplateFromLibrary);
        intersectVmTemplate.retainAll(vmTemplateFromVmImageResource);

        vmTemplateAddList = new HashSet<>(vmTemplateFromLibrary);
        vmTemplateAddList.removeAll(intersectVmTemplate);

        vmTemplateRemoveList = new HashSet<>(vmTemplateFromVmImageResource);
        vmTemplateRemoveList.removeAll(intersectVmTemplate);



        //create and remove the vmImage resources from the cluster
        StringBuilder messageBuilder = new StringBuilder();
        for (String vmImageName: ovfAddList) {
            createVmImageInCluster(cl,vmImageName,"ovf");
            messageBuilder.append("OVF Image Created: "+ vmImageName+";");
        }

        for (String vmImageName: vmTemplateAddList) {
            createVmImageInCluster(cl,vmImageName,"vm-template");
            messageBuilder.append("VM Template Image Created: "+ vmImageName+";");
        }

        for(String vmImageName: ovfRemoveList) {
            deleteVmImageInCluster(vmImageName);
            messageBuilder.append("OVF Image Deleted: "+ vmImageName+";");
        }

        for(String vmImageName: vmTemplateRemoveList) {
            deleteVmImageInCluster(vmImageName);
            messageBuilder.append("VM Template Image Deleted: "+ vmImageName+";");
        }

        //update the ContentLibrary status
        //construct the content library status
        ContentLibraryStatus status = new ContentLibraryStatus();
        status.setUuid(cl_uuid);
        status.setMessage(messageBuilder.toString());
        cl.setStatus(status);
        try {
            this.contentLibraryClient.updateStatus(cl);
        } catch (KubernetesClientException e) {
            logger.log(Level.WARNING, "ContentLibrary UpdateStatus failed for: "+clName);
            e.printStackTrace();
        }

    }

    private void createVmImageInCluster(ContentLibrary cl,String itemName, String imageType) {
        VirtualMachineImage vmImage = createNewVmImage(cl,itemName,imageType);
        this.vmImageClient.create(vmImage);
        //this.vmImageClient.updateStatus(vmImage);
    }

    private void deleteVmImageInCluster(String name) {
        this.vmImageClient.withName(name).delete();
    }

    private VirtualMachineImage createNewVmImage(ContentLibrary cl,String itemName, String imageType) {
        VirtualMachineImage vmImage = new VirtualMachineImage();

        //set metadata
        ObjectMeta metaData = new ObjectMeta();
        metaData.setName(itemName);
        //set OwnerReference
        OwnerReference ownerRef = new OwnerReference();
        ownerRef.setApiVersion("vmoperator.wangyu.cpbu.vmware/v1beta1");
        ownerRef.setController(true);
        ownerRef.setKind("ContentLibrary");
        ownerRef.setName(cl.getMetadata().getName());
        ownerRef.setUid(cl.getMetadata().getUid());
        List<OwnerReference> ownerRefs = new ArrayList<>();
        ownerRefs.add(ownerRef);
        metaData.setOwnerReferences(ownerRefs);
        //set contentLibrary and item_id to the annotations
        String itemId= clService.getItemId(cl.getSpec().getLibraryName(),itemName);
        Map<String,String> annotations = new HashMap<>();
        annotations.put(Constants.ANNOTATION_LIB_ITEMID,itemId);
        annotations.put(Constants.ANNOTATION_LIB_NAME,cl.getSpec().getLibraryName());
        metaData.setAnnotations(annotations);
        vmImage.setMetadata(metaData);

        //set spec
        VirtualMachineImageSpec vmImageSpec = new VirtualMachineImageSpec();
        vmImageSpec.setImageSourceType("ContentLibrary");
        vmImageSpec.setType(imageType);
        vmImage.setSpec(vmImageSpec);


        return vmImage;
    }

    private OwnerReference getControllerOf(VirtualMachineImage vmImage) {
        List<OwnerReference> ownerRefs = vmImage.getMetadata().getOwnerReferences();
        for(OwnerReference ownerRef : ownerRefs) {
            if(ownerRef.getController().equals(true)) {
                return ownerRef;
            }
        }
        return null;
    }
}
