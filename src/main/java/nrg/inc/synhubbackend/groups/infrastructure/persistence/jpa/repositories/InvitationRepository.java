package nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InvitationRepository extends JpaRepository<Invitation, Long> {
    Optional<Invitation> findByMember_Id(Long memberId);
    List<Invitation> findByGroup_Id(Long groupId);
    boolean existsByMember_Id(Long memberId);
}
