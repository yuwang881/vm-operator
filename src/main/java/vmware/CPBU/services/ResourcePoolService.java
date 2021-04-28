package vmware.CPBU.services;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vcenter.ResourcePool;
import com.vmware.vcenter.ResourcePoolTypes;
import com.vmware.vim25.*;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.utils.ClusterHelper;
import vmware.CPBU.utils.DatacenterHelper;
import vmware.CPBU.utils.ResourcePoolHelper;
import vmware.CPBU.utils.VimUtil;


import java.util.*;

public class ResourcePoolService {
    private LoginService loginService;
    private ResourcePool resourcePoolService;
    private StubFactory stubFactory;
    private StubConfiguration sessionStubConfig;
    private VimPortType vimPort;
    private ServiceContent serviceContent;

    public ResourcePoolService(LoginService loginService) {
        this.loginService = loginService;
        this.stubFactory = loginService.getVapiAuthHelper().getStubFactory();
        this.sessionStubConfig = loginService.getSessionStubConfig();
        this.vimPort= loginService.getVimAuthHelper().getVimPort();
        this.serviceContent = loginService.getVimAuthHelper().getServiceContent();
        this.resourcePoolService = stubFactory.createStub(ResourcePool.class, sessionStubConfig);
    }

    public String getResourceIdByName(String resourceName) {
        ResourcePoolTypes.FilterSpec resourceFilterSpec = new ResourcePoolTypes.FilterSpec.Builder()
                .setNames(Collections.singleton(resourceName)).build();
        List<ResourcePoolTypes.Summary> resourcePoolList = this.resourcePoolService.list(resourceFilterSpec);
        assert resourcePoolList.size() > 0 && resourcePoolList.get(0).getName().equals(
                resourceName) : "ResourcePool with name " + resourceName + " not found";
        return resourcePoolList.get(0).getResourcePool();
    }

    public void getInfobyId(String rpId) {
        ResourcePoolTypes.Info info = this.resourcePoolService.get(rpId);
        if (info == null) System.out.println("Failed to get Resource Pool Object!");
        long cpuReservation = info.getCpuAllocation().getReservation();
        long cpuLimit = info.getCpuAllocation().getLimit();
        long memoryReservation = info.getMemoryAllocation().getReservation();
        long memoryLimit = info.getMemoryAllocation().getLimit();
        System.out.println("CPU Reservation: "+cpuReservation);
        System.out.println("CPU Limit:" + cpuLimit);
        System.out.println("Memory Reservation: "+memoryReservation);
        System.out.println("Memory Limit: "+ memoryLimit);
    }

    public void getInfobyIdOld(String rpId) {
        ResourcePoolTypes.Info info = this.resourcePoolService.get(rpId);
        if (info == null) System.out.println("Failed to get Resource Pool Object!");
        StructValue data = info._getDataValue();
        Map<String, DataValue> values = data.getFields();
        System.out.println(values);
    }


    private List<ResourcePoolTypes.Summary> findResourcePool(
            String datacenterName,
            String clusterName,
            String resourcepoolName) throws VMOperatorException {

        ResourcePoolTypes.FilterSpec.Builder filterBuilder = new ResourcePoolTypes.FilterSpec.Builder();
        if(null != datacenterName && !datacenterName.isEmpty()){
            filterBuilder.setDatacenters(Collections.singleton(DatacenterHelper.
                    getDatacenter(stubFactory, sessionStubConfig,datacenterName)));
        }
        if(null != clusterName && !clusterName.isEmpty()) {
            filterBuilder.setClusters(Collections.singleton(ClusterHelper.
                    getCluster(stubFactory,sessionStubConfig,clusterName)));
        }

        if(null != resourcepoolName && !resourcepoolName.isEmpty())
        {
            filterBuilder.setResourcePools(Collections.singleton(ResourcePoolHelper.
                    getResourcePool(stubFactory, sessionStubConfig,datacenterName,resourcepoolName)));
        }

        return resourcePoolService.list(filterBuilder.build());
    }

    public List<String> getResourcePoolIdByDC(String datacenterName) throws VMOperatorException {
        List<ResourcePoolTypes.Summary> rps = findResourcePool(datacenterName,"","");
        List<String> rpids = new ArrayList<>();
        for (ResourcePoolTypes.Summary summary : rps) {
            rpids.add(summary.getResourcePool());
        }
        return rpids;
    }

    public boolean isResourcePoolExist(
            String datacenterName,
            String clusterName,
            String rpName) throws VMOperatorException {

        List<ResourcePoolTypes.Summary> rps = findResourcePool(datacenterName,clusterName,rpName);
        if (rps.isEmpty()) return false;
        return true;
    }

    public List<String> getResourcePoolNameByCluster(String clusterName) throws VMOperatorException {
        List<ResourcePoolTypes.Summary> rps = findResourcePool("",clusterName,"");
        List<String> rpids = new ArrayList<>();
        for (ResourcePoolTypes.Summary summary : rps) {
            System.out.println("Name: "+ summary.getName());
            System.out.println("ID: "+summary.getResourcePool());
            rpids.add(summary.getName());
        }
        return rpids;
    }

    public String getRootResourcePoolId(String clusterName)
            throws InvalidPropertyFaultMsg,
                NotFoundFaultMsg,
                RuntimeFaultFaultMsg {

        ManagedObjectReference clusterMoRef =
                VimUtil.getCluster(vimPort,serviceContent,clusterName);

        // Find the cluster's root resource pool
        List<DynamicProperty> dynamicProps =
                VimUtil.getProperties(vimPort,
                        serviceContent,
                        clusterMoRef,
                        clusterMoRef.getType(),
                        Arrays.asList("resourcePool"));

        ManagedObjectReference rootResPoolMoRef =
                (ManagedObjectReference) dynamicProps.get(0).getVal();
        System.out.println("Resource pool MoRef : " + rootResPoolMoRef.getType()
                + " : " + rootResPoolMoRef.getValue());
        return rootResPoolMoRef.getValue();
    }

    public ManagedObjectReference getRootResourcePoolRef(String clusterName)
            throws InvalidPropertyFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        ManagedObjectReference clusterMoRef =
                VimUtil.getCluster(vimPort,serviceContent,clusterName);
        // Find the cluster's root resource pool
        List<DynamicProperty> dynamicProps =
                VimUtil.getProperties(vimPort,
                        serviceContent,
                        clusterMoRef,
                        clusterMoRef.getType(),
                        Arrays.asList("resourcePool"));
        return (ManagedObjectReference) dynamicProps.get(0).getVal();
    }

    public ManagedObjectReference createReourcePoolOld(
            ManagedObjectReference parent,
            String name,
            long cpuReservation,
            long cpuLimit,
            long memoryReservation,
            long memoryLimit)
            throws DuplicateNameFaultMsg,
                InvalidNameFaultMsg,
                RuntimeFaultFaultMsg,
                InsufficientResourcesFaultFaultMsg {

        ResourceConfigSpec resConfigSpec = new ResourceConfigSpec();
        ResourceAllocationInfo cpuInfo = new ResourceAllocationInfo();
        ResourceAllocationInfo memInfo = new ResourceAllocationInfo();
        cpuInfo.setReservation(cpuReservation);
        cpuInfo.setExpandableReservation(false);
        SharesInfo sharedInfo = new SharesInfo();
        sharedInfo.setLevel(SharesLevel.LOW);
        cpuInfo.setShares(sharedInfo);
        cpuInfo.setLimit(cpuLimit);
        memInfo.setReservation(memoryReservation);
        memInfo.setLimit(memoryLimit);
        memInfo.setExpandableReservation(false);
        memInfo.setShares(sharedInfo);
        resConfigSpec.setMemoryAllocation(memInfo);
        resConfigSpec.setCpuAllocation(cpuInfo);
        return vimPort.createResourcePool(parent,name,resConfigSpec);
    }


    public String createResourcePool(
            String name,
            String parentID,
            long cpuReservation,
            long cpuLimit,
            long memoryReservation,
            long memoryLimit) {

        ResourcePoolTypes.ResourceAllocationCreateSpec cpuAllocation =
                 new ResourcePoolTypes.ResourceAllocationCreateSpec.Builder()
                .setLimit(cpuLimit)
                .setReservation(cpuReservation)
                .build();
        ResourcePoolTypes.ResourceAllocationCreateSpec memoryAllocation =
                new ResourcePoolTypes.ResourceAllocationCreateSpec.Builder()
                        .setLimit(memoryLimit)
                        .setReservation(memoryReservation)
                        .build();
        ResourcePoolTypes.CreateSpec createSpec =
                new ResourcePoolTypes.CreateSpec.Builder(name,parentID)
                .setCpuAllocation(cpuAllocation)
                .setMemoryAllocation(memoryAllocation).build();

        System.out.println("Creating the resource pool....");
        return this.resourcePoolService.create(createSpec);
    }

    public boolean createResourcePoolInCluster(
            String clusterName,
            String rpName,
            long cpuReservation,
            long cpuLimit,
            long memoryReservation,
            long memoryLimit)
            throws InvalidPropertyFaultMsg,
                NotFoundFaultMsg,
                RuntimeFaultFaultMsg,
                InvalidNameFaultMsg,
                DuplicateNameFaultMsg,
                InsufficientResourcesFaultFaultMsg,
                VMOperatorException {

        //if the cluster already has the resourcepool of the same name, return false;
        List<String> rpNames = this.getResourcePoolNameByCluster(clusterName);
        for(String name: rpNames) {
            if(name.equals(rpName)) return false;
        }

        ManagedObjectReference rootRPRef = this.getRootResourcePoolRef(clusterName);
        if (rootRPRef == null) throw new VMOperatorException(VMOperatorException.ExceptionCode.CLUSTER_NOTFOUNT_ERROR);
        createReourcePoolOld(rootRPRef,rpName,cpuReservation,cpuLimit,memoryReservation,memoryLimit);
        return true;
    }
}
