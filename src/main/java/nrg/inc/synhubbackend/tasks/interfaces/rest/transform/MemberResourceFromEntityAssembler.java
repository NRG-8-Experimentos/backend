package nrg.inc.synhubbackend.tasks.interfaces.rest.transform;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.MemberResource;

public class MemberResourceFromEntityAssembler {
    public static MemberResource toResourceFromEntity(Member entity) {
        return new MemberResource(
                entity.getId(),
                entity.getUser().getUsername(),
                entity.getUser().getName(),
                entity.getUser().getSurname(),
                entity.getUser().getImgUrl(),
                entity.getUser().getEmail()
        );
    }
}
