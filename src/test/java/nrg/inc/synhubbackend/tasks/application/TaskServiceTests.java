package nrg.inc.synhubbackend.tasks.application;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.GroupRepository;
import nrg.inc.synhubbackend.tasks.application.internal.commandservices.TaskCommandServiceImpl;
import nrg.inc.synhubbackend.tasks.application.internal.queryservices.TaskQueryServiceImpl;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.commands.*;
import nrg.inc.synhubbackend.tasks.domain.model.queries.*;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TaskServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @InjectMocks
    private TaskCommandServiceImpl taskCommandService;

    @InjectMocks
    private TaskQueryServiceImpl taskQueryService;

    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private Member testMember1;
    private Member testMember2;
    private Group testGroup;
    private OffsetDateTime futureDate;
    private OffsetDateTime pastDate;

    @BeforeEach
    void setUp() throws Exception {
        futureDate = OffsetDateTime.now(ZoneOffset.UTC).plusDays(7);
        pastDate = OffsetDateTime.now(ZoneOffset.UTC).minusDays(1);

        // Initialize Group
        testGroup = new Group();
        setIdUsingReflection(testGroup, 100L);
        testGroup.setMemberCount(2);

        // Initialize Members
        testMember1 = new Member();
        setIdUsingReflection(testMember1, 1L);
        testMember1.setGroup(testGroup);
        testMember1.setTasks(new ArrayList<>());

        testMember2 = new Member();
        setIdUsingReflection(testMember2, 2L);
        testMember2.setGroup(testGroup);
        testMember2.setTasks(new ArrayList<>());

        // Initialize Tasks
        testTask1 = new Task(new CreateTaskCommand(
                "Task 1",
                "Description 1",
                futureDate,
                1L
        ));
        setIdUsingReflection(testTask1, 1L);
        setCreatedAtUsingReflection(testTask1, OffsetDateTime.now(ZoneOffset.UTC).minusDays(2));
        setUpdatedAtUsingReflection(testTask1, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        testTask1.setMember(testMember1);
        testTask1.setGroup(testGroup);
        testTask1.setStatus(TaskStatus.IN_PROGRESS);

        testTask2 = new Task(new CreateTaskCommand(
                "Task 2",
                "Description 2",
                futureDate,
                1L
        ));
        setIdUsingReflection(testTask2, 2L);
        setCreatedAtUsingReflection(testTask2, OffsetDateTime.now(ZoneOffset.UTC).minusDays(3));
        setUpdatedAtUsingReflection(testTask2, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        testTask2.setMember(testMember1);
        testTask2.setGroup(testGroup);
        testTask2.setStatus(TaskStatus.COMPLETED);

        testTask3 = new Task(new CreateTaskCommand(
                "Task 3",
                "Description 3",
                futureDate,
                2L
        ));
        setIdUsingReflection(testTask3, 3L);
        setCreatedAtUsingReflection(testTask3, OffsetDateTime.now(ZoneOffset.UTC).minusDays(1));
        setUpdatedAtUsingReflection(testTask3, OffsetDateTime.now(ZoneOffset.UTC).minusHours(12));
        testTask3.setMember(testMember2);
        testTask3.setGroup(testGroup);
        testTask3.setStatus(TaskStatus.ON_HOLD);
    }

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

    // ============================================================
    // TASK COMMAND SERVICE TESTS
    // ============================================================

    // TESTS FOR CreateTaskCommand

    @Test
    void handleCreateTask_WhenValidCommand_ReturnsTask() {
        // Arrange
        CreateTaskCommand command = new CreateTaskCommand(
                "New Task",
                "New Description",
                futureDate,
                1L
        );
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task savedTask = invocation.getArgument(0);
            try {
                setIdUsingReflection(savedTask, 10L);
            } catch (Exception e) {
                fail("Failed to set task ID");
            }
            return savedTask;
        });

        // Act
        Optional<Task> result = taskCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getId());
        assertEquals(10L, result.get().getId());
        assertEquals("New Task", result.get().getTitle());
        assertEquals("New Description", result.get().getDescription());
        assertEquals(testMember1, result.get().getMember());
        assertEquals(testGroup, result.get().getGroup());
        verify(memberRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findById(100L);
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void handleCreateTask_WhenMemberNotFound_ThrowsException() {
        // Arrange
        CreateTaskCommand command = new CreateTaskCommand(
                "New Task",
                "New Description",
                futureDate,
                999L
        );
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Member with id 999 does not exist", exception.getMessage());
        verify(memberRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void handleCreateTask_WhenMemberHasNoGroup_ThrowsException() {
        // Arrange
        Member memberWithoutGroup = new Member();
        try {
            setIdUsingReflection(memberWithoutGroup, 5L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }
        memberWithoutGroup.setGroup(null);

        CreateTaskCommand command = new CreateTaskCommand(
                "New Task",
                "New Description",
                futureDate,
                5L
        );
        when(memberRepository.findById(5L)).thenReturn(Optional.of(memberWithoutGroup));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Member with id 5 does not belong to any group", exception.getMessage());
        verify(memberRepository, times(1)).findById(5L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void handleCreateTask_WhenGroupNotFound_ThrowsException() {
        // Arrange
        Member memberWithInvalidGroup = new Member();
        try {
            setIdUsingReflection(memberWithInvalidGroup, 6L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }
        Group invalidGroup = new Group();
        try {
            setIdUsingReflection(invalidGroup, 999L);
        } catch (Exception e) {
            fail("Failed to set group ID");
        }
        memberWithInvalidGroup.setGroup(invalidGroup);

        CreateTaskCommand command = new CreateTaskCommand(
                "New Task",
                "New Description",
                futureDate,
                6L
        );
        when(memberRepository.findById(6L)).thenReturn(Optional.of(memberWithInvalidGroup));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Group with id 999 does not exist", exception.getMessage());
        verify(memberRepository, times(1)).findById(6L);
        verify(groupRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    // TESTS FOR UpdateTaskCommand

    @Test
    void handleUpdateTask_WhenValidCommand_ReturnsUpdatedTask() {
        // Arrange
        UpdateTaskCommand command = new UpdateTaskCommand(
                1L,
                "Updated Title",
                "Updated Description",
                futureDate,
                1L
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Task> result = taskCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Title", result.get().getTitle());
        assertEquals("Updated Description", result.get().getDescription());
        verify(taskRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void handleUpdateTask_WhenTaskNotFound_ThrowsException() {
        // Arrange
        UpdateTaskCommand command = new UpdateTaskCommand(
                999L,
                "Updated Title",
                "Updated Description",
                futureDate,
                1L
        );
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Task with id 999 does not exist", exception.getMessage());
        verify(taskRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void handleUpdateTask_WhenMemberNotFound_ThrowsException() {
        // Arrange
        UpdateTaskCommand command = new UpdateTaskCommand(
                1L,
                "Updated Title",
                "Updated Description",
                futureDate,
                999L
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Member with id 999 does not exist", exception.getMessage());
        verify(taskRepository, times(1)).findById(1L);
        verify(memberRepository, times(1)).findById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void handleUpdateTask_WhenChangingMember_UpdatesBothMembers() {
        // Arrange
        UpdateTaskCommand command = new UpdateTaskCommand(
                1L,
                "Updated Title",
                "Updated Description",
                futureDate,
                2L
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(memberRepository.findById(2L)).thenReturn(Optional.of(testMember2));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Task> result = taskCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMember2, result.get().getMember());
        verify(memberRepository, times(2)).save(any(Member.class)); // Both old and new member
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void handleUpdateTask_WhenNewMemberHasNoGroup_ThrowsException() {
        // Arrange
        Member memberWithoutGroup = new Member();
        try {
            setIdUsingReflection(memberWithoutGroup, 7L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }
        memberWithoutGroup.setGroup(null);

        UpdateTaskCommand command = new UpdateTaskCommand(
                1L,
                "Updated Title",
                "Updated Description",
                futureDate,
                7L
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(memberRepository.findById(7L)).thenReturn(Optional.of(memberWithoutGroup));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Member with id 7 does not belong to any group", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void handleUpdateTask_WhenDueDateInPast_SetsStatusToExpired() {
        // Arrange
        UpdateTaskCommand command = new UpdateTaskCommand(
                1L,
                "Updated Title",
                "Updated Description",
                pastDate,
                1L
        );
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Task> result = taskCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TaskStatus.EXPIRED, result.get().getStatus());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // TESTS FOR UpdateTaskStatusCommand

    @Test
    void handleUpdateTaskStatus_WhenValidCommand_ReturnsUpdatedTask() {
        // Arrange
        UpdateTaskStatusCommand command = new UpdateTaskStatusCommand(1L, "COMPLETED");
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Task> result = taskCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TaskStatus.COMPLETED, result.get().getStatus());
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).findById(1L);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void handleUpdateTaskStatus_WhenTaskNotFound_ThrowsException() {
        // Arrange
        UpdateTaskStatusCommand command = new UpdateTaskStatusCommand(999L, "COMPLETED");
        when(taskRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Task with id 999 does not exist", exception.getMessage());
        verify(taskRepository, times(1)).existsById(999L);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void handleUpdateTaskStatus_WhenChangingFromInProgressToCompleted_UpdatesTimePassed() {
        // Arrange
        UpdateTaskStatusCommand command = new UpdateTaskStatusCommand(1L, "COMPLETED");
        testTask1.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Task> result = taskCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(TaskStatus.COMPLETED, result.get().getStatus());
        assertTrue(result.get().getTimePassed() > 0);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    // TESTS FOR DeleteTaskCommand

    @Test
    void handleDeleteTask_WhenValidCommand_DeletesTask() {
        // Arrange
        DeleteTaskCommand command = new DeleteTaskCommand(1L);
        testMember1.getTasks().add(testTask1);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(taskRepository).deleteById(1L);

        // Act
        taskCommandService.handle(command);

        // Assert
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(2)).findById(1L);
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(taskRepository, times(1)).deleteById(1L);
    }

    @Test
    void handleDeleteTask_WhenTaskNotFound_ThrowsException() {
        // Arrange
        DeleteTaskCommand command = new DeleteTaskCommand(999L);
        when(taskRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Task with id 999 does not exist", exception.getMessage());
        verify(taskRepository, times(1)).existsById(999L);
        verify(taskRepository, never()).deleteById(any());
    }

    @Test
    void handleDeleteTask_WhenTaskHasNoMember_DeletesTask() {
        // Arrange
        DeleteTaskCommand command = new DeleteTaskCommand(1L);
        testTask1.setMember(null);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        doNothing().when(taskRepository).deleteById(1L);

        // Act
        taskCommandService.handle(command);

        // Assert
        verify(taskRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).deleteById(1L);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void handleDeleteTask_WhenDeleteFails_ThrowsException() {
        // Arrange
        DeleteTaskCommand command = new DeleteTaskCommand(1L);
        when(taskRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));
        doThrow(new RuntimeException("Database error")).when(taskRepository).deleteById(1L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Error deleting task"));
        verify(taskRepository, times(1)).deleteById(1L);
    }

    // TESTS FOR DeleteTasksByMemberId

    @Test
    void handleDeleteTasksByMemberId_WhenValidCommand_DeletesAllTasks() {
        // Arrange
        DeleteTasksByMemberId command = new DeleteTasksByMemberId(1L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        testMember1.getTasks().addAll(tasks);
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByMember_Id(1L)).thenReturn(tasks);
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(taskRepository).delete(any(Task.class));

        // Act
        taskCommandService.handle(command);

        // Assert
        verify(memberRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).findByMember_Id(1L);
        verify(memberRepository, times(2)).save(any(Member.class)); // Once per task
        verify(taskRepository, times(2)).delete(any(Task.class)); // Once per task
    }

    @Test
    void handleDeleteTasksByMemberId_WhenMemberNotFound_ThrowsException() {
        // Arrange
        DeleteTasksByMemberId command = new DeleteTasksByMemberId(999L);
        when(memberRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertEquals("Member with id 999 does not exist", exception.getMessage());
        verify(memberRepository, times(1)).existsById(999L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void handleDeleteTasksByMemberId_WhenNoTasksExist_DoesNothing() {
        // Arrange
        DeleteTasksByMemberId command = new DeleteTasksByMemberId(1L);
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByMember_Id(1L)).thenReturn(new ArrayList<>());

        // Act
        taskCommandService.handle(command);

        // Assert
        verify(memberRepository, times(1)).existsById(1L);
        verify(taskRepository, times(1)).findByMember_Id(1L);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void handleDeleteTasksByMemberId_WhenDeleteFails_ThrowsException() {
        // Arrange
        DeleteTasksByMemberId command = new DeleteTasksByMemberId(1L);
        List<Task> tasks = Collections.singletonList(testTask1);
        when(memberRepository.existsById(1L)).thenReturn(true);
        when(taskRepository.findByMember_Id(1L)).thenReturn(tasks);
        doThrow(new RuntimeException("Database error")).when(taskRepository).delete(any(Task.class));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Error deleting tasks for member"));
        verify(taskRepository, times(1)).delete(any(Task.class));
    }

    // ============================================================
    // TASK QUERY SERVICE TESTS
    // ============================================================

    // TESTS FOR GetAllTasksQuery

    @Test
    void handleGetAllTasks_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        GetAllTasksQuery query = new GetAllTasksQuery();
        List<Task> expectedTasks = Arrays.asList(testTask1, testTask2, testTask3);
        when(taskRepository.findAll()).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(testTask1));
        assertTrue(result.contains(testTask2));
        assertTrue(result.contains(testTask3));
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    void handleGetAllTasks_WhenNoTasksExist_ReturnsEmptyList() {
        // Arrange
        GetAllTasksQuery query = new GetAllTasksQuery();
        when(taskRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository, times(1)).findAll();
    }

    // TESTS FOR GetTaskByIdQuery

    @Test
    void handleGetTaskById_WhenTaskExists_ReturnsTask() {
        // Arrange
        GetTaskByIdQuery query = new GetTaskByIdQuery(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask1));

        // Act
        Optional<Task> result = taskQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testTask1.getId(), result.get().getId());
        assertEquals("Task 1", result.get().getTitle());
        assertEquals(TaskStatus.IN_PROGRESS, result.get().getStatus());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    void handleGetTaskById_WhenTaskDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetTaskByIdQuery query = new GetTaskByIdQuery(999L);
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findById(999L);
    }

    // TESTS FOR GetAllTasksByMemberId

    @Test
    void handleGetAllTasksByMemberId_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        GetAllTasksByMemberId query = new GetAllTasksByMemberId(1L);
        List<Task> expectedTasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByMember_Id(1L)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testTask1));
        assertTrue(result.contains(testTask2));
        assertFalse(result.contains(testTask3)); // testTask3 belongs to member 2
        verify(taskRepository, times(1)).findByMember_Id(1L);
    }

    @Test
    void handleGetAllTasksByMemberId_WhenNoTasksExist_ReturnsEmptyList() {
        // Arrange
        GetAllTasksByMemberId query = new GetAllTasksByMemberId(999L);
        when(taskRepository.findByMember_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository, times(1)).findByMember_Id(999L);
    }

    // TESTS FOR GetAllTaskByStatusQuery

    @Test
    void handleGetAllTaskByStatus_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        GetAllTaskByStatusQuery query = new GetAllTaskByStatusQuery("IN_PROGRESS");
        List<Task> expectedTasks = Collections.singletonList(testTask1);
        when(taskRepository.findByStatus(TaskStatus.IN_PROGRESS)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testTask1));
        assertEquals(TaskStatus.IN_PROGRESS, result.get(0).getStatus());
        verify(taskRepository, times(1)).findByStatus(TaskStatus.IN_PROGRESS);
    }

    @Test
    void handleGetAllTaskByStatus_WhenNoTasksWithStatus_ReturnsEmptyList() {
        // Arrange
        GetAllTaskByStatusQuery query = new GetAllTaskByStatusQuery("DONE");
        when(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(new ArrayList<>());

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository, times(1)).findByStatus(TaskStatus.DONE);
    }

    @Test
    void handleGetAllTaskByStatus_WhenMultipleTasksWithStatus_ReturnsAllTasks() {
        // Arrange
        GetAllTaskByStatusQuery query = new GetAllTaskByStatusQuery("COMPLETED");
        testTask2.setStatus(TaskStatus.COMPLETED);
        testTask3.setStatus(TaskStatus.COMPLETED);
        List<Task> expectedTasks = Arrays.asList(testTask2, testTask3);
        when(taskRepository.findByStatus(TaskStatus.COMPLETED)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testTask2));
        assertTrue(result.contains(testTask3));
        verify(taskRepository, times(1)).findByStatus(TaskStatus.COMPLETED);
    }

    // TESTS FOR GetAllTasksByGroupIdQuery

    @Test
    void handleGetAllTasksByGroupId_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        GetAllTasksByGroupIdQuery query = new GetAllTasksByGroupIdQuery(100L);
        List<Task> expectedTasks = Arrays.asList(testTask1, testTask2, testTask3);
        when(taskRepository.findByGroup_Id(100L)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(testTask1));
        assertTrue(result.contains(testTask2));
        assertTrue(result.contains(testTask3));
        verify(taskRepository, times(1)).findByGroup_Id(100L);
    }

    @Test
    void handleGetAllTasksByGroupId_WhenNoTasksExist_ReturnsEmptyList() {
        // Arrange
        GetAllTasksByGroupIdQuery query = new GetAllTasksByGroupIdQuery(999L);
        when(taskRepository.findByGroup_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(taskRepository, times(1)).findByGroup_Id(999L);
    }

    @Test
    void handleGetAllTasksByGroupId_WhenGroupHasOneTask_ReturnsListWithOneTask() {
        // Arrange
        GetAllTasksByGroupIdQuery query = new GetAllTasksByGroupIdQuery(100L);
        List<Task> expectedTasks = Collections.singletonList(testTask1);
        when(taskRepository.findByGroup_Id(100L)).thenReturn(expectedTasks);

        // Act
        List<Task> result = taskQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testTask1.getId(), result.get(0).getId());
        verify(taskRepository, times(1)).findByGroup_Id(100L);
    }
}
