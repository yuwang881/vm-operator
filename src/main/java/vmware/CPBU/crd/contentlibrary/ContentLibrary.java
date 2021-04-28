package vmware.CPBU.crd.contentlibrary;

import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.api.model.ObjectMeta;

public class ContentLibrary extends CustomResource {
    private ContentLibrarySpec spec;
    private ContentLibraryStatus status;

    public ContentLibrarySpec getSpec() {
        return spec;
    }

    public void setSpec(ContentLibrarySpec spec) {
        this.spec = spec;
    }

    public ContentLibraryStatus getStatus() {
        return status;
    }

    public void setStatus(ContentLibraryStatus status) {
        this.status = status;
    }

    @Override
    public ObjectMeta getMetadata() {
        return super.getMetadata();
    }

    @Override
    public String toString() {
        return "ContentLibrary{" +
                "apiVersion='" + getApiVersion() + "'" +
                ", metadata=" + getMetadata() +
                ", spec=" + spec +
                ", status=" + status +
                "}";
    }
}
