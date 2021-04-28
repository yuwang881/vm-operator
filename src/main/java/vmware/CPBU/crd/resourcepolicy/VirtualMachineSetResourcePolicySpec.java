package vmware.CPBU.crd.resourcepolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

import java.util.List;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineSetResourcePolicySpec implements KubernetesResource {
    private ResourcePoolSpec resourcepool;
    private FolderSpec folder;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private List<ClusterModuleSpec> clustermodules;

    public ResourcePoolSpec getResourcepool() {
        return resourcepool;
    }

    public void setResourcepool(ResourcePoolSpec resourcepool) {
        this.resourcepool = resourcepool;
    }

    public FolderSpec getFolder() {
        return folder;
    }

    public void setFolder(FolderSpec folder) {
        this.folder = folder;
    }

    public List<ClusterModuleSpec> getClustermodules() {
        return clustermodules;
    }

    public void setClustermodules(List<ClusterModuleSpec> clustermodules) {
        this.clustermodules = clustermodules;
    }

    @Override
    public String toString() {
        return "VirtualMachineSetResourcePolicySpec{resourcepool=" + getResourcepool() +
                ", folder="+ getFolder() +
                ", clustermodules=" + getClustermodules() +
                "}";
    }
}
