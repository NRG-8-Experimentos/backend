package nrg.inc.synhubbackend.tasks.interfaces.acl;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;

import java.util.List;
import java.util.Optional;

public interface MemberContextFacade {
    Optional<Member> createMember();
    List<Member> getMemberByGroupId(Long groupId);
    Optional<Member> getMemberById(Long memberId);
}
