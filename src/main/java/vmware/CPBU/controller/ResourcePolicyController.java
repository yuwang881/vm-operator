package vmware.CPBU.controller;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vim25.*;
import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformer;
import io.fabric8.kubernetes.client.informers.cache.Cache;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import vmware.CPBU.crd.resourcepolicy.DoneableVirtualMachineSetResourcePolicy;
import vmware.CPBU.crd.resourcepolicy.ResourcePoolSpec;
import vmware.CPBU.crd.resourcepolicy.VirtualMachineSetResourcePolicy;
import vmware.CPBU.crd.resourcepolicy.VirtualMachineSetResourcePolicyList;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.services.*;
import vmware.CPBU.utils.*;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ResourcePolicyController implements Runnable{

    private LoginService loginService;
    private ResourcePoolService rpoolService;
    private VMFolderService folderService;
    private BlockingQueue<String> workqueue;
    private SharedInformer<VirtualMachineSetResourcePolicy> resourcePolicyInformer;
    private Lister<VirtualMachineSetResourcePolicy> resourcePolicyLister;
    private KubernetesClient k8sCoreClient;
    private MixedOperation<
            VirtualMachineSetResourcePolicy,
            VirtualMachineSetResourcePolicyList,
            DoneableVirtualMachineSetResourcePolicy,
            Resource<VirtualMachineSetResourcePolicy,DoneableVirtualMachineSetResourcePolicy>
            > resourcePolicyClient;
    public static Logger logger = Logger.getLogger(ResourcePolicyController.class.getName());

    public ResourcePolicyController(
            KubernetesClient k8sClient,
            MixedOperation<
                    VirtualMachineSetResourcePolicy,
                    VirtualMachineSetResourcePolicyList,
                    DoneableVirtualMachineSetResourcePolicy,
                    Resource<VirtualMachineSetResourcePolicy,DoneableVirtualMachineSetResourcePolicy>
                    > resourcePolicyClient,
            SharedIndexInformer<VirtualMachineSetResourcePolicy> resourcePolicyInformer
    ) throws Exception {

        this.k8sCoreClient = k8sClient;
        this.resourcePolicyClient = resourcePolicyClient;
        this.resourcePolicyInformer = resourcePolicyInformer;
        this.resourcePolicyLister = new Lister<>(resourcePolicyInformer.getIndexer());

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
        this.folderService = new VMFolderService(loginService);
        this.rpoolService = new ResourcePoolService(loginService);
    }

    public void init() {
        resourcePolicyInformer.addEventHandler(
            new ResourceEventHandler<VirtualMachineSetResourcePolicy>() {

                @Override
                public void onAdd(VirtualMachineSetResourcePolicy resourcePolicy) {
                    enqueueResourcePolicy(resourcePolicy);
                }

                @Override
                public void onUpdate(VirtualMachineSetResourcePolicy oldResourcePolicy,
                                     VirtualMachineSetResourcePolicy newResourcePolicy) {
                    String oldObjectVersion = oldResourcePolicy.getMetadata().getResourceVersion();
                    String newObjectVersion = newResourcePolicy.getMetadata().getResourceVersion();
                    if (oldObjectVersion.equals(newObjectVersion)) {
                        logger.log(Level.INFO, "Not a really Resource Policy Update Event!");
                        return;
                    }
                    enqueueResourcePolicy(newResourcePolicy);
                }

                @Override
                public void onDelete(VirtualMachineSetResourcePolicy resourcePolicy, boolean b) {
                    String name = resourcePolicy.getMetadata().getName();
                    logger.log(Level.INFO, "VirtualMachineSetResourcePolicy " + name + " is deleted!");
                }
            });
    }

    private void enqueueResourcePolicy(VirtualMachineSetResourcePolicy policy) {
        String key = Cache.metaNamespaceKeyFunc(policy);
        if(key != null && !key.isEmpty()) {
            workqueue.add(key);
        }
    }

    public void run() {
        System.out.println("-----------In the ResourcePolicy Controller run----------");
        while (!resourcePolicyInformer.hasSynced());
        while (true) {
            String key = null;
            try {
                key = workqueue.take();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE,"Content Library Controller interrupted!");
            }
            if (key == null || key.isEmpty()) {
                logger.log(Level.WARNING,"invalid resource key!");
                System.out.println("invalid resource key!");
                continue;
            }

            VirtualMachineSetResourcePolicy policy = resourcePolicyLister.get(key);
            if (policy == null) {
                logger.log(Level.SEVERE,"VirtualMachineSetResourcePolicy: "+key+" in workqueue no longer exists!");
                System.out.println("invalid resource key!");
                return;
            }

            try {
                reconcile(policy);
            } catch (RuntimeFaultFaultMsg runtimeFaultFaultMsg) {
                runtimeFaultFaultMsg.printStackTrace();
            } catch (InvalidPropertyFaultMsg invalidPropertyFaultMsg) {
                invalidPropertyFaultMsg.printStackTrace();
            } catch (DuplicateNameFaultMsg duplicateNameFaultMsg) {
                duplicateNameFaultMsg.printStackTrace();
            } catch (InvalidNameFaultMsg invalidNameFaultMsg) {
                invalidNameFaultMsg.printStackTrace();
            } catch (NotFoundFaultMsg notFoundFaultMsg) {
                notFoundFaultMsg.printStackTrace();
            } catch (VMOperatorException e) {
                e.printStackTrace();
            } catch (InsufficientResourcesFaultFaultMsg insufficientResourcesFaultFaultMsg) {
                insufficientResourcesFaultFaultMsg.printStackTrace();
            }
        }
    }

    private void reconcile(VirtualMachineSetResourcePolicy policy) throws
            RuntimeFaultFaultMsg,
            InvalidPropertyFaultMsg,
            DuplicateNameFaultMsg,
            InvalidNameFaultMsg,
            NotFoundFaultMsg,
            VMOperatorException,
            InsufficientResourcesFaultFaultMsg {

        System.out.println("----------VirtualMachineSetResourcePolicy Reconcile Process---------");
        String policyName = policy.getMetadata().getName();
        System.out.println("The VirtualMachineSetResourcePolicy Object Name: "+ policyName);

        //get VM Folder Name from the spec
        //Check if the VMFolder doesn't exist,create it
        String vmFolderName = policy.getSpec().getFolder().getName();


        String namespace = this.k8sCoreClient.getNamespace();
        if(namespace == null) namespace = Constants.NAMESPACE_NAME;
        String datacenter = KubernetesHelper.getStringValueFromConfigMap(
                this.k8sCoreClient,
                namespace,
                Constants.CONFIGMAP_NAME,
                Constants.VM_DATACENTER_NAME);

        if(!folderService.isFolderExist(datacenter,vmFolderName)){
            folderService.createVMFolderInDataCenter(datacenter,vmFolderName);
        }


        //get Resource Pool Info from the Spec
        //Check if the resource Pool doesn't exist, create it
        String clusterName = KubernetesHelper.getStringValueFromConfigMap(
                this.k8sCoreClient,
                namespace,
                Constants.CONFIGMAP_NAME,
                Constants.VM_CLUSTER_NAME);

        ResourcePoolSpec rpSpec = policy.getSpec().getResourcepool();
        String rpName = rpSpec.getName();
        if(!rpoolService.isResourcePoolExist(datacenter,clusterName,rpName)) {
            long cpuReservation = Quantity.getLong(rpSpec.getReservations().getCpu());
            long memoryReservation = Quantity.getLong(rpSpec.getReservations().getMemory());
            long cpuLimit = Quantity.getLong(rpSpec.getLimits().getCpu());
            long memoryLimit = Quantity.getLong(rpSpec.getLimits().getMemory());
            rpoolService.createResourcePoolInCluster(clusterName,rpName,cpuReservation,cpuLimit,memoryReservation,memoryLimit);
        }


        //update the  VirtualMachineSetResourcePolicy annotations if not exists
        //and if we don't find VM_FOLDER_ID and VM_RESOURCEPOLL_ID,
        //we still need to update the annotations
        Map<String,String> annotations = policy.getMetadata().getAnnotations();
        boolean modified = false;
        if(annotations == null || annotations.size()==0) annotations = new HashMap<>();
        if(!annotations.containsKey(Constants.VM_RESOURCEPOLL_NAME)) {
            annotations.put(Constants.VM_DATACENTER_NAME,datacenter);
            annotations.put(Constants.VM_CLUSTER_NAME,clusterName);
            annotations.put(Constants.VM_FOLDER_NAME,vmFolderName);
            annotations.put(Constants.VM_RESOURCEPOLL_NAME,rpName);
            modified = true;
        }

        if (!annotations.containsKey(Constants.VM_FOLDER_ID) || !annotations.containsKey(Constants.VM_RESOURCEPOLL_ID)) {
            StubFactory stubFactory = this.loginService.getVapiAuthHelper().getStubFactory();
            StubConfiguration sessionStubConfig = this.loginService.getSessionStubConfig();
            String vmFolderId = FolderHelper.getFolder(stubFactory,sessionStubConfig,datacenter,vmFolderName);
            String rPoolId = ResourcePoolHelper.getResourcePool(stubFactory,sessionStubConfig,datacenter,rpName);
            annotations.put(Constants.VM_FOLDER_ID,vmFolderId);
            annotations.put(Constants.VM_RESOURCEPOLL_ID,rPoolId);
            modified = true;
        }

        if (modified) {
            ObjectMeta metaData = policy.getMetadata();
            metaData.setAnnotations(annotations);
            policy.setMetadata(metaData);
            resourcePolicyClient.inNamespace(metaData.getNamespace()).withName(metaData.getName()).createOrReplace(policy);
        }
    }


}
