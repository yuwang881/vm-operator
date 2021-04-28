package vmware.CPBU.crd.resourcepolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import vmware.CPBU.crd.vmclass.VirtualMachineResourceSpec;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class ResourcePoolSpec implements KubernetesResource {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String name;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VirtualMachineResourceSpec reservations;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VirtualMachineResourceSpec limits;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public VirtualMachineResourceSpec getReservations() {
        return reservations;
    }

    public void setReservations(VirtualMachineResourceSpec reservations) {
        this.reservations = reservations;
    }

    public VirtualMachineResourceSpec getLimits() {
        return limits;
    }

    public void setLimits(VirtualMachineResourceSpec limits) {
        this.limits = limits;
    }

    @Override
    public String toString() {
        return "ResourcePoolSpec{" +
                "name=" + getName() +
                ", reservations=" + getReservations() +
                ", limits="+ getLimits() +
                "}";
    }
}
