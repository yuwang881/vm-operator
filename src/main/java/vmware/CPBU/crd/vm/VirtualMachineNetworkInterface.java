package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;

@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineNetworkInterface implements KubernetesResource {
    private String networkType;
    private String networkName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String ethernetCardType;

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public String getEthernetCardType() {
        return ethernetCardType;
    }

    public void setEthernetCardType(String ethernetCardType) {
        this.ethernetCardType = ethernetCardType;
    }

    @Override
    public String toString() {
        return "VirtualMachineNetworkInterface{ networkType=" + getNetworkType()+
                ", networkName="+ getNetworkName() +
                ", ethernetCardType="+ getEthernetCardType() +
                "}";
    }
}
