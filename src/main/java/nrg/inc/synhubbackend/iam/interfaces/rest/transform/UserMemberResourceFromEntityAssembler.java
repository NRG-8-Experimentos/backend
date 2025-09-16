package nrg.inc.synhubbackend.iam.interfaces.rest.transform;

import nrg.inc.synhubbackend.iam.interfaces.rest.resources.UserMemberResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;

public class UserMemberResourceFromEntityAssembler {
    public static UserMemberResource toResourceFromEntity(Member member) {
        return new UserMemberResource(
                member.getGroup() != null ? member.getGroup().getId() : 0
        );
    }
}
