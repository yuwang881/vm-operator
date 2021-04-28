package vmware.CPBU.crd.resourcepolicy;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class ClusterModuleSpec implements KubernetesResource {
    private String groupname;

    public String getGroupname() {
        return groupname;
    }
    public void setGroupname(String groupname) {
        this.groupname = groupname;
    }
    

    @Override
    public String toString() {
        return "ClusterModuleSpec{" +
                "groupname=" + getGroupname() +
                "}";
    }
}
