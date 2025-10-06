package nrg.inc.synhubbackend.tasks;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.requests.domain.model.commands.DeleteAllRequestsByTaskIdCommand;
import nrg.inc.synhubbackend.requests.domain.services.RequestCommandService;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.commands.CreateTaskCommand;
import nrg.inc.synhubbackend.tasks.domain.model.commands.DeleteTaskCommand;
import nrg.inc.synhubbackend.tasks.domain.model.commands.UpdateTaskCommand;
import nrg.inc.synhubbackend.tasks.domain.model.commands.UpdateTaskStatusCommand;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTaskByStatusQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetTaskByIdQuery;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;
import nrg.inc.synhubbackend.tasks.domain.services.TaskCommandService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
import nrg.inc.synhubbackend.tasks.interfaces.rest.TaskController;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.TaskResource;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.UpdateTaskResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskControllerTests {

    @Mock
    private TaskQueryService taskQueryService;

    @Mock
    private TaskCommandService taskCommandService;

    @Mock
    private RequestCommandService requestCommandService;

    @InjectMocks
    private TaskController taskController;

    private Member testMember;
    private User testUser;
    private Group testGroup;
    private Leader testLeader;
    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private UpdateTaskResource updateTaskResource;
    private CreateTaskCommand createTaskCommand;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize User
        testUser = new User(
                "testmember",
                "TestName",
                "TestSurname",
                "http://img/member.png",
                "test@example.com",
                "password123"
        );
        setIdUsingReflection(testUser, 10L);

        // Initialize Member
        testMember = new Member();
        setIdUsingReflection(testMember, 1L);
        testMember.setUser(testUser);
        testUser.setMember(testMember);

        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 20L);

        // Initialize Group
        GroupCode groupCode = new GroupCode("ABC123456");
        testGroup = new Group(
                "Test Group",
                "Test Description",
                "http://img/group.png",
                testLeader,
                groupCode
        );
        setIdUsingReflection(testGroup, 30L);
        testMember.setGroup(testGroup);

        // Initialize Tasks
        testTask1 = new Task();
        setIdUsingReflection(testTask1, 100L);
        setTaskTitleUsingReflection(testTask1, "Task 1");
        setTaskDescriptionUsingReflection(testTask1, "Description 1");
        testTask1.setMember(testMember);
        testTask1.setGroup(testGroup);
        testTask1.setStatus(TaskStatus.IN_PROGRESS);
        setTaskDueDateUsingReflection(testTask1, OffsetDateTime.now().plusDays(2));
        setCreatedAtUsingReflection(testTask1, OffsetDateTime.now().minusHours(2));
        setUpdatedAtUsingReflection(testTask1, OffsetDateTime.now().minusHours(1));

        testTask2 = new Task();
        setIdUsingReflection(testTask2, 101L);
        setTaskTitleUsingReflection(testTask2, "Task 2");
        setTaskDescriptionUsingReflection(testTask2, "Description 2");
        testTask2.setMember(testMember);
        testTask2.setGroup(testGroup);
        testTask2.setStatus(TaskStatus.IN_PROGRESS);
        setTaskDueDateUsingReflection(testTask2, OffsetDateTime.now().plusDays(1));
        setCreatedAtUsingReflection(testTask2, OffsetDateTime.now().minusHours(3));
        setUpdatedAtUsingReflection(testTask2, OffsetDateTime.now().minusHours(2));

        testTask3 = new Task();
        setIdUsingReflection(testTask3, 102L);
        setTaskTitleUsingReflection(testTask3, "Task 3");
        setTaskDescriptionUsingReflection(testTask3, "Description 3");
        testTask3.setMember(testMember);
        testTask3.setGroup(testGroup);
        testTask3.setStatus(TaskStatus.COMPLETED);
        setTaskDueDateUsingReflection(testTask3, OffsetDateTime.now().plusDays(3));
        setCreatedAtUsingReflection(testTask3, OffsetDateTime.now().minusHours(4));
        setUpdatedAtUsingReflection(testTask3, OffsetDateTime.now().minusHours(3));

        // Initialize UpdateTaskResource
        updateTaskResource = new UpdateTaskResource(
                "Updated Task",
                "Updated Description",
                OffsetDateTime.now().plusDays(10),
                1L
        );

        // Initialize CreateTaskCommand
        createTaskCommand = new CreateTaskCommand(
                "New Task",
                "New Task Description",
                OffsetDateTime.now().plusDays(5),
                1L
        );
    }

    /**
     * Helper method to set ID using reflection
     */
    private void setIdUsingReflection(Object entity, Long id) throws Exception {
        Field idField = null;
        Class<?> clazz = entity.getClass();

        while (clazz != null && idField == null) {
            try {
                idField = clazz.getDeclaredField("id");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (idField != null) {
            idField.setAccessible(true);
            idField.set(entity, id);
        }
    }

    /**
     * Helper method to set createdAt using reflection
     */
    private void setCreatedAtUsingReflection(Object entity, OffsetDateTime createdAt) throws Exception {
        Field createdAtField = null;
        Class<?> clazz = entity.getClass();

        while (clazz != null && createdAtField == null) {
            try {
                createdAtField = clazz.getDeclaredField("createdAt");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (createdAtField != null) {
            createdAtField.setAccessible(true);
            createdAtField.set(entity, createdAt);
        }
    }

    /**
     * Helper method to set updatedAt using reflection
     */
    private void setUpdatedAtUsingReflection(Object entity, OffsetDateTime updatedAt) throws Exception {
        Field updatedAtField = null;
        Class<?> clazz = entity.getClass();

        while (clazz != null && updatedAtField == null) {
            try {
                updatedAtField = clazz.getDeclaredField("updatedAt");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (updatedAtField != null) {
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, updatedAt);
        }
    }

    /**
     * Helper method to set Task title using reflection
     */
    private void setTaskTitleUsingReflection(Task task, String title) throws Exception {
        Field titleField = Task.class.getDeclaredField("title");
        titleField.setAccessible(true);
        titleField.set(task, title);
    }

    /**
     * Helper method to set Task description using reflection
     */
    private void setTaskDescriptionUsingReflection(Task task, String description) throws Exception {
        Field descriptionField = Task.class.getDeclaredField("description");
        descriptionField.setAccessible(true);
        descriptionField.set(task, description);
    }

    /**
     * Helper method to set Task dueDate using reflection
     */
    private void setTaskDueDateUsingReflection(Task task, OffsetDateTime dueDate) throws Exception {
        Field dueDateField = Task.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(task, dueDate);
    }

    // GET TASK BY ID TESTS

    @Test
    void getTaskById_WhenTaskExists_ReturnsTaskResource() {
        // Arrange
        Long taskId = 100L;
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        ResponseEntity<TaskResource> response = taskController.getTaskById(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
    }

    @Test
    void getTaskById_WhenTaskDoesNotExist_ReturnsNotFound() {
        // Arrange
        Long taskId = 999L;
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskResource> response = taskController.getTaskById(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
    }

    @Test
    void getTaskById_PassesCorrectTaskId() {
        // Arrange
        Long expectedTaskId = 100L;
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        taskController.getTaskById(expectedTaskId);

        // Assert
        verify(taskQueryService).handle(argThat((GetTaskByIdQuery query) ->
                query.taskId().equals(expectedTaskId)
        ));
    }

    // GET ALL TASKS BY STATUS TESTS

    @Test
    void getAllTasksByStatus_WhenTasksExist_ReturnsTasksList() {
        // Arrange
        String status = "IN_PROGRESS";
        when(taskQueryService.handle(any(GetAllTaskByStatusQuery.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2));

        // Act
        ResponseEntity<List<TaskResource>> response = taskController.getAllTasksByStatus(status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(taskQueryService, times(1)).handle(any(GetAllTaskByStatusQuery.class));
    }

    @Test
    void getAllTasksByStatus_WhenNoTasksExist_ReturnsEmptyList() {
        // Arrange
        String status = "COMPLETED";
        when(taskQueryService.handle(any(GetAllTaskByStatusQuery.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<TaskResource>> response = taskController.getAllTasksByStatus(status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(taskQueryService, times(1)).handle(any(GetAllTaskByStatusQuery.class));
    }

    @Test
    void getAllTasksByStatus_PassesCorrectStatus() {
        // Arrange
        String expectedStatus = "IN_PROGRESS";
        when(taskQueryService.handle(any(GetAllTaskByStatusQuery.class)))
                .thenReturn(Collections.emptyList());

        // Act
        taskController.getAllTasksByStatus(expectedStatus);

        // Assert
        verify(taskQueryService).handle(argThat((GetAllTaskByStatusQuery query) ->
                query.taskStatus().equals(expectedStatus)
        ));
    }

    // UPDATE TASK STATUS TESTS

    @Test
    void updateTaskStatus_WhenValidData_ReturnsUpdatedTaskResource() {
        // Arrange
        Long taskId = 100L;
        String status = "COMPLETED";
        when(taskCommandService.handle(any(UpdateTaskStatusCommand.class)))
                .thenReturn(Optional.of(testTask3));

        // Act
        ResponseEntity<TaskResource> response = taskController.updateTaskStatus(taskId, status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(102L, response.getBody().id());
        verify(taskCommandService, times(1)).handle(any(UpdateTaskStatusCommand.class));
    }

    @Test
    void updateTaskStatus_WhenUpdateFails_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;
        String status = "COMPLETED";
        when(taskCommandService.handle(any(UpdateTaskStatusCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskResource> response = taskController.updateTaskStatus(taskId, status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskCommandService, times(1)).handle(any(UpdateTaskStatusCommand.class));
    }

    @Test
    void updateTaskStatus_PassesCorrectParameters() {
        // Arrange
        Long expectedTaskId = 100L;
        String expectedStatus = "COMPLETED";
        when(taskCommandService.handle(any(UpdateTaskStatusCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        taskController.updateTaskStatus(expectedTaskId, expectedStatus);

        // Assert
        verify(taskCommandService).handle(argThat((UpdateTaskStatusCommand command) ->
                command.taskId().equals(expectedTaskId) &&
                command.status().equals(expectedStatus)
        ));
    }

    // UPDATE TASK TESTS

    @Test
    void updateTask_WhenValidData_ReturnsUpdatedTaskResource() {
        // Arrange
        Long taskId = 100L;
        when(taskCommandService.handle(any(UpdateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        ResponseEntity<TaskResource> response = taskController.updateTask(taskId, updateTaskResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        verify(taskCommandService, times(1)).handle(any(UpdateTaskCommand.class));
    }

    @Test
    void updateTask_WhenUpdateFails_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;
        when(taskCommandService.handle(any(UpdateTaskCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskResource> response = taskController.updateTask(taskId, updateTaskResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskCommandService, times(1)).handle(any(UpdateTaskCommand.class));
    }

    @Test
    void updateTask_PassesCorrectTaskId() {
        // Arrange
        Long expectedTaskId = 100L;
        when(taskCommandService.handle(any(UpdateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        taskController.updateTask(expectedTaskId, updateTaskResource);

        // Assert
        verify(taskCommandService).handle(argThat((UpdateTaskCommand command) ->
                command.taskId().equals(expectedTaskId)
        ));
    }

    // DELETE TASK TESTS

    @Test
    void deleteTask_WhenCalled_ReturnsNoContent() {
        // Arrange
        Long taskId = 100L;
        doNothing().when(requestCommandService).handle(any(DeleteAllRequestsByTaskIdCommand.class));
        doNothing().when(taskCommandService).handle(any(DeleteTaskCommand.class));

        // Act
        ResponseEntity<Void> response = taskController.deleteTask(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(requestCommandService, times(1)).handle(any(DeleteAllRequestsByTaskIdCommand.class));
        verify(taskCommandService, times(1)).handle(any(DeleteTaskCommand.class));
    }

    @Test
    void deleteTask_PassesCorrectTaskId() {
        // Arrange
        Long expectedTaskId = 100L;
        doNothing().when(requestCommandService).handle(any(DeleteAllRequestsByTaskIdCommand.class));
        doNothing().when(taskCommandService).handle(any(DeleteTaskCommand.class));

        // Act
        taskController.deleteTask(expectedTaskId);

        // Assert
        verify(requestCommandService).handle(argThat((DeleteAllRequestsByTaskIdCommand command) ->
                command.taskId().equals(expectedTaskId)
        ));
        verify(taskCommandService).handle(argThat((DeleteTaskCommand command) ->
                command.taskId().equals(expectedTaskId)
        ));
    }

    @Test
    void deleteTask_DeletesRequestsBeforeTask() {
        // Arrange
        Long taskId = 100L;
        doNothing().when(requestCommandService).handle(any(DeleteAllRequestsByTaskIdCommand.class));
        doNothing().when(taskCommandService).handle(any(DeleteTaskCommand.class));

        // Act
        taskController.deleteTask(taskId);

        // Assert
        // Verify requests are deleted first, then task
        var inOrder = inOrder(requestCommandService, taskCommandService);
        inOrder.verify(requestCommandService).handle(any(DeleteAllRequestsByTaskIdCommand.class));
        inOrder.verify(taskCommandService).handle(any(DeleteTaskCommand.class));
    }

    // CREATE TASK TESTS

    @Test
    void createTask_WhenValidData_ReturnsCreatedTaskResource() {
        // Arrange
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        ResponseEntity<TaskResource> response = taskController.createTask(createTaskCommand);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        verify(taskCommandService, times(1)).handle(any(CreateTaskCommand.class));
    }

    @Test
    void createTask_WhenCreationFails_ReturnsBadRequest() {
        // Arrange
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskResource> response = taskController.createTask(createTaskCommand);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskCommandService, times(1)).handle(any(CreateTaskCommand.class));
    }

    @Test
    void createTask_PassesCorrectCommand() {
        // Arrange
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        taskController.createTask(createTaskCommand);

        // Assert
        verify(taskCommandService).handle(argThat((CreateTaskCommand command) ->
                command.title().equals("New Task") &&
                command.description().equals("New Task Description") &&
                command.memberId().equals(1L)
        ));
    }
}
