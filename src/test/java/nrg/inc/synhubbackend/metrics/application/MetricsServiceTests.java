package nrg.inc.synhubbackend.metrics.application;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import nrg.inc.synhubbackend.metrics.application.internal.queryservice.TaskMetricsQueryServiceImpl;
import nrg.inc.synhubbackend.metrics.domain.model.queries.*;
import nrg.inc.synhubbackend.metrics.interfaces.rest.resources.*;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MetricsServiceTests {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private GroupQueryService groupQueryService;

    @InjectMocks
    private TaskMetricsQueryServiceImpl taskMetricsQueryService;

    private Group testGroup;
    private Leader testLeader;
    private Member testMember1;
    private Member testMember2;
    private User testUser1;
    private User testUser2;
    private Task testTask1;
    private Task testTask2;
    private Task testTask3;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        // Initialize Group
        GroupCode groupCode = new GroupCode("ABC123456");
        testGroup = new Group(
                "Test Group",
                "Test Description",
                "http://img/group.png",
                testLeader,
                groupCode
        );
        setIdUsingReflection(testGroup, 10L);

        // Initialize Members
        testMember1 = new Member();
        setIdUsingReflection(testMember1, 20L);
        testMember1.setGroup(testGroup);

        testMember2 = new Member();
        setIdUsingReflection(testMember2, 21L);
        testMember2.setGroup(testGroup);

        // Initialize Users
        testUser1 = new User(
                "member1",
                "John",
                "Doe",
                "http://img/user1.png",
                "john@example.com",
                "password123"
        );
        setIdUsingReflection(testUser1, 100L);
        testUser1.setMember(testMember1);

        testUser2 = new User(
                "member2",
                "Jane",
                "Smith",
                "http://img/user2.png",
                "jane@example.com",
                "password123"
        );
        setIdUsingReflection(testUser2, 101L);
        testUser2.setMember(testMember2);

        // Initialize Tasks
        testTask1 = new Task();
        setIdUsingReflection(testTask1, 200L);
        setTaskFieldUsingReflection(testTask1, "title", "Task 1");
        setTaskFieldUsingReflection(testTask1, "description", "Description 1");
        setTaskFieldUsingReflection(testTask1, "dueDate", OffsetDateTime.now().plusDays(5));
        testTask1.setStatus(TaskStatus.IN_PROGRESS);
        testTask1.setMember(testMember1);
        testTask1.setGroup(testGroup);
        setTaskFieldUsingReflection(testTask1, "timesRearranged", 2);
        setTaskFieldUsingReflection(testTask1, "timePassed", 3600000L); // 1 hour
        setCreatedAtUsingReflection(testTask1, OffsetDateTime.now().minusHours(2));

        testTask2 = new Task();
        setIdUsingReflection(testTask2, 201L);
        setTaskFieldUsingReflection(testTask2, "title", "Task 2");
        setTaskFieldUsingReflection(testTask2, "description", "Description 2");
        setTaskFieldUsingReflection(testTask2, "dueDate", OffsetDateTime.now().plusDays(3));
        testTask2.setStatus(TaskStatus.DONE);
        testTask2.setMember(testMember1);
        testTask2.setGroup(testGroup);
        setTaskFieldUsingReflection(testTask2, "timesRearranged", 0);
        setTaskFieldUsingReflection(testTask2, "timePassed", 7200000L); // 2 hours
        setCreatedAtUsingReflection(testTask2, OffsetDateTime.now().minusHours(3));

        testTask3 = new Task();
        setIdUsingReflection(testTask3, 202L);
        setTaskFieldUsingReflection(testTask3, "title", "Task 3");
        setTaskFieldUsingReflection(testTask3, "description", "Description 3");
        setTaskFieldUsingReflection(testTask3, "dueDate", OffsetDateTime.now().plusDays(1));
        testTask3.setStatus(TaskStatus.DONE);
        testTask3.setMember(testMember2);
        testTask3.setGroup(testGroup);
        setTaskFieldUsingReflection(testTask3, "timesRearranged", 1);
        setTaskFieldUsingReflection(testTask3, "timePassed", 10800000L); // 3 hours
        setCreatedAtUsingReflection(testTask3, OffsetDateTime.now().minusHours(4));
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

    private void setTaskFieldUsingReflection(Task task, String fieldName, Object value) throws Exception {
        Field field = Task.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(task, value);
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

    // TESTS FOR GetTaskTimePassedQuery

    @Test
    void handleGetTaskTimePassed_WhenMemberHasTasks_ReturnsAverageTimePassed() {
        // Arrange
        GetTaskTimePassedQuery query = new GetTaskTimePassedQuery(20L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByMember_Id(20L)).thenReturn(tasks);

        // Act
        TaskTimePassedResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.memberId());
        assertEquals(5400000L, result.timePassed()); // Average of 3600000 and 7200000
        verify(taskRepository, times(1)).findByMember_Id(20L);
    }

    @Test
    void handleGetTaskTimePassed_WhenMemberHasNoTasks_ReturnsZero() {
        // Arrange
        GetTaskTimePassedQuery query = new GetTaskTimePassedQuery(999L);
        when(taskRepository.findByMember_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        TaskTimePassedResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(999L, result.memberId());
        assertEquals(0L, result.timePassed());
        verify(taskRepository, times(1)).findByMember_Id(999L);
    }

    // TESTS FOR GetAvgCompletionTimeQuery

    @Test
    void handleGetAvgCompletionTime_WhenLeaderHasGroupWithCompletedTasks_ReturnsAverage() {
        // Arrange
        GetAvgCompletionTimeQuery query = new GetAvgCompletionTimeQuery(1L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2, testTask3);
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class))).thenReturn(Optional.of(testGroup));
        when(taskRepository.findByGroup_Id(10L)).thenReturn(tasks);

        // Act
        AvgCompletionTimeResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("AVG_COMPLETION_TIME", result.type());
        assertTrue(result.value() > 0);
        assertEquals(2, result.details().get("completedTasks"));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskRepository, times(1)).findByGroup_Id(10L);
    }

    @Test
    void handleGetAvgCompletionTime_WhenLeaderHasNoGroup_ReturnsZero() {
        // Arrange
        GetAvgCompletionTimeQuery query = new GetAvgCompletionTimeQuery(999L);
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class))).thenReturn(Optional.empty());

        // Act
        AvgCompletionTimeResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("AVG_COMPLETION_TIME", result.type());
        assertEquals(0.0, result.value());
        assertEquals(0, result.details().get("completedTasks"));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskRepository, never()).findByGroup_Id(any());
    }

    // TESTS FOR GetRescheduledTasksQuery

    @Test
    void handleGetRescheduledTasks_WhenGroupHasRescheduledTasks_ReturnsRescheduledInfo() {
        // Arrange
        GetRescheduledTasksQuery query = new GetRescheduledTasksQuery(10L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2, testTask3);
        when(taskRepository.findByGroup_Id(10L)).thenReturn(tasks);

        // Act
        RescheduledTasksResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("RESCHEDULED_TASKS", result.type());
        assertEquals(3L, result.value()); // 2 + 0 + 1
        assertEquals(3, result.details().get("total"));
        assertEquals(3, result.details().get("rescheduled"));
        assertTrue(result.rescheduledMemberIds().contains(20L));
        assertTrue(result.rescheduledMemberIds().contains(21L));
        verify(taskRepository, times(1)).findByGroup_Id(10L);
    }

    @Test
    void handleGetRescheduledTasks_WhenGroupHasNoTasks_ReturnsZero() {
        // Arrange
        GetRescheduledTasksQuery query = new GetRescheduledTasksQuery(999L);
        when(taskRepository.findByGroup_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        RescheduledTasksResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("RESCHEDULED_TASKS", result.type());
        assertEquals(0L, result.value());
        assertEquals(0, result.details().get("total"));
        verify(taskRepository, times(1)).findByGroup_Id(999L);
    }

    // TESTS FOR GetTaskDistributionQuery

    @Test
    void handleGetTaskDistribution_WhenGroupHasTasks_ReturnsDistribution() {
        // Arrange
        GetTaskDistributionQuery query = new GetTaskDistributionQuery(10L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2, testTask3);
        List<User> users = Arrays.asList(testUser1, testUser2);
        when(taskRepository.findByGroup_Id(10L)).thenReturn(tasks);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        TaskDistributionResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_DISTRIBUTION", result.type());
        assertEquals(3, result.value());
        assertEquals(2, result.details().size());
        assertTrue(result.details().containsKey("20"));
        assertTrue(result.details().containsKey("21"));
        assertEquals(2, result.details().get("20").taskCount());
        assertEquals(1, result.details().get("21").taskCount());
        verify(taskRepository, times(1)).findByGroup_Id(10L);
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void handleGetTaskDistribution_WhenGroupHasNoTasks_ReturnsEmpty() {
        // Arrange
        GetTaskDistributionQuery query = new GetTaskDistributionQuery(999L);
        when(taskRepository.findByGroup_Id(999L)).thenReturn(new ArrayList<>());
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        TaskDistributionResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_DISTRIBUTION", result.type());
        assertEquals(0, result.value());
        assertTrue(result.details().isEmpty());
        verify(taskRepository, times(1)).findByGroup_Id(999L);
    }

    // TESTS FOR GetTaskOverviewQuery

    @Test
    void handleGetTaskOverview_WhenGroupHasTasks_ReturnsOverviewByStatus() {
        // Arrange
        GetTaskOverviewQuery query = new GetTaskOverviewQuery(10L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2, testTask3);
        when(taskRepository.findByGroup_Id(10L)).thenReturn(tasks);

        // Act
        TaskOverviewResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_OVERVIEW", result.type());
        assertEquals(3, result.value());
        assertEquals(1, result.details().get("IN_PROGRESS"));
        assertEquals(2, result.details().get("DONE"));
        verify(taskRepository, times(1)).findByGroup_Id(10L);
    }

    @Test
    void handleGetTaskOverview_WhenGroupHasNoTasks_ReturnsEmptyOverview() {
        // Arrange
        GetTaskOverviewQuery query = new GetTaskOverviewQuery(999L);
        when(taskRepository.findByGroup_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        TaskOverviewResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_OVERVIEW", result.type());
        assertEquals(0, result.value());
        assertTrue(result.details().isEmpty());
        verify(taskRepository, times(1)).findByGroup_Id(999L);
    }

    // TESTS FOR GetTaskOverviewForMemberQuery

    @Test
    void handleGetTaskOverviewForMember_WhenMemberHasTasks_ReturnsOverview() {
        // Arrange
        GetTaskOverviewForMemberQuery query = new GetTaskOverviewForMemberQuery(20L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByMember_Id(20L)).thenReturn(tasks);

        // Act
        TaskOverviewResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_OVERVIEW_MEMBER", result.type());
        assertEquals(2, result.value());
        assertEquals(1, result.details().get("IN_PROGRESS"));
        assertEquals(1, result.details().get("DONE"));
        verify(taskRepository, times(1)).findByMember_Id(20L);
    }

    @Test
    void handleGetTaskOverviewForMember_WhenMemberHasNoTasks_ReturnsEmptyOverview() {
        // Arrange
        GetTaskOverviewForMemberQuery query = new GetTaskOverviewForMemberQuery(999L);
        when(taskRepository.findByMember_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        TaskOverviewResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_OVERVIEW_MEMBER", result.type());
        assertEquals(0, result.value());
        assertTrue(result.details().isEmpty());
        verify(taskRepository, times(1)).findByMember_Id(999L);
    }

    // TESTS FOR GetTaskDistributionForMemberQuery

    @Test
    void handleGetTaskDistributionForMember_WhenMemberHasTasks_ReturnsDistribution() {
        // Arrange
        GetTaskDistributionForMemberQuery query = new GetTaskDistributionForMemberQuery(20L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        List<User> users = List.of(testUser1);
        when(taskRepository.findByMember_Id(20L)).thenReturn(tasks);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        TaskDistributionResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("TASK_DISTRIBUTION_MEMBER", result.type());
        assertEquals(2, result.value());
        assertTrue(result.details().containsKey("20"));
        assertEquals(2, result.details().get("20").taskCount());
        assertEquals("John Doe", result.details().get("20").memberName());
        verify(taskRepository, times(1)).findByMember_Id(20L);
        verify(userRepository, times(1)).findAll();
    }

    // TESTS FOR GetRescheduledTasksForMemberQuery

    @Test
    void handleGetRescheduledTasksForMember_WhenMemberHasRescheduledTasks_ReturnsInfo() {
        // Arrange
        GetRescheduledTasksForMemberQuery query = new GetRescheduledTasksForMemberQuery(20L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByMember_Id(20L)).thenReturn(tasks);

        // Act
        RescheduledTasksResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("RESCHEDULED_TASKS_MEMBER", result.type());
        assertEquals(2L, result.value()); // 2 + 0
        assertEquals(2, result.details().get("total"));
        assertEquals(2, result.details().get("rescheduled"));
        assertTrue(result.rescheduledMemberIds().contains(20L));
        verify(taskRepository, times(1)).findByMember_Id(20L);
    }

    @Test
    void handleGetRescheduledTasksForMember_WhenMemberHasNoRescheduledTasks_ReturnsEmptyList() {
        // Arrange
        GetRescheduledTasksForMemberQuery query = new GetRescheduledTasksForMemberQuery(999L);
        when(taskRepository.findByMember_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        RescheduledTasksResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("RESCHEDULED_TASKS_MEMBER", result.type());
        assertEquals(0L, result.value());
        assertTrue(result.rescheduledMemberIds().isEmpty());
        verify(taskRepository, times(1)).findByMember_Id(999L);
    }

    // TESTS FOR GetAvgCompletionTimeForMemberQuery

    @Test
    void handleGetAvgCompletionTimeForMember_WhenMemberHasCompletedTasks_ReturnsAverage() {
        // Arrange
        GetAvgCompletionTimeForMemberQuery query = new GetAvgCompletionTimeForMemberQuery(20L);
        List<Task> tasks = Arrays.asList(testTask1, testTask2);
        when(taskRepository.findByMember_Id(20L)).thenReturn(tasks);

        // Act
        AvgCompletionTimeResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("AVG_COMPLETION_TIME_MEMBER", result.type());
        assertTrue(result.value() > 0);
        assertEquals(1, result.details().get("completedTasks"));
        verify(taskRepository, times(1)).findByMember_Id(20L);
    }

    @Test
    void handleGetAvgCompletionTimeForMember_WhenMemberHasNoCompletedTasks_ReturnsZero() {
        // Arrange
        GetAvgCompletionTimeForMemberQuery query = new GetAvgCompletionTimeForMemberQuery(999L);
        when(taskRepository.findByMember_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        AvgCompletionTimeResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals("AVG_COMPLETION_TIME_MEMBER", result.type());
        assertEquals(0.0, result.value());
        assertEquals(0, result.details().get("completedTasks"));
        verify(taskRepository, times(1)).findByMember_Id(999L);
    }

    // TESTS FOR GetTaskTimePassedByIdQuery

    @Test
    void handleGetTaskTimePassedById_WhenTaskIsCompleted_ReturnsTimePassed() {
        // Arrange
        GetTaskTimePassedByIdQuery query = new GetTaskTimePassedByIdQuery(201L);
        when(taskRepository.findById(201L)).thenReturn(Optional.of(testTask2));

        // Act
        TaskTimePassedResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(20L, result.memberId());
        assertEquals(7200000L, result.timePassed());
        verify(taskRepository, times(1)).findById(201L);
    }

    @Test
    void handleGetTaskTimePassedById_WhenTaskNotFound_ThrowsException() {
        // Arrange
        GetTaskTimePassedByIdQuery query = new GetTaskTimePassedByIdQuery(999L);
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskMetricsQueryService.handle(query)
        );
        assertEquals("Task not found with ID: 999", exception.getMessage());
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    void handleGetTaskTimePassedById_WhenTaskNotCompleted_ThrowsException() {
        // Arrange
        GetTaskTimePassedByIdQuery query = new GetTaskTimePassedByIdQuery(200L);
        when(taskRepository.findById(200L)).thenReturn(Optional.of(testTask1));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskMetricsQueryService.handle(query)
        );
        assertEquals("Task is not completed or done.", exception.getMessage());
        verify(taskRepository, times(1)).findById(200L);
    }

    // TESTS FOR GetTaskDurationByIdQuery

    @Test
    void handleGetTaskDurationById_WhenTaskIsInProgress_ReturnsDuration() {
        // Arrange
        GetTaskDurationByIdQuery query = new GetTaskDurationByIdQuery(200L);
        when(taskRepository.findById(200L)).thenReturn(Optional.of(testTask1));

        // Act
        TaskDurationResource result = taskMetricsQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(200L, result.taskId());
        assertTrue(result.durationInHours() >= 0);
        verify(taskRepository, times(1)).findById(200L);
    }

    @Test
    void handleGetTaskDurationById_WhenTaskNotFound_ThrowsException() {
        // Arrange
        GetTaskDurationByIdQuery query = new GetTaskDurationByIdQuery(999L);
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> taskMetricsQueryService.handle(query)
        );
        assertEquals("Task not found with ID: 999", exception.getMessage());
        verify(taskRepository, times(1)).findById(999L);
    }

    @Test
    void handleGetTaskDurationById_WhenTaskNotInProgress_ThrowsException() {
        // Arrange
        GetTaskDurationByIdQuery query = new GetTaskDurationByIdQuery(201L);
        when(taskRepository.findById(201L)).thenReturn(Optional.of(testTask2));

        // Act & Assert
        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> taskMetricsQueryService.handle(query)
        );
        assertEquals("Task is not IN_PROGRESS.", exception.getMessage());
        verify(taskRepository, times(1)).findById(201L);
    }
}
