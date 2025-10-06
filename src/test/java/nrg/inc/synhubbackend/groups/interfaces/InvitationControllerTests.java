package nrg.inc.synhubbackend.groups.interfaces;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.CancelInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationsByGroupIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.InvitationCommandService;
import nrg.inc.synhubbackend.groups.domain.services.InvitationQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.InvitationController;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.InvitationResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByIdQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByUsernameQuery;
import nrg.inc.synhubbackend.tasks.domain.services.MemberQueryService;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InvitationControllerTests {

    @Mock
    private InvitationQueryService invitationQueryService;

    @Mock
    private InvitationCommandService invitationCommandService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private LeaderQueryService leaderQueryService;

    @Mock
    private GroupQueryService groupQueryService;

    @InjectMocks
    private InvitationController invitationController;

    private Group testGroup;
    private Leader testLeader;
    private Member testMember;
    private Invitation testInvitation;
    private UserDetails testUserDetails;
    private nrg.inc.synhubbackend.iam.domain.model.aggregates.User testUser;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize common test data
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        testGroup = new Group("Test Group", "Test Description", "http://test.img", testLeader, new GroupCode("ABC123456"));
        setIdUsingReflection(testGroup, 2L);

        testMember = new Member();
        setIdUsingReflection(testMember, 10L);

        testUser = mock(nrg.inc.synhubbackend.iam.domain.model.aggregates.User.class);
        when(testUser.getUsername()).thenReturn("testmember");
        when(testUser.getName()).thenReturn("Test");
        when(testUser.getSurname()).thenReturn("Member");
        when(testUser.getImgUrl()).thenReturn("http://test.img");
        testMember.setUser(testUser);

        testInvitation = new Invitation(testMember, testGroup);
        setIdUsingReflection(testInvitation, 100L);

        testUserDetails = User.withUsername("testuser")
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

    @Test
    void createInvitation_WhenMemberExists_ReturnsInvitationResource() {
        // Arrange
        Long groupId = 2L;
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(invitationCommandService.handle(any(CreateInvitationCommand.class)))
                .thenReturn(Optional.of(testInvitation));

        // Act
        ResponseEntity<InvitationResource> response = invitationController.createInvitation(groupId, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationCommandService, times(1)).handle(any(CreateInvitationCommand.class));
    }

    @Test
    void createInvitation_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        Long groupId = 2L;
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<InvitationResource> response = invitationController.createInvitation(groupId, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationCommandService, never()).handle(any(CreateInvitationCommand.class));
    }

    @Test
    void createInvitation_WhenInvitationCreationFails_ReturnsBadRequest() {
        // Arrange
        Long groupId = 2L;
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(invitationCommandService.handle(any(CreateInvitationCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<InvitationResource> response = invitationController.createInvitation(groupId, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationCommandService, times(1)).handle(any(CreateInvitationCommand.class));
    }

    @Test
    void getInvitationByGroupId_WhenLeaderAndGroupExist_ReturnsInvitationsList() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));
        when(invitationQueryService.handle(any(GetInvitationsByGroupIdQuery.class)))
                .thenReturn(Arrays.asList(testInvitation));
        when(memberQueryService.handle(any(GetMemberByIdQuery.class)))
                .thenReturn(Optional.of(testMember));

        // Act
        ResponseEntity<List<InvitationResource>> response = invitationController.getInvitationByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(100L, response.getBody().get(0).id());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(invitationQueryService, times(1)).handle(any(GetInvitationsByGroupIdQuery.class));
    }

    @Test
    void getInvitationByGroupId_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<InvitationResource>> response = invitationController.getInvitationByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(invitationQueryService, never()).handle(any(GetInvitationsByGroupIdQuery.class));
    }

    @Test
    void getInvitationByGroupId_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<InvitationResource>> response = invitationController.getInvitationByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(invitationQueryService, never()).handle(any(GetInvitationsByGroupIdQuery.class));
    }

    @Test
    void cancelInvitation_WhenMemberAndInvitationExist_ReturnsNoContent() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(invitationQueryService.handle(any(GetInvitationByMemberIdQuery.class)))
                .thenReturn(Optional.of(testInvitation));
        doNothing().when(invitationCommandService).handle(any(CancelInvitationCommand.class));

        // Act
        ResponseEntity<Void> response = invitationController.cancelInvitation(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationQueryService, times(1)).handle(any(GetInvitationByMemberIdQuery.class));
        verify(invitationCommandService, times(1)).handle(any(CancelInvitationCommand.class));
    }

    @Test
    void cancelInvitation_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = invitationController.cancelInvitation(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationQueryService, never()).handle(any(GetInvitationByMemberIdQuery.class));
        verify(invitationCommandService, never()).handle(any(CancelInvitationCommand.class));
    }

    @Test
    void getInvitationByMember_WhenMemberAndInvitationExist_ReturnsInvitationResource() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(invitationQueryService.handle(any(GetInvitationByMemberIdQuery.class)))
                .thenReturn(Optional.of(testInvitation));

        // Act
        ResponseEntity<InvitationResource> response = invitationController.getInvitationByMember(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(100L, response.getBody().id());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationQueryService, times(1)).handle(any(GetInvitationByMemberIdQuery.class));
    }

    @Test
    void getInvitationByMember_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<InvitationResource> response = invitationController.getInvitationByMember(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationQueryService, never()).handle(any(GetInvitationByMemberIdQuery.class));
    }

    @Test
    void getInvitationByMember_WhenInvitationDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(invitationQueryService.handle(any(GetInvitationByMemberIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<InvitationResource> response = invitationController.getInvitationByMember(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(invitationQueryService, times(1)).handle(any(GetInvitationByMemberIdQuery.class));
    }
}
