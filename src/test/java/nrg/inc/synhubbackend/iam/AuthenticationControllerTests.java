package nrg.inc.synhubbackend.iam;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserLeaderCommand;
import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserMemberCommand;
import nrg.inc.synhubbackend.iam.domain.model.commands.SignInCommand;
import nrg.inc.synhubbackend.iam.domain.model.commands.SignUpCommand;
import nrg.inc.synhubbackend.iam.domain.model.entities.Role;
import nrg.inc.synhubbackend.iam.domain.model.valueobjects.Roles;
import nrg.inc.synhubbackend.iam.domain.services.UserCommandService;
import nrg.inc.synhubbackend.iam.interfaces.rest.AuthenticationController;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.AuthenticatedUserResource;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.SignInResource;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.SignUpResource;
import nrg.inc.synhubbackend.iam.interfaces.rest.resources.UserResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import org.apache.commons.lang3.tuple.ImmutablePair;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AuthenticationControllerTests {

    @Mock
    private UserCommandService userCommandService;

    @InjectMocks
    private AuthenticationController authenticationController;

    private User testUser;
    private User testLeaderUser;
    private User testMemberUser;
    private Role userRole;
    private Role leaderRole;
    private Role memberRole;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize roles
        userRole = new Role(Roles.ROLE_USER);
        setIdUsingReflection(userRole, 1L);

        leaderRole = new Role(Roles.ROLE_LEADER);
        setIdUsingReflection(leaderRole, 2L);

        memberRole = new Role(Roles.ROLE_MEMBER);
        setIdUsingReflection(memberRole, 3L);

        // Initialize regular user
        testUser = new User(
                "testuser",
                "Test",
                "User",
                "http://test.img",
                "testuser@test.com",
                "password123"
        );
        testUser.setRoles(new HashSet<>(Arrays.asList(userRole)));
        setIdUsingReflection(testUser, 10L);

        // Initialize leader user
        testLeaderUser = new User(
                "testleader",
                "Test",
                "Leader",
                "http://leader.img",
                "testleader@test.com",
                "password123"
        );
        testLeaderUser.setRoles(new HashSet<>(Arrays.asList(leaderRole)));
        setIdUsingReflection(testLeaderUser, 20L);

        Leader leader = new Leader();
        setIdUsingReflection(leader, 100L);
        testLeaderUser.setLeader(leader);

        // Initialize member user
        testMemberUser = new User(
                "testmember",
                "Test",
                "Member",
                "http://member.img",
                "testmember@test.com",
                "password123"
        );
        testMemberUser.setRoles(new HashSet<>(Arrays.asList(memberRole)));
        setIdUsingReflection(testMemberUser, 30L);

        Member member = new Member();
        setIdUsingReflection(member, 200L);
        testMemberUser.setMember(member);
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

    // SIGN-IN TESTS

    @Test
    void signIn_WhenCredentialsAreValid_ReturnsAuthenticatedUserResource() {
        // Arrange
        SignInResource signInResource = new SignInResource("testuser", "password123");
        String token = "jwt-token-123";
        ImmutablePair<User, String> authenticatedPair = new ImmutablePair<>(testUser, token);

        when(userCommandService.handle(any(SignInCommand.class)))
                .thenReturn(Optional.of(authenticatedPair));

        // Act
        ResponseEntity<AuthenticatedUserResource> response = authenticationController.signIn(signInResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().id());
        assertEquals("testuser", response.getBody().username());
        assertEquals("jwt-token-123", response.getBody().token());
        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    @Test
    void signIn_WhenCredentialsAreInvalid_ReturnsNotFound() {
        // Arrange
        SignInResource signInResource = new SignInResource("testuser", "wrongpassword");

        when(userCommandService.handle(any(SignInCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<AuthenticatedUserResource> response = authenticationController.signIn(signInResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    @Test
    void signIn_WhenUserDoesNotExist_ReturnsNotFound() {
        // Arrange
        SignInResource signInResource = new SignInResource("nonexistent", "password123");

        when(userCommandService.handle(any(SignInCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<AuthenticatedUserResource> response = authenticationController.signIn(signInResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userCommandService, times(1)).handle(any(SignInCommand.class));
    }

    // SIGN-UP TESTS - REGULAR USER

    @Test
    void signUp_WhenDataIsValid_ReturnsCreatedUserResource() {
        // Arrange
        SignUpResource signUpResource = new SignUpResource(
                "newuser",
                "New",
                "User",
                "http://new.img",
                "newuser@test.com",
                "password123",
                Arrays.asList("ROLE_USER")
        );

        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Optional.of(testUser));

        // Act
        ResponseEntity<UserResource> response = authenticationController.signUp(signUpResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().id());
        assertEquals("testuser", response.getBody().username());
        assertEquals("Test", response.getBody().name());
        verify(userCommandService, times(1)).handle(any(SignUpCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserLeaderCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserMemberCommand.class));
    }

    @Test
    void signUp_WhenUserCreationFails_ReturnsBadRequest() {
        // Arrange
        SignUpResource signUpResource = new SignUpResource(
                "newuser",
                "New",
                "User",
                "http://new.img",
                "newuser@test.com",
                "password123",
                Arrays.asList("ROLE_USER")
        );

        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResource> response = authenticationController.signUp(signUpResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(userCommandService, times(1)).handle(any(SignUpCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserLeaderCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserMemberCommand.class));
    }

    // SIGN-UP TESTS - LEADER

    @Test
    void signUp_WhenRoleIsLeader_CreatesLeaderAndReturnsUserResource() {
        // Arrange
        SignUpResource signUpResource = new SignUpResource(
                "newleader",
                "New",
                "Leader",
                "http://leader.img",
                "newleader@test.com",
                "password123",
                Arrays.asList("ROLE_LEADER")
        );

        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Optional.of(testLeaderUser));
        when(userCommandService.handle(any(CreateUserLeaderCommand.class)))
                .thenReturn(Optional.of(testLeaderUser));

        // Act
        ResponseEntity<UserResource> response = authenticationController.signUp(signUpResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(20L, response.getBody().id());
        assertEquals("testleader", response.getBody().username());
        verify(userCommandService, times(1)).handle(any(SignUpCommand.class));
        verify(userCommandService, times(1)).handle(any(CreateUserLeaderCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserMemberCommand.class));
    }

    @Test
    void signUp_WhenRoleIsLeaderButLeaderCreationFails_ReturnsBadRequest() {
        // Arrange
        SignUpResource signUpResource = new SignUpResource(
                "newleader",
                "New",
                "Leader",
                "http://leader.img",
                "newleader@test.com",
                "password123",
                Arrays.asList("ROLE_LEADER")
        );

        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Optional.of(testLeaderUser));
        when(userCommandService.handle(any(CreateUserLeaderCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResource> response = authenticationController.signUp(signUpResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(userCommandService, times(1)).handle(any(SignUpCommand.class));
        verify(userCommandService, times(1)).handle(any(CreateUserLeaderCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserMemberCommand.class));
    }

    // SIGN-UP TESTS - MEMBER

    @Test
    void signUp_WhenRoleIsMember_CreatesMemberAndReturnsUserResource() {
        // Arrange
        SignUpResource signUpResource = new SignUpResource(
                "newmember",
                "New",
                "Member",
                "http://member.img",
                "newmember@test.com",
                "password123",
                Arrays.asList("ROLE_MEMBER")
        );

        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Optional.of(testMemberUser));
        when(userCommandService.handle(any(CreateUserMemberCommand.class)))
                .thenReturn(Optional.of(testMemberUser));

        // Act
        ResponseEntity<UserResource> response = authenticationController.signUp(signUpResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(30L, response.getBody().id());
        assertEquals("testmember", response.getBody().username());
        verify(userCommandService, times(1)).handle(any(SignUpCommand.class));
        verify(userCommandService, times(1)).handle(any(CreateUserMemberCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserLeaderCommand.class));
    }

    @Test
    void signUp_WhenRoleIsMemberButMemberCreationFails_ReturnsBadRequest() {
        // Arrange
        SignUpResource signUpResource = new SignUpResource(
                "newmember",
                "New",
                "Member",
                "http://member.img",
                "newmember@test.com",
                "password123",
                Arrays.asList("ROLE_MEMBER")
        );

        when(userCommandService.handle(any(SignUpCommand.class)))
                .thenReturn(Optional.of(testMemberUser));
        when(userCommandService.handle(any(CreateUserMemberCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<UserResource> response = authenticationController.signUp(signUpResource);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(userCommandService, times(1)).handle(any(SignUpCommand.class));
        verify(userCommandService, times(1)).handle(any(CreateUserMemberCommand.class));
        verify(userCommandService, never()).handle(any(CreateUserLeaderCommand.class));
    }
}
