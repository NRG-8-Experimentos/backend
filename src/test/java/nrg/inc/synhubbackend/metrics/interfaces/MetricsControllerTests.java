package nrg.inc.synhubbackend.metrics.interfaces;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.metrics.domain.model.queries.*;
import nrg.inc.synhubbackend.metrics.domain.model.services.TaskMetricsQueryService;
import nrg.inc.synhubbackend.metrics.interfaces.rest.MetricsController;
import nrg.inc.synhubbackend.metrics.interfaces.rest.resources.*;
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
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MetricsControllerTests {

    @Mock
    private TaskMetricsQueryService taskMetricsQueryService;

    @Mock
    private LeaderQueryService leaderQueryService;

    @Mock
    private GroupQueryService groupQueryService;

    @InjectMocks
    private MetricsController metricsController;

    private Leader testLeader;
    private Group testGroup;
    private UserDetails testUserDetails;
    private nrg.inc.synhubbackend.iam.domain.model.aggregates.User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize common test data
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        // Set up mock User for Leader
        testUser = mock(nrg.inc.synhubbackend.iam.domain.model.aggregates.User.class);
        when(testUser.getUsername()).thenReturn("testleader");
        when(testUser.getName()).thenReturn("Test");
        when(testUser.getSurname()).thenReturn("Leader");
        when(testUser.getImgUrl()).thenReturn("http://test.img");
        testLeader.setUser(testUser);

        testGroup = new Group("Test Group", "Test Description", "http://test.img", testLeader, new GroupCode("ABC123456"));
        setIdUsingReflection(testGroup, 2L);

        testUserDetails = User.withUsername("testleader")
                .password("password")
                .roles("USER")
                .build();
    }

    /**
     * Helper method to set ID using reflection since AuditableAbstractAggregateRoot has private id field
     */
    private void setIdUsingReflection(Object entity, Long id) throws Exception {
        Field idField = entity.getClass().getSuperclass().getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(entity, id);
    }

    // GET AVERAGE TASK TIME PASSED TESTS

    @Test
    void getAverageTaskTimePassed_WhenMemberIdProvided_ReturnsTaskTimePassedResource() {
        // Arrange
        Long memberId = 10L;
        TaskTimePassedResource expectedResource = new TaskTimePassedResource(memberId, 3600000L);

        when(taskMetricsQueryService.handle(any(GetTaskTimePassedQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<TaskTimePassedResource> response = metricsController.getAverageTaskTimePassed(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(memberId, response.getBody().memberId());
        assertEquals(3600000L, response.getBody().timePassed());
        verify(taskMetricsQueryService, times(1)).handle(any(GetTaskTimePassedQuery.class));
    }

    // GET TASK OVERVIEW TESTS (GROUP)

    @Test
    void getTaskOverview_WhenLeaderAndGroupExist_ReturnsTaskOverviewResource() {
        // Arrange
        Map<String, Integer> details = new HashMap<>();
        details.put("completed", 5);
        details.put("in_progress", 3);
        details.put("pending", 2);
        TaskOverviewResource expectedResource = new TaskOverviewResource("tasks", 10, details);

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(taskMetricsQueryService.handle(any(GetTaskOverviewQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<TaskOverviewResource> response = metricsController.getTaskOverview(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("tasks", response.getBody().type());
        assertEquals(10, response.getBody().value());
        assertEquals(5, response.getBody().details().get("completed"));
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, times(1)).handle(any(GetTaskOverviewQuery.class));
    }

    @Test
    void getTaskOverview_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskOverviewResource> response = metricsController.getTaskOverview(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, never()).handle(any(GetTaskOverviewQuery.class));
    }

    @Test
    void getTaskOverview_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskOverviewResource> response = metricsController.getTaskOverview(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, never()).handle(any(GetTaskOverviewQuery.class));
    }

    // GET TASK DISTRIBUTION TESTS (GROUP)

    @Test
    void getTaskDistribution_WhenLeaderAndGroupExist_ReturnsTaskDistributionResource() {
        // Arrange
        Map<String, MemberTaskInfo> details = new HashMap<>();
        TaskDistributionResource expectedResource = new TaskDistributionResource("distribution", 10, details);

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(taskMetricsQueryService.handle(any(GetTaskDistributionQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<TaskDistributionResource> response = metricsController.getTaskDistribution(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("distribution", response.getBody().type());
        assertEquals(10, response.getBody().value());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, times(1)).handle(any(GetTaskDistributionQuery.class));
    }

    @Test
    void getTaskDistribution_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskDistributionResource> response = metricsController.getTaskDistribution(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, never()).handle(any(GetTaskDistributionQuery.class));
    }

    // GET RESCHEDULED TASKS TESTS (GROUP)

    @Test
    void getRescheduledTasks_WhenLeaderAndGroupExist_ReturnsRescheduledTasksResource() {
        // Arrange
        Map<String, Integer> details = new HashMap<>();
        details.put("rescheduled", 3);
        details.put("non_rescheduled", 7);
        List<Long> rescheduledMemberIds = Arrays.asList(10L, 20L);
        RescheduledTasksResource expectedResource = new RescheduledTasksResource("rescheduled", 3L, details, rescheduledMemberIds);

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(taskMetricsQueryService.handle(any(GetRescheduledTasksQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<RescheduledTasksResource> response = metricsController.getRescheduledTasks(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("rescheduled", response.getBody().type());
        assertEquals(3L, response.getBody().value());
        assertEquals(2, response.getBody().rescheduledMemberIds().size());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, times(1)).handle(any(GetRescheduledTasksQuery.class));
    }

    @Test
    void getRescheduledTasks_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<RescheduledTasksResource> response = metricsController.getRescheduledTasks(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskMetricsQueryService, never()).handle(any(GetRescheduledTasksQuery.class));
    }

    // GET AVERAGE COMPLETION TIME TESTS (GROUP)

    @Test
    void getAvgCompletionTime_WhenLeaderExists_ReturnsAvgCompletionTimeResource() {
        // Arrange
        Map<String, Integer> details = new HashMap<>();
        AvgCompletionTimeResource expectedResource = new AvgCompletionTimeResource("avg_completion_time", 3.5, details);

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(taskMetricsQueryService.handle(any(GetAvgCompletionTimeQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<AvgCompletionTimeResource> response = metricsController.getAvgCompletionTime(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("avg_completion_time", response.getBody().type());
        assertEquals(3.5, response.getBody().value());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(taskMetricsQueryService, times(1)).handle(any(GetAvgCompletionTimeQuery.class));
    }

    @Test
    void getAvgCompletionTime_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<AvgCompletionTimeResource> response = metricsController.getAvgCompletionTime(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(taskMetricsQueryService, never()).handle(any(GetAvgCompletionTimeQuery.class));
    }

    // GET TASK OVERVIEW FOR MEMBER TESTS

    @Test
    void getTaskOverviewForMember_WhenMemberIdProvided_ReturnsTaskOverviewResource() {
        // Arrange
        Long memberId = 10L;
        Map<String, Integer> details = new HashMap<>();
        details.put("completed", 3);
        details.put("in_progress", 1);
        TaskOverviewResource expectedResource = new TaskOverviewResource("tasks", 4, details);

        when(taskMetricsQueryService.handle(any(GetTaskOverviewForMemberQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<TaskOverviewResource> response = metricsController.getTaskOverviewForMember(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("tasks", response.getBody().type());
        assertEquals(4, response.getBody().value());
        verify(taskMetricsQueryService, times(1)).handle(any(GetTaskOverviewForMemberQuery.class));
    }

    // GET TASK DISTRIBUTION FOR MEMBER TESTS

    @Test
    void getTaskDistributionForMember_WhenMemberIdProvided_ReturnsTaskDistributionResource() {
        // Arrange
        Long memberId = 10L;
        Map<String, MemberTaskInfo> details = new HashMap<>();
        TaskDistributionResource expectedResource = new TaskDistributionResource("distribution", 5, details);

        when(taskMetricsQueryService.handle(any(GetTaskDistributionForMemberQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<TaskDistributionResource> response = metricsController.getTaskDistributionForMember(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("distribution", response.getBody().type());
        assertEquals(5, response.getBody().value());
        verify(taskMetricsQueryService, times(1)).handle(any(GetTaskDistributionForMemberQuery.class));
    }

    // GET RESCHEDULED TASKS FOR MEMBER TESTS

    @Test
    void getRescheduledTasksForMember_WhenMemberIdProvided_ReturnsRescheduledTasksResource() {
        // Arrange
        Long memberId = 10L;
        Map<String, Integer> details = new HashMap<>();
        details.put("rescheduled", 2);
        details.put("non_rescheduled", 3);
        List<Long> rescheduledMemberIds = Arrays.asList(memberId);
        RescheduledTasksResource expectedResource = new RescheduledTasksResource("rescheduled", 2L, details, rescheduledMemberIds);

        when(taskMetricsQueryService.handle(any(GetRescheduledTasksForMemberQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<RescheduledTasksResource> response = metricsController.getRescheduledTasksForMember(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("rescheduled", response.getBody().type());
        assertEquals(2L, response.getBody().value());
        verify(taskMetricsQueryService, times(1)).handle(any(GetRescheduledTasksForMemberQuery.class));
    }

    // GET AVERAGE COMPLETION TIME FOR MEMBER TESTS

    @Test
    void getAvgCompletionTimeForMember_WhenMemberIdProvided_ReturnsAvgCompletionTimeResource() {
        // Arrange
        Long memberId = 10L;
        Map<String, Integer> details = new HashMap<>();
        AvgCompletionTimeResource expectedResource = new AvgCompletionTimeResource("avg_completion_time", 2.8, details);

        when(taskMetricsQueryService.handle(any(GetAvgCompletionTimeForMemberQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<AvgCompletionTimeResource> response = metricsController.getAvgCompletionTimeForMember(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("avg_completion_time", response.getBody().type());
        assertEquals(2.8, response.getBody().value());
        verify(taskMetricsQueryService, times(1)).handle(any(GetAvgCompletionTimeForMemberQuery.class));
    }

    // GET TASK DURATION TESTS

    @Test
    void getTaskDuration_WhenTaskIdProvided_ReturnsTaskDurationResource() {
        // Arrange
        Long taskId = 100L;
        TaskDurationResource expectedResource = new TaskDurationResource(taskId, 24L);

        when(taskMetricsQueryService.handle(any(GetTaskDurationByIdQuery.class)))
                .thenReturn(expectedResource);

        // Act
        ResponseEntity<TaskDurationResource> response = metricsController.getTaskDuration(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(taskId, response.getBody().taskId());
        assertEquals(24L, response.getBody().durationInHours());
        verify(taskMetricsQueryService, times(1)).handle(any(GetTaskDurationByIdQuery.class));
    }
}
