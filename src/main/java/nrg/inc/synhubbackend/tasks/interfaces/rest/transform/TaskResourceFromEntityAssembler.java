package nrg.inc.synhubbackend.tasks.interfaces.rest.transform;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.TaskResource;

public class TaskResourceFromEntityAssembler {
    public static TaskResource toResourceFromEntity(Task entity){
        return new TaskResource(
                entity.getId(),
                entity.getTitle(),
                entity.getDescription(),
                entity.getDueDate().toString(),
                entity.getCreatedAt().toString(),
                entity.getUpdatedAt().toString(),
                entity.getStatus().toString(),
                TaskMemberResourceFromEntityAssembler.toResourceFromEntity(entity.getMember()),
                entity.getGroup().getId()
        );
    }
}
