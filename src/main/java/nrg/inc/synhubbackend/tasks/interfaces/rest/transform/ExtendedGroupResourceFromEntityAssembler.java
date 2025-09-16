package nrg.inc.synhubbackend.tasks.interfaces.rest.transform;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.ExtendedGroupResource;

public class ExtendedGroupResourceFromEntityAssembler {
    public static ExtendedGroupResource toResourceFromEntity(Group group) {
        return new ExtendedGroupResource(
                group.getId(),
                group.getName(),
                group.getImgUrl().imgUrl(),
                group.getDescription(),
                group.getCode().code(),
                group.getMembers().stream()
                        .map(member -> MemberResourceFromEntityAssembler.toResourceFromEntity(member))
                        .toList()
        );
    }
}
