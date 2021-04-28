package vmware.CPBU.services;

import com.vmware.appliance.networking.interfaces.Ipv4;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.VM;
import com.vmware.vcenter.VMTypes;
import com.vmware.vcenter.vm.Power;
import com.vmware.vcenter.vm.guest.Identity;
import com.vmware.vcenter.vm.hardware.*;
import com.vmware.vim25.*;
import vmware.CPBU.crd.vm.VirtualMachineNetworkInterface;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.utils.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/*
 * ToDo: currently support set one network adaptor
 *  need to support set more than one interfaces
 *  with mixed types---done!!!
 */

public class VMService {
    private VM vm;
    private Cpu cpu;
    private Memory memory;
    private LoginService loginService;
    private NetworkService networkService;
    private VMFolderService vmFolderService;
    private Power powerService;
    private Identity identityService;
    private Ipv4 ipv4Service;
    private Ethernet ethernet;
    private StubFactory stubFactory;
    private StubConfiguration sessionStubConfig;
    private String systemVersion;


    public VMService(LoginService loginService) {
        this.loginService = loginService;
        this.networkService = new NetworkService(loginService);
        this.vmFolderService = new VMFolderService(loginService);
        this.stubFactory = loginService.getVapiAuthHelper().getStubFactory();
        this.sessionStubConfig = loginService.getSessionStubConfig();
        vm = stubFactory.createStub(VM.class, sessionStubConfig);
        ethernet = stubFactory.createStub(Ethernet.class, sessionStubConfig);
        cpu = stubFactory.createStub(Cpu.class,sessionStubConfig);
        memory = stubFactory.createStub(Memory.class,sessionStubConfig);
        powerService = stubFactory.createStub(Power.class,sessionStubConfig);
        ipv4Service = stubFactory.createStub(Ipv4.class,sessionStubConfig);
        identityService = stubFactory.createStub(Identity.class,sessionStubConfig);
        systemVersion = VersionHelper.getVersion(stubFactory,sessionStubConfig);
    }

    public String getSystemVersion() {
        return this.systemVersion;
    }

    public String getVmIdByName(String vmName) {
        VMTypes.FilterSpec vmFilterSpec = new VMTypes.FilterSpec.Builder()
                .setNames(Collections.singleton(vmName)).build();
        List<VMTypes.Summary> vmList = this.vm.list(vmFilterSpec);
        if(vmList.size() < 1) return null;
        return vmList.get(0).getVm();
    }

    private List<VMTypes.Summary> findVMs(String datacenterName,String clusterName,
                                          String vmFolderName,String resourcepoolName) throws VMOperatorException {
        VMTypes.FilterSpec.Builder filterBuilder = new VMTypes.FilterSpec.Builder();
        if(null != datacenterName && !datacenterName.isEmpty()){
            filterBuilder.setDatacenters(Collections.singleton(DatacenterHelper.
                    getDatacenter(stubFactory, sessionStubConfig,datacenterName)));
        }
        if(null != clusterName && !clusterName.isEmpty()) {
            filterBuilder.setClusters(Collections.singleton(ClusterHelper.
                    getCluster(stubFactory, sessionStubConfig,clusterName)));
        }
        if(null != vmFolderName && !vmFolderName.isEmpty())
        {
            filterBuilder.setFolders(Collections.singleton(FolderHelper.
                    getFolder(stubFactory, sessionStubConfig,vmFolderName)));
        }
        if(null != resourcepoolName && !resourcepoolName.isEmpty())
        {
            filterBuilder.setResourcePools(Collections.singleton(ResourcePoolHelper.
                    getResourcePool(stubFactory, sessionStubConfig,resourcepoolName)));
        }
        return vm.list(filterBuilder.build());
    }

    public List<String> getVMNamesByDC(String datacenterName) throws VMOperatorException {
        List<VMTypes.Summary> vms = findVMs(datacenterName,"","","");
        List<String> vmnames = new ArrayList<>();
        for (VMTypes.Summary summary : vms) {
            vmnames.add(summary.getName());
        }
        return vmnames;
    }

    public String getCpuInfoByVmId(String vmid) {
        CpuTypes.Info cpuinfo = this.cpu.get(vmid);
        long count = cpuinfo.getCount();
        boolean hotadd = cpuinfo.getHotAddEnabled();
        boolean hotremove = cpuinfo.getHotRemoveEnabled();
        long coreNum = cpuinfo.getCoresPerSocket();
        return new String("The CPU Info of this VM: \n"+
                "    Count of CPU: "+count + "\n" +
                "    Core per Socket: "+ coreNum +"\n" +
                "    HotAddable: "+ hotadd+ "\n" +
                "    HotRemovable: "+hotremove);
    }

    public String getMemoryInfoByVmId(String vmid) {
        MemoryTypes.Info memoryinfo = this.memory.get(vmid);
        long size  = memoryinfo.getSizeMiB();
        boolean hotadd = memoryinfo.getHotAddEnabled();
        return new String("The Memory Info of this VM: \n"+
                "    Size of the Memory: "+ size +" MiB\n" +
                "    HotAddable: "+hotadd+"\n");
    }

    public void setCpuByVmId(String vmid,long count) {
        CpuTypes.UpdateSpec cpuUpdate =
                new CpuTypes.UpdateSpec.Builder()
                        .setCount(count)
                        .build();
        cpu.update(vmid,cpuUpdate);
    }

    public void setMemoryByVmId(String vmid, long size) {
        MemoryTypes.UpdateSpec memoryUpdate =
                new MemoryTypes.UpdateSpec.Builder()
                        .setSizeMiB(size)
                        .build();
        memory.update(vmid,memoryUpdate);
    }

    public String updateFirstStandardPortGroupNic(
            String datacenterName,
            String vmId,
            String networkName) throws VMOperatorException {

        String nicId = getFirstNicId(vmId);
        return updateStandardPortGroupNic(datacenterName,vmId,nicId,networkName);
    }

    public String updateStandardPortGroupNic(
            String datacenterName,
            String vmId,
            String nicId,
            String networkName) throws VMOperatorException {

        String stdNetworkId = networkService.getStandardNetworkBacking(datacenterName, networkName);
        EthernetTypes.UpdateSpec nicUpdateSpec =
                new EthernetTypes.UpdateSpec.Builder().setBacking(
                        new EthernetTypes.BackingSpec.Builder(
                                EthernetTypes.BackingType.STANDARD_PORTGROUP)
                                .setNetwork(stdNetworkId).build()).build();
        ethernet.update(vmId, nicId, nicUpdateSpec);
        return nicId;
    }

    public String updateFirstDistributedPortGroupNic(
            String datacenterName,
            String vmId,
            String networkName) throws VMOperatorException {

        String nicId = getFirstNicId(vmId);
        return updateDistributedPortGroupNic(datacenterName,vmId,nicId,networkName);
    }

    public String updateDistributedPortGroupNic(
            String datacenterName,
            String vmId,
            String nicId,
            String networkName) throws VMOperatorException {

        String stdNetworkId = networkService.getDistributedNetworkBacking(datacenterName, networkName);
        EthernetTypes.UpdateSpec nicUpdateSpec =
                new EthernetTypes.UpdateSpec.Builder().setBacking(
                        new EthernetTypes.BackingSpec.Builder(
                                EthernetTypes.BackingType.DISTRIBUTED_PORTGROUP)
                                .setNetwork(stdNetworkId).build()).build();
        ethernet.update(vmId, nicId, nicUpdateSpec);
        return nicId;
    }


    public String createStandardPortGroupNic(
            String datacenterName,
            String vmId,
            String networkName) throws VMOperatorException {


        String stdNetworkId = networkService.getStandardNetworkBacking(datacenterName, networkName);
        EthernetTypes.CreateSpec nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setBacking(
                        new EthernetTypes.BackingSpec.Builder(
                                EthernetTypes.BackingType.STANDARD_PORTGROUP)
                                .setNetwork(stdNetworkId).build()).build();
        nicCreateSpec.setStartConnected(true);
        nicCreateSpec.setAllowGuestControl(true);
        nicCreateSpec.setWakeOnLanEnabled(true);
        //nicCreateSpec.setUptCompatibilityEnabled(true);
        nicCreateSpec.setPciSlotNumber(null);

        return ethernet.create(vmId, nicCreateSpec);
    }

    public String createDistributedPortGroupNic(
            String datacenterName,
            String vmId,
            String networkName) throws VMOperatorException {

        String stdNetworkId = networkService.getDistributedNetworkBacking(datacenterName, networkName);
        EthernetTypes.CreateSpec nicCreateSpec =
                new EthernetTypes.CreateSpec.Builder().setBacking(
                        new EthernetTypes.BackingSpec.Builder(
                                EthernetTypes.BackingType.DISTRIBUTED_PORTGROUP)
                                .setNetwork(stdNetworkId).build()).build();
        nicCreateSpec.setStartConnected(true);
        nicCreateSpec.setAllowGuestControl(true);
        //nicCreateSpec.setUptCompatibilityEnabled(true);
        nicCreateSpec.setWakeOnLanEnabled(true);
        nicCreateSpec.setPciSlotNumber(null);

        return ethernet.create(vmId, nicCreateSpec);
    }

    public void applyNetworkInterfaces(
            String datacenterName,
            String vmId,
            List<VirtualMachineNetworkInterface> interfaces) throws VMOperatorException {

        if (interfaces == null || interfaces.size() ==0)
            return;

        int vmNicNumber = getNicNumber(vmId);

        //update the existed Nics the VM has
        //we try to keep the network nic names to work properly
        //for example, if we delete one and then create a new one
        // we will get eth1 instead of eth0, which may cause problems
        for (int i=0;i<vmNicNumber;i++) {
            if(i+1 > interfaces.size()) return;
            String networkName = interfaces.get(i).getNetworkName();
            String networkType = interfaces.get(i).getNetworkType();
            String nicId = getIndexedNicId(vmId,i);
            //String etherCardType = interfaces[i].getEthernetCardType();
            if(networkType.equals(Constants.VM_NETWORK_TYPE_STANDARD_PORTGROUP)) {
                updateStandardPortGroupNic(datacenterName,vmId,nicId,networkName);
            } else if (networkType.equals(Constants.VM_NETWORK_TYPE_DISTRIBUTED_PORTGROUP)) {
                updateDistributedPortGroupNic(datacenterName,vmId,nicId,networkName);
            }
        }

        //create the new nics
        int needCreatNicNumber = interfaces.size() - vmNicNumber;
        if(needCreatNicNumber < 1) return;
        for (int i=0;i<needCreatNicNumber;i++) {
            String networkName = interfaces.get(vmNicNumber+i).getNetworkName();
            String networkType = interfaces.get(vmNicNumber+i).getNetworkType();

            //String etherCardType = interfaces[i].getEthernetCardType();
            if(networkType.equals(Constants.VM_NETWORK_TYPE_STANDARD_PORTGROUP)) {
                createStandardPortGroupNic(datacenterName,vmId,networkName);
            } else if (networkType.equals(Constants.VM_NETWORK_TYPE_DISTRIBUTED_PORTGROUP)) {
                createDistributedPortGroupNic(datacenterName,vmId,networkName);
            }
        }

    }

    public void startVM(String vmId) {
        powerService.start(vmId);
//        List<EthernetTypes.Summary> summarys = ethernet.list(vmId);
//        for (EthernetTypes.Summary summary : summarys) {
//            String nicId = summary.getNic();
//            ethernet.connect(vmId,nicId);
//        }
    }

    public void stopVM(String vmId) {
        powerService.stop(vmId);
    }

    public void startVMAsync(String dcName, String vmName) throws
            InvalidPropertyFaultMsg,
            NotFoundFaultMsg,
            RuntimeFaultFaultMsg,
            VMOperatorException,
            InvalidStateFaultMsg,
            FileFaultFaultMsg,
            TaskInProgressFaultMsg,
            InsufficientResourcesFaultFaultMsg,
            VmConfigFaultFaultMsg {

        //get ref for vmfolder
        ManagedObjectReference vmFolder = vmFolderService.getRootFolderRef(dcName);

        // get Ref for VM
        ManagedObjectReference vmRef = VimUtil.getVM(loginService.getVimAuthHelper().getVimPort(),
                loginService.getVimAuthHelper().getServiceContent(),
                vmFolder,
                vmName);
        startVMAsync(vmRef);

    }

    public void startVMAsync(String vmId) throws
            VMOperatorException,
            RuntimeFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg,
            FileFaultFaultMsg,
            VmConfigFaultFaultMsg,
            TaskInProgressFaultMsg,
            InvalidStateFaultMsg {

        ManagedObjectReference vmRef = new ManagedObjectReference();
        vmRef.setType("VirtualMachine");
        vmRef.setValue(vmId);
        startVMAsync(vmRef);
    }

    public void stopVMAsync(String vmId) throws
            VMOperatorException,
            TaskInProgressFaultMsg,
            RuntimeFaultFaultMsg,
            InvalidStateFaultMsg {
        ManagedObjectReference vmRef = new ManagedObjectReference();
        vmRef.setType("VirtualMachine");
        vmRef.setValue(vmId);
        stopVMAsync(vmRef);
    }

    public void stopVMAsync(ManagedObjectReference vmRef) throws
            TaskInProgressFaultMsg,
            InvalidStateFaultMsg,
            RuntimeFaultFaultMsg,
            VMOperatorException {
        if (vmRef == null) throw new VMOperatorException(VMOperatorException.ExceptionCode.VM_NOTFOUNT_ERROR);
        ManagedObjectReference taskmor = loginService.getVimAuthHelper().getVimPort().powerOffVMTask(vmRef);
    }

    private void startVMAsync(ManagedObjectReference vmRef) throws
            VMOperatorException,
            RuntimeFaultFaultMsg,
            InsufficientResourcesFaultFaultMsg,
            FileFaultFaultMsg,
            VmConfigFaultFaultMsg,
            TaskInProgressFaultMsg,
            InvalidStateFaultMsg {

        if (vmRef == null) throw new VMOperatorException(VMOperatorException.ExceptionCode.VM_NOTFOUNT_ERROR);
        ManagedObjectReference taskmor = loginService.getVimAuthHelper().getVimPort().powerOnVMTask(vmRef, null);
    }

    public String getIPFromOldVC(String vmid) {

        List<EthernetTypes.Summary> summarys = ethernet.list(vmid);
        for (EthernetTypes.Summary summary : summarys) {
            System.out.println("Got Nic: "+ summary.getNic());
            String ip = ipv4Service.get(summary.getNic()).getAddress();
            if (ip != null) return ip;
            System.out.println("Got ip: "+ ip);
        }
        return "";
    }

    private void deleteAllNics(String vmid) {
        List<EthernetTypes.Summary> summarys = ethernet.list(vmid);
        for (EthernetTypes.Summary summary : summarys) {
            String nicId = summary.getNic();
            ethernet.delete(vmid,nicId);
        }
    }

    private String getFirstNicId(String vmid) {
        return getIndexedNicId(vmid,0);
    }

    private String getIndexedNicId(String vmid,int index) {
        List<EthernetTypes.Summary> summarys = ethernet.list(vmid);
        if (summarys == null || summarys.size() < (index+1))
            return null;
        return summarys.get(index).getNic();
    }

    private int getNicNumber(String vmid) {
        List<EthernetTypes.Summary> summarys = ethernet.list(vmid);
        if (summarys == null) return 0;
        return summarys.size();

    }



    public void printNicsInfo(String vmid) {
        List<EthernetTypes.Summary> summarys = ethernet.list(vmid);
        for (EthernetTypes.Summary summary : summarys) {
            String nicId = summary.getNic();
            EthernetTypes.Info nicInfo = ethernet.get(vmid,nicId);
            System.out.println(nicInfo);
        }
    }

    public String getIpAddress(String vmid) {
        if(this.getSystemVersion().startsWith("7"))
            return identityService.get(vmid).getIpAddress();
        else
            return getIPFromOldVC(vmid);
    }

    public void deleteVM(String vmId) {
        this.vm.delete(vmId);
    }
}
