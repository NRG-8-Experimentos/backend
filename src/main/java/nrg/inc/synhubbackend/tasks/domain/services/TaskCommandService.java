package nrg.inc.synhubbackend.tasks.domain.services;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.commands.*;

import java.util.Optional;

public interface TaskCommandService {
    Optional<Task> handle(CreateTaskCommand command);
    Optional<Task> handle(UpdateTaskCommand command);
    void handle(DeleteTaskCommand command);
    Optional<Task> handle(UpdateTaskStatusCommand command);
    void handle(DeleteTasksByMemberId command);
}
