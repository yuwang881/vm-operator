package vmware.CPBU.crd.vmclass;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Date;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineClassStatus implements KubernetesResource {

    @Override
    public String toString() {
        return "VirtualMachineClassStatus";
    }
}
