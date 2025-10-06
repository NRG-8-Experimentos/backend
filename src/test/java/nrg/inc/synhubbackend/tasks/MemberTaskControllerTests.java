package nrg.inc.synhubbackend.tasks;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.commands.CreateTaskCommand;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTasksByMemberId;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;
import nrg.inc.synhubbackend.tasks.domain.services.MemberQueryService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskCommandService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
import nrg.inc.synhubbackend.tasks.interfaces.rest.MemberTaskController;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.CreateTaskResource;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.TaskResource;
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
public class MemberTaskControllerTests {

    @Mock
    private TaskCommandService taskCommandService;

    @Mock
    private TaskQueryService taskQueryService;

    @Mock
    private MemberQueryService memberQueryService;

    @InjectMocks
    private MemberTaskController memberTaskController;

    private Member testMember;
    private User testUser;
    private Group testGroup;
    private Leader testLeader;
    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private CreateTaskResource createTaskResource;

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

        // Initialize CreateTaskResource
        createTaskResource = new CreateTaskResource(
                "New Task",
                "New Task Description",
                OffsetDateTime.now().plusDays(5)
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

    // CREATE TASK TESTS

    @Test
    void createTask_WhenValidData_ReturnsCreatedTaskResource() {
        // Arrange
        Long memberId = 1L;
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        ResponseEntity<TaskResource> response = memberTaskController.createTask(memberId, createTaskResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        verify(taskCommandService, times(1)).handle(any(CreateTaskCommand.class));
    }

    @Test
    void createTask_WhenTaskCreationFails_ReturnsBadRequest() {
        // Arrange
        Long memberId = 1L;
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskResource> response = memberTaskController.createTask(memberId, createTaskResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskCommandService, times(1)).handle(any(CreateTaskCommand.class));
    }

    @Test
    void createTask_PassesCorrectMemberId() {
        // Arrange
        Long expectedMemberId = 1L;
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        memberTaskController.createTask(expectedMemberId, createTaskResource);

        // Assert
        verify(taskCommandService).handle(argThat((CreateTaskCommand command) ->
                command.memberId().equals(expectedMemberId)
        ));
    }

    @Test
    void createTask_PassesCorrectTaskDetails() {
        // Arrange
        Long memberId = 1L;
        when(taskCommandService.handle(any(CreateTaskCommand.class)))
                .thenReturn(Optional.of(testTask1));

        // Act
        memberTaskController.createTask(memberId, createTaskResource);

        // Assert
        verify(taskCommandService).handle(argThat((CreateTaskCommand command) ->
                command.title().equals("New Task") &&
                command.description().equals("New Task Description")
        ));
    }

    // GET ALL TASKS BY MEMBER ID TESTS

    @Test
    void getAllTasksByMemberId_WhenMemberHasTasks_ReturnsTasksList() {
        // Arrange
        Long memberId = 1L;
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act
        ResponseEntity<List<TaskResource>> response = memberTaskController.getAllTasksByMemberId(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getAllTasksByMemberId_WhenMemberHasNoTasks_ReturnsEmptyList() {
        // Arrange
        Long memberId = 1L;
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<TaskResource>> response = memberTaskController.getAllTasksByMemberId(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getAllTasksByMemberId_PassesCorrectMemberId() {
        // Arrange
        Long expectedMemberId = 1L;
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        memberTaskController.getAllTasksByMemberId(expectedMemberId);

        // Assert
        verify(taskQueryService).handle(argThat((GetAllTasksByMemberId query) ->
                query.memberId().equals(expectedMemberId)
        ));
    }

    // GET NEXT TASK BY MEMBER ID TESTS

    @Test
    void getLastNextByMemberId_WhenNextTaskExists_ReturnsTaskResource() {
        // Arrange
        Long memberId = 1L;
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act
        ResponseEntity<TaskResource> response = memberTaskController.getLastNextByMemberId(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should return testTask2 as it has the earliest due date among in-progress tasks
        assertEquals(101L, response.getBody().id());
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getLastNextByMemberId_WhenNoTasksExist_ReturnsNotFound() {
        // Arrange
        Long memberId = 1L;
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<TaskResource> response = memberTaskController.getLastNextByMemberId(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getLastNextByMemberId_WhenNoInProgressTasks_ThrowsException() {
        // Arrange
        Long memberId = 1L;
        testTask1.setStatus(TaskStatus.COMPLETED);
        testTask2.setStatus(TaskStatus.COMPLETED);

        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act & Assert
        assertThrows(java.util.NoSuchElementException.class, () -> {
            memberTaskController.getLastNextByMemberId(memberId);
        });

        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));

        // Reset for other tests
        testTask1.setStatus(TaskStatus.IN_PROGRESS);
        testTask2.setStatus(TaskStatus.IN_PROGRESS);
    }

    @Test
    void getLastNextByMemberId_PassesCorrectMemberId() {
        // Arrange
        Long expectedMemberId = 1L;
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act
        memberTaskController.getLastNextByMemberId(expectedMemberId);

        // Assert
        verify(taskQueryService).handle(argThat((GetAllTasksByMemberId query) ->
                query.memberId().equals(expectedMemberId)
        ));
    }

    @Test
    void getLastNextByMemberId_ReturnsTaskWithEarliestDueDate() {
        // Arrange
        Long memberId = 1L;
        // testTask2 has due date +1 day, testTask1 has +2 days
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2));

        // Act
        ResponseEntity<TaskResource> response = memberTaskController.getLastNextByMemberId(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should return testTask2 (ID 101) as it has the earliest due date
        assertEquals(101L, response.getBody().id());
    }
}
