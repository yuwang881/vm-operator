package vmware.CPBU.test;

import vmware.CPBU.services.ContentLibraryService;
import vmware.CPBU.services.LoginService;
import vmware.CPBU.utils.KubernetesHelper;

public class ContentLibraryTest {
    public static void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();

        ContentLibraryService cls = new ContentLibraryService(loginservice);
        System.out.println(cls.getLibraryIdByName("Kubernetes"));
        System.out.println(cls.getAllLibraryIds());
        System.out.println(cls.getAllItemsName("MyLib"));

        cls.prinfOvfSummary("WCP","MyLib");
        //cls.deployVMFromOvfItem("wangyu1","wangyu","","vm001","");

        loginservice.logout();
    }
}
