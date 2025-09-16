package nrg.inc.synhubbackend.groups.interfaces.rest.acl;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;

import java.util.Optional;

public interface LeaderContextFacade {
    Optional<Leader> createLeader();
}
