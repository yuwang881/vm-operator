package vmware.CPBU.services;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.utils.Constants;
import vmware.CPBU.utils.FolderHelper;
import vmware.CPBU.utils.KubernetesHelper;
import vmware.CPBU.utils.ResourcePoolHelper;

import java.util.Map;
import java.util.UUID;

public class Ovf2VMService {
    private LoginService loginService;
    private ContentLibraryService clService;
    private ResourcePoolService rpoolService;
    private VMFolderService folderService;
    private VMService vmService;
    private DataStoreService datastoreService;

    public Ovf2VMService(LoginService loginService) {
        this.loginService = loginService;
        this.clService = new ContentLibraryService(loginService);
        this.rpoolService = new ResourcePoolService(loginService);
        this.folderService = new VMFolderService(loginService);
        this.vmService = new VMService(loginService);
        this.datastoreService = new DataStoreService(loginService);
    }


    public void deployVMFromOvfTest(Map<String,String> props) throws VMOperatorException {
        String vmName = props.get(Constants.VM_NAME);
        if (vmName == null) vmName = autoGenVMName();
        String dataCenterName = props.get(Constants.VM_DATACENTER_NAME);
        String vmFolderName = props.get(Constants.VM_FOLDER_NAME);
        String rPoolName = props.get(Constants.VM_RESOURCEPOLL_NAME);
        String libraryName = props.get(Constants.VM_LIBRARY_NAME);
        String itemName = props.get(Constants.VM_LIBRARYITEM_NAME);
        String datastoreName = props.get(Constants.VM_DATASTORE_NAME);
        if (dataCenterName == null || vmFolderName == null || rPoolName == null)
            throw new VMOperatorException(VMOperatorException.ExceptionCode.VM_CREATION_PROPS_ERROR);

        StubFactory stubFactory = this.loginService.getVapiAuthHelper().getStubFactory();
        StubConfiguration sessionStubConfig = this.loginService.getSessionStubConfig();
        String vmFolderId = FolderHelper.getFolder(stubFactory,sessionStubConfig,dataCenterName,vmFolderName);
        String rPoolId = ResourcePoolHelper.getResourcePool(stubFactory,sessionStubConfig,dataCenterName,rPoolName);
        String libraryItemId = clService.getItemId(libraryName,itemName);
        String dataStoreId = datastoreService.getDatastore(datastoreName);
        String userData = KubernetesHelper.getUserData("default",Constants.USER_DATA);
        clService.deployVMFromOvfItem(rPoolId,vmFolderId,dataStoreId,vmName,libraryItemId,userData);
    }

    public void deployVMFromOvf(Map<String,String> props) throws VMOperatorException {
        String vmName = props.get(Constants.VM_NAME);
        if (vmName == null) vmName = autoGenVMName();
        String dataCenterId = props.get(Constants.VM_DATACENTER_ID);
        String vmFolderId = props.get(Constants.VM_FOLDER_ID);
        String rPoolId = props.get(Constants.VM_RESOURCEPOLL_ID);
        String libraryItemId = props.get(Constants.VM_LIBRARYITEM_ID);
        String datastoreId = props.get(Constants.VM_DATASTORE_ID);
        if (dataCenterId == null || vmFolderId == null || rPoolId == null)
            throw new VMOperatorException(VMOperatorException.ExceptionCode.VM_CREATION_PROPS_ERROR);

        String userData = KubernetesHelper.getUserData("default",Constants.USER_DATA);
        clService.deployVMFromOvfItem(rPoolId,vmFolderId,datastoreId,vmName,libraryItemId,userData);
    }



    private String autoGenVMName() {
        return "VM-"+UUID.randomUUID().toString();
    }
}
