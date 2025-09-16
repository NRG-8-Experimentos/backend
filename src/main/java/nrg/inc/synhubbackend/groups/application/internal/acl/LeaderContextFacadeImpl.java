package nrg.inc.synhubbackend.groups.application.internal.acl;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.LeaderRepository;
import nrg.inc.synhubbackend.groups.interfaces.rest.acl.LeaderContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LeaderContextFacadeImpl implements LeaderContextFacade {

    private final LeaderRepository leaderRepository;

    public LeaderContextFacadeImpl(LeaderRepository leaderRepository) {
        this.leaderRepository = leaderRepository;
    }

    @Override
    public Optional<Leader> createLeader() {
        var leader = new Leader();
        var createdLeader = leaderRepository.save(leader);
        return Optional.of(createdLeader);
    }
}
