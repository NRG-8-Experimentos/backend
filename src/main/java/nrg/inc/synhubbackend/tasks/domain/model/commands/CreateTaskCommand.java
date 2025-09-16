package nrg.inc.synhubbackend.tasks.domain.model.commands;

import java.time.OffsetDateTime;

public record CreateTaskCommand(
        String title,
        String description,
        OffsetDateTime dueDate,
        Long memberId
) {
}
