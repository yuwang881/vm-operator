package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineSpec implements KubernetesResource {
    private String imageName;
    private String className;
    private VirtualMachinePowerState powerState;
    public enum VirtualMachinePowerState{poweredOff,poweredOn}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VirtualMachinePort> ports;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VirtualMachineMetadata vmMetadata;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String storageClass;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VirtualMachineNetworkInterface> networkInterfaces;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String resourcePolicyName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VirtualMachineVolume> volumes;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Probe readinessProbe;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public VirtualMachinePowerState getPowerState() {
        return powerState;
    }

    public void setPowerState(VirtualMachinePowerState powerState) {
        this.powerState = powerState;
    }

    public List<VirtualMachinePort> getPorts() {
        return ports;
    }

    public void setPorts(List<VirtualMachinePort> ports) {
        this.ports = ports;
    }

    public VirtualMachineMetadata getVmMetadata() {
        return vmMetadata;
    }

    public void setVmMetadata(VirtualMachineMetadata vmMetadata) {
        this.vmMetadata = vmMetadata;
    }

    public String getStorageClass() {
        return storageClass;
    }

    public void setStorageClass(String storageClass) {
        this.storageClass = storageClass;
    }

    public List<VirtualMachineNetworkInterface> getNetworkInterfaces() {
        return networkInterfaces;
    }

    public void setNetworkInterfaces(List<VirtualMachineNetworkInterface> networkInterfaces) {
        this.networkInterfaces = networkInterfaces;
    }

    public String getResourcePolicyName() {
        return resourcePolicyName;
    }

    public void setResourcePolicyName(String resourcePolicyName) {
        this.resourcePolicyName = resourcePolicyName;
    }

    public List<VirtualMachineVolume> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VirtualMachineVolume> volumes) {
        this.volumes = volumes;
    }

    public Probe getReadinessProbe() {
        return readinessProbe;
    }

    public void setReadinessProbe(Probe readinessProbe) {
        this.readinessProbe = readinessProbe;
    }

    @Override
    public String toString() {
        return "VirtualMachineSpec{imageName=" + getImageName() +
                ", className="+ getClassName() +
                ", powerState=" + getPowerState() +
                "}";
    }
}
