package vmware.CPBU.crd.vmimage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineImageSpec implements KubernetesResource {
    private String type;
    private String imageSourceType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VirtualMachineImageProductInfo productInfo;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VirtualMachineImageOSInfo osInfo;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageSourceType() {
        return imageSourceType;
    }

    public void setImageSourceType(String imageSourceType) {
        this.imageSourceType = imageSourceType;
    }

    public VirtualMachineImageProductInfo getProductInfo() {
        return productInfo;
    }

    public void setProductInfo(VirtualMachineImageProductInfo productInfo) {
        this.productInfo = productInfo;
    }

    public VirtualMachineImageOSInfo getOsInfo() {
        return osInfo;
    }

    public void setOsInfo(VirtualMachineImageOSInfo osInfo) {
        this.osInfo = osInfo;
    }

    @Override
    public String toString() {
        return "VirtualMachineImageSpec{type=" + getType() +
                ", imageSourceType="+ getImageSourceType() +
                ", productInfo=" + getProductInfo() +
                ", osInfo=" + getOsInfo() +
                "}";
    }
}
