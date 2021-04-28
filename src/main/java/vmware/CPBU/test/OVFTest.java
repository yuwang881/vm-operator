package vmware.CPBU.test;

import vmware.CPBU.services.LoginService;
import vmware.CPBU.services.Ovf2VMService;
import vmware.CPBU.utils.Constants;
import vmware.CPBU.utils.KubernetesHelper;

import java.util.HashMap;
import java.util.Map;

public class OVFTest {
    public static void test() throws Exception {
        String vcUrl = KubernetesHelper.getStringValueFromSecret("vcurl");
        String username = KubernetesHelper.getStringValueFromSecret("username");
        String password = KubernetesHelper.getStringValueFromSecret("password");
        LoginService loginservice = new LoginService().setServer(vcUrl)
                .setUsername(username).setPassword(password);
        loginservice.login();
        System.out.println("login successful!");

        Ovf2VMService ovfS = new Ovf2VMService(loginservice);
        Map<String,String> props = new HashMap<>();
        props.put(Constants.VM_DATACENTER_NAME,"VCP");
        props.put(Constants.VM_LIBRARY_NAME,"MyLib");
        props.put(Constants.VM_LIBRARYITEM_NAME,"ubuntu-18.10");
        props.put(Constants.VM_FOLDER_NAME,"wangyufolder");
        props.put(Constants.VM_RESOURCEPOLL_NAME,"wangyupool");
        props.put(Constants.VM_NAME,"vm-wangyu-002");
        props.put(Constants.VM_DATASTORE_NAME,"vsanDatastore");

        ovfS.deployVMFromOvfTest(props);
        loginservice.logout();

    }
}
