package vmware.CPBU.test;

import com.vmware.vim25.ManagedObjectReference;
import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.ResourcePoolService;
import vmware.CPBU.utils.KubernetesHelper;

import java.util.List;

public class ResourcePoolTest {
    public static void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();

        ResourcePoolService rps = new ResourcePoolService(loginservice);

        boolean iscreated = rps.createResourcePoolInCluster("WCP","wangyu1",1000,1000,1000,1000);
        boolean exist = rps.isResourcePoolExist("VCP","WCP","wangyu1");
        System.out.println(exist);
        loginservice.logout();
    }
}
