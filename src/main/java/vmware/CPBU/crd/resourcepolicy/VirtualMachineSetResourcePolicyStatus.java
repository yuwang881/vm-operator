package vmware.CPBU.crd.resourcepolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineSetResourcePolicyStatus implements KubernetesResource {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ClusterModuleStatus> clustermodules;

    public List<ClusterModuleStatus> getClustermodules() {
        return clustermodules;
    }

    public void setClustermodules(List<ClusterModuleStatus> clustermodules) {
        this.clustermodules = clustermodules;
    }


    @Override
    public String toString() {
        return "VirtualMachineSetResourcePolicyStatus{ clustermodules=" + getClustermodules()
                +"}";
    }
}
