package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineVolumeStatus implements KubernetesResource {
    private String name;
    private boolean attached;
    private String diskUUID;
    private String error;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAttached() {
        return attached;
    }

    public void setAttached(boolean attached) {
        this.attached = attached;
    }

    public String getDiskUUID() {
        return diskUUID;
    }

    public void setDiskUUID(String diskUUID) {
        this.diskUUID = diskUUID;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    @Override
    public String toString() {
        return "VirtualMachineVolumeStatus{ name=" + getName()+
                ", attached="+ isAttached() +
                ", diskUUID="+ getDiskUUID() +
                ", error="+ getError() +
                "}";
    }
}
