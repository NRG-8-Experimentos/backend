package nrg.inc.synhubbackend.groups.application.internal.queryservices;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByCodeQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.GroupRepository;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GroupQueryServiceImpl implements GroupQueryService {
    private final GroupRepository groupRepository;
    private final MemberRepository memberRepository;

    public GroupQueryServiceImpl(GroupRepository groupRepository, MemberRepository memberRepository) {
        this.groupRepository = groupRepository;
        this.memberRepository = memberRepository;
    }

    @Override
    public Optional<Group> handle(GetGroupByLeaderIdQuery query) {
        return this.groupRepository.findByLeader_Id(query.leaderId());
    }

    @Override
    public Optional<Group> handle(GetGroupByCodeQuery query) {
        var code = new GroupCode(query.code());
        var group = this.groupRepository.findByCode(code);
        if (group.isEmpty()) {
            return Optional.empty();
        }
        return group;
    }

    @Override
    public Optional<Group> handle(GetGroupByMemberIdQuery query) {
        var member = this.memberRepository.findById(query.memberId());
        var group = member.get().getGroup();
        if(group == null) {
            return Optional.empty();
        }
        var groupId = group.getId();

        var groupSearched = this.groupRepository.findById(groupId);

        return groupSearched;
    }

    @Override
    public Optional<Group> handle(GetGroupByIdQuery query) {
        return this.groupRepository.findById(query.groupId());
    }



}
