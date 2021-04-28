package vmware.CPBU.crd.vmimage;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineImageProductInfo implements KubernetesResource {
    private String product;
    private String vendor;
    private String version;
    private String fullVersion;

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getFullVersion() {
        return fullVersion;
    }

    public void setFullVersion(String fullVersion) {
        this.fullVersion = fullVersion;
    }

    @Override
    public String toString() {
        return "VirtualMachineImageProductInfo{" +
                "product=" + getProduct() +
                ", vendor=" + getVendor() +
                ", fullVersion="+ getFullVersion() +
                "}";
    }
}
