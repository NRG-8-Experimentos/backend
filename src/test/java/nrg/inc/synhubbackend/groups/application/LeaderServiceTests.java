package nrg.inc.synhubbackend.groups.application;

import nrg.inc.synhubbackend.groups.application.internal.commandservices.LeaderCommandServiceImpl;
import nrg.inc.synhubbackend.groups.application.internal.queryservices.LeaderQueryServiceImpl;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateLeaderCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.LeaderRepository;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.domain.model.entities.Role;
import nrg.inc.synhubbackend.iam.domain.model.valueobjects.Roles;
import nrg.inc.synhubbackend.shared.application.external.outboundedservices.ExternalIamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LeaderServiceTests {

    @Mock
    private LeaderRepository leaderRepository;

    @Mock
    private ExternalIamService externalIamService;

    @InjectMocks
    private LeaderCommandServiceImpl leaderCommandService;

    @InjectMocks
    private LeaderQueryServiceImpl leaderQueryService;

    private Leader testLeader;
    private User testLeaderUser;
    private User testMemberUser;
    private Role leaderRole;
    private Role memberRole;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        // Initialize Roles
        leaderRole = new Role(Roles.ROLE_LEADER);
        setIdUsingReflection(leaderRole, 1L);

        memberRole = new Role(Roles.ROLE_MEMBER);
        setIdUsingReflection(memberRole, 2L);

        // Initialize Leader User
        testLeaderUser = new User(
                "testleader",
                "Leader",
                "User",
                "http://img/leader.png",
                "leader@example.com",
                "password123"
        );
        setIdUsingReflection(testLeaderUser, 10L);
        Set<Role> leaderRoles = new HashSet<>();
        leaderRoles.add(leaderRole);
        testLeaderUser.setRoles(leaderRoles);
        testLeaderUser.setLeader(testLeader);

        // Initialize Member User (for negative test cases)
        testMemberUser = new User(
                "testmember",
                "Member",
                "User",
                "http://img/member.png",
                "member@example.com",
                "password123"
        );
        setIdUsingReflection(testMemberUser, 20L);
        Set<Role> memberRoles = new HashSet<>();
        memberRoles.add(memberRole);
        testMemberUser.setRoles(memberRoles);
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
    // LEADER COMMAND SERVICE TESTS
    // ============================================================

    // TESTS FOR CreateLeaderCommand

    @Test
    void handleCreateLeader_WhenValidCommand_ReturnsCreatedLeader() {
        // Arrange
        CreateLeaderCommand command = new CreateLeaderCommand();
        when(leaderRepository.save(any(Leader.class))).thenAnswer(invocation -> {
            Leader savedLeader = invocation.getArgument(0);
            try {
                setIdUsingReflection(savedLeader, 100L);
            } catch (Exception e) {
                fail("Failed to set leader ID");
            }
            return savedLeader;
        });

        // Act
        Optional<Leader> result = leaderCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get());
        assertEquals(100L, result.get().getId());
        assertEquals(Integer.valueOf(0), result.get().getSolvedRequests());
        assertNotNull(result.get().getAverageSolutionTime());
        verify(leaderRepository, times(1)).save(any(Leader.class));
    }

    @Test
    void handleCreateLeader_WhenRepositorySaveFails_ReturnsEmpty() {
        // Arrange
        CreateLeaderCommand command = new CreateLeaderCommand();
        when(leaderRepository.save(any(Leader.class))).thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> leaderCommandService.handle(command));
        verify(leaderRepository, times(1)).save(any(Leader.class));
    }

    // ============================================================
    // LEADER QUERY SERVICE TESTS
    // ============================================================

    // TESTS FOR GetLeaderByIdQuery

    @Test
    void handleGetLeaderById_WhenLeaderExists_ReturnsLeader() {
        // Arrange
        Long leaderId = 1L;
        GetLeaderByIdQuery query = new GetLeaderByIdQuery(leaderId);
        when(leaderRepository.findById(leaderId)).thenReturn(Optional.of(testLeader));

        // Act
        Optional<Leader> result = leaderQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLeader.getId(), result.get().getId());
        assertEquals(Integer.valueOf(0), result.get().getSolvedRequests());
        verify(leaderRepository, times(1)).findById(leaderId);
    }

    @Test
    void handleGetLeaderById_WhenLeaderDoesNotExist_ReturnsEmpty() {
        // Arrange
        Long leaderId = 999L;
        GetLeaderByIdQuery query = new GetLeaderByIdQuery(leaderId);
        when(leaderRepository.findById(leaderId)).thenReturn(Optional.empty());

        // Act
        Optional<Leader> result = leaderQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(leaderRepository, times(1)).findById(leaderId);
    }

    // TESTS FOR GetLeaderByUsernameQuery

    @Test
    void handleGetLeaderByUsername_WhenUserIsLeaderWithLeaderRole_ReturnsLeader() {
        // Arrange
        String username = "testleader";
        GetLeaderByUsernameQuery query = new GetLeaderByUsernameQuery(username);
        when(externalIamService.getUserByUsername(username)).thenReturn(Optional.of(testLeaderUser));

        // Act
        Optional<Leader> result = leaderQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testLeader.getId(), result.get().getId());
        verify(externalIamService, times(1)).getUserByUsername(username);
    }

    @Test
    void handleGetLeaderByUsername_WhenUserHasMemberRole_ReturnsEmpty() {
        // Arrange
        String username = "testmember";
        GetLeaderByUsernameQuery query = new GetLeaderByUsernameQuery(username);
        when(externalIamService.getUserByUsername(username)).thenReturn(Optional.of(testMemberUser));

        // Act
        Optional<Leader> result = leaderQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(externalIamService, times(1)).getUserByUsername(username);
    }

    @Test
    void handleGetLeaderByUsername_WhenUserHasNoLeaderAssociation_ReturnsEmpty() {
        // Arrange
        String username = "testleader";
        User userWithoutLeader = new User(
                "testleader",
                "Leader",
                "User",
                "http://img/leader.png",
                "leader@example.com",
                "password123"
        );
        try {
            setIdUsingReflection(userWithoutLeader, 30L);
        } catch (Exception e) {
            fail("Failed to set user ID");
        }
        Set<Role> roles = new HashSet<>();
        roles.add(leaderRole);
        userWithoutLeader.setRoles(roles);
        userWithoutLeader.setLeader(null); // No leader association

        GetLeaderByUsernameQuery query = new GetLeaderByUsernameQuery(username);
        when(externalIamService.getUserByUsername(username)).thenReturn(Optional.of(userWithoutLeader));

        // Act
        Optional<Leader> result = leaderQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(externalIamService, times(1)).getUserByUsername(username);
    }

    @Test
    void handleGetLeaderByUsername_WhenUserNotFound_ThrowsException() {
        // Arrange
        String username = "nonexistent";
        GetLeaderByUsernameQuery query = new GetLeaderByUsernameQuery(username);
        when(externalIamService.getUserByUsername(username))
                .thenThrow(new IllegalArgumentException("User not found for username: " + username));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> leaderQueryService.handle(query)
        );
        assertEquals("User not found for username: nonexistent", exception.getMessage());
        verify(externalIamService, times(1)).getUserByUsername(username);
    }

    @Test
    void handleGetLeaderByUsername_WhenUserServiceReturnsEmpty_ThrowsException() {
        // Arrange
        String username = "testleader";
        GetLeaderByUsernameQuery query = new GetLeaderByUsernameQuery(username);
        when(externalIamService.getUserByUsername(username)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(
                Exception.class,
                () -> leaderQueryService.handle(query)
        );
        verify(externalIamService, times(1)).getUserByUsername(username);
    }
}
