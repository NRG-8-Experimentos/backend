package nrg.inc.synhubbackend.groups.interfaces;

import nrg.inc.synhubbackend.groups.domain.model.aggregates.Leader;
import nrg.inc.synhubbackend.groups.domain.model.queries.GetLeaderByUsernameQuery;
import nrg.inc.synhubbackend.groups.domain.services.LeaderQueryService;
import nrg.inc.synhubbackend.groups.interfaces.rest.LeaderController;
import nrg.inc.synhubbackend.groups.interfaces.rest.resources.LeaderResource;
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
import java.sql.Time;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LeaderControllerTests {

    @Mock
    private LeaderQueryService leaderQueryService;

    @InjectMocks
    private LeaderController leaderController;

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
        when(testUser.getEmail()).thenReturn("testleader@test.com");
        testLeader.setUser(testUser);

        // Set up Leader's averageSolutionTime and solvedRequests
        testLeader.setAverageSolutionTime(new Time(3600000)); // 1 hour
        testLeader.setSolvedRequests(5);

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

    @Test
    void getLeaderByAuthentication_WhenLeaderExists_ReturnsLeaderResource() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.of(testLeader));

        // Act
        ResponseEntity<LeaderResource> response = leaderController.getLeaderByAuthentication(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testleader", response.getBody().username());
        assertEquals("Test", response.getBody().name());
        assertEquals("Leader", response.getBody().surname());
        assertEquals("testleader@test.com", response.getBody().email());
        assertEquals(5, response.getBody().solvedRequests());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
    }

    @Test
    void getLeaderByAuthentication_WhenLeaderDoesNotExist_ReturnsNotFound() {
        // Arrange
        when(leaderQueryService.handle(any(GetLeaderByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<LeaderResource> response = leaderController.getLeaderByAuthentication(testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(leaderQueryService, times(1)).handle(any(GetLeaderByUsernameQuery.class));
    }
}
