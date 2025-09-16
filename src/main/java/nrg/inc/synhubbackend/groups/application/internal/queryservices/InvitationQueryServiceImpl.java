package nrg.inc.synhubbackend.groups.application.internal.queryservices;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationsByGroupIdQuery;
import nrg.inc.synhubbackend.groups.domain.services.InvitationQueryService;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.InvitationRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class InvitationQueryServiceImpl implements InvitationQueryService {

    private final InvitationRepository invitationRepository;

    public InvitationQueryServiceImpl(InvitationRepository invitationRepository) {
        this.invitationRepository = invitationRepository;
    }

    @Override
    public Optional<Invitation> handle(GetInvitationByMemberIdQuery query) {
        return this.invitationRepository.findByMember_Id(query.memberId());
    }

    @Override
    public List<Invitation> handle(GetInvitationsByGroupIdQuery query) {
        return this.invitationRepository.findByGroup_Id(query.groupId());
    }
}
