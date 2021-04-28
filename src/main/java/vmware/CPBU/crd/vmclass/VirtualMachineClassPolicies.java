package vmware.CPBU.crd.vmclass;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineClassPolicies implements KubernetesResource {

    private VirtualMachineClassResources resources;

    public VirtualMachineClassResources getResources() {
        return resources;
    }

    public void setResources(VirtualMachineClassResources resources) {
        this.resources = resources;
    }

    @Override
    public String toString() {
        return "VirtualMachineClassPolicies{resources=" + getResources() + "}";
    }
}
