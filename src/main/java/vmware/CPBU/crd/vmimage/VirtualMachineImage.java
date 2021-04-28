package vmware.CPBU.crd.vmimage;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class VirtualMachineImage extends CustomResource {
    private VirtualMachineImageSpec spec;
    private VirtualMachineImageStatus status;

    public VirtualMachineImageSpec getSpec() {
        return spec;
    }

    public void setSpec(VirtualMachineImageSpec spec) {
        this.spec = spec;
    }

    public VirtualMachineImageStatus getStatus() {
        return status;
    }

    public void setStatus(VirtualMachineImageStatus status) {
        this.status = status;
    }

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

    @Override
    public String toString() {
        return "VirtualMachineImage{" +
                "apiVersion='" + getApiVersion() + "'" +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                ", status=" + status +
                "}";
    }
}
