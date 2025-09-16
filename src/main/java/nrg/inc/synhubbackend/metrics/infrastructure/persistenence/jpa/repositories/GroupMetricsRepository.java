package nrg.inc.synhubbackend.metrics.infrastructure.persistenence.jpa.repositories;

import nrg.inc.synhubbackend.metrics.domain.model.aggregates.GroupMetrics;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupMetricsRepository {
    Optional<GroupMetrics> getGroupMetrics(Long groupId);
}
