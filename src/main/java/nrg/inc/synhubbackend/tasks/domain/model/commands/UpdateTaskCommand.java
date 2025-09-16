package nrg.inc.synhubbackend.tasks.domain.model.commands;

import java.time.OffsetDateTime;

public record UpdateTaskCommand(
        Long taskId,
        String title,
        String description,
        OffsetDateTime dueDate,
        Long memberId
) {
}
