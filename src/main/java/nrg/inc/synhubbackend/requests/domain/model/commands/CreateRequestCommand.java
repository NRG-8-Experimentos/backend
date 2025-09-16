package nrg.inc.synhubbackend.requests.domain.model.commands;

public record CreateRequestCommand(
        String description,
        String requestType,
        Long taskId
) {
}
