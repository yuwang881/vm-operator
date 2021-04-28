package vmware.CPBU.crd.resourcepolicy;

import io.fabric8.kubernetes.api.model.ObjectMeta;
import io.fabric8.kubernetes.client.CustomResource;

public class VirtualMachineSetResourcePolicy extends CustomResource {
    private VirtualMachineSetResourcePolicySpec spec;
    private VirtualMachineSetResourcePolicyStatus status;

    public VirtualMachineSetResourcePolicySpec getSpec() {
        return spec;
    }

    public void setSpec(VirtualMachineSetResourcePolicySpec spec) {
        this.spec = spec;
    }

    public VirtualMachineSetResourcePolicyStatus getStatus() {
        return status;
    }

    public void setStatus(VirtualMachineSetResourcePolicyStatus status) {
        this.status = status;
    }

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

    @Override
    public String toString() {
        return "VirtualMachineSetResourcePolicy{" +
                "apiVersion='" + getApiVersion() + "'" +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                ", status=" + status +
                "}";
    }
}
