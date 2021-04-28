package vmware.CPBU.controller;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.MixedOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.kubernetes.client.informers.ResourceEventHandler;
import io.fabric8.kubernetes.client.informers.SharedIndexInformer;
import io.fabric8.kubernetes.client.informers.SharedInformer;
import io.fabric8.kubernetes.client.informers.cache.Lister;
import vmware.CPBU.crd.virtmachine.*;



public class VirtualMachineSimpleController implements Runnable{


    private SharedInformer<VirtualMachine> vmInformer;
    private Lister<VirtualMachine> vmLister;
    private KubernetesClient k8sCoreClient;
    private MixedOperation<
            VirtualMachine,
            VirtualMachineList,
            DoneableVirtualMachine,
            Resource<VirtualMachine,DoneableVirtualMachine>
            > vmClient;



    public VirtualMachineSimpleController(
            KubernetesClient k8sClient,
            MixedOperation<
                    VirtualMachine,
                    VirtualMachineList,
                    DoneableVirtualMachine,
                    Resource<VirtualMachine,DoneableVirtualMachine>
                    > vmClient,
            SharedIndexInformer<VirtualMachine> vmInformer
    ) {
        this.k8sCoreClient = k8sClient;
        this.vmClient = vmClient;
        this.vmInformer = vmInformer;
        this.vmLister = new Lister<>(vmInformer.getIndexer());

    }

    public void init() {
    vmInformer.addEventHandler(
        new ResourceEventHandler<VirtualMachine>() {
          @Override
          public void onAdd(VirtualMachine vm) {
            System.out.println("This is VM Add event: "+ vm.getMetadata().getName());
          }

          @Override
          public void onUpdate(VirtualMachine oldVM, VirtualMachine newVM) {
              System.out.println("This is VM Update event!"+ newVM.getMetadata().getName());
          }

          @Override
          public void onDelete(VirtualMachine vm, boolean b) {
              System.out.println("This is VM Delete event!"+ vm.getMetadata().getName());
          }
        });
    }


    @Override
    public void run() {
        System.out.println("Waiting the VM Informer to be sycned...");
        while(!vmInformer.hasSynced());
        System.out.println("The VM Informer has sycned!!");

        //testCreateVM();
    }

    private void testCreateVM() {
        VirtualMachine vm = new VirtualMachine();
        ObjectMeta metaData = new ObjectMeta();
        metaData.setName("mytestvm");
        metaData.setNamespace("default");
        vm.setMetadata(metaData);
        VirtualMachineSpec spec = new VirtualMachineSpec();
        spec.setClassName("mylarge");
        spec.setImageName("myimage");
        spec.setPowerState("poweredOn");
        spec.setResourcePolicyName("mypolicyname");
        spec.setStorageClass("mydatastore");
        vm.setSpec(spec);
        vmClient.inNamespace(metaData.getNamespace()).withName(metaData.getName()).createOrReplace(vm);

        int size  = vmLister.list().size();
        System.out.println("------------------"+size);
    }
}
