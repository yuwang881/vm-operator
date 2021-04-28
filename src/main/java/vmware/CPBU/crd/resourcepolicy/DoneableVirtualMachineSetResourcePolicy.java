package vmware.CPBU.crd.resourcepolicy;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableVirtualMachineSetResourcePolicy extends CustomResourceDoneable<VirtualMachineSetResourcePolicy> {
    public DoneableVirtualMachineSetResourcePolicy(VirtualMachineSetResourcePolicy resource, Function<VirtualMachineSetResourcePolicy, VirtualMachineSetResourcePolicy> function) {
        super(resource, function);
    }
}

