package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineStatus implements KubernetesResource {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String host;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VirtualMachinePowerState powerState;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private VMStatusPhase phase;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VirtualMachineCondition> conditions;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String vmIp;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uniqueID;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String biosUUID;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<VirtualMachineVolumeStatus> volumes;

    public enum VirtualMachinePowerState{poweredOff,poweredOn}
    public enum VMStatusPhase{Creating,Created,Deleting,Deleted,Unknown}

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public VirtualMachinePowerState getPowerState() {
        return powerState;
    }

    public void setPowerState(VirtualMachinePowerState powerState) {
        this.powerState = powerState;
    }

    public List<VirtualMachineCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<VirtualMachineCondition> conditions) {
        this.conditions = conditions;
    }

    public String getVmIp() {
        return vmIp;
    }

    public void setVmIp(String vmIp) {
        this.vmIp = vmIp;
    }

    public String getUniqueID() {
        return uniqueID;
    }

    public void setUniqueID(String uniqueID) {
        this.uniqueID = uniqueID;
    }

    public String getBiosUUID() {
        return biosUUID;
    }

    public void setBiosUUID(String biosUUID) {
        this.biosUUID = biosUUID;
    }

    public List<VirtualMachineVolumeStatus> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<VirtualMachineVolumeStatus> volumes) {
        this.volumes = volumes;
    }

    public VMStatusPhase getPhase() {
        return phase;
    }

    public void setPhase(VMStatusPhase phase) {
        this.phase = phase;
    }

    @Override
    public String toString() {
        return "VirtualMachineStatus{ host=" + getHost()+
                ", powerState="+ getPowerState() +
                ", phase="+ getPhase() +
                ", vmIp="+ getVmIp() +
                ", uniqueId="+ getUniqueID() +
                ", biosUUID="+ getBiosUUID() +
                "}";
    }
}
