package nrg.inc.synhubbackend.groups.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByCodeQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupMemberResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.transform.GroupMemberResourceFromEntityAssembler;
import nrg.inc.synhubbackend.groups.interfaces.rest.transform.GroupResourceFromEntityAssembler;
import nrg.inc.synhubbackend.shared.application.external.outboundedservices.ExternalMemberService;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTasksByGroupIdQuery;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.TaskResource;
import nrg.inc.synhubbackend.tasks.interfaces.rest.transform.TaskResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/groups")
@Tag(name = "Groups", description = "Group management API")
public class GroupController {
    private final GroupQueryService groupQueryService;
    private final ExternalMemberService externalMemberService;
    private final TaskQueryService taskQueryService;
    private final LeaderQueryService leaderQueryService;

    public GroupController(GroupQueryService groupQueryService, ExternalMemberService externalMemberService, TaskQueryService taskQueryService, LeaderQueryService leaderQueryService) {
        this.groupQueryService = groupQueryService;
        this.externalMemberService = externalMemberService;
        this.taskQueryService = taskQueryService;
        this.leaderQueryService = leaderQueryService;
    }

    @GetMapping("/search")
    @Operation(summary = "Search for a group by code", description = "Search for a group by code")
    public ResponseEntity<GroupResource> searchGroupByCode(@RequestParam String code) {
        var getGroupByCodeQuery = new GetGroupByCodeQuery(code);
        var group = this.groupQueryService.handle(getGroupByCodeQuery);
        if (group.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var groupResource = GroupResourceFromEntityAssembler.toResourceFromEntity(group.get());
        return ResponseEntity.ok(groupResource);
    }



    @GetMapping("/members")
    @Operation(summary = "Get all group members", description = "Retrieve all members of a group")
    public ResponseEntity<List<GroupMemberResource>> getAllMembersByGroupId(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var getGroupByLeaderIdQuery = new GetGroupByLeaderIdQuery(leader.get().getId());

        var group = this.groupQueryService.handle(getGroupByLeaderIdQuery);

        if (group.isEmpty()) return ResponseEntity.notFound().build();

        Long groupId = group.get().getId();

        var members = externalMemberService.getMembersByGroupId(groupId);

        var memberResources = members.stream()
                .map(GroupMemberResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(memberResources);
    }

    @GetMapping("/tasks")
    @Operation(summary = "Get all tasks by group ID", description = "Retrieve all tasks associated with a specific group ID")
    public ResponseEntity<List<TaskResource>> getAllTasksByGroupId(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var getGroupByLeaderIdQuery = new GetGroupByLeaderIdQuery(leader.get().getId());

        var group = this.groupQueryService.handle(getGroupByLeaderIdQuery);

        if (group.isEmpty()) return ResponseEntity.notFound().build();

        Long groupId = group.get().getId();

        var getAllTasksByGroupIdQuery = new GetAllTasksByGroupIdQuery(groupId);

        var tasks = taskQueryService.handle(getAllTasksByGroupIdQuery);

        var taskResources = tasks.stream()
                .map(TaskResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());

        return ResponseEntity.ok(taskResources);
    }
}
