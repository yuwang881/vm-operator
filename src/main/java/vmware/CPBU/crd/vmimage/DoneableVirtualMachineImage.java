package vmware.CPBU.crd.vmimage;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableVirtualMachineImage extends CustomResourceDoneable<VirtualMachineImage> {
    public DoneableVirtualMachineImage(VirtualMachineImage resource, Function<VirtualMachineImage, VirtualMachineImage> function) {
        super(resource, function);
    }
}
