package nrg.inc.synhubbackend.tasks.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import nrg.inc.synhubbackend.tasks.domain.model.commands.CreateTaskCommand;
import nrg.inc.synhubbackend.tasks.domain.model.commands.UpdateTaskCommand;
import nrg.inc.synhubbackend.tasks.domain.model.commands.UpdateTaskStatusCommand;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Getter
@Entity
public class Task extends AuditableAbstractAggregateRoot<Task> {
    @NonNull
    private String title;

    @NonNull
    private String description;

    @Setter
    @Enumerated(EnumType.STRING)
    private TaskStatus status;

    @NonNull
    private OffsetDateTime dueDate;

    @Setter
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Setter
    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    @Column(nullable = false)
    private Integer timesRearranged = 0;

    @Column(nullable = false)
    private Long timePassed = 0L;

    public Task() {
        this.status = TaskStatus.IN_PROGRESS;
        this.timesRearranged = 0;
    }

    public Task(CreateTaskCommand command) {
        this.title = command.title();
        this.description = command.description();
        this.dueDate = command.dueDate();
        this.status = TaskStatus.IN_PROGRESS;
    }

    public void updateStatus(UpdateTaskStatusCommand command) {

        var commandStatus = TaskStatus.valueOf(command.status());

        if(this.status == TaskStatus.IN_PROGRESS && commandStatus == TaskStatus.COMPLETED) {
            OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
            if (timesRearranged > 0) {
                long updatedAt = this.getUpdatedAt().toInstant().toEpochMilli();
                this.timePassed += now.toInstant().toEpochMilli() - updatedAt;
            } else {
                this.timePassed = now.toInstant().toEpochMilli() - this.getCreatedAt().toInstant().toEpochMilli();
            }
        } else if(this.status == TaskStatus.COMPLETED && commandStatus == TaskStatus.IN_PROGRESS) {
            timesRearranged++;
        } else if (this.status == TaskStatus.ON_HOLD && commandStatus == TaskStatus.IN_PROGRESS) {
            timesRearranged++;
        } else if (this.status == TaskStatus.EXPIRED && commandStatus == TaskStatus.IN_PROGRESS) {
            timesRearranged++;
        }
        this.status = TaskStatus.valueOf(command.status());
    }

    public void updateTask(UpdateTaskCommand command) {
        if(command.title() != null && command.title() != "") {
            this.title = command.title();
        }
        if(command.description() != null && command.description() != "") {
            this.description = command.description();
        }
        if(command.dueDate() != null) {
            this.dueDate = command.dueDate();
            if(command.dueDate().isBefore(OffsetDateTime.now(ZoneOffset.UTC))) {
                this.status = TaskStatus.EXPIRED;
            } else {
                this.status = TaskStatus.IN_PROGRESS;
            }
        }
    }
}
