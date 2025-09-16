package nrg.inc.synhubbackend.metrics.infrastructure.client;

import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByIdQuery;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.metrics.domain.model.aggregates.GroupMetrics;
import nrg.inc.synhubbackend.metrics.infrastructure.persistenence.jpa.repositories.GroupMetricsRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class GroupsClient implements GroupMetricsRepository {

    private final GroupQueryService groupQueryService;

    public GroupsClient(GroupQueryService groupQueryService) {
        this.groupQueryService = groupQueryService;
    }

    @Override
    public Optional<GroupMetrics> getGroupMetrics(Long groupId) {
        return groupQueryService.handle(new GetGroupByIdQuery(groupId))
                .map(group -> new GroupMetrics(groupId, group.getMemberCount()));
    }
}
