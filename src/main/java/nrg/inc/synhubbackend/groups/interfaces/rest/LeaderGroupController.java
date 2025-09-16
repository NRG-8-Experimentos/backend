package nrg.inc.synhubbackend.groups.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nrg.inc.synhubbackend.groups.application.external.ExternalTasksService;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.DeleteGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.RemoveMemberFromGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.UpdateGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.GroupCommandService;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.CreateGroupResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.UpdateGroupResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.transform.GroupResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/v1/leader/group")
@Tag(name = "Groups", description = "Group management API")

public class LeaderGroupController {

    private final GroupQueryService groupQueryService;
    private final GroupCommandService groupCommandService;
    private final LeaderQueryService leaderQueryService;
    private final ExternalTasksService externalTasksService;

    public LeaderGroupController(GroupQueryService groupQueryService, GroupCommandService groupCommandService, LeaderQueryService leaderQueryService, ExternalTasksService externalTasksService) {
        this.groupQueryService = groupQueryService;
        this.groupCommandService = groupCommandService;
        this.leaderQueryService = leaderQueryService;
        this.externalTasksService = externalTasksService;
    }

    @PostMapping
    @Operation(summary = "Create a new group", description = "Creates a new group")
    public ResponseEntity<GroupResource> createGroup(@RequestBody CreateGroupResource resource, @AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var createGroupCommand = new CreateGroupCommand(
                resource.name(),
                resource.imgUrl(),
                resource.description(),
                leader.get().getId()
        );

        var group = this.groupCommandService.handle(createGroupCommand);

        if (group.isEmpty()) return ResponseEntity.notFound().build();

        var groupResourceCreated = GroupResourceFromEntityAssembler.toResourceFromEntity(group.get());
        return ResponseEntity.ok(groupResourceCreated);
    }

    @PutMapping
    @Operation(summary = "Update a group", description = "Updates a group")
    public ResponseEntity<GroupResource> updateGroup(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UpdateGroupResource groupResource) {

        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var updateGroupCommand = new UpdateGroupCommand(
                leader.get().getId(),
                groupResource.name(),
                groupResource.description(),
                groupResource.imgUrl()
        );

        var group = this.groupCommandService.handle(updateGroupCommand);

        if (group.isEmpty()) return ResponseEntity.notFound().build();

        var groupResourceUpdated = GroupResourceFromEntityAssembler.toResourceFromEntity(group.get());
        return ResponseEntity.ok(groupResourceUpdated);
    }

    @DeleteMapping
    @Operation(summary = "Delete a group", description = "Deletes a group")
    public ResponseEntity<Void> deleteGroup(@AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var deleteGroupCommand = new DeleteGroupCommand(leader.get().getId());

        this.groupCommandService.handle(deleteGroupCommand);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @Operation(summary = "Get a group by ID", description = "Gets a group by ID")
    public ResponseEntity<GroupResource> getGroupById(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var getGroupByLeaderIdQuery = new GetGroupByLeaderIdQuery(leader.get().getId());

        var group = this.groupQueryService.handle(getGroupByLeaderIdQuery);

        if (group.isEmpty()) return ResponseEntity.notFound().build();

        var groupResource = GroupResourceFromEntityAssembler.toResourceFromEntity(group.get());

        return ResponseEntity.ok(groupResource);
    }

    @DeleteMapping("/members/{memberId}")
    @Operation(summary = "Remove a member from the group", description = "Removes a member from the group")
    public ResponseEntity<Void> removeMemberFromGroup(@AuthenticationPrincipal UserDetails userDetails, @PathVariable Long memberId) {
        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        Long leaderId = leader.get().getId();

        externalTasksService.deleteTasksByMemberId(memberId);

        var removeMemberFromGroupCommand = new RemoveMemberFromGroupCommand(leaderId, memberId);

        this.groupCommandService.handle(removeMemberFromGroupCommand);

        return ResponseEntity.noContent().build();
    }

}
