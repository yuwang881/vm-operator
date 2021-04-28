package vmware.CPBU.crd.virtmachine;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineStatus implements KubernetesResource {

    @Override
    public String toString() {
        return "VirtualMachineClassStatus";
    }
}
