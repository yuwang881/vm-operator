package vmware.CPBU.crd.vm;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class VirtualMachine extends CustomResource {
    private VirtualMachineSpec spec;
    private VirtualMachineStatus status;

    public VirtualMachineSpec getSpec() {
        return spec;
    }

    public void setSpec(VirtualMachineSpec spec) {
        this.spec = spec;
    }

    public VirtualMachineStatus getStatus() {
        return status;
    }

    public void setStatus(VirtualMachineStatus status) {
        this.status = status;
    }

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

    @Override
    public String toString() {
        return "VirtualMachine{" +
                "apiVersion='" + getApiVersion() + "'" +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                ", status=" + status +
                "}";
    }
}
