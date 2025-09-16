package nrg.inc.synhubbackend.requests.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nrg.inc.synhubbackend.requests.domain.model.commands.CreateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.valueobjects.RequestStatus;
import nrg.inc.synhubbackend.requests.domain.model.valueobjects.RequestType;
import nrg.inc.synhubbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;

@Getter
@Entity
@Table(name = "requests")
public class Request extends AuditableAbstractAggregateRoot<Request> {
    @NonNull
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_type")
    private RequestType requestType;

    @Enumerated(EnumType.STRING)
    @Column(name = "request_status")
    private RequestStatus requestStatus;

    @Setter
    @ManyToOne
    @JoinColumn(name = "task_id")
    private Task task;

    public Request() {
    }

    public String getRequestType() {
        return requestType.toString();
    }

    public String getRequestStatus() {
        return requestStatus.toString();
    }

    public Request(CreateRequestCommand command) {
        this.description = command.description();
        this.requestType = RequestType.fromString(command.requestType());
        this.requestStatus = RequestStatus.PENDING;
    }

    public void updateRequestStatus(String requestStatus) {
        this.requestStatus = RequestStatus.fromString(requestStatus);
    }
}
