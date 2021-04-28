package vmware.CPBU.test;

import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.VMService;
import vmware.CPBU.utils.KubernetesHelper;

public class VersionTest {
    public static void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();

        VMService vms = new VMService(loginservice);
        System.out.println("VC Version: "+vms.getSystemVersion());

        loginservice.logout();

    }
}
