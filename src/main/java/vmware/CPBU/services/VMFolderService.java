package vmware.CPBU.services;

import com.vmware.vcenter.Folder;
import com.vmware.vcenter.FolderTypes;
import com.vmware.vim25.*;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.utils.DatacenterHelper;
import vmware.CPBU.utils.GetMOREF;
import vmware.CPBU.utils.VimUtil;

import java.util.*;


public class VMFolderService {
    private LoginService loginService;
    private Folder folderService;

    public VMFolderService(LoginService loginService) {
        this.loginService = loginService;
        this.folderService = this.loginService.getVapiAuthHelper().getStubFactory()
                .createStub(Folder.class, this.loginService.getSessionStubConfig());
    }


    public List<String> getVMFoldersInDataCenter(String datacenterName) throws VMOperatorException {
        if (null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections
                    .singleton(DatacenterHelper.getDatacenter(loginService.getVapiAuthHelper().getStubFactory(),
                            loginService.getSessionStubConfig(), datacenterName));
        }
        List<FolderTypes.Summary> folderSummaries = folderService.list(
                new FolderTypes.FilterSpec());
        List<String> result = new ArrayList<>();
        for (FolderTypes.Summary summary: folderSummaries) {
            result.add(summary.getName());
        }
        return result;
    }

    public boolean isFolderExist(String datacenterName,String vmFolderName) throws VMOperatorException {
        Set<String> vmFolders = Collections.singleton(vmFolderName);
        FolderTypes.FilterSpec.Builder vmFolderFilterSpecBuilder =
                new FolderTypes.FilterSpec.Builder().setNames(vmFolders);

        if (null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections
                    .singleton(DatacenterHelper.getDatacenter(loginService.getVapiAuthHelper().getStubFactory(),
                            loginService.getSessionStubConfig(), datacenterName));
            vmFolderFilterSpecBuilder.setDatacenters(datacenters);
        }

        List<FolderTypes.Summary> folderSummaries = folderService.list(
                vmFolderFilterSpecBuilder.build());
        if (folderSummaries.size()>0) return true;
        return false;
    }

    public ManagedObjectReference getRootFolderRef(String dcName) throws InvalidPropertyFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg, VMOperatorException {
        ManagedObjectReference dcRef = getDataCenterRef(dcName);
        if (dcRef == null)
            throw new VMOperatorException(VMOperatorException.ExceptionCode.DATACENTER_NOTFOUNT_ERROR);

        // Find the datacenter's root folder
        List<DynamicProperty> dynamicProps =
                VimUtil.getProperties(loginService.getVimAuthHelper().getVimPort(),
                        loginService.getVimAuthHelper().getServiceContent(),
                        dcRef,
                        dcRef.getType(),
                        Arrays.asList("vmFolder"));
        return (ManagedObjectReference) dynamicProps.get(0).getVal();
    }

    private ManagedObjectReference getDataCenterRef(String dcName) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg {
        VimPortType vimPort = loginService.getVimAuthHelper().getVimPort();
        ServiceContent serviceContent = loginService.getVimAuthHelper().getServiceContent();
        GetMOREF getMOREF = new GetMOREF(vimPort,serviceContent);
        return getMOREF.inContainerByType(serviceContent.getRootFolder(),"Datacenter").get(dcName);
    }

    public boolean createVMFolderInDataCenter(String datacenterName, String vmFolderName) throws InvalidPropertyFaultMsg, RuntimeFaultFaultMsg, VMOperatorException, InvalidNameFaultMsg, DuplicateNameFaultMsg, NotFoundFaultMsg {
        if(isFolderExist(datacenterName,vmFolderName))
            return false;

        ManagedObjectReference dcRef = getRootFolderRef(datacenterName);
        if (dcRef == null)
            throw new VMOperatorException(VMOperatorException.ExceptionCode.DATACENTER_NOTFOUNT_ERROR);
        loginService.getVimAuthHelper().getVimPort().createFolder(dcRef,vmFolderName);
        return true;

    }
}
