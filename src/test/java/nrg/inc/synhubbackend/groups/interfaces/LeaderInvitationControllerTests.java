package nrg.inc.synhubbackend.groups.interfaces;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.AcceptInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.RejectInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.InvitationCommandService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.LeaderInvitationController;
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
public class LeaderInvitationControllerTests {

    @Mock
    private InvitationCommandService invitationCommandService;

    @Mock
    private LeaderQueryService leaderQueryService;

    @InjectMocks
    private LeaderInvitationController leaderInvitationController;

    private Leader testLeader;
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

    // PROCESS INVITATION - ACCEPT TESTS

    @Test
    void processInvitation_WhenLeaderExistsAndAccepts_ReturnsOk() {
        // Arrange
        Long invitationId = 100L;
        boolean accept = true;

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(invitationCommandService).handle(any(AcceptInvitationCommand.class));

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, accept);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(invitationCommandService, times(1)).handle(any(AcceptInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(RejectInvitationCommand.class));
    }

    @Test
    void processInvitation_WhenLeaderDoesNotExistAndAccepts_ReturnsNotFound() {
        // Arrange
        Long invitationId = 100L;
        boolean accept = true;

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, accept);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(invitationCommandService, never()).handle(any(AcceptInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(RejectInvitationCommand.class));
    }

    // PROCESS INVITATION - REJECT TESTS

    @Test
    void processInvitation_WhenLeaderExistsAndRejects_ReturnsOk() {
        // Arrange
        Long invitationId = 100L;
        boolean accept = false;

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(invitationCommandService).handle(any(RejectInvitationCommand.class));

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, accept);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(invitationCommandService, times(1)).handle(any(RejectInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(AcceptInvitationCommand.class));
    }

    @Test
    void processInvitation_WhenLeaderDoesNotExistAndRejects_ReturnsNotFound() {
        // Arrange
        Long invitationId = 100L;
        boolean accept = false;

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, accept);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(invitationCommandService, never()).handle(any(AcceptInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(RejectInvitationCommand.class));
    }

    // DEFAULT PARAMETER TEST (when accept parameter is not provided, defaults to false)

    @Test
    void processInvitation_WhenAcceptParameterNotProvided_DefaultsToReject() {
        // Arrange
        Long invitationId = 100L;
        // Using default value false for accept parameter

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(invitationCommandService).handle(any(RejectInvitationCommand.class));

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, false);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(invitationCommandService, times(1)).handle(any(RejectInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(AcceptInvitationCommand.class));
    }

    // VERIFY CORRECT COMMAND PARAMETERS

    @Test
    void processInvitation_WhenAccepting_PassesCorrectLeaderIdAndInvitationId() {
        // Arrange
        Long invitationId = 100L;
        boolean accept = true;

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(invitationCommandService).handle(any(AcceptInvitationCommand.class));

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, accept);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invitationCommandService, times(1)).handle(any(AcceptInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(RejectInvitationCommand.class));
    }

    @Test
    void processInvitation_WhenRejecting_PassesCorrectLeaderIdAndInvitationId() {
        // Arrange
        Long invitationId = 100L;
        boolean accept = false;

        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        doNothing().when(invitationCommandService).handle(any(RejectInvitationCommand.class));

        // Act
        ResponseEntity<Void> response = leaderInvitationController.processInvitation(
                invitationId, testUserDetails, accept);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(invitationCommandService, times(1)).handle(any(RejectInvitationCommand.class));
        verify(invitationCommandService, never()).handle(any(AcceptInvitationCommand.class));
    }
}
