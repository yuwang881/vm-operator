package vmware.CPBU.crd.resourcepolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import vmware.CPBU.crd.vmclass.VirtualMachineResourceSpec;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class FolderSpec implements KubernetesResource {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    

    @Override
    public String toString() {
        return "ResourcePoolSpec{" +
                "name=" + getName() +
                "}";
    }
}
