package vmware.CPBU.test;

import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.VMService;
import vmware.CPBU.utils.KubernetesHelper;

public class VirtualMachineTest {
    public static void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();

        VMService vms = new VMService(loginservice);
        String vmid = vms.getVmIdByName("ubuntu18");
        System.out.println(vms.getCpuInfoByVmId(vmid));
        System.out.println(vms.getMemoryInfoByVmId(vmid));
        System.out.println("----------setting new Config-------------");
        //vms.setCpuByVmId(vmid,2);
        //vms.setMemoryByVmId(vmid,2048);
        //String nic = vms.updateFirstStandardPortGroupNic("VCP",vmid,"VM Network");
        //vms.startVM(vmid);
        vms.printNicsInfo(vmid);
        String ip = vms.getIpAddress(vmid);
        System.out.println("--------ip------"+ip);

        loginservice.logout();

    }
}
