package nrg.inc.synhubbackend.groups.application;

import nrg.inc.synhubbackend.groups.application.internal.commandservices.GroupCommandServiceImpl;
import nrg.inc.synhubbackend.groups.application.internal.queryservices.GroupQueryServiceImpl;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.*;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByCodeQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.GroupRepository;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.LeaderRepository;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class GroupServiceTests {

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private GroupQueryServiceImpl groupQueryService;

    private Group testGroup;
    private Leader testLeader;
    private Member testMember;
    private GroupCode testGroupCode;

    @Mock
    private LeaderRepository leaderRepository;


    @InjectMocks
    private GroupCommandServiceImpl groupCommandService;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        // Initialize GroupCode
        testGroupCode = new GroupCode("ABC123456");

        // Initialize Group
        testGroup = new Group(
                "Test Group",
                "Test Group Description",
                "http://img/group.png",
                testLeader,
                testGroupCode
        );
        setIdUsingReflection(testGroup, 10L);

        // Initialize Member
        testMember = new Member();
        setIdUsingReflection(testMember, 20L);
        testMember.setGroup(testGroup);

        // Initialize members list for the group
        List<Member> members = new ArrayList<>();
        members.add(testMember);
        testGroup.setMembers(members);
        testGroup.setMemberCount(1);
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

    // TESTS FOR GetGroupByLeaderIdQuery

    @Test
    void handleGetGroupByLeaderId_WhenGroupExists_ReturnsGroup() {
        // Arrange
        Long leaderId = 1L;
        GetGroupByLeaderIdQuery query = new GetGroupByLeaderIdQuery(leaderId);
        when(groupRepository.findByLeader_Id(leaderId)).thenReturn(Optional.of(testGroup));

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGroup.getId(), result.get().getId());
        assertEquals("Test Group", result.get().getName());
        assertEquals("Test Group Description", result.get().getDescription());
        verify(groupRepository, times(1)).findByLeader_Id(leaderId);
    }

    @Test
    void handleGetGroupByLeaderId_WhenGroupDoesNotExist_ReturnsEmpty() {
        // Arrange
        Long leaderId = 999L;
        GetGroupByLeaderIdQuery query = new GetGroupByLeaderIdQuery(leaderId);
        when(groupRepository.findByLeader_Id(leaderId)).thenReturn(Optional.empty());

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(groupRepository, times(1)).findByLeader_Id(leaderId);
    }

    // TESTS FOR GetGroupByCodeQuery

    @Test
    void handleGetGroupByCode_WhenGroupExists_ReturnsGroup() {
        // Arrange
        String code = "ABC123456";
        GetGroupByCodeQuery query = new GetGroupByCodeQuery(code);
        when(groupRepository.findByCode(any(GroupCode.class))).thenReturn(Optional.of(testGroup));

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGroup.getId(), result.get().getId());
        assertEquals("Test Group", result.get().getName());
        verify(groupRepository, times(1)).findByCode(any(GroupCode.class));
    }

    @Test
    void handleGetGroupByCode_WhenGroupDoesNotExist_ReturnsEmpty() {
        // Arrange
        String code = "INVALID99";
        GetGroupByCodeQuery query = new GetGroupByCodeQuery(code);
        when(groupRepository.findByCode(any(GroupCode.class))).thenReturn(Optional.empty());

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(groupRepository, times(1)).findByCode(any(GroupCode.class));
    }

    // TESTS FOR GetGroupByMemberIdQuery

    @Test
    void handleGetGroupByMemberId_WhenMemberExistsWithGroup_ReturnsGroup() {
        // Arrange
        Long memberId = 20L;
        GetGroupByMemberIdQuery query = new GetGroupByMemberIdQuery(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(testMember));
        when(groupRepository.findById(testGroup.getId())).thenReturn(Optional.of(testGroup));

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGroup.getId(), result.get().getId());
        assertEquals("Test Group", result.get().getName());
        verify(memberRepository, times(1)).findById(memberId);
        verify(groupRepository, times(1)).findById(testGroup.getId());
    }

    @Test
    void handleGetGroupByMemberId_WhenMemberHasNoGroup_ReturnsEmpty() {
        // Arrange
        Long memberId = 20L;
        Member memberWithoutGroup = new Member();
        try {
            setIdUsingReflection(memberWithoutGroup, memberId);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }
        memberWithoutGroup.setGroup(null);

        GetGroupByMemberIdQuery query = new GetGroupByMemberIdQuery(memberId);
        when(memberRepository.findById(memberId)).thenReturn(Optional.of(memberWithoutGroup));

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(memberRepository, times(1)).findById(memberId);
        verify(groupRepository, never()).findById(any());
    }

    // TESTS FOR GetGroupByIdQuery

    @Test
    void handleGetGroupById_WhenGroupExists_ReturnsGroup() {
        // Arrange
        Long groupId = 10L;
        GetGroupByIdQuery query = new GetGroupByIdQuery(groupId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.of(testGroup));

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testGroup.getId(), result.get().getId());
        assertEquals("Test Group", result.get().getName());
        assertEquals("Test Group Description", result.get().getDescription());
        verify(groupRepository, times(1)).findById(groupId);
    }

    @Test
    void handleGetGroupById_WhenGroupDoesNotExist_ReturnsEmpty() {
        // Arrange
        Long groupId = 999L;
        GetGroupByIdQuery query = new GetGroupByIdQuery(groupId);
        when(groupRepository.findById(groupId)).thenReturn(Optional.empty());

        // Act
        Optional<Group> result = groupQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(groupRepository, times(1)).findById(groupId);
    }

    // COMMANDS

    // TESTS FOR CreateGroupCommand

    @Test
    void handleCreateGroup_WhenValidCommand_ReturnsCreatedGroup() {
        // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
                "New Group",
                "http://img/newgroup.png",
                "New Group Description",
                1L
        );

        when(leaderRepository.findById(1L)).thenReturn(Optional.of(testLeader));
        when(groupRepository.existsByCode(any(GroupCode.class))).thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Group> result = groupCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("New Group", result.get().getName());
        assertEquals("New Group Description", result.get().getDescription());
        assertEquals(testLeader, result.get().getLeader());
        verify(leaderRepository, times(1)).findById(1L);
        verify(groupRepository, times(1)).existsByCode(any(GroupCode.class));
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void handleCreateGroup_WhenCodeAlreadyExists_GeneratesNewCode() {
        // Arrange
        CreateGroupCommand command = new CreateGroupCommand(
                "New Group",
                "http://img/newgroup.png",
                "New Group Description",
                1L
        );

        when(leaderRepository.findById(1L)).thenReturn(Optional.of(testLeader));
        when(groupRepository.existsByCode(any(GroupCode.class)))
                .thenReturn(true)
                .thenReturn(true)
                .thenReturn(false);
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Group> result = groupCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        verify(groupRepository, times(3)).existsByCode(any(GroupCode.class));
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    // TESTS FOR UpdateGroupCommand

    @Test
    void handleUpdateGroup_WhenValidCommand_ReturnsUpdatedGroup() {
        // Arrange
        UpdateGroupCommand command = new UpdateGroupCommand(
                1L,
                "Updated Group Name",
                "Updated Description",
                "http://img/updated.png"
        );

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.existsById(10L)).thenReturn(true);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Group> result = groupCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Updated Group Name", result.get().getName());
        assertEquals("Updated Description", result.get().getDescription());
        verify(groupRepository, times(1)).findByLeader_Id(1L);
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    @Test
    void handleUpdateGroup_WhenGroupDoesNotExist_ThrowsException() {
        // Arrange
        UpdateGroupCommand command = new UpdateGroupCommand(
                1L,
                "Updated Group Name",
                "Updated Description",
                "http://img/updated.png"
        );

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.existsById(10L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void handleUpdateGroup_WhenEmptyFields_KeepsOriginalValues() {
        // Arrange
        UpdateGroupCommand command = new UpdateGroupCommand(
                1L,
                "",
                "",
                ""
        );

        String originalName = testGroup.getName();
        String originalDescription = testGroup.getDescription();

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.existsById(10L)).thenReturn(true);
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Group> result = groupCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(originalName, result.get().getName());
        assertEquals(originalDescription, result.get().getDescription());
        verify(groupRepository, times(1)).save(any(Group.class));
    }

    // TESTS FOR DeleteGroupCommand

    @Test
    void handleDeleteGroup_WhenValidCommand_DeletesGroup() {
        // Arrange
        DeleteGroupCommand command = new DeleteGroupCommand(1L);

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.existsById(10L)).thenReturn(true);
        doNothing().when(groupRepository).delete(any(Group.class));

        // Act
        groupCommandService.handle(command);

        // Assert
        verify(groupRepository, times(1)).findByLeader_Id(1L);
        verify(groupRepository, times(1)).delete(testGroup);
    }

    @Test
    void handleDeleteGroup_WhenGroupDoesNotExist_ThrowsException() {
        // Arrange
        DeleteGroupCommand command = new DeleteGroupCommand(1L);

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.existsById(10L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(groupRepository, never()).delete(any(Group.class));
    }

    // TESTS FOR RemoveMemberFromGroupCommand

    @Test
    void handleRemoveMemberFromGroup_WhenValidCommand_RemovesMember() {
        // Arrange
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(1L, 20L);

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        groupCommandService.handle(command);

        // Assert
        assertNull(testMember.getGroup());
        assertEquals(0, testGroup.getMembers().size());
        verify(memberRepository, times(1)).save(testMember);
        verify(groupRepository, times(1)).save(testGroup);
    }

    @Test
    void handleRemoveMemberFromGroup_WhenGroupNotFound_ThrowsException() {
        // Arrange
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(1L, 20L);

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void handleRemoveMemberFromGroup_WhenMemberNotFound_ThrowsException() {
        // Arrange
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(1L, 999L);

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void handleRemoveMemberFromGroup_WhenMemberNotInGroup_ThrowsException() {
        // Arrange
        RemoveMemberFromGroupCommand command = new RemoveMemberFromGroupCommand(1L, 20L);
        Member otherMember = new Member();
        try {
            setIdUsingReflection(otherMember, 20L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }

        when(groupRepository.findByLeader_Id(1L)).thenReturn(Optional.of(testGroup));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(otherMember));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
    }

    // TESTS FOR LeaveGroupCommand

    @Test
    void handleLeaveGroup_WhenValidCommand_RemovesMemberFromGroup() {
        // Arrange
        LeaveGroupCommand command = new LeaveGroupCommand(20L, 10L);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        groupCommandService.handle(command);

        // Assert
        assertNull(testMember.getGroup());
        assertEquals(0, testGroup.getMembers().size());
        verify(memberRepository, times(1)).save(testMember);
        verify(groupRepository, times(1)).save(testGroup);
    }

    @Test
    void handleLeaveGroup_WhenGroupNotFound_ThrowsException() {
        // Arrange
        LeaveGroupCommand command = new LeaveGroupCommand(20L, 999L);

        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void handleLeaveGroup_WhenMemberNotFound_ThrowsException() {
        // Arrange
        LeaveGroupCommand command = new LeaveGroupCommand(999L, 10L);

        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
    }

    @Test
    void handleLeaveGroup_WhenMemberNotInGroup_ThrowsException() {
        // Arrange
        LeaveGroupCommand command = new LeaveGroupCommand(20L, 10L);
        Member otherMember = new Member();
        try {
            setIdUsingReflection(otherMember, 20L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }

        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(otherMember));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> groupCommandService.handle(command));
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
    }
}
