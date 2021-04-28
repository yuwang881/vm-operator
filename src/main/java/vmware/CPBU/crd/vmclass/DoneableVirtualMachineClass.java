package vmware.CPBU.crd.vmclass;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableVirtualMachineClass extends CustomResourceDoneable<VirtualMachineClass> {
    public DoneableVirtualMachineClass(VirtualMachineClass resource, Function<VirtualMachineClass, VirtualMachineClass> function) {
        super(resource, function);
    }
}
