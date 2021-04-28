package vmware.CPBU.crd.contentlibrary;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import java.util.Date;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class ContentLibraryStatus implements KubernetesResource {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String uuid="";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String message="";

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Date lastUpdateTime;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public String toString() {
        return "ContentLibraryStatus{ uuid=" + uuid
                + ", message=" + message
                +"}";
    }
}
