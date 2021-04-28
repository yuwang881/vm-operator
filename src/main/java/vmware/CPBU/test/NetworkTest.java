package vmware.CPBU.test;

import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.NetworkService;
import vmware.CPBU.utils.KubernetesHelper;

public class NetworkTest {
    static public void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();
        NetworkService network = new NetworkService(loginservice);
        System.out.println(network.getDistributedNetworkBacking("DC1","DPortGroup"));
        System.out.println(network.getStandardNetworkBacking("DC1","VM Network"));
        loginservice.logout();
    }
}
