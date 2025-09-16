package nrg.inc.synhubbackend.groups.interfaces.rest.transform;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.LeaderResource;

public class LeaderResourceFromEntityAssembler {
    public static LeaderResource toResourceFromEntity(Leader leader) {
        return new LeaderResource(
                leader.getUser().getUsername(),
                leader.getUser().getName(),
                leader.getUser().getSurname(),
                leader.getUser().getImgUrl(),
                leader.getUser().getEmail(),
                leader.getAverageSolutionTime().toString(),
                leader.getSolvedRequests()
        );
    }
}
