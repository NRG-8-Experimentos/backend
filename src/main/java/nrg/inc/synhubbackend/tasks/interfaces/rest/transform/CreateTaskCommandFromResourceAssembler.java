package nrg.inc.synhubbackend.tasks.interfaces.rest.transform;

import nrg.inc.synhubbackend.tasks.domain.model.commands.CreateTaskCommand;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.CreateTaskResource;

public class CreateTaskCommandFromResourceAssembler {
    public static CreateTaskCommand toCommandFromResource(CreateTaskResource resource, Long memberId) {
        return new CreateTaskCommand(
                resource.title(),
                resource.description(),
                resource.dueDate(),
                memberId
        );
    }
}
