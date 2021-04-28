package vmware.CPBU.crd.vm;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableVirtualMachine extends CustomResourceDoneable<VirtualMachine> {
    public DoneableVirtualMachine(VirtualMachine resource, Function<VirtualMachine, VirtualMachine> function) {
        super(resource, function);
    }
}

