package nrg.inc.synhubbackend.groups.interfaces.rest.transform;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.InvitationResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;

public class InvitationResourceFromEntityAssembler {
    public static InvitationResource toResourceFromEntity(
            Invitation invitation,
            Member member
    ) {
        return new InvitationResource(
                invitation.getId(),
                InvitationMemberResourceFromEntityAssembler.toResourceFromEntity(member),
                GroupResourceFromEntityAssembler.toResourceFromEntity(invitation.getGroup())
        );
    }
}
