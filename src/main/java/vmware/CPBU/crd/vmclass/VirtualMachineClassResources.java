package vmware.CPBU.crd.vmclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import vmware.CPBU.crd.vmimage.VirtualMachineImageOSInfo;
import vmware.CPBU.crd.vmimage.VirtualMachineImageProductInfo;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineClassResources implements KubernetesResource {

    private VirtualMachineResourceSpec requests;
    private VirtualMachineResourceSpec limits;

    public VirtualMachineResourceSpec getRequests() {
        return requests;
    }

    public void setRequests(VirtualMachineResourceSpec requests) {
        this.requests = requests;
    }

    public VirtualMachineResourceSpec getLimits() {
        return limits;
    }

    public void setLimits(VirtualMachineResourceSpec limits) {
        this.limits = limits;
    }

    @Override
    public String toString() {
        return "VirtualMachineClassResources{requests=" + getRequests() +
                ", limits="+ getLimits() +
                "}";
    }
}
