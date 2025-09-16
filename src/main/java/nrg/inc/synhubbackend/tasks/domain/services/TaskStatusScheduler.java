package nrg.inc.synhubbackend.tasks.domain.services;

import nrg.inc.synhubbackend.requests.domain.model.commands.CreateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.services.RequestCommandService;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class TaskStatusScheduler {
    private final TaskRepository taskRepository;
    private final RequestCommandService requestCommandService;

    public TaskStatusScheduler(TaskRepository taskRepository, RequestCommandService requestCommandService) {
        this.taskRepository = taskRepository;
        this.requestCommandService = requestCommandService;
    }

    @Scheduled(fixedRate = 30000) // Ejecuta cada 60 segundos
    @Transactional
    public void updateExpiredTasks() {
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC);
        taskRepository.findAllByStatusAndDueDateBefore(TaskStatus.IN_PROGRESS, now)
                .forEach(task -> {
                    // Console log task id
                    System.out.println("Updating task with ID: " + task.getId());
                    task.setStatus(TaskStatus.EXPIRED);
                    var createRequestCommand = new CreateRequestCommand(
                            "La tarea venció automáticamente.",
                            "EXPIRED",
                            task.getId()
                    );

                    requestCommandService.handle(createRequestCommand);

                    taskRepository.save(task);
                });
    }
}
