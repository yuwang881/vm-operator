package vmware.CPBU.crd.resourcepolicy;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class ClusterModuleStatus implements KubernetesResource {
    private String groupname;
    private String moduleUUID;

    public String getGroupname() {
        return groupname;
    }
    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }

    public String getModuleUUID() {
        return moduleUUID;
    }

    public void setModuleUUID(String moduleUUID) {
        this.moduleUUID = moduleUUID;
    }

    @Override
    public String toString() {
        return "ClusterModuleStatus{" +
                "groupname=" + getGroupname() +
                ", uuid=" + getModuleUUID() +
                "}";
    }
}
