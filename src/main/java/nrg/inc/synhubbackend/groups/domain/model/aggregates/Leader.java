package nrg.inc.synhubbackend.groups.domain.model.aggregates;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;

import java.sql.Time;

@Entity
@Getter
@Setter
public class Leader extends AuditableAbstractAggregateRoot<Leader> {

    Time averageSolutionTime;

    Integer solvedRequests;

    @OneToOne(mappedBy = "leader")
    private User user;

    public Leader() {
        this.averageSolutionTime = new Time(0);
        this.solvedRequests = 0;
    }

}
