package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)

public class VirtualMachineMetadata implements KubernetesResource {
    private String configMapName;
    private VirtualMachineMetadataTransport transport;

    enum VirtualMachineMetadataTransport {ExtraConfig,OvfEnv}

    public String getConfigMapName() {
        return configMapName;
    }

    public void setConfigMapName(String configMapName) {
        this.configMapName = configMapName;
    }

    public VirtualMachineMetadataTransport getTransport() {
        return transport;
    }

    public void setTransport(VirtualMachineMetadataTransport transport) {
        this.transport = transport;
    }

    @Override
    public String toString() {
        return "VirtualMachineMetadata{ configMapName=" + getConfigMapName()+
                ", transport="+ getTransport() +
                "}";
    }
}
