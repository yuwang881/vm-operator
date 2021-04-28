package vmware.CPBU.services;

import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Network;
import com.vmware.vcenter.NetworkTypes;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.utils.DatacenterHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NetworkService {
    private LoginService loginService;
    private Network networkService;
    private StubFactory stubFactory;
    private StubConfiguration sessionStubConfig;

    public NetworkService(LoginService loginService) {
        this.loginService = loginService;
        this.stubFactory = loginService.getVapiAuthHelper().getStubFactory();
        this.sessionStubConfig = loginService.getSessionStubConfig();
        this.networkService = stubFactory.createStub(Network.class, sessionStubConfig);
    }

    /**
     * Returns the identifier of a standard network.
     *
     * Note: The method assumes that there is only one standard portgroup
     * and datacenter with the mentioned names.
     *
     * @param datacenterName name of the datacenter on which the network exists
     * @param stdPortgroupName name of the standard portgroup
     * @return identifier of a standard network.
     */
    public String getStandardNetworkBacking(
            String datacenterName, String stdPortgroupName) throws VMOperatorException {

        // Get the datacenter id
        Set<String> datacenters = Collections.singleton(DatacenterHelper
                .getDatacenter(stubFactory, sessionStubConfig, datacenterName));

        // Get the network id
        Set<String> networkNames = Collections.singleton(stdPortgroupName);
        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
                .singletonList(NetworkTypes.Type.STANDARD_PORTGROUP));
        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder().setDatacenters(
                        datacenters)
                        .setNames(networkNames)
                        .setTypes(networkTypes)
                        .build();
        List<NetworkTypes.Summary> networkSummaries = networkService.list(
                networkFilterSpec);
        if(networkSummaries.size() <1) return null;
        return networkSummaries.get(0).getNetwork();
    }

    /**
     * Returns the identifier of a distributed network
     *
     * Note: The method assumes that there is only one distributed portgroup
     * and datacenter with the mentioned names.
     *
     * @param datacenterName name of the datacenter on which the distributed
     * network exists
     * @param vdPortgroupName name of the distributed portgroup
     * @return identifier of the distributed network
     */
    public String getDistributedNetworkBacking(
            String datacenterName, String vdPortgroupName) throws VMOperatorException {

        // Get the datacenter id
        Set<String> datacenters = Collections.singleton(DatacenterHelper
                .getDatacenter(stubFactory, sessionStubConfig, datacenterName));

        // Get the network id
        Set<String> networkNames = Collections.singleton(vdPortgroupName);
        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
                .singletonList(NetworkTypes.Type.DISTRIBUTED_PORTGROUP));
        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder().setDatacenters(
                        datacenters)
                        .setNames(networkNames)
                        .setTypes(networkTypes)
                        .build();
        List<NetworkTypes.Summary> networkSummaries = networkService.list(
                networkFilterSpec);

        if(networkSummaries.size() <1) return null;
        return networkSummaries.get(0).getNetwork();
    }

    /**
     * Returns the identifier of a Opaque network
     *
     * Note: The method assumes that there is only one Opaque portgroup
     * with the mentioned name.
     *
     * @param opaquePortgroup name of the opaque portgroup
     * @return identifier of the opaque network
     */
    public String getOpaqueNetworkBacking(String opaquePortgroup) {

        // Get the network id
        Set<String> networkNames = Collections.singleton(opaquePortgroup);
        Set<NetworkTypes.Type> networkTypes = new HashSet<>(Collections
                .singletonList(NetworkTypes.Type.OPAQUE_NETWORK));
        NetworkTypes.FilterSpec networkFilterSpec =
                new NetworkTypes.FilterSpec.Builder()
                        .setNames(networkNames)
                        .setTypes(networkTypes)
                        .build();
        List<NetworkTypes.Summary> networkSummaries = networkService.list(
                networkFilterSpec);

        if(networkSummaries.size() <1) return null;
        return networkSummaries.get(0).getNetwork();
    }
}
