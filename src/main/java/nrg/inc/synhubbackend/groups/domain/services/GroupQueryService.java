package nrg.inc.synhubbackend.groups.domain.services;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByCodeQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByMemberIdQuery;

import java.util.Optional;

public interface GroupQueryService {
    Optional<Group> handle(GetGroupByLeaderIdQuery query);
    Optional<Group> handle(GetGroupByCodeQuery query);
    Optional<Group> handle(GetGroupByMemberIdQuery query);
    Optional<Group> handle(GetGroupByIdQuery query);
}
