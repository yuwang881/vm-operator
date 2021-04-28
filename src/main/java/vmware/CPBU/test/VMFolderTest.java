package vmware.CPBU.test;

import com.vmware.vim25.ManagedObjectReference;
import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.ResourcePoolService;
import vmware.CPBU.services.VMFolderService;
import vmware.CPBU.utils.KubernetesHelper;

import java.util.List;

public class VMFolderTest {
    public static void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();

        VMFolderService vmfs = new VMFolderService(loginservice);
        boolean ref =vmfs.createVMFolderInDataCenter("VCP","wangyu");
        List<String> foldernames = vmfs.getVMFoldersInDataCenter("VCP");

        System.out.println(foldernames);
        loginservice.logout();
    }
}
