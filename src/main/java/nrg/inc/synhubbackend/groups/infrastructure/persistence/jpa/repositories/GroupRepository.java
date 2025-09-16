package nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Long> {

    Optional<Group> findByLeader_Id(Long leaderId);

    boolean existsByCode(GroupCode code);

    Optional<Group> findByCode(GroupCode code);
}
