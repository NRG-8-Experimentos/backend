package nrg.inc.synhubbackend.groups.interfaces.rest.transform;

import nrg.inc.synhubbackend.groups.interfaces.rest.resources.InvitationMemberResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;

public class InvitationMemberResourceFromEntityAssembler {
    public static InvitationMemberResource toResourceFromEntity(Member entity) {
        return new InvitationMemberResource(
                entity.getId(),
                entity.getUser().getUsername(),
                entity.getUser().getName(),
                entity.getUser().getSurname(),
                entity.getUser().getImgUrl()
        );
    }
}
