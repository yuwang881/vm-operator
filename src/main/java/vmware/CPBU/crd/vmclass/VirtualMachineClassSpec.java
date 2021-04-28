package vmware.CPBU.crd.vmclass;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineClassSpec implements KubernetesResource {
    private VirtualMachineClassHardware hardware;


    private VirtualMachineClassPolicies policies;

    public VirtualMachineClassHardware getHardware() {
        return hardware;
    }

    public void setHardware(VirtualMachineClassHardware hardware) {
        this.hardware = hardware;
    }

    public VirtualMachineClassPolicies getPolicies() {
        return policies;
    }

    public void setPolicies(VirtualMachineClassPolicies policies) {
        this.policies = policies;
    }

    @Override
    public String toString() {
        return "VirtualMachineClassSpec{hardware=" + hardware +
                ", policies="+policies +
                "}";
    }
}
