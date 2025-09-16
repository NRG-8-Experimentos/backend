package nrg.inc.synhubbackend.groups.domain.services;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationsByGroupIdQuery;

import java.util.List;
import java.util.Optional;

public interface InvitationQueryService {
    Optional<Invitation> handle(GetInvitationByMemberIdQuery query);
    List<Invitation> handle(GetInvitationsByGroupIdQuery query);
}
