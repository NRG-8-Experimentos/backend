package nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LeaderRepository extends JpaRepository<Leader, Long> {
}
