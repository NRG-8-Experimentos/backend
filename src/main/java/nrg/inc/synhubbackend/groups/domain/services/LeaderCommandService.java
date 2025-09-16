package nrg.inc.synhubbackend.groups.domain.services;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateLeaderCommand;

import java.util.Optional;

public interface LeaderCommandService {

    Optional<Leader> handle(CreateLeaderCommand command);
}
