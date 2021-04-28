package vmware.CPBU.crd.vmimage;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.Date;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineImageStatus implements KubernetesResource {
    private String uuid;
    private String internalId;
    private String powerState;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getPowerState() {
        return powerState;
    }

    public void setPowerState(String powerState) {
        this.powerState = powerState;
    }

    @Override
    public String toString() {
        return "ContentLibraryStatus{ uuid=" + uuid
                + ", internalId=" + internalId
                +"}";
    }
}
