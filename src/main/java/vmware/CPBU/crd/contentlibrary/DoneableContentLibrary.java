package vmware.CPBU.crd.contentlibrary;

import io.fabric8.kubernetes.api.builder.Function;
import io.fabric8.kubernetes.client.CustomResourceDoneable;

public class DoneableContentLibrary extends CustomResourceDoneable<ContentLibrary> {
    public DoneableContentLibrary(ContentLibrary resource, Function<ContentLibrary, ContentLibrary> function) {
        super(resource, function);
    }
}
