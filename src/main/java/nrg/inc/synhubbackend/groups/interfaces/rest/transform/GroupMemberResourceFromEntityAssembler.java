package nrg.inc.synhubbackend.groups.interfaces.rest.transform;

import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupMemberResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;

public class GroupMemberResourceFromEntityAssembler {
    public static GroupMemberResource toResourceFromEntity(Member entity) {
        return new GroupMemberResource(
                entity.getId(),
                entity.getUser().getUsername(),
                entity.getUser().getName(),
                entity.getUser().getSurname(),
                entity.getUser().getImgUrl()
        );
    }
}
