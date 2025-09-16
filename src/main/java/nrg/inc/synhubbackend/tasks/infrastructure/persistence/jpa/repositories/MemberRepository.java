package nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    List<Member> findMembersByGroup_Id(Long groupId);
}
