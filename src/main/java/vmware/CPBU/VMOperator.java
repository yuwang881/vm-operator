package vmware.CPBU;

import com.fasterxml.jackson.databind.util.JSONPObject;
import com.vmware.vim25.*;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinition;
import io.fabric8.kubernetes.api.model.apiextensions.CustomResourceDefinitionBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.Watcher;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.dsl.base.CustomResourceDefinitionContext;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformerFactory;
import vmware.CPBU.controller.ContentLibraryController;
import vmware.CPBU.controller.ResourcePolicyController;
import vmware.CPBU.controller.VirtualMachineController;
import vmware.CPBU.controller.VirtualMachineSimpleController;
import vmware.CPBU.crd.contentlibrary.ContentLibrary;
import vmware.CPBU.crd.contentlibrary.ContentLibraryList;
import vmware.CPBU.crd.contentlibrary.DoneableContentLibrary;
import vmware.CPBU.crd.resourcepolicy.DoneableVirtualMachineSetResourcePolicy;
import vmware.CPBU.crd.resourcepolicy.VirtualMachineSetResourcePolicy;
import vmware.CPBU.crd.resourcepolicy.VirtualMachineSetResourcePolicyList;
import vmware.CPBU.crd.vm.*;

import vmware.CPBU.crd.vmclass.VirtualMachineClass;
import vmware.CPBU.crd.vmclass.VirtualMachineClassList;
import vmware.CPBU.crd.vmimage.DoneableVirtualMachineImage;
import vmware.CPBU.crd.vmimage.VirtualMachineImage;
import vmware.CPBU.crd.vmimage.VirtualMachineImageList;
import vmware.CPBU.exceptions.VMOperatorException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.logging.Logger;

public class VMOperator {
    private static KubernetesClient k8sCoreClient;
    private static SharedInformerFactory informerFactory;
    private static CustomResourceDefinition contentLibraryCRD;
    private static CustomResourceDefinition vmImageCRD;
    private static CustomResourceDefinition resourcePolicyCRD;
    private static CustomResourceDefinition vmCRD;
    private static CustomResourceDefinition virtMachineCRD;
    private static CustomResourceDefinitionContext contentLibraryCRDContext;
    private static CustomResourceDefinitionContext vmImageCRDContext;
    private static CustomResourceDefinitionContext resourcePolicyCRDContext;
    private static CustomResourceDefinitionContext vmCRDContext;
    private static CustomResourceDefinitionContext virtMachineCRDContext;
    private static CustomResourceDefinitionContext vmClassCRDContext;
    private static SharedIndexInformer<ContentLibrary> contentLibrarySharedIndexInformer;
    private static SharedIndexInformer<VirtualMachineImage> vmImageSharedIndexInformer;
    private static SharedIndexInformer<VirtualMachineSetResourcePolicy> resourcePolicySharedIndexInformer;
    private static SharedIndexInformer<VirtualMachine> vmSharedIndexInformer;
    private static SharedIndexInformer<VirtualMachine> virtMachineSharedIndexInformer;
    private static SharedIndexInformer<VirtualMachineClass> vmClassSharedIndexInformer;
    private static ContentLibraryController clController;
    private static ResourcePolicyController resourcePolicyController;
    private static VirtualMachineController vmController;
    private static VirtualMachineSimpleController vmSimpleController;
    private static MixedOperation<
            ContentLibrary,
            ContentLibraryList,
            DoneableContentLibrary,
            Resource<ContentLibrary, DoneableContentLibrary>> contentLibraryClient;
    private static MixedOperation<
            VirtualMachineImage,
            VirtualMachineImageList,
            DoneableVirtualMachineImage,
            Resource<VirtualMachineImage, DoneableVirtualMachineImage>> vmImageClient;
    private static MixedOperation<
            VirtualMachine,
            VirtualMachineList,
            DoneableVirtualMachine,
            Resource<VirtualMachine, DoneableVirtualMachine>> vmClient;
    private static MixedOperation<
            VirtualMachine,
            VirtualMachineList,
            DoneableVirtualMachine,
            Resource<VirtualMachine, DoneableVirtualMachine>> virtMachineClient;
    private static MixedOperation<
            VirtualMachineSetResourcePolicy,
            VirtualMachineSetResourcePolicyList,
            DoneableVirtualMachineSetResourcePolicy,
            Resource<VirtualMachineSetResourcePolicy, DoneableVirtualMachineSetResourcePolicy>> resourcePolicyClient;
    public static Logger logger = Logger.getLogger(VMOperator.class.getName());

    private static void init(String kubeconfig) {
        if (kubeconfig != null) {
            File configYml = new File(kubeconfig);
            try (InputStream is = new FileInputStream(configYml)) {
                k8sCoreClient = DefaultKubernetesClient.fromConfig(is);
            } catch (Exception e) {
                System.out.println("Read file failed: "+ kubeconfig);
            }

        }

        if (k8sCoreClient == null)
            k8sCoreClient = new DefaultKubernetesClient();

        if (k8sCoreClient != null) {
            String cluster = k8sCoreClient.getMasterUrl().toString();
            String namespace = k8sCoreClient.getNamespace();
            System.out.println("Get K8S Cluster info: ");
            System.out.println("Master URL: "+ cluster);
            System.out.println("Namespace: "+namespace);
        }
        informerFactory = k8sCoreClient.informers();
    }


    private static void createCRDs() {
        contentLibraryCRD = new CustomResourceDefinitionBuilder()
                .withNewMetadata()
                .withName("contentlibraries.vmoperator.wangyu.cpbu.vmware")
                .endMetadata()
                .withNewSpec()
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withVersion("v1beta1")
                .withNewNames()
                .withKind("ContentLibrary")
                .withPlural("contentlibraries")
                .endNames()
                .withScope("Cluster")
                .endSpec()
                .build();

        vmImageCRD =  new CustomResourceDefinitionBuilder()
                .withNewMetadata()
                .withName("virtualmachineimages.vmoperator.wangyu.cpbu.vmware")
                .endMetadata()
                .withNewSpec()
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withVersion("v1beta1")
                .withNewNames()
                .withKind("VirtualMachineImage")
                .withPlural("virtualmachineimages")
                .endNames()
                .withScope("Cluster")
                .endSpec()
                .build();

        vmCRD =  new CustomResourceDefinitionBuilder()
                .withNewMetadata()
                .withName("virtualmachines.vmoperator.wangyu.cpbu.vmware")
                .endMetadata()
                .withNewSpec()
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withVersion("v1beta1")
                .withNewNames()
                .withKind("VirtualMachine")
                .withPlural("virtualmachines")
                .endNames()
                .withScope("Namespaced")
                .endSpec()
                .build();

        virtMachineCRD =  new CustomResourceDefinitionBuilder()
                .withNewMetadata()
                .withName("virtualmachines.vmoperator.wangyu.cpbu.vmware")
                .endMetadata()
                .withNewSpec()
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withVersion("v1beta1")
                .withNewNames()
                .withKind("VirtMachine")
                .withPlural("virtualmachines")
                .endNames()
                .withScope("Namespaced")
                .endSpec()
                .build();

        resourcePolicyCRD =  new CustomResourceDefinitionBuilder()
                .withNewMetadata()
                .withName("virtualmachinesetresourcepolicies.vmoperator.wangyu.cpbu.vmware")
                .endMetadata()
                .withNewSpec()
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withVersion("v1beta1")
                .withNewNames()
                .withKind("VirtualMachineSetResourcePolicy")
                .withPlural("virtualmachinesetresourcepolicies")
                .endNames()
                .withScope("Namespaced")
                .endSpec()
                .build();

        contentLibraryCRDContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1beta1")
                .withScope("Cluster")
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withPlural("contentlibraries")
                .build();

        vmImageCRDContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1beta1")
                .withScope("Cluster")
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withPlural("virtualmachineimages")
                .build();

        vmClassCRDContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1beta1")
                .withScope("Cluster")
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withPlural("virtualmachineclasses")
                .build();

        vmCRDContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1beta1")
                .withScope("Namespaced")
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withPlural("virtualmachines")
                .build();

        virtMachineCRDContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1beta1")
                .withScope("Namespaced")
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withPlural("virtualmachines")
                .build();

        resourcePolicyCRDContext = new CustomResourceDefinitionContext.Builder()
                .withVersion("v1beta1")
                .withScope("Namespaced")
                .withGroup("vmoperator.wangyu.cpbu.vmware")
                .withPlural("virtualmachinesetresourcepolicies")
                .build();
    }

    private static void createCRDClients() {
        contentLibraryClient = k8sCoreClient.customResources(
                contentLibraryCRDContext,
                ContentLibrary.class,
                ContentLibraryList.class,
                DoneableContentLibrary.class);

        vmImageClient = k8sCoreClient.customResources(
                vmImageCRDContext,
                VirtualMachineImage.class,
                VirtualMachineImageList.class,
                DoneableVirtualMachineImage.class);

        vmClient = k8sCoreClient.customResources(
                vmCRDContext,
                VirtualMachine.class,
                VirtualMachineList.class,
                DoneableVirtualMachine.class);

        virtMachineClient = k8sCoreClient.customResources(
                virtMachineCRDContext,
                VirtualMachine.class,
                VirtualMachineList.class,
                DoneableVirtualMachine.class);

        resourcePolicyClient = k8sCoreClient.customResources(
                resourcePolicyCRDContext,
                VirtualMachineSetResourcePolicy.class,
                VirtualMachineSetResourcePolicyList.class,
                DoneableVirtualMachineSetResourcePolicy.class);
    }

    private static void createInformers() {
        contentLibrarySharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(
                contentLibraryCRDContext,
                ContentLibrary.class,
                ContentLibraryList.class,
                10 * 60 * 1000);

        vmImageSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(
                vmImageCRDContext,
                VirtualMachineImage.class,
                VirtualMachineImageList.class,
                10 * 60 * 1000);

        vmClassSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(
                vmClassCRDContext,
                VirtualMachineClass.class,
                VirtualMachineClassList.class,
                10 * 60 * 1000);


        resourcePolicySharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(
                resourcePolicyCRDContext,
                VirtualMachineSetResourcePolicy.class,
                VirtualMachineSetResourcePolicyList.class,
                10 * 60 * 1000);

        vmSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(
                vmCRDContext,
                VirtualMachine.class,
                VirtualMachineList.class,
                10 * 60 * 1000);

//        virtMachineSharedIndexInformer = informerFactory.sharedIndexInformerForCustomResource(
//                virtMachineCRDContext,
//                VirtualMachine.class,
//                VirtualMachineList.class,
//                10 * 60 * 1000);
    }

    private static void createController() throws Exception {
        clController = new ContentLibraryController(
                k8sCoreClient,
                contentLibraryClient,
                contentLibrarySharedIndexInformer,
                vmImageClient,
                vmImageSharedIndexInformer);

        resourcePolicyController = new ResourcePolicyController(
                k8sCoreClient,
                resourcePolicyClient,
                resourcePolicySharedIndexInformer);

        vmController = new VirtualMachineController(
                k8sCoreClient,
                vmClient,
                vmSharedIndexInformer,
                vmClassSharedIndexInformer,
                vmImageSharedIndexInformer,
                resourcePolicySharedIndexInformer
        );

//        vmSimpleController = new VirtualMachineSimpleController(
//                k8sCoreClient,
//                virtMachineClient,
//                virtMachineSharedIndexInformer
//        );

        vmController.init();
        clController.init();
        resourcePolicyController.init();
//        vmSimpleController.init();

    }

    private static void startAll() throws RuntimeFaultFaultMsg, InsufficientResourcesFaultFaultMsg, InvalidPropertyFaultMsg, DuplicateNameFaultMsg, InvalidNameFaultMsg, NotFoundFaultMsg, VMOperatorException {

        informerFactory.startAllRegisteredInformers();
        (new Thread(vmController)).start();
        (new Thread(clController)).start();
        (new Thread(resourcePolicyController)).start();
//        (new Thread(vmSimpleController)).start();
    }


    public static void process(String configFile) throws Exception {
        init(configFile);
        createCRDs();
        createCRDClients();
        createInformers();
        createController();
        startAll();
        //test();
    }

    private static void test() throws IOException {
        Map<String,Object> result= k8sCoreClient.customResource(virtMachineCRDContext).list();
        System.out.println(result);

        k8sCoreClient
            .customResource(virtMachineCRDContext)
            .watch(
                "default",
                new Watcher<String>() {
                @Override
                public void eventReceived(Action action, String s) {
                    String act = action.toString();
                    System.out.println("get action: " + act);
                    System.out.println("get Json:" + s);
                }

                @Override
                public void onClose(KubernetesClientException e) {}
                });
    }


}
