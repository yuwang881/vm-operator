package vmware.CPBU.services;

import com.vmware.content.library.StorageBacking;
import com.vmware.vapi.bindings.StubConfiguration;
import com.vmware.vapi.bindings.StubFactory;
import com.vmware.vcenter.Datastore;
import com.vmware.vcenter.DatastoreTypes;
import vmware.CPBU.exceptions.VMOperatorException;
import vmware.CPBU.utils.DatacenterHelper;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class DataStoreService {
    private LoginService loginService;
    private Datastore datastoreService;
    private StubFactory stubFactory;
    private StubConfiguration sessionStubConfig;

    public DataStoreService(LoginService loginService) {
        this.loginService = loginService;
        this.stubFactory = loginService.getVapiAuthHelper().getStubFactory();
        this.sessionStubConfig = loginService.getSessionStubConfig();
        this.datastoreService = stubFactory.createStub(Datastore.class, sessionStubConfig);
    }

    /**
     * Returns the identifier of a datastore
     *
     * Note: The method assumes that there is only one datastore and datacenter
     * with the mentioned names.
     *
     * @param datacenterName name of the datacenter for the placement spec
     * @param datastoreName name of the datastore for the placement spec
     * @return identifier of a datastore
     */
    public String getDatastore(String datacenterName, String datastoreName)
            throws VMOperatorException {
        // Get the datastore
        Datastore datastoreService = stubFactory.createStub(Datastore.class,
                sessionStubConfig);
        Set<String> datastores = Collections.singleton(datastoreName);
        List<DatastoreTypes.Summary> datastoreSummaries = null;
        DatastoreTypes.FilterSpec datastoreFilterSpec = null;
        if(null != datacenterName) {
            // Get the datacenter
            Set<String> datacenters = Collections.singleton(DatacenterHelper
                    .getDatacenter(stubFactory, sessionStubConfig, datacenterName));
            datastoreFilterSpec =
                    new DatastoreTypes.FilterSpec.Builder().setNames(datastores)
                            .setDatacenters(datacenters)
                            .build();
            datastoreSummaries = datastoreService.list(
                    datastoreFilterSpec);
            datastoreSummaries = datastoreService.list(datastoreFilterSpec);
            if(datastoreSummaries.size() < 1)  return null;
        }else {
            datastoreFilterSpec =
                    new DatastoreTypes.FilterSpec.Builder().setNames(datastores)
                            .build();
            datastoreSummaries = datastoreService.list(datastoreFilterSpec);
            if(datastoreSummaries.size() < 1)
                return null;
        }
        return datastoreSummaries.get(
                datastoreSummaries.size()-1).getDatastore();

    }

    public String getDatastore(String datastoreName) throws VMOperatorException {
        return getDatastore(null, datastoreName);
    }

    /**
     * Creates a datastore storage backing.
     *
     * @return the storage backing
     */
    public StorageBacking createStorageBacking( String dsName ) throws VMOperatorException {
        String dsId = getDatastore(dsName);

        //Build the storage backing with the datastore Id
        StorageBacking storageBacking = new StorageBacking();
        storageBacking.setType(StorageBacking.Type.DATASTORE);
        storageBacking.setDatastoreId(dsId);
        return storageBacking;
    }

}
