package nrg.inc.synhubbackend.tasks;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.RemoveMemberFromGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupCommandService;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.commands.DeleteTasksByMemberId;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTasksByMemberId;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByIdQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByUsernameQuery;
import nrg.inc.synhubbackend.tasks.domain.model.valueobjects.TaskStatus;
import nrg.inc.synhubbackend.tasks.domain.services.MemberQueryService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskCommandService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
import nrg.inc.synhubbackend.tasks.interfaces.rest.MemberController;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.ExtendedGroupResource;
import nrg.inc.synhubbackend.tasks.interfaces.rest.resources.MemberResource;
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
import org.springframework.security.core.userdetails.UserDetails;

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
public class MemberControllerTests {

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private GroupQueryService groupQueryService;

    @Mock
    private GroupCommandService groupCommandService;

    @Mock
    private TaskQueryService taskQueryService;

    @Mock
    private TaskCommandService taskCommandService;

    @InjectMocks
    private MemberController memberController;

    private Member testMember;
    private User testUser;
    private Group testGroup;
    private Leader testLeader;
    private Task testTask1;
    private Task testTask2;
    private Task testTask3;
    private UserDetails testUserDetails;

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

        // Initialize members list for the group (required by ExtendedGroupResourceFromEntityAssembler)
        setGroupMembersUsingReflection(testGroup, java.util.Collections.singletonList(testMember));

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

        // Initialize UserDetails
        testUserDetails = org.springframework.security.core.userdetails.User.withUsername("testmember")
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

    /**
     * Helper method to set Group members list using reflection
     */
    private void setGroupMembersUsingReflection(Group group, java.util.List<Member> members) throws Exception {
        Field membersField = Group.class.getDeclaredField("members");
        membersField.setAccessible(true);
        membersField.set(group, members);
    }

    // GET MEMBER BY AUTHENTICATION TESTS

    @Test
    void getMemberByAuthentication_WhenMemberExists_ReturnsMemberResource() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));

        // Act
        ResponseEntity<MemberResource> response = memberController.getMemberByAuthentication(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
    }

    @Test
    void getMemberByAuthentication_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<MemberResource> response = memberController.getMemberByAuthentication(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
    }

    @Test
    void getMemberByAuthentication_PassesCorrectUsername() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));

        // Act
        memberController.getMemberByAuthentication(testUserDetails);

        // Assert
        verify(memberQueryService).handle(argThat((GetMemberByUsernameQuery query) ->
                query.username().equals("testmember")
        ));
    }

    // GET MEMBER BY ID TESTS

    @Test
    void getMemberById_WhenMemberExists_ReturnsMemberResource() {
        // Arrange
        Long memberId = 1L;
        when(memberQueryService.handle(any(GetMemberByIdQuery.class)))
                .thenReturn(Optional.of(testMember));

        // Act
        ResponseEntity<MemberResource> response = memberController.getMemberById(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().id());
        verify(memberQueryService, times(1)).handle(any(GetMemberByIdQuery.class));
    }

    @Test
    void getMemberById_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        Long memberId = 999L;
        when(memberQueryService.handle(any(GetMemberByIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<MemberResource> response = memberController.getMemberById(memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByIdQuery.class));
    }

    @Test
    void getMemberById_PassesCorrectMemberId() {
        // Arrange
        Long expectedMemberId = 1L;
        when(memberQueryService.handle(any(GetMemberByIdQuery.class)))
                .thenReturn(Optional.of(testMember));

        // Act
        memberController.getMemberById(expectedMemberId);

        // Assert
        verify(memberQueryService).handle(argThat((GetMemberByIdQuery query) ->
                query.memberId().equals(expectedMemberId)
        ));
    }

    // GET GROUP BY MEMBER TESTS

    @Test
    void getGroupByMemberId_WhenMemberAndGroupExist_ReturnsGroupResource() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(groupQueryService.handle(any(GetGroupByMemberIdQuery.class)))
                .thenReturn(Optional.of(testGroup));

        // Act
        ResponseEntity<ExtendedGroupResource> response = memberController.getGroupByMemberId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByMemberIdQuery.class));
    }

    @Test
    void getGroupByMemberId_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<ExtendedGroupResource> response = memberController.getGroupByMemberId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByMemberIdQuery.class));
    }

    @Test
    void getGroupByMemberId_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(groupQueryService.handle(any(GetGroupByMemberIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<ExtendedGroupResource> response = memberController.getGroupByMemberId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByMemberIdQuery.class));
    }

    // GET ALL TASKS BY MEMBER AUTHENTICATED TESTS

    @Test
    void getAllTasksByMemberAuthenticated_WhenMemberHasTasks_ReturnsTasksList() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act
        ResponseEntity<List<TaskResource>> response = memberController.getAllTasksByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3, response.getBody().size());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getAllTasksByMemberAuthenticated_WhenMemberHasNoTasks_ReturnsEmptyList() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<TaskResource>> response = memberController.getAllTasksByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getAllTasksByMemberAuthenticated_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<TaskResource>> response = memberController.getAllTasksByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, never()).handle(any(GetAllTasksByMemberId.class));
    }

    // LEAVE GROUP TESTS

    @Test
    void leaveGroupByMemberAuthenticated_WhenMemberAndGroupExist_ReturnsNoContent() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(groupQueryService.handle(any(GetGroupByMemberIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        doNothing().when(taskCommandService).handle(any(DeleteTasksByMemberId.class));
        doNothing().when(groupCommandService).handle(any(RemoveMemberFromGroupCommand.class));

        // Act
        ResponseEntity<Void> response = memberController.leaveGroupByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByMemberIdQuery.class));
        verify(taskCommandService, times(1)).handle(any(DeleteTasksByMemberId.class));
        verify(groupCommandService, times(1)).handle(any(RemoveMemberFromGroupCommand.class));
    }

    @Test
    void leaveGroupByMemberAuthenticated_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = memberController.leaveGroupByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByMemberIdQuery.class));
        verify(taskCommandService, never()).handle(any(DeleteTasksByMemberId.class));
        verify(groupCommandService, never()).handle(any(RemoveMemberFromGroupCommand.class));
    }

    @Test
    void leaveGroupByMemberAuthenticated_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(groupQueryService.handle(any(GetGroupByMemberIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = memberController.leaveGroupByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByMemberIdQuery.class));
        verify(taskCommandService, never()).handle(any(DeleteTasksByMemberId.class));
        verify(groupCommandService, never()).handle(any(RemoveMemberFromGroupCommand.class));
    }

    // GET NEXT TASK TESTS

    @Test
    void getNextTaskByMemberAuthenticated_WhenNextTaskExists_ReturnsTaskResource() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act
        ResponseEntity<TaskResource> response = memberController.getNextTaskByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        // Should return testTask2 as it has the earliest due date among in-progress tasks
        assertEquals(101L, response.getBody().id());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getNextTaskByMemberAuthenticated_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<TaskResource> response = memberController.getNextTaskByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, never()).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getNextTaskByMemberAuthenticated_WhenNoTasksExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<TaskResource> response = memberController.getNextTaskByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));
    }

    @Test
    void getNextTaskByMemberAuthenticated_WhenNoInProgressTasks_ReturnsNotFound() {
        // Arrange
        testTask1.setStatus(TaskStatus.COMPLETED);
        testTask2.setStatus(TaskStatus.COMPLETED);

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetAllTasksByMemberId.class)))
                .thenReturn(Arrays.asList(testTask1, testTask2, testTask3));

        // Act
        ResponseEntity<TaskResource> response = memberController.getNextTaskByMemberAuthenticated(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetAllTasksByMemberId.class));

        // Reset for other tests
        testTask1.setStatus(TaskStatus.IN_PROGRESS);
        testTask2.setStatus(TaskStatus.IN_PROGRESS);
    }
}
