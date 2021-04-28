package vmware.CPBU.crd.vmclass;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import vmware.CPBU.crd.vmimage.VirtualMachineImageOSInfo;
import vmware.CPBU.crd.vmimage.VirtualMachineImageProductInfo;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineClassHardware implements KubernetesResource {
    private int cpus;
    private String memory;

    public int getCpus() {
        return cpus;
    }

    public void setCpus(int cpus) {
        this.cpus = cpus;
    }

    public String getMemory() {
        return memory;
    }

    public void setMemory(String memory) {
        this.memory = memory;
    }

    @Override
    public String toString() {
        return "VirtualMachineClassHardware{cpus=" + getCpus() +
                ", memory="+ getMemory() +
                "}";
    }
}
