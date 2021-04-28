package vmware.CPBU.crd.vmclass;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineResourceSpec implements KubernetesResource {
    private String cpu;
    private String memory;

    public String getCpu() {
        return cpu;
    }

    public void setCpus(String cpu) {
        this.cpu = cpu;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    @Override
    public String toString() {
        return "VirtualMachineResourceSpec{cpu=" + getCpu() +
                ", memory="+ getMemory() +
                "}";
    }
}
