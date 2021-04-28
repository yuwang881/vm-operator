package vmware.CPBU.crd.vm;

import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import io.fabric8.kubernetes.api.model.KubernetesResource;


@JsonDeserialize(
        using = JsonDeserializer.None.class
)
public class VirtualMachineCondition implements KubernetesResource{
    private String type;
    private Status status;
    private String lastTransactionTime;
    private String reason;
    private String message;

    public enum Status {True, False, Unknown}

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getLastTransactionTime() {
        return lastTransactionTime;
    }

    public void setLastTransactionTime(String lastTransactionTime) {
        this.lastTransactionTime = lastTransactionTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "VirtualMachineCondition{ type=" + getType()+
                ", status="+ getStatus() +
                ", lastTransactionTime="+ getLastTransactionTime() +
                ", reason="+ getReason() +
                ", message="+ getMessage() +
                "}";
    }
}
