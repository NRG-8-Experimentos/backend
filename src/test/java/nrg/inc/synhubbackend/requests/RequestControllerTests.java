package nrg.inc.synhubbackend.requests;

import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import nrg.inc.synhubbackend.requests.domain.model.commands.CreateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.DeleteRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.UpdateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestByIdQuery;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestsByTaskIdQuery;
import nrg.inc.synhubbackend.requests.domain.services.RequestCommandService;
import nrg.inc.synhubbackend.requests.domain.services.RequestQueryService;
import nrg.inc.synhubbackend.requests.interfaces.rest.RequestController;
import nrg.inc.synhubbackend.requests.interfaces.rest.resources.CreateRequestResource;
import nrg.inc.synhubbackend.requests.interfaces.rest.resources.RequestResource;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Member;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetMemberByUsernameQuery;
import nrg.inc.synhubbackend.tasks.domain.model.queries.GetTaskByIdQuery;
import nrg.inc.synhubbackend.tasks.domain.services.MemberQueryService;
import nrg.inc.synhubbackend.tasks.domain.services.TaskQueryService;
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
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class RequestControllerTests {

    @Mock
    private RequestCommandService requestCommandService;

    @Mock
    private RequestQueryService requestQueryService;

    @Mock
    private MemberQueryService memberQueryService;

    @Mock
    private TaskQueryService taskQueryService;

    @InjectMocks
    private RequestController requestController;

    private Member testMember;
    private Task testTask;
    private Request testRequest1;
    private Request testRequest2;
    private UserDetails testUserDetails;
    private CreateRequestResource createRequestResource;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Member
        testMember = new Member();
        setIdUsingReflection(testMember, 1L);

        // Initialize Task
        testTask = new Task();
        setIdUsingReflection(testTask, 100L);
        testTask.setMember(testMember);

        // Initialize Requests
        testRequest1 = new Request();
        setIdUsingReflection(testRequest1, 1000L);
        testRequest1.setTask(testTask);
        setDescriptionUsingReflection(testRequest1, "Test request 1 description");
        setRequestTypeUsingReflection(testRequest1, "SUBMISSION");
        setRequestStatusUsingReflection(testRequest1, "PENDING");

        testRequest2 = new Request();
        setIdUsingReflection(testRequest2, 2000L);
        testRequest2.setTask(testTask);
        setDescriptionUsingReflection(testRequest2, "Test request 2 description");
        setRequestTypeUsingReflection(testRequest2, "MODIFICATION");
        setRequestStatusUsingReflection(testRequest2, "APPROVED");

        // Initialize UserDetails
        testUserDetails = User.withUsername("testmember")
                .password("password")
                .roles("MEMBER")
                .build();

        // Initialize CreateRequestResource
        createRequestResource = new CreateRequestResource(
                "Test request description",
                "SUBMISSION"
        );
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

    /**
     * Helper method to set requestType using reflection
     */
    private void setRequestTypeUsingReflection(Request request, String requestType) throws Exception {
        Field requestTypeField = Request.class.getDeclaredField("requestType");
        requestTypeField.setAccessible(true);
        // Use the fromString method to get the enum value
        Class<?> requestTypeClass = Class.forName("nrg.inc.synhubbackend.requests.domain.model.valueobjects.RequestType");
        Object requestTypeEnum = requestTypeClass.getMethod("fromString", String.class).invoke(null, requestType);
        requestTypeField.set(request, requestTypeEnum);
    }

    /**
     * Helper method to set requestStatus using reflection
     */
    private void setRequestStatusUsingReflection(Request request, String requestStatus) throws Exception {
        Field requestStatusField = Request.class.getDeclaredField("requestStatus");
        requestStatusField.setAccessible(true);
        // Use the fromString method to get the enum value
        Class<?> requestStatusClass = Class.forName("nrg.inc.synhubbackend.requests.domain.model.valueobjects.RequestStatus");
        Object requestStatusEnum = requestStatusClass.getMethod("fromString", String.class).invoke(null, requestStatus);
        requestStatusField.set(request, requestStatusEnum);
    }

    /**
     * Helper method to set description using reflection
     */
    private void setDescriptionUsingReflection(Request request, String description) throws Exception {
        Field descriptionField = Request.class.getDeclaredField("description");
        descriptionField.setAccessible(true);
        descriptionField.set(request, description);
    }

    /**
     * Helper method to set dueDate using reflection for Task
     */
    private void setTaskDueDateUsingReflection(Task task, java.time.OffsetDateTime dueDate) throws Exception {
        Field dueDateField = Task.class.getDeclaredField("dueDate");
        dueDateField.setAccessible(true);
        dueDateField.set(task, dueDate);
    }

    /**
     * Helper method to set createdAt using reflection
     */
    private void setCreatedAtUsingReflection(Object entity, java.time.OffsetDateTime createdAt) throws Exception {
        Field createdAtField = null;
        Class<?> clazz = entity.getClass();

        // Try to find the createdAt field in the class hierarchy
        while (clazz != null && createdAtField == null) {
            try {
                createdAtField = clazz.getDeclaredField("createdAt");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (createdAtField != null) {
            createdAtField.setAccessible(true);
            createdAtField.set(entity, createdAt);
        }
    }

    /**
     * Helper method to set updatedAt using reflection
     */
    private void setUpdatedAtUsingReflection(Object entity, java.time.OffsetDateTime updatedAt) throws Exception {
        Field updatedAtField = null;
        Class<?> clazz = entity.getClass();

        // Try to find the updatedAt field in the class hierarchy
        while (clazz != null && updatedAtField == null) {
            try {
                updatedAtField = clazz.getDeclaredField("updatedAt");
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            }
        }

        if (updatedAtField != null) {
            updatedAtField.setAccessible(true);
            updatedAtField.set(entity, updatedAt);
        }
    }

    // CREATE REQUEST TESTS

    @Test
    void createRequest_WhenValidData_ReturnsCreatedRequestResource() {
        // Arrange
        Long taskId = 100L;
        Long newRequestId = 3000L;

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.of(testTask));
        when(requestCommandService.handle(any(CreateRequestCommand.class)))
                .thenReturn(newRequestId);
        when(requestQueryService.handle(any(GetRequestByIdQuery.class)))
                .thenReturn(Optional.of(testRequest1));

        // Act
        ResponseEntity<RequestResource> response = requestController.createRequest(taskId, createRequestResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1000L, response.getBody().id());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
        verify(requestCommandService, times(1)).handle(any(CreateRequestCommand.class));
        verify(requestQueryService, times(1)).handle(any(GetRequestByIdQuery.class));
    }

    @Test
    void createRequest_WhenMemberDoesNotExist_ReturnsNotFound() {
        // Arrange
        Long taskId = 100L;

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<RequestResource> response = requestController.createRequest(taskId, createRequestResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, never()).handle(any(GetTaskByIdQuery.class));
        verify(requestCommandService, never()).handle(any(CreateRequestCommand.class));
    }

    @Test
    void createRequest_WhenTaskDoesNotExist_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<RequestResource> response = requestController.createRequest(taskId, createRequestResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
        verify(requestCommandService, never()).handle(any(CreateRequestCommand.class));
    }

    @Test
    void createRequest_WhenTaskDoesNotBelongToMember_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;
        Member differentMember = new Member();
        try {
            setIdUsingReflection(differentMember, 999L);
        } catch (Exception e) {
            fail("Failed to set ID for different member");
        }
        testTask.setMember(differentMember);

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.of(testTask));

        // Act
        ResponseEntity<RequestResource> response = requestController.createRequest(taskId, createRequestResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
        verify(requestCommandService, never()).handle(any(CreateRequestCommand.class));

        // Reset task member for other tests
        testTask.setMember(testMember);
    }

    @Test
    void createRequest_WhenCommandServiceReturnsZero_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.of(testTask));
        when(requestCommandService.handle(any(CreateRequestCommand.class)))
                .thenReturn(0L);

        // Act
        ResponseEntity<RequestResource> response = requestController.createRequest(taskId, createRequestResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
        verify(requestCommandService, times(1)).handle(any(CreateRequestCommand.class));
        verify(requestQueryService, never()).handle(any(GetRequestByIdQuery.class));
    }

    @Test
    void createRequest_WhenCreatedRequestNotFound_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;
        Long newRequestId = 3000L;

        when(memberQueryService.handle(any(GetMemberByUsernameQuery.class)))
                .thenReturn(Optional.of(testMember));
        when(taskQueryService.handle(any(GetTaskByIdQuery.class)))
                .thenReturn(Optional.of(testTask));
        when(requestCommandService.handle(any(CreateRequestCommand.class)))
                .thenReturn(newRequestId);
        when(requestQueryService.handle(any(GetRequestByIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<RequestResource> response = requestController.createRequest(taskId, createRequestResource, testUserDetails);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(memberQueryService, times(1)).handle(any(GetMemberByUsernameQuery.class));
        verify(taskQueryService, times(1)).handle(any(GetTaskByIdQuery.class));
        verify(requestCommandService, times(1)).handle(any(CreateRequestCommand.class));
        verify(requestQueryService, times(1)).handle(any(GetRequestByIdQuery.class));
    }

    // GET REQUESTS BY TASK ID TESTS

    @Test
    void getRequestsByTaskId_WhenRequestsExist_ReturnsRequestResources() {
        // Arrange
        Long taskId = 100L;

        when(requestQueryService.handle((GetRequestsByTaskIdQuery) any()))
                .thenReturn(Arrays.asList(testRequest1, testRequest2));

        // Act
        ResponseEntity<List<RequestResource>> response = requestController.getRequestsByTaskId(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        verify(requestQueryService, times(1)).handle((GetRequestsByTaskIdQuery) any());
    }

    @Test
    void getRequestsByTaskId_WhenNoRequestsExist_ReturnsEmptyList() {
        // Arrange
        Long taskId = 100L;

        when(requestQueryService.handle((GetRequestsByTaskIdQuery) any()))
                .thenReturn(Collections.emptyList());

        // Act
        ResponseEntity<List<RequestResource>> response = requestController.getRequestsByTaskId(taskId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
        verify(requestQueryService, times(1)).handle((GetRequestsByTaskIdQuery) any());
    }

    @Test
    void getRequestsByTaskId_PassesCorrectTaskId() {
        // Arrange
        Long expectedTaskId = 100L;

        when(requestQueryService.handle((GetRequestsByTaskIdQuery) any()))
                .thenReturn(Collections.emptyList());

        // Act
        requestController.getRequestsByTaskId(expectedTaskId);

        // Assert
        verify(requestQueryService).handle(argThat((GetRequestsByTaskIdQuery query) ->
                query.taskId().equals(expectedTaskId)
        ));
    }

    // GET REQUEST BY ID TESTS

    @Test
    void getRequestById_WhenRequestExistsAndBelongsToTask_ReturnsRequestResource() {
        // Arrange
        Long taskId = 100L;
        Long requestId = 1000L;

        when(requestQueryService.handle(any(GetRequestByIdQuery.class)))
                .thenReturn(Optional.of(testRequest1));

        // Act
        ResponseEntity<RequestResource> response = requestController.getRequestById(taskId, requestId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1000L, response.getBody().id());
        verify(requestQueryService, times(1)).handle(any(GetRequestByIdQuery.class));
    }

    @Test
    void getRequestById_WhenRequestDoesNotExist_ReturnsNotFound() {
        // Arrange
        Long taskId = 100L;
        Long requestId = 1000L;

        when(requestQueryService.handle(any(GetRequestByIdQuery.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<RequestResource> response = requestController.getRequestById(taskId, requestId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(requestQueryService, times(1)).handle(any(GetRequestByIdQuery.class));
    }

    @Test
    void getRequestById_WhenRequestDoesNotBelongToTask_ReturnsBadRequest() {
        // Arrange
        Long taskId = 999L;
        Long requestId = 1000L;

        when(requestQueryService.handle(any(GetRequestByIdQuery.class)))
                .thenReturn(Optional.of(testRequest1));

        // Act
        ResponseEntity<RequestResource> response = requestController.getRequestById(taskId, requestId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(requestQueryService, times(1)).handle(any(GetRequestByIdQuery.class));
    }

    @Test
    void getRequestById_PassesCorrectRequestId() {
        // Arrange
        Long taskId = 100L;
        Long expectedRequestId = 1000L;

        when(requestQueryService.handle(any(GetRequestByIdQuery.class)))
                .thenReturn(Optional.of(testRequest1));

        // Act
        requestController.getRequestById(taskId, expectedRequestId);

        // Assert
        verify(requestQueryService).handle(argThat((GetRequestByIdQuery query) ->
                query.requestId().equals(expectedRequestId)
        ));
    }

    // UPDATE REQUEST STATUS TESTS

    @Test
    void updateRequestStatus_WhenValidData_ReturnsUpdatedRequestResource() {
        // Arrange
        Long taskId = 100L;
        Long requestId = 1000L;
        String status = "APPROVED";

        when(requestCommandService.handle(any(UpdateRequestCommand.class)))
                .thenReturn(Optional.of(testRequest1));

        // Act
        ResponseEntity<RequestResource> response = requestController.updateRequestStatus(taskId, requestId, status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1000L, response.getBody().id());
        verify(requestCommandService, times(1)).handle(any(UpdateRequestCommand.class));
    }

    @Test
    void updateRequestStatus_WhenUpdateFails_ReturnsBadRequest() {
        // Arrange
        Long taskId = 100L;
        Long requestId = 1000L;
        String status = "APPROVED";

        when(requestCommandService.handle(any(UpdateRequestCommand.class)))
                .thenReturn(Optional.empty());

        // Act
        ResponseEntity<RequestResource> response = requestController.updateRequestStatus(taskId, requestId, status);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNull(response.getBody());
        verify(requestCommandService, times(1)).handle(any(UpdateRequestCommand.class));
    }

    @Test
    void updateRequestStatus_PassesCorrectParameters() {
        // Arrange
        Long taskId = 100L;
        Long expectedRequestId = 1000L;
        String expectedStatus = "APPROVED";

        when(requestCommandService.handle(any(UpdateRequestCommand.class)))
                .thenReturn(Optional.of(testRequest1));

        // Act
        requestController.updateRequestStatus(taskId, expectedRequestId, expectedStatus);

        // Assert
        verify(requestCommandService).handle(argThat((UpdateRequestCommand command) ->
                command.requestId().equals(expectedRequestId) &&
                        command.requestStatus().equals(expectedStatus)
        ));
    }

    // DELETE REQUEST TESTS

    @Test
    void deleteRequestById_WhenCalled_ReturnsNoContent() {
        // Arrange
        Long taskId = 100L;
        Long requestId = 1000L;

        doNothing().when(requestCommandService).handle(any(DeleteRequestCommand.class));

        // Act
        ResponseEntity<Void> response = requestController.deleteRequestById(taskId, requestId);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(requestCommandService, times(1)).handle(any(DeleteRequestCommand.class));
    }

    @Test
    void deleteRequestById_PassesCorrectRequestId() {
        // Arrange
        Long taskId = 100L;
        Long expectedRequestId = 1000L;

        doNothing().when(requestCommandService).handle(any(DeleteRequestCommand.class));

        // Act
        requestController.deleteRequestById(taskId, expectedRequestId);

        // Assert
        verify(requestCommandService).handle(argThat((DeleteRequestCommand command) ->
                command.requestId().equals(expectedRequestId)
        ));
    }
}
