package vmware.CPBU.utils;

public class Constants {
    public static final String NAMESPACE_NAME="vmoperator";
    public static final String CONFIGMAP_NAME="vmoperator";
    public static final String SECRET_NAME="vmoperator";

    //VM Creating Properties Keys
    public static final String VM_DATACENTER_NAME="datacenter";
    public static final String VM_CLUSTER_NAME="cluster";
    public static final String VM_FOLDER_NAME="folder";
    public static final String VM_RESOURCEPOLL_NAME="resourcePool";
    public static final String VM_LIBRARY_NAME="libraryName";
    public static final String VM_LIBRARYITEM_NAME="libraryItemName";
    public static final String VM_DATASTORE_NAME="datastoreName";
    public static final String VM_DATACENTER_ID="datacenterId";
    public static final String VM_CLUSTER_ID="clusterId";
    public static final String VM_FOLDER_ID="folderId";
    public static final String VM_RESOURCEPOLL_ID="resourcePoolId";
    public static final String VM_LIBRARYITEM_ID="libraryItemId";
    public static final String VM_DATASTORE_ID="datastoreId";

    public static final String VM_NAME="vmName";
    public static final String VM_CPU="cpu";
    public static final String VM_MEMORY="memory";
    public static final String VM_POWER_STATUS="power";

    public static final String VM_NETWORK_TYPE_STANDARD_PORTGROUP = "standard-portgroup";
    public static final String VM_NETWORK_TYPE_DISTRIBUTED_PORTGROUP = "distributed-portgroup";

    public static final String ANNOTATION_VMID = "VMID";
    public static final String ANNOTATION_LIB_ITEMID = "contentlibrary-item-id";
    public static final String ANNOTATION_LIB_NAME = "contentlibrary-name";
    public static final String FINALIZER_STRING = "virtualmachine.vmoperator.wangyu.cpbu.vmware";
    public static final String ANNOTATION_POWERON_ACTION = "power-on-action";
    public static final String USER_DATA = "user-data";


//    public static final String VirtualMachinePoweredOff = "poweredOff";
//    public static final String VirtualMachinePoweredOn = "poweredOn";

}
