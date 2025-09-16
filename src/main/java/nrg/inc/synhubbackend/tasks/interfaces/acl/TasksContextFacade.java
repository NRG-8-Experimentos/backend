package nrg.inc.synhubbackend.tasks.interfaces.acl;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;

import java.util.Optional;

public interface TasksContextFacade {
    Optional<Task> getTaskById(Long taskId);
    void deleteTasksByMemberId(Long memberId);
}
