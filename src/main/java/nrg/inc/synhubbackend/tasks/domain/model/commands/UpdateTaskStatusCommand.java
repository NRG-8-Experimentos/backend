package nrg.inc.synhubbackend.tasks.domain.model.commands;

public record UpdateTaskStatusCommand(
        Long taskId,
        String status
) {
}
