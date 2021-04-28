package vmware.CPBU.crd.virtmachine;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineSpec implements KubernetesResource {

    private String imageName;
    private String className;
    private String powerState;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String storageClass;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resourcePolicyName;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public String getResourcePolicyName() {
        return resourcePolicyName;
    }

    public void setResourcePolicyName(String resourcePolicyName) {
        this.resourcePolicyName = resourcePolicyName;
    }

    @Override
    public String toString() {
        return "VirtualMachineClassSpec{imageName=" + imageName +
                ", className="+className +
                "}";
    }
}
