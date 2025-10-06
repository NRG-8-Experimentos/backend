package nrg.inc.synhubbackend.iam.application;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.iam.application.external.outboundedservices.ExternalLeaderService;
import nrg.inc.synhubbackend.iam.application.internal.commandservices.UserCommandServiceImpl;
import nrg.inc.synhubbackend.iam.application.internal.outboundservices.hashing.HashingService;
import nrg.inc.synhubbackend.iam.application.internal.outboundservices.tokens.TokenService;
import nrg.inc.synhubbackend.iam.application.internal.queryservices.UserQueryServiceImpl;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserLeaderCommand;
import nrg.inc.synhubbackend.iam.domain.model.commands.CreateUserMemberCommand;
import nrg.inc.synhubbackend.iam.domain.model.commands.SignInCommand;
import nrg.inc.synhubbackend.iam.domain.model.commands.SignUpCommand;
import nrg.inc.synhubbackend.iam.domain.model.entities.Role;
import nrg.inc.synhubbackend.iam.domain.model.queries.*;
import nrg.inc.synhubbackend.iam.domain.model.valueobjects.Roles;
import nrg.inc.synhubbackend.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import nrg.inc.synhubbackend.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import nrg.inc.synhubbackend.shared.application.external.outboundedservices.ExternalMemberService;
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

import java.lang.reflect.Field;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class UserServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HashingService hashingService;

    @Mock
    private TokenService tokenService;

    @Mock
    private ExternalMemberService externalMemberService;

    @Mock
    private ExternalLeaderService externalLeaderService;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserCommandServiceImpl userCommandService;

    @InjectMocks
    private UserQueryServiceImpl userQueryService;

    private User testUser;
    private User testLeaderUser;
    private User testMemberUser;
    private Role userRole;
    private Role leaderRole;
    private Role memberRole;
    private Leader testLeader;
    private Member testMember;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Roles
        userRole = new Role(Roles.ROLE_USER);
        setIdUsingReflection(userRole, 1L);

        leaderRole = new Role(Roles.ROLE_LEADER);
        setIdUsingReflection(leaderRole, 2L);

        memberRole = new Role(Roles.ROLE_MEMBER);
        setIdUsingReflection(memberRole, 3L);

        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 10L);

        // Initialize Member
        testMember = new Member();
        setIdUsingReflection(testMember, 20L);

        // Initialize Basic User
        testUser = new User(
                "testuser",
                "Test",
                "User",
                "http://img/user.png",
                "user@example.com",
                "encodedPassword123"
        );
        setIdUsingReflection(testUser, 100L);
        testUser.addRole(userRole);

        // Initialize Leader User
        testLeaderUser = new User(
                "testleader",
                "Leader",
                "User",
                "http://img/leader.png",
                "leader@example.com",
                "encodedPassword123"
        );
        setIdUsingReflection(testLeaderUser, 200L);
        testLeaderUser.addRole(leaderRole);
        testLeaderUser.setLeader(testLeader);

        // Initialize Member User
        testMemberUser = new User(
                "testmember",
                "Member",
                "User",
                "http://img/member.png",
                "member@example.com",
                "encodedPassword123"
        );
        setIdUsingReflection(testMemberUser, 300L);
        testMemberUser.addRole(memberRole);
        testMemberUser.setMember(testMember);
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

    // ============================================================
    // USER COMMAND SERVICE TESTS
    // ============================================================

    // TESTS FOR SignInCommand

    @Test
    void handleSignIn_WhenValidCredentials_ReturnsUserAndToken() {
        // Arrange
        SignInCommand command = new SignInCommand("testuser", "password123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(hashingService.matches("password123", "encodedPassword123")).thenReturn(true);
        when(tokenService.generateToken("testuser")).thenReturn("generated-token-123");

        // Act
        Optional<ImmutablePair<User, String>> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser, result.get().getLeft());
        assertEquals("generated-token-123", result.get().getRight());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(hashingService, times(1)).matches("password123", "encodedPassword123");
        verify(tokenService, times(1)).generateToken("testuser");
    }

    @Test
    void handleSignIn_WhenUserNotFound_ThrowsException() {
        // Arrange
        SignInCommand command = new SignInCommand("nonexistent", "password123");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("nonexistent");
        verify(hashingService, never()).matches(any(), any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    void handleSignIn_WhenInvalidPassword_ThrowsException() {
        // Arrange
        SignInCommand command = new SignInCommand("testuser", "wrongpassword");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));
        when(hashingService.matches("wrongpassword", "encodedPassword123")).thenReturn(false);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("Invalid password", exception.getMessage());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(hashingService, times(1)).matches("wrongpassword", "encodedPassword123");
        verify(tokenService, never()).generateToken(any());
    }

    // TESTS FOR SignUpCommand

    @Test
    void handleSignUp_WhenValidCommand_ReturnsCreatedUser() {
        // Arrange
        List<Role> roles = Arrays.asList(userRole);
        SignUpCommand command = new SignUpCommand(
                "newuser",
                "New",
                "User",
                "http://img/newuser.png",
                "newuser@example.com",
                "password123",
                roles
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.of(userRole));
        when(hashingService.encode("password123")).thenReturn("encodedPassword123");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findByUsername("newuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser");
        verify(roleRepository, times(1)).findByName(Roles.ROLE_USER);
        verify(hashingService, times(1)).encode("password123");
        verify(userRepository, times(1)).save(any(User.class));
        verify(userRepository, times(1)).findByUsername("newuser");
    }

    @Test
    void handleSignUp_WhenUsernameExists_ThrowsException() {
        // Arrange
        List<Role> roles = Arrays.asList(userRole);
        SignUpCommand command = new SignUpCommand(
                "testuser",
                "New",
                "User",
                "http://img/newuser.png",
                "newuser@example.com",
                "password123",
                roles
        );

        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("Username already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("testuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handleSignUp_WhenEmailExists_ThrowsException() {
        // Arrange
        List<Role> roles = Arrays.asList(userRole);
        SignUpCommand command = new SignUpCommand(
                "newuser",
                "New",
                "User",
                "http://img/newuser.png",
                "user@example.com",
                "password123",
                roles
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser")).thenReturn(true);

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByUsername("newuser");
        verify(userRepository, times(1)).existsByEmail("newuser");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handleSignUp_WhenRoleNotFound_ThrowsException() {
        // Arrange
        List<Role> roles = Arrays.asList(userRole);
        SignUpCommand command = new SignUpCommand(
                "newuser",
                "New",
                "User",
                "http://img/newuser.png",
                "newuser@example.com",
                "password123",
                roles
        );

        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("newuser")).thenReturn(false);
        when(roleRepository.findByName(Roles.ROLE_USER)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("Role name not found", exception.getMessage());
        verify(roleRepository, times(1)).findByName(Roles.ROLE_USER);
        verify(userRepository, never()).save(any(User.class));
    }

    // TESTS FOR CreateUserLeaderCommand

    @Test
    void handleCreateUserLeader_WhenValidCommand_ReturnsUserWithLeader() {
        // Arrange
        CreateUserLeaderCommand command = new CreateUserLeaderCommand(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(externalLeaderService.createUserLeader(command)).thenReturn(Optional.of(testLeader));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<User> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLeader, testUser.getLeader());
        verify(userRepository, times(2)).findById(100L); // Called twice in implementation
        verify(externalLeaderService, times(1)).createUserLeader(command);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void handleCreateUserLeader_WhenUserNotFound_ThrowsException() {
        // Arrange
        CreateUserLeaderCommand command = new CreateUserLeaderCommand(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(externalLeaderService, never()).createUserLeader(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handleCreateUserLeader_WhenSaveFails_ThrowsException() {
        // Arrange
        CreateUserLeaderCommand command = new CreateUserLeaderCommand(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(externalLeaderService.createUserLeader(command)).thenReturn(Optional.of(testLeader));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Failed to create user as leader"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    // TESTS FOR CreateUserMemberCommand

    @Test
    void handleCreateUserMember_WhenValidCommand_ReturnsUserWithMember() {
        // Arrange
        CreateUserMemberCommand command = new CreateUserMemberCommand(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(externalMemberService.createUserMember(command)).thenReturn(Optional.of(testMember));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<User> result = userCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMember, testUser.getMember());
        verify(userRepository, times(2)).findById(100L); // Called twice in implementation
        verify(externalMemberService, times(1)).createUserMember(command);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    void handleCreateUserMember_WhenUserNotFound_ThrowsException() {
        // Arrange
        CreateUserMemberCommand command = new CreateUserMemberCommand(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findById(999L);
        verify(externalMemberService, never()).createUserMember(any());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void handleCreateUserMember_WhenSaveFails_ThrowsException() {
        // Arrange
        CreateUserMemberCommand command = new CreateUserMemberCommand(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));
        when(externalMemberService.createUserMember(command)).thenReturn(Optional.of(testMember));
        when(userRepository.save(any(User.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Failed to create user as member"));
        verify(userRepository, times(1)).save(any(User.class));
    }

    // ============================================================
    // USER QUERY SERVICE TESTS
    // ============================================================

    // TESTS FOR GetAllUsersQuery

    @Test
    void handleGetAllUsers_WhenUsersExist_ReturnsUserList() {
        // Arrange
        GetAllUsersQuery query = new GetAllUsersQuery();
        List<User> expectedUsers = Arrays.asList(testUser, testLeaderUser, testMemberUser);
        when(userRepository.findAll()).thenReturn(expectedUsers);

        // Act
        List<User> result = userQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(testUser));
        assertTrue(result.contains(testLeaderUser));
        assertTrue(result.contains(testMemberUser));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void handleGetAllUsers_WhenNoUsersExist_ReturnsEmptyList() {
        // Arrange
        GetAllUsersQuery query = new GetAllUsersQuery();
        when(userRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<User> result = userQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, times(1)).findAll();
    }

    // TESTS FOR GetUserByIdQuery

    @Test
    void handleGetUserById_WhenUserExists_ReturnsUser() {
        // Arrange
        GetUserByIdQuery query = new GetUserByIdQuery(100L);
        when(userRepository.findById(100L)).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findById(100L);
    }

    @Test
    void handleGetUserById_WhenUserDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetUserByIdQuery query = new GetUserByIdQuery(999L);
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }

    // TESTS FOR GetUserByUsernameQuery

    @Test
    void handleGetUserByUsername_WhenUserExists_ReturnsUser() {
        // Arrange
        GetUserByUsernameQuery query = new GetUserByUsernameQuery("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        assertEquals(testUser.getId(), result.get().getId());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    void handleGetUserByUsername_WhenUserDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetUserByUsernameQuery query = new GetUserByUsernameQuery("nonexistent");
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    // TESTS FOR GetUserByMemberId

    @Test
    void handleGetUserByMemberId_WhenUserExists_ReturnsUser() {
        // Arrange
        GetUserByMemberId query = new GetUserByMemberId(20L);
        when(userRepository.findByMember_Id(20L)).thenReturn(Optional.of(testMemberUser));

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMemberUser.getId(), result.get().getId());
        assertEquals(testMember, result.get().getMember());
        verify(userRepository, times(1)).findByMember_Id(20L);
    }

    @Test
    void handleGetUserByMemberId_WhenUserDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetUserByMemberId query = new GetUserByMemberId(999L);
        when(userRepository.findByMember_Id(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByMember_Id(999L);
    }

    // TESTS FOR GetUserByLeaderId

    @Test
    void handleGetUserByLeaderId_WhenUserExists_ReturnsUser() {
        // Arrange
        GetUserByLeaderId query = new GetUserByLeaderId(10L);
        when(userRepository.findByLeader_Id(10L)).thenReturn(Optional.of(testLeaderUser));

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLeaderUser.getId(), result.get().getId());
        assertEquals(testLeader, result.get().getLeader());
        verify(userRepository, times(1)).findByLeader_Id(10L);
    }

    @Test
    void handleGetUserByLeaderId_WhenUserDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetUserByLeaderId query = new GetUserByLeaderId(999L);
        when(userRepository.findByLeader_Id(999L)).thenReturn(Optional.empty());

        // Act
        Optional<User> result = userQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByLeader_Id(999L);
    }

    // TESTS FOR GetUsersByGroupIdQuery

    @Test
    void handleGetUsersByGroupId_WhenUsersExist_ReturnsUserList() {
        // Arrange
        GetUsersByGroupIdQuery query = new GetUsersByGroupIdQuery(50L);
        List<User> expectedUsers = Arrays.asList(testMemberUser);
        when(userRepository.findByMember_Group_Id(50L)).thenReturn(expectedUsers);

        // Act
        List<User> result = userQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.contains(testMemberUser));
        verify(userRepository, times(1)).findByMember_Group_Id(50L);
    }

    @Test
    void handleGetUsersByGroupId_WhenNoUsersExist_ReturnsEmptyList() {
        // Arrange
        GetUsersByGroupIdQuery query = new GetUsersByGroupIdQuery(999L);
        when(userRepository.findByMember_Group_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        List<User> result = userQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(userRepository, times(1)).findByMember_Group_Id(999L);
    }
}
