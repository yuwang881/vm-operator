package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.PersistentVolumeClaim;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineVolume implements KubernetesResource {
    private String name;
    private PersistentVolumeClaim persistentVolumeClaim;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PersistentVolumeClaim getPersistentVolumeClaim() {
        return persistentVolumeClaim;
    }

    public void setPersistentVolumeClaim(PersistentVolumeClaim persistentVolumeClaim) {
        this.persistentVolumeClaim = persistentVolumeClaim;
    }

    @Override
    public String toString() {
        return "VirtualMachineVolume{ name=" + getName()+
                ", persistentVolumeClaim="+ getPersistentVolumeClaim() +
                "}";
    }
}
