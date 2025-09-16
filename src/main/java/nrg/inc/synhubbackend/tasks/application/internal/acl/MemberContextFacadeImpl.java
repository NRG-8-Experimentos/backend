package nrg.inc.synhubbackend.tasks.application.internal.acl;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;
import nrg.inc.synhubbackend.tasks.interfaces.acl.MemberContextFacade;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberContextFacadeImpl implements MemberContextFacade {

    private final MemberRepository memberRepository;

    public MemberContextFacadeImpl(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public Optional<Member> createMember() {
        var member = new Member();
        var createdMember = memberRepository.save(member);
        return Optional.of(createdMember);
    }

    @Override
    public List<Member> getMemberByGroupId(Long groupId) {
        var members = memberRepository.findMembersByGroup_Id(groupId);
        return members;
    }

    @Override
    public Optional<Member> getMemberById(Long memberId) {
        var member = memberRepository.findById(memberId);
        return member;
    }
}
