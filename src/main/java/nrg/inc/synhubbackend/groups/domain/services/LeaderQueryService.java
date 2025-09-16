package nrg.inc.synhubbackend.groups.domain.services;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;

import java.util.Optional;

public interface LeaderQueryService {
    Optional<Leader> handle(GetLeaderByIdQuery query);
    Optional<Leader> handle(GetLeaderByUsernameQuery query);
}
