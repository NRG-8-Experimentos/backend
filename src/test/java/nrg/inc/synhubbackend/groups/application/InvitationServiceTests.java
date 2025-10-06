package nrg.inc.synhubbackend.groups.application;

import nrg.inc.synhubbackend.groups.application.internal.commandservices.InvitationCommandServiceImpl;
import nrg.inc.synhubbackend.groups.application.internal.queryservices.InvitationQueryServiceImpl;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Invitation;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.commands.AcceptInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.CancelInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.CreateInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.commands.RejectInvitationCommand;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationByMemberIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetInvitationsByGroupIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.GroupRepository;
import nrg.inc.synhubbackend.groups.infrastructure.persistence.jpa.repositories.InvitationRepository;
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
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class InvitationServiceTests {

    @Mock
    private InvitationRepository invitationRepository;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private LeaderRepository leaderRepository;

    @InjectMocks
    private InvitationCommandServiceImpl invitationCommandService;

    @InjectMocks
    private InvitationQueryServiceImpl invitationQueryService;

    private Invitation testInvitation;
    private Group testGroup;
    private Leader testLeader;
    private Member testMember;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Leader
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        // Initialize GroupCode
        GroupCode testGroupCode = new GroupCode("ABC123456");

        // Initialize Group
        testGroup = new Group(
                "Test Group",
                "Test Group Description",
                "http://img/group.png",
                testLeader,
                testGroupCode
        );
        setIdUsingReflection(testGroup, 10L);
        testGroup.setMembers(new ArrayList<>());
        testGroup.setMemberCount(0);

        // Initialize Member
        testMember = new Member();
        setIdUsingReflection(testMember, 20L);

        // Initialize Invitation
        testInvitation = new Invitation(testMember, testGroup);
        setIdUsingReflection(testInvitation, 100L);
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
    // INVITATION COMMAND SERVICE TESTS
    // ============================================================

    // TESTS FOR CreateInvitationCommand

    @Test
    void handleCreateInvitation_WhenValidCommand_ReturnsCreatedInvitation() {
        // Arrange
        CreateInvitationCommand command = new CreateInvitationCommand(20L, 10L);

        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(invitationRepository.existsByMember_Id(20L)).thenReturn(false);
        when(invitationRepository.save(any(Invitation.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Invitation> result = invitationCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testMember, result.get().getMember());
        assertEquals(testGroup, result.get().getGroup());
        verify(memberRepository, times(1)).findById(20L);
        verify(groupRepository, times(1)).findById(10L);
        verify(invitationRepository, times(1)).existsByMember_Id(20L);
        verify(invitationRepository, times(1)).save(any(Invitation.class));
    }

    @Test
    void handleCreateInvitation_WhenMemberNotFound_ThrowsException() {
        // Arrange
        CreateInvitationCommand command = new CreateInvitationCommand(999L, 10L);

        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Member with id 999 does not exist", exception.getMessage());
        verify(invitationRepository, never()).save(any(Invitation.class));
    }

    @Test
    void handleCreateInvitation_WhenGroupNotFound_ThrowsException() {
        // Arrange
        CreateInvitationCommand command = new CreateInvitationCommand(20L, 999L);

        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(groupRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Group with id 999 does not exist", exception.getMessage());
        verify(invitationRepository, never()).save(any(Invitation.class));
    }

    @Test
    void handleCreateInvitation_WhenMemberAlreadyHasInvitation_ThrowsException() {
        // Arrange
        CreateInvitationCommand command = new CreateInvitationCommand(20L, 10L);

        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        when(groupRepository.findById(10L)).thenReturn(Optional.of(testGroup));
        when(invitationRepository.existsByMember_Id(20L)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Member with id 20 already has an invitation", exception.getMessage());
        verify(invitationRepository, never()).save(any(Invitation.class));
    }

    // TESTS FOR CancelInvitationCommand

    @Test
    void handleCancelInvitation_WhenValidCommand_DeletesInvitation() {
        // Arrange
        CancelInvitationCommand command = new CancelInvitationCommand(20L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(memberRepository.findById(20L)).thenReturn(Optional.of(testMember));
        doNothing().when(invitationRepository).delete(any(Invitation.class));

        // Act
        invitationCommandService.handle(command);

        // Assert
        verify(invitationRepository, times(1)).findById(100L);
        verify(memberRepository, times(1)).findById(20L);
        verify(invitationRepository, times(1)).delete(testInvitation);
    }

    @Test
    void handleCancelInvitation_WhenInvitationNotFound_ThrowsException() {
        // Arrange
        CancelInvitationCommand command = new CancelInvitationCommand(20L, 999L);

        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Invitation with id 999 does not exist", exception.getMessage());
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    @Test
    void handleCancelInvitation_WhenMemberNotFound_ThrowsException() {
        // Arrange
        CancelInvitationCommand command = new CancelInvitationCommand(999L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(memberRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Member with id 999 does not exist", exception.getMessage());
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    @Test
    void handleCancelInvitation_WhenMemberNotOwnerOfInvitation_ThrowsException() {
        // Arrange
        Member otherMember = new Member();
        try {
            setIdUsingReflection(otherMember, 30L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }

        CancelInvitationCommand command = new CancelInvitationCommand(30L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(memberRepository.findById(30L)).thenReturn(Optional.of(otherMember));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Member with id 30 is not the owner of the invitation", exception.getMessage());
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    // TESTS FOR RejectInvitationCommand

    @Test
    void handleRejectInvitation_WhenValidCommand_DeletesInvitation() {
        // Arrange
        RejectInvitationCommand command = new RejectInvitationCommand(1L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(leaderRepository.findById(1L)).thenReturn(Optional.of(testLeader));
        doNothing().when(invitationRepository).delete(any(Invitation.class));

        // Act
        invitationCommandService.handle(command);

        // Assert
        verify(invitationRepository, times(1)).findById(100L);
        verify(leaderRepository, times(1)).findById(1L);
        verify(invitationRepository, times(1)).delete(testInvitation);
    }

    @Test
    void handleRejectInvitation_WhenInvitationNotFound_ThrowsException() {
        // Arrange
        RejectInvitationCommand command = new RejectInvitationCommand(1L, 999L);

        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Invitation with id 999 does not exist", exception.getMessage());
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    @Test
    void handleRejectInvitation_WhenLeaderNotFound_ThrowsException() {
        // Arrange
        RejectInvitationCommand command = new RejectInvitationCommand(999L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(leaderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Leader with id 999 does not exist", exception.getMessage());
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    @Test
    void handleRejectInvitation_WhenLeaderNotOwnerOfInvitation_ThrowsException() {
        // Arrange
        Leader otherLeader = new Leader();
        try {
            setIdUsingReflection(otherLeader, 2L);
        } catch (Exception e) {
            fail("Failed to set leader ID");
        }

        RejectInvitationCommand command = new RejectInvitationCommand(2L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(leaderRepository.findById(2L)).thenReturn(Optional.of(otherLeader));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Leader with id 2 is not the owner of the invitation", exception.getMessage());
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    // TESTS FOR AcceptInvitationCommand

    @Test
    void handleAcceptInvitation_WhenValidCommand_AddsMemberToGroupAndDeletesInvitation() {
        // Arrange
        AcceptInvitationCommand command = new AcceptInvitationCommand(1L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(leaderRepository.findById(1L)).thenReturn(Optional.of(testLeader));
        when(memberRepository.save(any(Member.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(groupRepository.save(any(Group.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doNothing().when(invitationRepository).delete(any(Invitation.class));

        // Act
        invitationCommandService.handle(command);

        // Assert
        assertEquals(testGroup, testMember.getGroup());
        assertTrue(testGroup.getMembers().contains(testMember));
        assertEquals(1, testGroup.getMemberCount());
        verify(memberRepository, times(1)).save(testMember);
        verify(groupRepository, times(1)).save(testGroup);
        verify(invitationRepository, times(1)).delete(testInvitation);
    }

    @Test
    void handleAcceptInvitation_WhenInvitationNotFound_ThrowsException() {
        // Arrange
        AcceptInvitationCommand command = new AcceptInvitationCommand(1L, 999L);

        when(invitationRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Invitation with id 999 does not exist", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    @Test
    void handleAcceptInvitation_WhenLeaderNotFound_ThrowsException() {
        // Arrange
        AcceptInvitationCommand command = new AcceptInvitationCommand(999L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(leaderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Leader with id 999 does not exist", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    @Test
    void handleAcceptInvitation_WhenLeaderNotOwnerOfInvitation_ThrowsException() {
        // Arrange
        Leader otherLeader = new Leader();
        try {
            setIdUsingReflection(otherLeader, 2L);
        } catch (Exception e) {
            fail("Failed to set leader ID");
        }

        AcceptInvitationCommand command = new AcceptInvitationCommand(2L, 100L);

        when(invitationRepository.findById(100L)).thenReturn(Optional.of(testInvitation));
        when(leaderRepository.findById(2L)).thenReturn(Optional.of(otherLeader));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> invitationCommandService.handle(command)
        );
        assertEquals("Leader with id 2 is not the owner of the invitation", exception.getMessage());
        verify(memberRepository, never()).save(any(Member.class));
        verify(groupRepository, never()).save(any(Group.class));
        verify(invitationRepository, never()).delete(any(Invitation.class));
    }

    // ============================================================
    // INVITATION QUERY SERVICE TESTS
    // ============================================================

    // TESTS FOR GetInvitationByMemberIdQuery

    @Test
    void handleGetInvitationByMemberId_WhenInvitationExists_ReturnsInvitation() {
        // Arrange
        Long memberId = 20L;
        GetInvitationByMemberIdQuery query = new GetInvitationByMemberIdQuery(memberId);

        when(invitationRepository.findByMember_Id(memberId)).thenReturn(Optional.of(testInvitation));

        // Act
        Optional<Invitation> result = invitationQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testInvitation.getId(), result.get().getId());
        assertEquals(testMember, result.get().getMember());
        assertEquals(testGroup, result.get().getGroup());
        verify(invitationRepository, times(1)).findByMember_Id(memberId);
    }

    @Test
    void handleGetInvitationByMemberId_WhenInvitationDoesNotExist_ReturnsEmpty() {
        // Arrange
        Long memberId = 999L;
        GetInvitationByMemberIdQuery query = new GetInvitationByMemberIdQuery(memberId);

        when(invitationRepository.findByMember_Id(memberId)).thenReturn(Optional.empty());

        // Act
        Optional<Invitation> result = invitationQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(invitationRepository, times(1)).findByMember_Id(memberId);
    }

    // TESTS FOR GetInvitationsByGroupIdQuery

    @Test
    void handleGetInvitationsByGroupId_WhenInvitationsExist_ReturnsInvitationList() {
        // Arrange
        Long groupId = 10L;

        Member member2 = new Member();
        try {
            setIdUsingReflection(member2, 30L);
        } catch (Exception e) {
            fail("Failed to set member ID");
        }

        Invitation invitation2 = new Invitation(member2, testGroup);
        try {
            setIdUsingReflection(invitation2, 101L);
        } catch (Exception e) {
            fail("Failed to set invitation ID");
        }

        List<Invitation> expectedInvitations = Arrays.asList(testInvitation, invitation2);
        GetInvitationsByGroupIdQuery query = new GetInvitationsByGroupIdQuery(groupId);

        when(invitationRepository.findByGroup_Id(groupId)).thenReturn(expectedInvitations);

        // Act
        List<Invitation> result = invitationQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(testInvitation));
        assertTrue(result.contains(invitation2));
        verify(invitationRepository, times(1)).findByGroup_Id(groupId);
    }

    @Test
    void handleGetInvitationsByGroupId_WhenNoInvitationsExist_ReturnsEmptyList() {
        // Arrange
        Long groupId = 999L;
        GetInvitationsByGroupIdQuery query = new GetInvitationsByGroupIdQuery(groupId);

        when(invitationRepository.findByGroup_Id(groupId)).thenReturn(new ArrayList<>());

        // Act
        List<Invitation> result = invitationQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(invitationRepository, times(1)).findByGroup_Id(groupId);
    }
}
