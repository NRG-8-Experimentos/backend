package nrg.inc.synhubbackend.groups.interfaces;

import nrg.inc.synhubbackend.groups.application.external.ExternalTasksService;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.DeleteGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.RemoveMemberFromGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.UpdateGroupCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupCommandService;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.LeaderGroupController;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.CreateGroupResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.UpdateGroupResource;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LeaderGroupControllerTests {

    @Mock
    private GroupQueryService groupQueryService;

    @Mock
    private GroupCommandService groupCommandService;

    @Mock
    private LeaderQueryService leaderQueryService;

    @Mock
    private ExternalTasksService externalTasksService;

    @InjectMocks
    private LeaderGroupController leaderGroupController;

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

    // CREATE GROUP TESTS

    @Test
    void createGroup_WhenLeaderExistsAndGroupCreated_ReturnsGroupResource() {
        // Arrange
        CreateGroupResource createResource = new CreateGroupResource(
                "New Group",
                "http://newgroup.img",
                "New Group Description"
        );

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupCommandService.handle(any(CreateGroupCommand.class)))
                .thenReturn(Optional.of(testGroup));

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.createGroup(createResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().id());
        assertEquals("Test Group", response.getBody().name());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, times(1)).handle(any(CreateGroupCommand.class));
    }

    @Test
    void createGroup_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        CreateGroupResource createResource = new CreateGroupResource(
                "New Group",
                "http://newgroup.img",
                "New Group Description"
        );

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.createGroup(createResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, never()).handle(any(CreateGroupCommand.class));
    }

    @Test
    void createGroup_WhenGroupCreationFails_ReturnsNotFound() {
        // Arrange
        CreateGroupResource createResource = new CreateGroupResource(
                "New Group",
                "http://newgroup.img",
                "New Group Description"
        );

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupCommandService.handle(any(CreateGroupCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.createGroup(createResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, times(1)).handle(any(CreateGroupCommand.class));
    }

    // UPDATE GROUP TESTS

    @Test
    void updateGroup_WhenLeaderExistsAndGroupUpdated_ReturnsGroupResource() {
        // Arrange
        UpdateGroupResource updateResource = new UpdateGroupResource(
                "Updated Group",
                "http://updated.img",
                "Updated Description"
        );

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupCommandService.handle(any(UpdateGroupCommand.class)))
                .thenReturn(Optional.of(testGroup));

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.updateGroup(testUserDetails, updateResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().id());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, times(1)).handle(any(UpdateGroupCommand.class));
    }

    @Test
    void updateGroup_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        UpdateGroupResource updateResource = new UpdateGroupResource(
                "Updated Group",
                "http://updated.img",
                "Updated Description"
        );

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.updateGroup(testUserDetails, updateResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, never()).handle(any(UpdateGroupCommand.class));
    }

    @Test
    void updateGroup_WhenGroupUpdateFails_ReturnsNotFound() {
        // Arrange
        UpdateGroupResource updateResource = new UpdateGroupResource(
                "Updated Group",
                "http://updated.img",
                "Updated Description"
        );

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupCommandService.handle(any(UpdateGroupCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.updateGroup(testUserDetails, updateResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, times(1)).handle(any(UpdateGroupCommand.class));
    }

    // DELETE GROUP TESTS

    @Test
    void deleteGroup_WhenLeaderExists_ReturnsNoContent() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(groupCommandService).handle(any(DeleteGroupCommand.class));

        // Act
        ResponseEntity<Void> response = leaderGroupController.deleteGroup(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, times(1)).handle(any(DeleteGroupCommand.class));
    }

    @Test
    void deleteGroup_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = leaderGroupController.deleteGroup(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupCommandService, never()).handle(any(DeleteGroupCommand.class));
    }

    // GET GROUP TESTS

    @Test
    void getGroupById_WhenLeaderAndGroupExist_ReturnsGroupResource() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.getGroupById(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2L, response.getBody().id());
        assertEquals("Test Group", response.getBody().name());
        assertEquals("ABC123456", response.getBody().code());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
    }

    @Test
    void getGroupById_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.getGroupById(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
    }

    @Test
    void getGroupById_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = leaderGroupController.getGroupById(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
    }

    // REMOVE MEMBER FROM GROUP TESTS

    @Test
    void removeMemberFromGroup_WhenLeaderExists_ReturnsNoContent() {
        // Arrange
        Long memberId = 10L;
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(externalTasksService).deleteTasksByMemberId(memberId);
        doNothing().when(groupCommandService).handle(any(RemoveMemberFromGroupCommand.class));

        // Act
        ResponseEntity<Void> response = leaderGroupController.removeMemberFromGroup(testUserDetails, memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(externalTasksService, times(1)).deleteTasksByMemberId(memberId);
        verify(groupCommandService, times(1)).handle(any(RemoveMemberFromGroupCommand.class));
    }

    @Test
    void removeMemberFromGroup_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        Long memberId = 10L;
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = leaderGroupController.removeMemberFromGroup(testUserDetails, memberId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(externalTasksService, never()).deleteTasksByMemberId(anyLong());
        verify(groupCommandService, never()).handle(any(RemoveMemberFromGroupCommand.class));
    }
}
