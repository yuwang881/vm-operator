package vmware.CPBU.controller;

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
import vmware.CPBU.crd.resourcepolicy.VirtualMachineSetResourcePolicy;
import vmware.CPBU.crd.vm.*;
import vmware.CPBU.crd.vmclass.VirtualMachineClass;
import vmware.CPBU.crd.vmimage.VirtualMachineImage;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.services.*;
import vmware.CPBU.utils.Constants;
import vmware.CPBU.utils.DelayedVMEvent;
import vmware.CPBU.utils.KubernetesHelper;
import vmware.CPBU.utils.Quantity;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class VirtualMachineController implements Runnable{

    //this cache will lost data after restart of the controller
    //If we don't get vmName from the cache doesn't mean we haven't handled it before
    //So the logic is: if get the vmName from cache, we may shortcut the operation,
    //if absent from the cache, we still get information from the event object.
    private Map<String,VMOpsPhase> cachedVMOpsPhase;
    public enum VMOpsPhase{
        Inited,
        FinalizeSet,
        Creating,
        Created,
        Deleting,
        Deleted,
        PoweringOn,
        PoweredOn,
        PoweringOff,
        PoweredOff}

    private String dataCenter;

    private LoginService loginService;
    private ResourcePoolService rpoolService;
    private VMFolderService folderService;
    private VMService vmService;
    private ContentLibraryService clService;
    private DataStoreService dsService;

    private final BlockingQueue<DelayedVMEvent> workqueue = new DelayQueue<DelayedVMEvent>();

    private SharedInformer<VirtualMachine> vmInformer;
    private SharedInformer<VirtualMachineSetResourcePolicy> resourcePolicyInformer;
    private SharedInformer<VirtualMachineClass> vmClassInformer;
    private SharedInformer<VirtualMachineImage> vmImageInformer;

    private Lister<VirtualMachineSetResourcePolicy> resourcePolicyLister;
    private Lister<VirtualMachine> vmLister;
    private Lister<VirtualMachineClass> vmClassLister;
    private Lister<VirtualMachineImage> vmImageLister;

    private KubernetesClient k8sCoreClient;
    private MixedOperation<
            VirtualMachine,
            VirtualMachineList,
            DoneableVirtualMachine,
            Resource<VirtualMachine,DoneableVirtualMachine>
            > vmClient;

    public static Logger logger = Logger.getLogger(ResourcePolicyController.class.getName());

    public VirtualMachineController(
            KubernetesClient k8sClient,
            MixedOperation<
                    VirtualMachine,
                    VirtualMachineList,
                    DoneableVirtualMachine,
                    Resource<VirtualMachine,DoneableVirtualMachine>
                    > vmClient,
            SharedIndexInformer<VirtualMachine> vmInformer,
            SharedIndexInformer<VirtualMachineClass> vmClassInformer,
            SharedIndexInformer<VirtualMachineImage> vmImageInformer,
            SharedIndexInformer<VirtualMachineSetResourcePolicy> resourcePolicyInformer
    ) throws Exception {

        this.cachedVMOpsPhase = new HashMap<>();
        this.k8sCoreClient = k8sClient;
        this.vmClient = vmClient;

        this.vmInformer = vmInformer;
        this.vmClassInformer = vmClassInformer;
        this.resourcePolicyInformer = resourcePolicyInformer;
        this.vmImageInformer = vmImageInformer;

        this.vmLister = new Lister<>(vmInformer.getIndexer());
        this.vmClassLister = new Lister<>(vmClassInformer.getIndexer());
        this.vmImageLister = new Lister<>(vmImageInformer.getIndexer());
        this.resourcePolicyLister = new Lister<>(resourcePolicyInformer.getIndexer());

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
        this.vmService = new VMService(loginService);
        this.rpoolService = new ResourcePoolService(loginService);
        this.clService = new ContentLibraryService(loginService);
        this.dsService = new DataStoreService(loginService);
        this.dataCenter = KubernetesHelper.getStringValueFromConfigMap(
                this.k8sCoreClient,
                namespace,
                Constants.CONFIGMAP_NAME,
                Constants.VM_DATACENTER_NAME);
    }

    public void init() {
        vmInformer.addEventHandler(
                new ResourceEventHandler<VirtualMachine>() {
                    @Override
                    public void onAdd(VirtualMachine vm) {
                        logger.log(Level.INFO, "We get VM Created event: "
                                + vm.getMetadata().getName());
                        enqueueVirtualMachine(vm,0l);
                    }

                    @Override
                    public void onUpdate(VirtualMachine oldVM,
                                         VirtualMachine newVM) {
                        String oldObjectVersion = oldVM.getMetadata().getResourceVersion();
                        String newObjectVersion = newVM.getMetadata().getResourceVersion();
                        if (oldObjectVersion.equals(newObjectVersion)) {
                            logger.log(Level.INFO,
                                    "This is not a really Virtual Machine UpdateEvent, Ignored!");
                            return;
                        }
                        enqueueVirtualMachine(newVM,0l);
                    }
                    @Override
                    public void onDelete(VirtualMachine vm, boolean b) {
                        //if finalizer is set, the delete event will trigger a update
                        //so we will not enqueue this event
                        String name = vm.getMetadata().getName();
                        logger.log(Level.INFO, "VirtualMachine " + name + " is deleted!");
                    }
                });
    }

    private void enqueueVirtualMachine(VirtualMachine vm,long delayedTime) {
        String key = Cache.metaNamespaceKeyFunc(vm);
        if(key != null && !key.isEmpty()) {
            logger.log(Level.INFO, "VM key: " + key + " is enqueue with delayed time: "+ delayedTime);
            workqueue.add(new DelayedVMEvent(key,delayedTime));
        } else {
            logger.log(Level.WARNING,"Can not get VM key from Cache!");
        }
    }

    @Override
    public void run() {

        logger.log(Level.INFO, "Waiting for VirtualMachineInformer,VirtualMachineClassInformer," +
                "VirtualMachineImageInformer and ResourcePolicyInformer to be Synchronized");

        while (!vmClassInformer.hasSynced());
        logger.log(Level.INFO,"VM Class Informers Synchronized!");

        while (!vmImageInformer.hasSynced());
        logger.log(Level.INFO,"VM Image Informers Synchronized!");

        while (!resourcePolicyInformer.hasSynced());
        logger.log(Level.INFO,"ResourcePolicy Informers Synchronized!");

        while(!vmInformer.hasSynced());
        logger.log(Level.INFO,"VM Informers Synchronized!");


        while (true) {
            String key = null;
            try {
                key = workqueue.take().getVm();
            } catch (InterruptedException e) {
                logger.log(Level.SEVERE,"Virtual Machine Controller interrupted!");
            }
            if (key == null || key.isEmpty()) {
                logger.log(Level.WARNING,"invalid resource key!");
                continue;
            }

            VirtualMachine vm = vmLister.get(key);
            if (vm == null) {
                logger.log(Level.INFO,"VirtualMachine: "+key+" in workqueue no longer exists!");
                continue;
            }
            try {
                reconcile(vm);
            } catch (VMOperatorException |
                    RuntimeFaultFaultMsg |
                    InsufficientResourcesFaultFaultMsg |
                    FileFaultFaultMsg |
                    VmConfigFaultFaultMsg |
                    TaskInProgressFaultMsg |
                    InvalidStateFaultMsg e) {
                logger.log(Level.WARNING,"Reconcile for the VM Failed: "+key);
                e.printStackTrace();
            }
        }
    }

    private void reconcile(VirtualMachine vm) throws
            VMOperatorException,
            RuntimeFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg,
            FileFaultFaultMsg,
            VmConfigFaultFaultMsg,
            TaskInProgressFaultMsg,
            InvalidStateFaultMsg {

        //should check here to decide if it is valid for the reconcile
        VirtualMachineSpec vmSpec = vm.getSpec();
        String resourcePolicyName = vmSpec.getResourcePolicyName();
        String vmClassName = vmSpec.getClassName();
        String vmImageName = vmSpec.getImageName();
        String storageClassName = vmSpec.getStorageClass();

        // check if resourcePolicyName is valid
        VirtualMachineSetResourcePolicy resourcePolicy =
                resourcePolicyLister.get(vm.getMetadata().getNamespace()+"/"+resourcePolicyName);
        if (resourcePolicy == null) {
            logger.log(Level.WARNING,"Failed to find the ResourcePolicy Resource named: " +
                    resourcePolicyName);
            //setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Unknown);
            throw new VMOperatorException(VMOperatorException.ExceptionCode.RESOURCEPOLICY_NOTFOUNT_ERROR);
        }

        // check if vmClassName is valid
        VirtualMachineClass vmClass = vmClassLister.get(vmClassName);
        if (vmClass == null) {
            logger.log(Level.WARNING,"Failed to find the VirtualMachineClass Resource named: " +
                    vmClassName);
            //setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Unknown);
            throw new VMOperatorException(VMOperatorException.ExceptionCode.VMCLASS_NOTFOUNT_ERROR);
        }

        // check if vmImageName is valid
        VirtualMachineImage vmImage = vmImageLister.get(vmImageName);
        if (vmImage == null) {
            logger.log(Level.WARNING,"Failed to find the VirtualMachineImage Resource named: " +
                    vmImageName);
            //setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Unknown);
            throw new VMOperatorException(VMOperatorException.ExceptionCode.VMIMAGE_NOTFOUNT_ERROR);
        }

        // check if StorageClass is valid
        String dataStoreId = dsService.getDatastore(dataCenter,storageClassName);
        if (dataStoreId == null) {
            logger.log(Level.WARNING,"Failed to find the StorageClass Resource named: " +
                    storageClassName);
            //setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Unknown);
            throw new VMOperatorException(VMOperatorException.ExceptionCode.DATASTORE_NOTFOUNT_ERROR);
        }


        //If object is being deleted and the finalizer is still present in finalizers list,
        //then execute the pre-delete logic and remove the finalizer and update the object.
        String deleteTimeStamp = vm.getMetadata().getDeletionTimestamp();
        boolean isFinalizerSet = false;
        List<String> finalizers =  vm.getMetadata().getFinalizers();
        if (finalizers != null && finalizers.contains(Constants.FINALIZER_STRING)) {
            logger.log(Level.INFO,"Finalizer is set for the VM Resource: " +
                    vm.getMetadata().getName());
            isFinalizerSet = true;
        }
        if (deleteTimeStamp == null || deleteTimeStamp.trim().isEmpty()) {
            if (!isFinalizerSet) {
                //If the object is not being deleted and does not have the finalizer registered,
                //then add the finalizer and update the object in Kubernetes.

                // to check if the VM name conflict with existing VM
                // in case that this is a new event
                if( vmService.getVmIdByName(vm.getMetadata().getName()) != null) {
                    logger.log(Level.WARNING,"The VM Name exists already!" +
                            vm.getMetadata().getName());
                    throw new VMOperatorException(VMOperatorException.ExceptionCode.VMNAME_EXIST_ERROR);
                }

                logger.log(Level.INFO,"Finalizer and deleteTimeStamp are not set: " +
                        vm.getMetadata().getName());
                logger.log(Level.INFO,"We will try to add Finalizer to the resource...");
                reconcileAddFinalizer(vm);
            } else {
                logger.log(Level.INFO,"Finalizer is set, but deleteTimeStamp is not: " +
                        vm.getMetadata().getName());
                logger.log(Level.INFO,"This is normal update event.");
                reconcileAddOrUpdate(vm);
            }
        } else {
            if (isFinalizerSet) {
                //If object is being deleted and the finalizer is still present in finalizers list,
                //then execute the pre-delete logic and remove the finalizer and update the object.
                logger.log(Level.INFO,"Both Finalizer and deleteTimeStamp are set: " +
                        vm.getMetadata().getName());
                logger.log(Level.INFO,"This is a delete event.");
                reconcileDelete(vm);
            }
        }
    }

    private void reconcileDelete(VirtualMachine vm) {
        String vmId = vm.getStatus().getUniqueID();
        //if the local cache is not set, this maybe duplicated delete event
        if(! this.cachedVMOpsPhase.containsKey(vmId)) return;

        //update the status to : deleting
        logger.log(Level.INFO,"In ReconsileDelete Process: Set VM Status Phase to Deleting");
        setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Deleting);

        //power off and delete the VM by virtual machine service
        logger.log(Level.INFO,"Trying to stop and delete the VM: "+ vm.getMetadata().getName());

        if (vmId != null) {
            this.vmService.stopVM(vmId);
            this.vmService.deleteVM(vmId);
        }

        logger.log(Level.INFO,"The VM is stopped and deleted: "+ vm.getMetadata().getName());

        //update status to : deleted
        logger.log(Level.INFO,"Set VM Status Phase to powerOff and Deleted.");
        setVmStatusPower(vm,VirtualMachineStatus.VirtualMachinePowerState.poweredOff);
        setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Deleted);

        //remove the finalizer string
        logger.log(Level.INFO,"Set VM MetaData to remove finalizer string.");
        ObjectMeta meta = vm.getMetadata();
        meta.setFinalizers(null);
        vm.setMetadata(meta);
        vmClient.inNamespace(meta.getNamespace()).withName(meta.getName()).createOrReplace(vm);

        //remove local cache
        this.cachedVMOpsPhase.remove(vmId);
    }

    private void setVmStatusPhase(VirtualMachine oldvm,
                                  VirtualMachineStatus.VMStatusPhase phase) {
        VirtualMachine vm = getLatestVM(oldvm);
        VirtualMachineStatus status = vm.getStatus();
        if (status == null) {
            status = new VirtualMachineStatus();
            System.out.println("Cannot get Status from the VM, Generate a new One.");
        }
        status.setPhase(phase);
        vm.setStatus(status);
        this.vmClient.inNamespace(vm.getMetadata().getNamespace()).updateStatus(vm);
    }

    private VirtualMachine getLatestVM(VirtualMachine oldVm) {
        VirtualMachine latestVM = oldVm;
        ObjectMeta meta = oldVm.getMetadata();
        String vmName = meta.getName();
        List<VirtualMachine> vms = vmClient.inNamespace(meta.getNamespace()).list().getItems();
        for (VirtualMachine vm : vms) {
            if (vm.getMetadata().getName().equals(vmName)) latestVM = vm;
        }
        return latestVM;
    }


    private void setVmStatusPower(VirtualMachine oldvm,
                                  VirtualMachineStatus.VirtualMachinePowerState powerState) {
        VirtualMachine vm = getLatestVM(oldvm);
        VirtualMachineStatus status = vm.getStatus();
        if (status == null) status = new VirtualMachineStatus();
        status.setPowerState(powerState);
        vm.setStatus(status);
        this.vmClient.inNamespace(vm.getMetadata().getNamespace()).updateStatus(vm);
    }

//    private void setAnnotations(VirtualMachine oldvm,String key, String value) {
//        VirtualMachine vm = getLatestVM(oldvm);
//        ObjectMeta meta =vm.getMetadata();
//        Map<String, String> annotations = meta.getAnnotations();
//        if(annotations == null) annotations = new HashMap<>();
//        annotations.put(key,value);
//        meta.setAnnotations(annotations);
//        vm.setMetadata(meta);
//        updateVM(vm);
//    }

    private void setVmStatusPowerAction(VirtualMachine oldvm,
                                        VirtualMachineCondition.Status conditionStatus) {

        VirtualMachine vm = getLatestVM(oldvm);
        VirtualMachineStatus status = vm.getStatus();
        if (status == null) status = new VirtualMachineStatus();
        List<VirtualMachineCondition> conditions = status.getConditions();
        if (conditions == null) conditions = new ArrayList<>();
        if(conditions.size() == 0) conditions.add(new VirtualMachineCondition());
        VirtualMachineCondition condition = conditions.get(0);
        condition.setType(Constants.ANNOTATION_POWERON_ACTION);
        condition.setStatus(conditionStatus);
        condition.setLastTransactionTime(" ");
        condition.setMessage(" ");
        condition.setReason(" ");
        conditions.set(0,condition);
        status.setConditions(conditions);
        vm.setStatus(status);
        vmClient.inNamespace(vm.getMetadata().getNamespace()).updateStatus(vm);
    }

    private boolean isPowerActionSet(VirtualMachine vm) {
        VirtualMachineStatus status = vm.getStatus();
        if (status == null)
            return false;
        List<VirtualMachineCondition> conditions = status.getConditions();
        if (conditions == null || conditions.size() ==0)
            return false;
        VirtualMachineCondition condition = conditions.get(0);
        if(!condition.getType().equals(Constants.ANNOTATION_POWERON_ACTION))
            return false;
        if(!condition.getStatus().equals(VirtualMachineCondition.Status.True))
            return false;
        return true;
    }

    private void setUniqueID(VirtualMachine oldvm,String vmRefId){
        VirtualMachine vm = getLatestVM(oldvm);
        VirtualMachineStatus status = vm.getStatus();
        if (status == null) status = new VirtualMachineStatus();
        status.setUniqueID(vmRefId);
        vm.setStatus(status);
        vmClient.inNamespace(vm.getMetadata().getNamespace())
                .updateStatus(vm);
    }

    private void setVmStatusIp(VirtualMachine oldvm,String ip) {
        VirtualMachine vm = getLatestVM(oldvm);
        VirtualMachineStatus status = vm.getStatus();
        if (status == null) status = new VirtualMachineStatus();
        status.setVmIp(ip);
        vm.setStatus(status);
        vmClient.inNamespace(vm.getMetadata().getNamespace()).updateStatus(vm);
    }

    private void reconcileAddOrUpdate(VirtualMachine vm) throws
            VMOperatorException,
            RuntimeFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg,
            FileFaultFaultMsg,
            VmConfigFaultFaultMsg,
            TaskInProgressFaultMsg,
            InvalidStateFaultMsg {

        //test if the vm exist, by checking the uniqueID in status
        logger.log(Level.INFO,"In Reconsile New or Update Process for: " +
                vm.getMetadata().getName());
        VirtualMachineSpec vmSpec = vm.getSpec();
        VirtualMachineStatus vmStatus = vm.getStatus();
        String vmId = null;
        if (vmStatus != null) vmId = vmStatus.getUniqueID();
        if (vmId == null || vmId.trim().isEmpty()) { //it is a new created virtual machine
            logger.log(Level.INFO,"This is a new created VM resource: " +
                    vm.getMetadata().getName());

            //read the VM spec and get all infomration
            //deploy the VM with Content Library service
            //if the ResourcePolicy,VirtualMachineClass and VirtualMachine instances
            // are not in the apiserver, we aborted the operation without change anything,
            // set the status "unknow". This should be checked in the admission control web hook
            // So the invalid VM instance should be stored in etcd.
            // But it is necessary to check in the controller, in case of mis-delete crd
            // instances after creation of this VM.
            String resourcePolicyName = vmSpec.getResourcePolicyName();
            String vmClassName = vmSpec.getClassName();
            String vmImageName = vmSpec.getImageName();

            VirtualMachineSetResourcePolicy resourcePolicy =
                    resourcePolicyLister.get(vm.getMetadata().getNamespace()+"/"+resourcePolicyName);
            VirtualMachineClass vmClass = vmClassLister.get(vmClassName);
            VirtualMachineImage vmImage = vmImageLister.get(vmImageName);
            String storageClassName = vmSpec.getStorageClass();
            String dataStoreId = dsService.getDatastore(dataCenter,storageClassName);


            // set VM status : creating
            logger.log(Level.INFO,"Update the VM Status Phase to: Creating");
            setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Creating);

            //get VM Spec
            String vmName = vm.getMetadata().getName();
            VirtualMachineSpec.VirtualMachinePowerState powerState = vmSpec.getPowerState();
            long cpuCount = vmClass.getSpec().getHardware().getCpus();
            String memorySize = vmClass.getSpec().getHardware().getMemory();
            List<VirtualMachineNetworkInterface> nics = vmSpec.getNetworkInterfaces();
            String vmFolderId = resourcePolicy.getMetadata().getAnnotations().get(Constants.VM_FOLDER_ID);
            String rPoolId = resourcePolicy.getMetadata().getAnnotations().get(Constants.VM_RESOURCEPOLL_ID);
            String libItemId = vmImage.getMetadata().getAnnotations().get(Constants.ANNOTATION_LIB_ITEMID);

            // check if we get these ids from annotation
            logger.log(Level.INFO,"VM Folder ID: "+vmFolderId);
            logger.log(Level.INFO,"ResourcePool ID: "+rPoolId);
            logger.log(Level.INFO,"Content Library Item ID: "+libItemId);

            //get cloud.init base64 encoded data
            String configMap="";
            String userData="";
            VirtualMachineMetadata meta = vm.getSpec().getVmMetadata();
            if(meta != null) {
                configMap = vm.getSpec().getVmMetadata().getConfigMapName();
                logger.log(Level.INFO,"Found vmMetaData: configMap: "+configMap);
            }

            if(configMap != null) {
                String namespace = vm.getMetadata().getNamespace();
                if(namespace == null) namespace = "default";
                userData = KubernetesHelper.getStringValueFromConfigMap(
                        k8sCoreClient,
                        namespace,
                        configMap,
                        "guestinfo.userdata"
                );
            }


            //deploy the VM
            logger.log(Level.INFO,"Deploying this VM...");
            String vmRefId = clService.deployVMFromOvfItem(
                    rPoolId,
                    vmFolderId,
                    dataStoreId,
                    vmName,
                    libItemId,
                    userData);

            logger.log(Level.INFO,"Deployment is done!");

            //if deployment failed, how to retry? how to tell the retry event with other sync events

            // set vm class and network interfaces
            logger.log(Level.INFO,"Set the CPU,Memory and network interfaces.");
            vmService.setCpuByVmId(vmRefId,cpuCount);
            vmService.setMemoryByVmId(vmRefId, Quantity.getLong(memorySize));
            vmService.applyNetworkInterfaces(dataCenter,vmRefId,nics);

            //set local cache
            this.cachedVMOpsPhase.put(vmRefId,VMOpsPhase.Created);

            //set VM status : created and powered-off
            logger.log(Level.INFO,"Update the VM Status Phase to: Created and poweredOff");
            setVmStatusPhase(vm,VirtualMachineStatus.VMStatusPhase.Created);
            setVmStatusPower(vm,VirtualMachineStatus.VirtualMachinePowerState.poweredOff);

            //set status UniqueId to VMID
            logger.log(Level.INFO,"Update the VM Status: UniqueID");
            setUniqueID(vm,vmRefId);

            //if need power on (default is on)
            if (powerState == null || powerState.equals(VirtualMachineSpec.VirtualMachinePowerState.poweredOn)) {
                logger.log(Level.INFO,"This VM need to power-on, try to start it asynchronously.");
                vmService.startVMAsync(vmRefId);

                //set annotation: power-on-action:true : setting annotation is bad, we try to change!!!
                logger.log(Level.INFO,"Setting the status to mark the poweron action to true.");
                setVmStatusPowerAction(vm,VirtualMachineCondition.Status.True);

                //set the local cache
                this.cachedVMOpsPhase.put(vmRefId,VMOpsPhase.PoweringOn);

                //wait for 10 seconds to get the VM powered-on
                enqueueVirtualMachine(vm,10000);
            }
        } else {
            // it is a update event, currently only support power state update
            // vm spec update is not implemented and ignored
            logger.log(Level.INFO,"This VM need to be updated.");

            VirtualMachineStatus status = vm.getStatus();
            // check the vm spec for power state
            VirtualMachineSpec.VirtualMachinePowerState powerStateSpec = vmSpec.getPowerState();

            // check the vm status for power state
            VirtualMachineStatus.VirtualMachinePowerState powerStatus = status.getPowerState();

            if (powerStateSpec == null ||
                    powerStateSpec.equals(VirtualMachineSpec.VirtualMachinePowerState.poweredOn)) {
                //if the spec is poweron and status is poweroff
                if (powerStatus == null || powerStatus.equals(VirtualMachineStatus.VirtualMachinePowerState.poweredOff)) {
                    logger.log(Level.INFO,"The Spec is PoweredOn, but the status is poweredOff");


                    if (!isPowerActionSet(vm) && !cachedVMOpsPhase.get(vmId).equals(VMOpsPhase.PoweringOn)) { // this is a spec change from poweroff to poweron

                        // we get problems here, we already set the PowerAction and poweron the VM
                        // but we failed to get the status sometime from the update events
                        // which will power-on the VM twice.
                        // we may need local cache for the VM phases


                        //we should power-on the VM
                        logger.log(Level.INFO,"The poweron action status is absent, we try to start the VM.");
                        vmService.startVMAsync(vmId);
                        //set status: power-on-action:true
                        logger.log(Level.INFO,"Set poweron action status to true.");
                        setVmStatusPowerAction(vm,VirtualMachineCondition.Status.True);

                        //set the local cache
                        this.cachedVMOpsPhase.put(vmId,VMOpsPhase.PoweringOn);

                        //wait for 10 seconds to get the VM powered-on
                        logger.log(Level.INFO,"Wait for another 10 seconds");
                        enqueueVirtualMachine(vm,10000l);
                    } else {
                        // we test the power status
                        logger.log(Level.INFO,"The poweron action status is set, we need to check the vm availability.");
                        boolean isPoweredOn = testVmStatus(vm);
                        if (isPoweredOn) {
                            String ip = getIp(vm);
                            logger.log(Level.INFO,"The VM is power-on, we update the status and reset poweron action status to false");
                            setVmStatusPower(vm,VirtualMachineStatus.VirtualMachinePowerState.poweredOn);
                            setVmStatusPowerAction(vm,VirtualMachineCondition.Status.False);
                            setVmStatusIp(vm,ip);
                        } else {
                            //if still not power on, we delayed to queue for 10 seconds
                            logger.log(Level.INFO,"The vm is still un-available, we need another 10 seconds");
                            enqueueVirtualMachine(vm, 10000l);
                        }
                    }
                }

            } else {  // if the spec is set to power-off
                // check if the status is powered-on, then this is a spec change
                if (powerStatus != null && powerStatus.equals(VirtualMachineStatus.VirtualMachinePowerState.poweredOn)) {
                    // we need power-off the vm, set the status and reset the poweron action
                    logger.log(Level.INFO,"This is a Spec change, we are trying to stop a running VM.");
                    vmService.stopVM(vmId);
                    setVmStatusPower(vm,VirtualMachineStatus.VirtualMachinePowerState.poweredOff);
                    setVmStatusPowerAction(vm,VirtualMachineCondition.Status.False);
                    //updateVM(vm);
                }
            }
        }

    }

    private boolean testVmStatus(VirtualMachine vm) {
        String ip = getIp(vm);
        if(ip == null || ip.trim().length() ==0) return false;
        return true;
    }

    private String getIp(VirtualMachine vm) {
        VirtualMachineStatus vmStatus = vm.getStatus();
        if (vmStatus == null) return null;

        String vmId = vmStatus.getUniqueID();
        if (vmId == null || vmId.trim().isEmpty()) return null;

        String ip = null;
        try {
            ip = vmService.getIpAddress(vmId);
        } catch (Exception e) {}
        return ip;
    }

    private void reconcileAddFinalizer(VirtualMachine vm) {
        //If the object is not being deleted and does not have the finalizer registered,
        //then add the finalizer and update the object in Kubernetes.
        System.out.println("Add finalizer String for: "+ vm.getMetadata().getName());
        ObjectMeta meta = vm.getMetadata();
        meta.setFinalizers(Arrays.asList(Constants.FINALIZER_STRING));
        vm.setMetadata(meta);
        vmClient.inNamespace(meta.getNamespace()).withName(meta.getName()).createOrReplace(vm);
    }


}
