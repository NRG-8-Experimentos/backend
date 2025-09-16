package nrg.inc.synhubbackend.groups.interfaces.rest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.LeaderResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.transform.LeaderResourceFromEntityAssembler;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/leader")
@Tag(name = "Leaders", description = "Leader management API")
public class LeaderController {

    private final LeaderQueryService leaderQueryService;

    public LeaderController(LeaderQueryService leaderQueryService) {
        this.leaderQueryService = leaderQueryService;
    }

    @GetMapping("/details")
    @Operation(summary = "Get leader details by authentication", description = "Fetches the details of the authenticated leader.")
    public ResponseEntity<LeaderResource> getLeaderByAuthentication(@AuthenticationPrincipal UserDetails userDetails) {

        String username = userDetails.getUsername();

        var getLeaderByUsernameQuery = new GetLeaderByUsernameQuery(username);

        var leader = this.leaderQueryService.handle(getLeaderByUsernameQuery);

        if (leader.isEmpty()) return ResponseEntity.notFound().build();

        var leaderResource = LeaderResourceFromEntityAssembler.toResourceFromEntity(leader.get());

        return ResponseEntity.ok(leaderResource);
    }

}
