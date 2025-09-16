package nrg.inc.synhubbackend.iam.interfaces.rest.transform;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.UserLeaderResource;

public class UserLeaderResourceFromEntityAssembler {
    public static UserLeaderResource toResourceFromEntity(Leader leader){
        return new UserLeaderResource(
                leader.getAverageSolutionTime().toString(),
                leader.getSolvedRequests()
        );
    }
}
