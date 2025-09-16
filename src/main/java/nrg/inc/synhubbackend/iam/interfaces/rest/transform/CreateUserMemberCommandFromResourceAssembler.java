package nrg.inc.synhubbackend.iam.interfaces.rest.transform;

import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserMemberCommand;

public class CreateUserMemberCommandFromResourceAssembler {
    public static CreateUserMemberCommand toCommandFromResource(Long userId) {
        return new CreateUserMemberCommand(userId);
    }
}
