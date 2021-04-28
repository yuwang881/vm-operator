package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;
import io.fabric8.kubernetes.api.model.TCPSocketAction;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class Probe implements KubernetesResource {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private TCPSocketAction tcpSocket;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private int timeoutSeconds;

    public TCPSocketAction getTcpSocket() {
        return tcpSocket;
    }

    public void setTcpSocket(TCPSocketAction tcpSocket) {
        this.tcpSocket = tcpSocket;
    }

    public int getTimeoutSeconds() {
        return timeoutSeconds;
    }

    public void setTimeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
    }

    @Override
    public String toString() {
        return "Probe{ tcpSocket=" + getTcpSocket()+
                ", timeoutSeconds="+ getTimeoutSeconds() +
                "}";
    }
}
