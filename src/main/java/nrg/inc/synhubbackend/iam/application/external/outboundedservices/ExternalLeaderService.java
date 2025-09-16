package nrg.inc.synhubbackend.iam.application.external.outboundedservices;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.interfaces.rest.acl.LeaderContextFacade;
import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserLeaderCommand;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ExternalLeaderService {
    private final LeaderContextFacade leaderContextFacade;

    public ExternalLeaderService(LeaderContextFacade leaderContextFacade) {
        this.leaderContextFacade = leaderContextFacade;
    }

    public Optional<Leader> createUserLeader(CreateUserLeaderCommand command) {
        var leader = this.leaderContextFacade.createLeader();
        if (leader.isEmpty()) {
            throw new IllegalArgumentException("Error creating leader");
        }
        return leader;
    }
}
