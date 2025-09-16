package nrg.inc.synhubbackend.groups.application.internal.commandservices;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateLeaderCommand;
import nrg.inc.synhubbackend.groups.domain.services.LeaderCommandService;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.LeaderRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LeaderCommandServiceImpl implements LeaderCommandService {

    private final LeaderRepository leaderRepository;

    public LeaderCommandServiceImpl(LeaderRepository leaderRepository) {
        this.leaderRepository = leaderRepository;
    }


    @Override
    public Optional<Leader> handle(CreateLeaderCommand command) {
        Leader leader = new Leader();
        leaderRepository.save(leader);
        return Optional.of(leader);
    }
}


