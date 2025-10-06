package nrg.inc.synhubbackend.tasks.application;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.GroupRepository;
import nrg.inc.synhubbackend.iam.domain.model.aggregates.User;
import nrg.inc.synhubbackend.iam.domain.model.entities.Role;
import nrg.inc.synhubbackend.iam.domain.model.valueobjects.Roles;
import nrg.inc.synhubbackend.shared.application.external.outboundedservices.ExternalIamService;
import nrg.inc.synhubbackend.tasks.application.internal.commandservices.MemberCommandServiceImpl;
import nrg.inc.synhubbackend.tasks.application.internal.queryservices.MemberQueryServiceImpl;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.commands.AddGroupToMemberCommand;
import nrg.inc.synhubbackend.tasks.domain.model.commands.CreateMemberCommand;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllMembersQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByIdQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByUsernameQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMembersByGroupIdQuery;
import nrg.inc.synhubbackend.tasks.infrastructure.persistence.jpa.repositories.MemberRepository;
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
public class MemberServiceTests {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ExternalIamService externalIamService;

    @InjectMocks
    private MemberCommandServiceImpl memberCommandService;

    @InjectMocks
    private MemberQueryServiceImpl memberQueryService;

    private Member testMember1;
    private Member testMember2;
    private Member testMember3;
    private Group testGroup;
    private User testUser;
    private Role memberRole;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Group
        testGroup = new Group();
        setIdUsingReflection(testGroup, 100L);
        testGroup.setMemberCount(0);

        // Initialize Members
        testMember1 = new Member(new CreateMemberCommand());
        setIdUsingReflection(testMember1, 1L);

        testMember2 = new Member(new CreateMemberCommand());
        setIdUsingReflection(testMember2, 2L);
        testMember2.setGroup(testGroup);

        testMember3 = new Member(new CreateMemberCommand());
        setIdUsingReflection(testMember3, 3L);
        testMember3.setGroup(testGroup);

        // Initialize Role
        memberRole = new Role(Roles.ROLE_MEMBER);
        setIdUsingReflection(memberRole, 1L);

        // Initialize User
        testUser = new User(
                "testuser",
                "Test",
                "User",
                "http://example.com/img.jpg",
                "test@example.com",
                "password123"
        );
        setIdUsingReflection(testUser, 1L);
        testUser.setMember(testMember1);
        testUser.getRoles().add(memberRole);
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

    // ============================================================
    // MEMBER COMMAND SERVICE TESTS
    // ============================================================

    // TESTS FOR CreateMemberCommand

    @Test
    void handleCreateMember_WhenValidCommand_ReturnsMember() {
        // Arrange
        CreateMemberCommand command = new CreateMemberCommand();
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> {
            Member savedMember = invocation.getArgument(0);
            try {
                setIdUsingReflection(savedMember, 10L);
            } catch (Exception e) {
                fail("Failed to set member ID");
            }
            return savedMember;
        });

        // Act
        Optional<Member> result = memberCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getId());
        assertEquals(10L, result.get().getId());
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    @Test
    void handleCreateMember_WhenRepositoryThrowsException_ThrowsException() {
        // Arrange
        CreateMemberCommand command = new CreateMemberCommand();
        when(memberRepository.save(any(Member.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> memberCommandService.handle(command));
        verify(memberRepository, times(1)).save(any(Member.class));
    }

    // TESTS FOR AddGroupToMemberCommand

    @Test
    void handleAddGroupToMember_WhenValidCommand_ReturnsUpdatedMember() {
        // Arrange
        AddGroupToMemberCommand command = new AddGroupToMemberCommand(100L, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Member> result = memberCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertNotNull(result.get().getGroup());
        assertEquals(100L, result.get().getGroup().getId());
        assertEquals(1, testGroup.getMemberCount());
        verify(memberRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findById(100L);
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void handleAddGroupToMember_WhenMemberNotFound_ThrowsException() {
        // Arrange
        AddGroupToMemberCommand command = new AddGroupToMemberCommand(100L, 999L);
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberCommandService.handle(command)
        );
        assertEquals("Member not found", exception.getMessage());
        verify(memberRepository, times(1)).findById(999L);
        verify(groupRepository, never()).findById(any());
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void handleAddGroupToMember_WhenGroupNotFound_ThrowsException() {
        // Arrange
        AddGroupToMemberCommand command = new AddGroupToMemberCommand(999L, 1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> memberCommandService.handle(command)
        );
        assertEquals("Group not found", exception.getMessage());
        verify(memberRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).findById(999L);
        verify(memberRepository, never()).save(any(Member.class));
    }

    @Test
    void handleAddGroupToMember_WhenMemberAlreadyHasGroup_UpdatesGroup() {
        // Arrange
        AddGroupToMemberCommand command = new AddGroupToMemberCommand(100L, 2L);
        Group oldGroup = new Group();
        try {
            setIdUsingReflection(oldGroup, 50L);
        } catch (Exception e) {
            fail("Failed to set group ID");
        }
        oldGroup.setMemberCount(5);
        testMember2.setGroup(oldGroup);

        when(memberRepository.findById(2L)).thenReturn(Optional.of(testMember2));
        when(groupRepository.findById(100L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Member> result = memberCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(100L, result.get().getGroup().getId());
        assertEquals(1, testGroup.getMemberCount());
        verify(memberRepository, times(1)).save(any(Member.class));
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    // ============================================================
    // MEMBER QUERY SERVICE TESTS
    // ============================================================

    // TESTS FOR GetMemberByIdQuery

    @Test
    void handleGetMemberById_WhenMemberExists_ReturnsMember() {
        // Arrange
        GetMemberByIdQuery query = new GetMemberByIdQuery(1L);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(testMember1));

        // Act
        Optional<Member> result = memberQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMember1.getId(), result.get().getId());
        verify(memberRepository, times(1)).findById(1L);
    }

    @Test
    void handleGetMemberById_WhenMemberDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetMemberByIdQuery query = new GetMemberByIdQuery(999L);
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Member> result = memberQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(memberRepository, times(1)).findById(999L);
    }

    // TESTS FOR GetMemberByUsernameQuery

    @Test
    void handleGetMemberByUsername_WhenUserWithMemberRoleExists_ReturnsMember() {
        // Arrange
        GetMemberByUsernameQuery query = new GetMemberByUsernameQuery("testuser");
        when(externalIamService.getUserByUsername("testuser")).thenReturn(Optional.of(testUser));

        // Act
        Optional<Member> result = memberQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMember1.getId(), result.get().getId());
        verify(externalIamService, times(1)).getUserByUsername("testuser");
    }

    @Test
    void handleGetMemberByUsername_WhenUserNotFound_ReturnsEmpty() {
        // Arrange
        GetMemberByUsernameQuery query = new GetMemberByUsernameQuery("nonexistent");
        when(externalIamService.getUserByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NoSuchElementException.class, () -> memberQueryService.handle(query));
        verify(externalIamService, times(1)).getUserByUsername("nonexistent");
    }

    @Test
    void handleGetMemberByUsername_WhenUserHasNonMemberRole_ReturnsEmpty() {
        // Arrange
        GetMemberByUsernameQuery query = new GetMemberByUsernameQuery("leaderuser");
        User leaderUser = new User(
                "leaderuser",
                "Leader",
                "User",
                "http://example.com/img.jpg",
                "leader@example.com",
                "password123"
        );
        Role leaderRole = new Role(Roles.ROLE_LEADER);
        leaderUser.getRoles().add(leaderRole);

        when(externalIamService.getUserByUsername("leaderuser")).thenReturn(Optional.of(leaderUser));

        // Act
        Optional<Member> result = memberQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(externalIamService, times(1)).getUserByUsername("leaderuser");
    }

    @Test
    void handleGetMemberByUsername_WhenUserHasMemberRoleButNoMember_ReturnsEmpty() {
        // Arrange
        GetMemberByUsernameQuery query = new GetMemberByUsernameQuery("usernomember");
        User userWithoutMember = new User(
                "usernomember",
                "Test",
                "User",
                "http://example.com/img.jpg",
                "test@example.com",
                "password123"
        );
        userWithoutMember.getRoles().add(memberRole);
        userWithoutMember.setMember(null);

        when(externalIamService.getUserByUsername("usernomember")).thenReturn(Optional.of(userWithoutMember));

        // Act
        Optional<Member> result = memberQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(externalIamService, times(1)).getUserByUsername("usernomember");
    }

    // TESTS FOR GetAllMembersQuery

    @Test
    void handleGetAllMembers_WhenMembersExist_ReturnsMemberList() {
        // Arrange
        GetAllMembersQuery query = new GetAllMembersQuery();
        List<Member> expectedMembers = Arrays.asList(testMember1, testMember2, testMember3);
        when(memberRepository.findAll()).thenReturn(expectedMembers);

        // Act
        List<Member> result = memberQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(testMember1));
        assertTrue(result.contains(testMember2));
        assertTrue(result.contains(testMember3));
        verify(memberRepository, times(1)).findAll();
    }

    @Test
    void handleGetAllMembers_WhenNoMembersExist_ReturnsEmptyList() {
        // Arrange
        GetAllMembersQuery query = new GetAllMembersQuery();
        when(memberRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Member> result = memberQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(memberRepository, times(1)).findAll();
    }

    // TESTS FOR GetMembersByGroupIdQuery

    @Test
    void handleGetMembersByGroupId_WhenMembersExist_ReturnsMemberList() {
        // Arrange
        GetMembersByGroupIdQuery query = new GetMembersByGroupIdQuery(100L);
        List<Member> expectedMembers = Arrays.asList(testMember2, testMember3);
        when(memberRepository.findMembersByGroup_Id(100L)).thenReturn(expectedMembers);

        // Act
        List<Member> result = memberQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testMember2));
        assertTrue(result.contains(testMember3));
        assertFalse(result.contains(testMember1)); // testMember1 doesn't belong to the group
        verify(memberRepository, times(1)).findMembersByGroup_Id(100L);
    }

    @Test
    void handleGetMembersByGroupId_WhenNoMembersInGroup_ReturnsEmptyList() {
        // Arrange
        GetMembersByGroupIdQuery query = new GetMembersByGroupIdQuery(999L);
        when(memberRepository.findMembersByGroup_Id(999L)).thenReturn(new ArrayList<>());

        // Act
        List<Member> result = memberQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(memberRepository, times(1)).findMembersByGroup_Id(999L);
    }

    @Test
    void handleGetMembersByGroupId_WhenGroupHasOneMember_ReturnsListWithOneMember() {
        // Arrange
        GetMembersByGroupIdQuery query = new GetMembersByGroupIdQuery(100L);
        List<Member> expectedMembers = Collections.singletonList(testMember2);
        when(memberRepository.findMembersByGroup_Id(100L)).thenReturn(expectedMembers);

        // Act
        List<Member> result = memberQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testMember2.getId(), result.get(0).getId());
        verify(memberRepository, times(1)).findMembersByGroup_Id(100L);
    }
}
