package nrg.inc.synhubbackend.tasks.domain.services;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.queries.*;

import java.util.List;
import java.util.Optional;

public interface TaskQueryService {
    /**
     * Retrieves all tasks.
     * @param query
     * @return
     */
    List<Task> handle(GetAllTasksQuery query);

    /**
     * Retrieves tasks assigned to a specific member.
     * @param query
     * @return
     */
    Optional<Task> handle(GetTaskByIdQuery query);

    /**
     * Retrieves tasks assigned to a specific member.
     * @param query
     * @return
     */
    List<Task> handle(GetAllTasksByMemberId query);

    /**
     * Retrieves tasks by their status.
     * @param query
     * @return
     */
    List<Task> handle(GetAllTaskByStatusQuery query);

    /**
     * Retrieves tasks by their group ID.
     * @param query
     * @return
     */
    List<Task> handle(GetAllTasksByGroupIdQuery query);
}
