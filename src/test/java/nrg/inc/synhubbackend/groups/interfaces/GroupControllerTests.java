package nrg.inc.synhubbackend.groups.interfaces;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Group;
import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByCodeQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetGroupByLeaderIdQuery;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.model.valueobjects.GroupCode;
import nrg.inc.synhubbackend.groups.domain.services.GroupQueryService;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.GroupController;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupMemberResource;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.GroupResource;
import nrg.inc.synhubbackend.shared.application.external.outboundedservices.ExternalMemberService;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetAllTasksByGroupIdQuery;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GroupControllerTests {

    @Mock
    private GroupQueryService groupQueryService;

    @Mock
    private ExternalMemberService externalMemberService;

    @Mock
    private TaskQueryService taskQueryService;

    @Mock
    private LeaderQueryService leaderQueryService;

    @InjectMocks
    private GroupController groupController;

    private Group testGroup;
    private Leader testLeader;
    private UserDetails testUserDetails;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize common test data
        testLeader = new Leader();
        setIdUsingReflection(testLeader, 1L);

        testGroup = new Group("Test Group", "Test Description", "http://test.img", testLeader, new GroupCode("ABC123456"));
        setIdUsingReflection(testGroup, 2L);

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
    void searchGroupByCode_WhenGroupExists_ReturnsGroupResource() {
        // Arrange
        String code = "ABC123456";
        when(groupQueryService.handle(any(GetGroupByCodeQuery.class)))
                .thenReturn(Optional.of(testGroup));

        // Act
        ResponseEntity<GroupResource> response = groupController.searchGroupByCode(code);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("ABC123456", response.getBody().code());
        verify(groupQueryService, times(1)).handle(any(GetGroupByCodeQuery.class));
    }

    @Test
    void searchGroupByCode_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        String code = "NOTFOUND";
        when(groupQueryService.handle(any(GetGroupByCodeQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<GroupResource> response = groupController.searchGroupByCode(code);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(groupQueryService, times(1)).handle(any(GetGroupByCodeQuery.class));
    }

    @Test
    void getAllMembersByGroupId_WhenLeaderAndGroupExist_ReturnsMembersList() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.of(testGroup));

        // Create mock Member with User
        Member mockMember = mock(Member.class);
        nrg.inc.synhubbackend.iam.domain.model.aggregates.User mockUser =
                mock(nrg.inc.synhubbackend.iam.domain.model.aggregates.User.class);
        when(mockMember.getId()).thenReturn(10L);
        when(mockMember.getUser()).thenReturn(mockUser);
        when(mockUser.getUsername()).thenReturn("johndoe");
        when(mockUser.getName()).thenReturn("John");
        when(mockUser.getSurname()).thenReturn("Doe");
        when(mockUser.getImgUrl()).thenReturn("http://example.com/img.jpg");

        List<Member> members = Collections.singletonList(mockMember);
        when(externalMemberService.getMembersByGroupId(2L)).thenReturn(members);

        // Act
        ResponseEntity<List<GroupMemberResource>> response =
                groupController.getAllMembersByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("John", response.getBody().get(0).name());
        assertEquals("johndoe", response.getBody().get(0).username());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(externalMemberService, times(1)).getMembersByGroupId(2L);
    }

    @Test
    void getAllMembersByGroupId_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<GroupMemberResource>> response =
                groupController.getAllMembersByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(externalMemberService, never()).getMembersByGroupId(any());
    }

    @Test
    void getAllMembersByGroupId_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<List<GroupMemberResource>> response =
                groupController.getAllMembersByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(externalMemberService, never()).getMembersByGroupId(any());
    }

    @Test
    void getAllTasksByGroupId_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        var response = groupController.getAllTasksByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, never()).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskQueryService, never()).handle(any(GetAllTasksByGroupIdQuery.class));
    }

    @Test
    void getAllTasksByGroupId_WhenGroupDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));
        when(groupQueryService.handle(any(GetGroupByLeaderIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        var response = groupController.getAllTasksByGroupId(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
        verify(groupQueryService, times(1)).handle(any(GetGroupByLeaderIdQuery.class));
        verify(taskQueryService, never()).handle(any(GetAllTasksByGroupIdQuery.class));
    }
}
