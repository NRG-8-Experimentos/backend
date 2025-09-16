package nrg.inc.synhubbackend.groups.application.internal.queryservices;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.LeaderRepository;
import nrg.inc.synhubbackend.shared.application.external.outboundedservices.ExternalIamService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class LeaderQueryServiceImpl implements LeaderQueryService {

    private final LeaderRepository leaderRepository;
    private final ExternalIamService externalIamService;

    public LeaderQueryServiceImpl(LeaderRepository leaderRepository, ExternalIamService externalIamService) {
        this.leaderRepository = leaderRepository;
        this.externalIamService = externalIamService;
    }

    @Override
    public Optional<Leader> handle(GetLeaderByIdQuery query) {
        return leaderRepository.findById(query.leaderId());
    }

    @Override
    public Optional<Leader> handle(GetLeaderByUsernameQuery query) {
        var user = externalIamService.getUserByUsername(query.username());

        var role = user.get().getRoles().stream().findFirst().get().getName().toString();

        if (!role.equals("ROLE_LEADER")){
            return Optional.empty();
        }

        var leader = user.get().getLeader();
        if (leader == null) {
            return Optional.empty();
        }

        return Optional.of(leader);
    }
}
