package nrg.inc.synhubbackend.groups.domain.model.aggregates;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import nrg.inc.synhubbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;

@Entity
@Setter
@Getter
public class Invitation extends AuditableAbstractAggregateRoot<Invitation> {
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne
    @JoinColumn(name = "group_id")
    private Group group;

    public Invitation() {}

    public Invitation(Member member, Group group) {
        this.member = member;
        this.group = group;
    }
}
