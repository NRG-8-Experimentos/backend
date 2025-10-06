package nrg.inc.synhubbackend.requests.interfaces;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestsByTaskIdQuery;
import nrg.inc.synhubbackend.requests.domain.services.RequestQueryService;
import nrg.inc.synhubbackend.requests.interfaces.rest.GroupRequestController;
import nrg.inc.synhubbackend.requests.interfaces.rest.resources.RequestResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTasksByGroupIdQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTasksByMemberId;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByUsernameQuery;
import nrg.inc.synhubbackend.tasks.domain.services.MemberQueryService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GroupRequestControllerTests {

    @Mock
    private RequestQueryService requestQueryService;

    @Mock
    private LeaderQueryService leaderQueryService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private GroupQueryService groupQueryService;

    @Mock
    private TaskQueryService taskQueryService;

    @InjectMocks
    private GroupRequestController groupRequestController;

    private Leader testLeader;
    private Member testMember;
    private Group testGroup;
    private Task testTask1;
    private Task testTask2;
    private Request testRequest1;
    private Request testRequest2;
    private Request testRequest3;
    private UserDetails testLeaderUserDetails;
    private UserDetails testMemberUserDetails;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        // Initialize Member
        testMember = new Member();
        setIdUsingReflection(testMember, 10L);

        // Initialize Group
        testGroup = new Group();
        setIdUsingReflection(testGroup, 100L);

        // Initialize Tasks
        testTask1 = new Task();
        setIdUsingReflection(testTask1, 1000L);
        testTask1.setGroup(testGroup);
        setDueDateUsingReflection(testTask1, java.time.OffsetDateTime.now());
        setCreatedAtUsingReflection(testTask1, java.time.OffsetDateTime.now());
        setUpdatedAtUsingReflection(testTask1, java.time.OffsetDateTime.now());

        testTask2 = new Task();
        setIdUsingReflection(testTask2, 2000L);
        testTask2.setGroup(testGroup);
        setDueDateUsingReflection(testTask2, java.time.OffsetDateTime.now());
        setCreatedAtUsingReflection(testTask2, java.time.OffsetDateTime.now());
        setUpdatedAtUsingReflection(testTask2, java.time.OffsetDateTime.now());

        // Initialize Requests
        testRequest1 = new Request();
        setIdUsingReflection(testRequest1, 10000L);
        testRequest1.setTask(testTask1);

        testRequest2 = new Request();
        setIdUsingReflection(testRequest2, 20000L);
        testRequest2.setTask(testTask1);

        testRequest3 = new Request();
        setIdUsingReflection(testRequest3, 30000L);
        testRequest3.setTask(testTask2);

        // Initialize UserDetails
        testLeaderUserDetails = User.withUsername("testleader")
                .password("password")
                .roles("LEADER")
                .build();

        testMemberUserDetails = User.withUsername("testmember")
                .password("password")
                .roles("MEMBER")
                .build();
    }

    /**
     * Helper method to set ID using reflection
     */
    private void setIdUsingReflection(Object entity, Long id) throws Exception {
        Field idField = null;
        Class<?> clazz = entity.getClass();

        // Try to find the id field in the class hierarchy
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
     * Helper method to set dueDate using reflection
     */
    private void setDueDateUsingReflection(Task task, java.time.OffsetDateTime dueDate) throws Exception {
        Field dueDateField = Task.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(task, dueDate);
    }

    /**
     * Helper method to set createdAt using reflection
     */
    private void setCreatedAtUsingReflection(Object entity, java.time.OffsetDateTime createdAt) throws Exception {
        Field createdAtField = null;
        Class<?> clazz = entity.getClass();

        // Try to find the createdAt field in the class hierarchy
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
    private void setUpdatedAtUsingReflection(Object entity, java.time.OffsetDateTime updatedAt) throws Exception {
        Field updatedAtField = null;
        Class<?> clazz = entity.getClass();

        // Try to find the updatedAt field in the class hierarchy
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

    // TESTS FOR getAllRequestsFromGroup (LEADER ENDPOINT)


    @Test
    void getAllRequestsFromGroup_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<RequestResource>> response = groupRequestController.getAllRequestsFromGroup(testLeaderUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskQueryService, never()).handle(any(GetAllTasksByGroupIdQuery.class));
        verify(requestQueryService, never()).handle((GetRequestsByTaskIdQuery) any());
    }

    @Test
    void getAllRequestsFromGroup_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<RequestResource>> response = groupRequestController.getAllRequestsFromGroup(testLeaderUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskQueryService, never()).handle(any(GetAllTasksByGroupIdQuery.class));
        verify(requestQueryService, never()).handle((GetRequestsByTaskIdQuery) any());
    }

    @Test
    void getAllRequestsFromGroup_WhenGroupHasNoTasks_ReturnsEmptyList() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(taskQueryService.handle(any(GetAllTasksByGroupIdQuery.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<RequestResource>> response = groupRequestController.getAllRequestsFromGroup(testLeaderUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByGroupIdQuery.class));
        verify(requestQueryService, never()).handle((GetRequestsByTaskIdQuery) any());
    }

    @Test
    void getAllRequestsFromGroup_PassesCorrectLeaderId() {
        // Arrange
        Long expectedLeaderId = 1L;
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(taskQueryService.handle(any(GetAllTasksByGroupIdQuery.class)))
                .thenReturn(Collections.emptyList());

        // Act
        groupRequestController.getAllRequestsFromGroup(testLeaderUserDetails);

        // Assert
        verify(groupQueryService).handle(argThat((GetGroupByLeaderIdQuery query) ->
                query.leaderId().equals(expectedLeaderId)
        ));
    }

    @Test
    void getAllRequestsFromGroup_PassesCorrectGroupId() {
        // Arrange
        Long expectedGroupId = 100L;
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(taskQueryService.handle(any(GetAllTasksByGroupIdQuery.class)))
                .thenReturn(Collections.emptyList());

        // Act
        groupRequestController.getAllRequestsFromGroup(testLeaderUserDetails);

        // Assert
        verify(taskQueryService).handle(argThat((GetAllTasksByGroupIdQuery query) ->
                query.groupId().equals(expectedGroupId)
        ));
    }

    // TESTS FOR getAllRequestsFromMember (MEMBER ENDPOINT)

    @Test
    void getAllRequestsFromMember_PassesCorrectMemberUsername() {
        // Arrange
        String expectedUsername = "testmember";
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        groupRequestController.getAllRequestsFromMember(testMemberUserDetails);

        // Assert
        verify(memberQueryService).handle((GetMemberByUsernameQuery) argThat(query ->
                query instanceof GetMemberByUsernameQuery &&
                        ((GetMemberByUsernameQuery) query).username().equals(expectedUsername)
        ));
    }

    @Test
    void getAllRequestsFromMember_PassesCorrectMemberId() {
        // Arrange
        Long expectedMemberId = 10L;
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        groupRequestController.getAllRequestsFromMember(testMemberUserDetails);

        // Assert
        verify(taskQueryService).handle((GetAllTasksByMemberId) argThat(query ->
                query instanceof GetAllTasksByMemberId &&
                        ((GetAllTasksByMemberId) query).memberId().equals(expectedMemberId)
        ));
    }

}
