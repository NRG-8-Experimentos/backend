package nrg.inc.synhubbackend.tasks.interfaces.rest.transform;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.TaskMemberResource;

public class TaskMemberResourceFromEntityAssembler {

    public static TaskMemberResource toResourceFromEntity(Member member) {
        return new TaskMemberResource(
                member.getId(),
                member.getUser().getName(),
                member.getUser().getSurname(),
                member.getUser().getImgUrl()
        );
    }
}
