package vmware.CPBU.test;

import vmware.CPBU.services.DataStoreService;
import vmware.CPBU.services.LoginService;
import vmware.CPBU.utils.KubernetesHelper;


public class DataStoreTest {
    static public void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();
        DataStoreService datastore = new DataStoreService(loginservice);
        System.out.println(datastore.getDatastore("vsanDatastore"));
        loginservice.logout();
    }
}
