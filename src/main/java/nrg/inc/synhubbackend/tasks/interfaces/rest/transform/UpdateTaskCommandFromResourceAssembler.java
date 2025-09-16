package nrg.inc.synhubbackend.tasks.interfaces.rest.transform;

import nrg.inc.synhubbackend.tasks.domain.model.commands.UpdateTaskCommand;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.UpdateTaskResource;

public class UpdateTaskCommandFromResourceAssembler {
    public static UpdateTaskCommand toCommandFromResource(UpdateTaskResource command, Long taskId) {
        return new UpdateTaskCommand(
                taskId,
                command.title(),
                command.description(),
                command.dueDate(),
                command.memberId()
        );

    }
}
