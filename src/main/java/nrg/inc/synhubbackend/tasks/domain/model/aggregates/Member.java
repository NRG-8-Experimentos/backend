package nrg.inc.synhubbackend.tasks.domain.model.aggregates;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import nrg.inc.synhubbackend.tasks.domain.model.commands.CreateMemberCommand;

import java.util.List;

@Getter
@Setter
@Entity
public class Member extends AuditableAbstractAggregateRoot<Member> {

    @ManyToOne
    @JoinColumn(name = "group_id", nullable = true)
    private Group group;

    @OneToMany(mappedBy = "member")
    private List<Task> tasks;

    @OneToOne(mappedBy = "member")
    private User user;

    public Member() {}

    public Member(CreateMemberCommand command) {
    }

    public void addTask(Task task) {
        this.tasks.add(task);
        task.setMember(this);
    }

    public void removeTask(Task task) {
        this.tasks.remove(task);
        task.setMember(null);
    }
}
