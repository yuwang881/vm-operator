package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachinePort implements KubernetesResource {
    enum Protocol{TCP,UDP,SCTP}

    private int port;
    private String ip;
    private String name;
    private Protocol protocol;

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Protocol getProtocol() {
        return protocol;
    }

    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String toString() {
        return "VirtualMachinePort{ port=" + getPort()+
                ", ip="+ getIp() +
                ", name="+ getName() +
                ", protocol="+ getProtocol() +
                "}";
    }
}
