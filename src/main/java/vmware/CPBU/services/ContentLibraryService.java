package vmware.CPBU.services;

import com.vmware.content.Library;
import com.vmware.content.LibraryTypes;
import com.vmware.content.library.Item;
import com.vmware.content.library.ItemModel;
import com.vmware.vapi.bindings.Structure;
import com.vmware.vapi.data.DataValue;
import com.vmware.vapi.data.OptionalValue;
import com.vmware.vapi.data.StructValue;
import com.vmware.vcenter.ovf.*;
import com.vmware.vim25.*;


import java.util.*;

public class ContentLibraryService {
    private LoginService loginService;
    private final Library libraryService;
    private final Item itemService;
    private final LibraryItem ovfLibraryItemService;


    public ContentLibraryService(LoginService loginService) {
        this.loginService = loginService;
        this.itemService = this.loginService.getVapiAuthHelper().getStubFactory()
                .createStub(Item.class, this.loginService.getSessionStubConfig());
        this.libraryService = this.loginService.getVapiAuthHelper().getStubFactory()
                .createStub(Library.class, this.loginService.getSessionStubConfig());
        this.ovfLibraryItemService = this.loginService.getVapiAuthHelper().getStubFactory()
                .createStub(LibraryItem.class, this.loginService.getSessionStubConfig());
    }

    public List<String> getAllLibraryIds() {
        return this.libraryService.list();
    }

    public String getLibraryIdByName(String libraryName) {
        LibraryTypes.FindSpec findSpec = new LibraryTypes.FindSpec();
        findSpec.setName(libraryName);
        List<String> libraryIds = this.libraryService.find(findSpec);
        if (libraryIds.size()==0) return null;
        String libraryId = libraryIds.get(0);
        return  libraryId;
    }

    public List<String> getAllItemsName(String libraryName) {
        String libraryId = getLibraryIdByName(libraryName);
        List<String> itemIds = itemService.list(libraryId);
        List<String> result = new ArrayList<>();
        for (String itemId : itemIds) {
            ItemModel singleItem = itemService.get(itemId);
            result.add(singleItem.getName());
        }
        return result;
    }

    public List<String> getAllItemsId(String libraryName) {
        if (libraryName == null) return new ArrayList<String>();
        String libraryId = getLibraryIdByName(libraryName);
        if (libraryId == null) return new ArrayList<String>();
        return itemService.list(libraryId);
    }

    public String getFirstItemId(String libraryName) {
        String libraryId = getLibraryIdByName(libraryName);
        List<String> itemIds = itemService.list(libraryId);
        if (itemIds.size() == 0) return null;
        return itemIds.get(0);
    }

    public Map<String,String> getItemPropsById(String itemId) {
        Map<String,String> props = new HashMap<>();
        ItemModel item = itemService.get(itemId);
        String itemName = item.getName();
        String itemtype = item.getType();
        props.put("itemName",itemName);
        props.put("itemType",itemtype);
        return props;
    }

    public String getItemId(String libraryName,String itemName) {
        List<String> ids = getAllItemsId(libraryName);
        for (String id: ids) {
            ItemModel item = itemService.get(id);
            System.out.println(item.getName());
            if(item.getName().equals(itemName)) {
                return id;
            }
        }
        return null;
    }

    /*
     * Deploy a VM from OVF Item in the Content Library
     * Return poweroffed VMId
     */
    public String deployVMFromOvfItem(String resourcePoolId,
                                      String vmFolderId,
                                      String dataStoreId,
                                      String vmName,
                                      String libItemId,
                                      String userData) {
        // Creating the deployment.
        LibraryItemTypes.DeploymentTarget deploymentTarget = new LibraryItemTypes.DeploymentTarget();
        deploymentTarget.setResourcePoolId(resourcePoolId);
        deploymentTarget.setFolderId(vmFolderId);

        // Creating and setting the resource pool deployment spec.
        LibraryItemTypes.ResourcePoolDeploymentSpec deploymentSpec =
                new LibraryItemTypes.ResourcePoolDeploymentSpec();
        deploymentSpec.setName(vmName);
        deploymentSpec.setAcceptAllEULA(true);
        LibraryItemTypes.OvfSummary ovfSummary = this.ovfLibraryItemService
                .filter(libItemId, deploymentTarget);
        deploymentSpec.setAnnotation(ovfSummary.getAnnotation());
        deploymentSpec.setDefaultDatastoreId(dataStoreId);

        List<Structure> parameters = new ArrayList<>();
        parameters.add(addUserDataOVFParameters(userData));
        deploymentSpec.setAdditionalParameters(parameters);
        //printAdditionalParams(parameters);

        // Calling the deploy and getting the deployment result.
        LibraryItemTypes.DeploymentResult deploymentResult = this.ovfLibraryItemService
                .deploy(UUID.randomUUID().toString(),
                        libItemId,
                        deploymentTarget,
                        deploymentSpec);
        if (deploymentResult.getSucceeded()) {
            return deploymentResult.getResourceId().getId();
        } else
            throw new RuntimeException(deploymentResult.getError().toString());
    }


    private PropertyParams addUserDataOVFParameters(String userDataBase64) {
        PropertyParams.Builder ovfParamsBuilder = new PropertyParams.Builder();
        List<Property> properties = new ArrayList<>();
        Property.Builder propBuilder = new Property.Builder();
        Property userData = propBuilder
                .setId("user-data")
                .setType("string")
                .setValue(userDataBase64)
                .setUiOptional(false)
                .setCategory("")
                .setLabel("Encoded user-data")
                .setInstanceId("")
                .setClassId("")
                .setDescription("Base64 encoded data")
                .build();
        properties.add(userData);

        Property instanceId = propBuilder
                .setId("instance-id")
                .setType("string")
                .setValue("id-ovf")
                .setUiOptional(false)
                .setCategory("")
                .setLabel("A Unique Instance ID for this instance")
                .setInstanceId("")
                .setClassId("")
                .setDescription("Specifies the instance id.")
                .build();
        properties.add(instanceId);

        Property hostname = propBuilder
                .setId("hostname")
                .setType("string")
                .setValue("wangyudemo")
                .setUiOptional(false)
                .setCategory("")
                .setLabel("")
                .setInstanceId("")
                .setClassId("")
                .setDescription("Specify the hostname for the appliance")
                .build();
        properties.add(hostname);

        Property seedfrom = propBuilder
                .setId("seedfrom")
                .setType("string")
                .setValue("")
                .setUiOptional(false)
                .setCategory("")
                .setLabel("Url to seed instance data from")
                .setInstanceId("")
                .setClassId("")
                .setDescription("This field is optional.")
                .build();
        properties.add(seedfrom);

        Property pubKey = propBuilder
                .setId("public-keys")
                .setType("string")
                .setValue("")
                .setUiOptional(false)
                .setCategory("")
                .setLabel("ssh public keys")
                .setInstanceId("")
                .setClassId("")
                .setDescription("This field is optional.")
                .build();
        properties.add(pubKey);

        Property password = propBuilder
                .setId("password")
                .setType("string")
                .setValue("123456")
                .setUiOptional(false)
                .setCategory("")
                .setLabel("Default User's password")
                .setInstanceId("")
                .setClassId("")
                .setDescription("This field is optional.")
                .build();
        properties.add(password);
        return ovfParamsBuilder.setType("PropertyParams").setProperties(properties).build();
    }

    public void prinfOvfSummary(String clusterName,String libraryName) throws InvalidPropertyFaultMsg, NotFoundFaultMsg, RuntimeFaultFaultMsg {
        String ovfItemId = this.getFirstItemId(libraryName);
        LibraryItemTypes.DeploymentTarget target = new LibraryItemTypes.DeploymentTarget();

        ResourcePoolService rps = new ResourcePoolService(this.loginService);
        String resourcePoolId = rps.getResourceIdByName("Resources");

        target.setResourcePoolId(resourcePoolId);

        LibraryItemTypes.OvfSummary summary = ovfLibraryItemService.filter(ovfItemId,target);
        if(summary == null) {
            System.out.println("Cannot get OVF Summary!");
        } else {
            String annotation = summary.getAnnotation();
            if(annotation != null)
                System.out.println("Annotation: "+annotation);
            System.out.println("Name: "+summary.getName());
            System.out.println("Network: "+summary.getNetworks());
            System.out.println("-------------------------------");
            List<Structure> parameters = summary.getAdditionalParams();
            printAdditionalParams(parameters);
        }

    }

    private void printAdditionalParams(List<Structure> parameters) {
        for (Structure structure: parameters) {
            String structName = structure._getCanonicalName();
            System.out.println("The structure Name: "+structName);
            System.out.println("The structure Value is Map<String,DataValue>: ");
            System.out.println("The structure is OvfParams: "+ structure._hasTypeNameOf(OvfParams.class));
            System.out.println("The structure Type: "+ structure._convertTo(OvfParams.class).getType());
            StructValue values = structure._getDataValue();
            Map<String, DataValue> fields = values.getFields();

            for (String key: fields.keySet()) {
                if (key.equals("properties")) {
                    System.out.println("  Key: properties");
                    DataValue properties = fields.get(key);
                    OptionalValue castprops = (OptionalValue) properties;
                    System.out.println("  Properties is List<Map>");
                    for (Object item : castprops.getList()) {
                        System.out.println("    ************************");
                        StructValue myMap = (StructValue)item;
                        Map<String,DataValue> newValue = myMap.getFields();
                        for (String keys : newValue.keySet()) {
                            System.out.println("    Key: "+ keys.trim());
                            DataValue value = newValue.get(keys);
                            System.out.println("    Value: "+ value);
                        }
                    }
                } else {
                    System.out.println("  Key: "+key);
                    DataValue value = fields.get(key);
                    System.out.println("  Value: "+value);
                }

            }
        }
    }

}
