package nrg.inc.synhubbackend.iam.interfaces.rest.transform;

import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserLeaderCommand;

public class CreateUserLeaderCommandFromResourceAssembler {
    public static CreateUserLeaderCommand toCommandFromResource(Long userId) {
        return new CreateUserLeaderCommand(userId);
    }
}
