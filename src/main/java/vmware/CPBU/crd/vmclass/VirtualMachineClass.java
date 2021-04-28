package vmware.CPBU.crd.vmclass;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class VirtualMachineClass extends CustomResource {
    private VirtualMachineClassSpec spec;
    private VirtualMachineClassStatus status;

    public VirtualMachineClassSpec getSpec() {
        return spec;
    }

    public void setSpec(VirtualMachineClassSpec spec) {
        this.spec = spec;
    }

    public VirtualMachineClassStatus getStatus() {
        return status;
    }

    public void setStatus(VirtualMachineClassStatus status) {
        this.status = status;
    }

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

    @Override
    public String toString() {
        return "VirtualMachineClass{" +
                "apiVersion='" + getApiVersion() + "'" +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                ", status=" + status +
                "}";
    }
}
