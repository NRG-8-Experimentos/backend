package nrg.inc.synhubbackend.tasks.application.internal.acl;

import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.commands.DeleteTasksByMemberId;
import nrg.inc.synhubbackend.tasks.domain.services.TaskCommandService;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import nrg.inc.synhubbackend.tasks.interfaces.acl.TasksContextFacade;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TaskContextFacadeImpl implements TasksContextFacade {

    private final TaskCommandService taskCommandService;
    private final TaskRepository taskRepository;

    public TaskContextFacadeImpl(TaskCommandService taskCommandService, TaskRepository taskRepository) {
        this.taskCommandService = taskCommandService;
        this.taskRepository = taskRepository;
    }

    @Override
    public Optional<Task> getTaskById(Long id) {
        var task = taskRepository.findById(id);
        return task;
    }

    @Override
    public void deleteTasksByMemberId(Long memberId) {
        var deleteTasksByMemberId = new DeleteTasksByMemberId(memberId);
        taskCommandService.handle(deleteTasksByMemberId);
    }
}
