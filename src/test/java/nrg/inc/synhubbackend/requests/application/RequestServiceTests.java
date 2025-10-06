package nrg.inc.synhubbackend.requests.application;

import nrg.inc.synhubbackend.requests.application.internal.commandservices.RequestCommandServiceImpl;
import nrg.inc.synhubbackend.requests.application.internal.queryservices.RequestQueryServiceImpl;
import nrg.inc.synhubbackend.requests.domain.model.aggregates.Request;
import nrg.inc.synhubbackend.requests.domain.model.commands.CreateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.DeleteAllRequestsByTaskIdCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.DeleteRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.commands.UpdateRequestCommand;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetAllRequestsQuery;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestByIdQuery;
import nrg.inc.synhubbackend.requests.domain.model.queries.GetRequestsByTaskIdQuery;
import nrg.inc.synhubbackend.requests.infrastructure.persistence.jpa.repositories.RequestRepository;
import nrg.inc.synhubbackend.tasks.domain.model.aggregates.Task;
import nrg.inc.synhubbackend.tasks.interfaces.acl.TasksContextFacade;
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
public class RequestServiceTests {

    @Mock
    private RequestRepository requestRepository;

    @Mock
    private TasksContextFacade tasksContextFacade;

    @InjectMocks
    private RequestCommandServiceImpl requestCommandService;

    @InjectMocks
    private RequestQueryServiceImpl requestQueryService;

    private Request testRequest1;
    private Request testRequest2;
    private Request testRequest3;
    private Task testTask;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize Task
        testTask = new Task();
        setIdUsingReflection(testTask, 100L);

        // Initialize Request 1
        testRequest1 = new Request(new CreateRequestCommand(
                "Request 1 description",
                "SUBMISSION",
                100L
        ));
        setIdUsingReflection(testRequest1, 1L);
        testRequest1.setTask(testTask);

        // Initialize Request 2
        testRequest2 = new Request(new CreateRequestCommand(
                "Request 2 description",
                "MODIFICATION",
                100L
        ));
        setIdUsingReflection(testRequest2, 2L);
        testRequest2.setTask(testTask);

        // Initialize Request 3
        testRequest3 = new Request(new CreateRequestCommand(
                "Request 3 description",
                "EXPIRED",
                100L
        ));
        setIdUsingReflection(testRequest3, 3L);
        testRequest3.setTask(testTask);
        testRequest3.updateRequestStatus("APPROVED");
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
    // REQUEST COMMAND SERVICE TESTS
    // ============================================================

    // TESTS FOR CreateRequestCommand

    @Test
    void handleCreateRequest_WhenValidCommand_ReturnsRequestId() {
        // Arrange
        CreateRequestCommand command = new CreateRequestCommand(
                "New request description",
                "SUBMISSION",
                100L
        );
        when(tasksContextFacade.getTaskById(100L)).thenReturn(Optional.of(testTask));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> {
            Request savedRequest = invocation.getArgument(0);
            try {
                setIdUsingReflection(savedRequest, 10L);
            } catch (Exception e) {
                fail("Failed to set request ID");
            }
            return savedRequest;
        });

        // Act
        Long result = requestCommandService.handle(command);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result);
        verify(tasksContextFacade, times(1)).getTaskById(100L);
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void handleCreateRequest_WhenInvalidRequestType_ThrowsException() {
        // Arrange
        CreateRequestCommand command = new CreateRequestCommand(
                "New request description",
                "INVALID_TYPE",
                100L
        );

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertEquals("Invalid request type", exception.getMessage());
        verify(tasksContextFacade, never()).getTaskById(any());
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void handleCreateRequest_WhenTaskDoesNotExist_ThrowsException() {
        // Arrange
        CreateRequestCommand command = new CreateRequestCommand(
                "New request description",
                "SUBMISSION",
                999L
        );
        when(tasksContextFacade.getTaskById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertEquals("Task with id 999 does not exist", exception.getMessage());
        verify(tasksContextFacade, times(1)).getTaskById(999L);
        verify(requestRepository, never()).save(any(Request.class));
    }

    // TESTS FOR UpdateRequestCommand

    @Test
    void handleUpdateRequest_WhenValidCommand_ReturnsUpdatedRequest() {
        // Arrange
        UpdateRequestCommand command = new UpdateRequestCommand(1L, "APPROVED");
        when(requestRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest1));
        when(requestRepository.save(any(Request.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Optional<Request> result = requestCommandService.handle(command);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("APPROVED", result.get().getRequestStatus());
        verify(requestRepository, times(1)).existsById(1L);
        verify(requestRepository, times(1)).findById(1L);
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    @Test
    void handleUpdateRequest_WhenRequestDoesNotExist_ThrowsException() {
        // Arrange
        UpdateRequestCommand command = new UpdateRequestCommand(999L, "APPROVED");
        when(requestRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertEquals("Request with id 999 does not exist", exception.getMessage());
        verify(requestRepository, times(1)).existsById(999L);
        verify(requestRepository, never()).save(any(Request.class));
    }

    @Test
    void handleUpdateRequest_WhenSaveFails_ThrowsException() {
        // Arrange
        UpdateRequestCommand command = new UpdateRequestCommand(1L, "APPROVED");
        when(requestRepository.existsById(1L)).thenReturn(true);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest1));
        when(requestRepository.save(any(Request.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Error while updating request"));
        verify(requestRepository, times(1)).save(any(Request.class));
    }

    // TESTS FOR DeleteRequestCommand

    @Test
    void handleDeleteRequest_WhenValidCommand_DeletesRequest() {
        // Arrange
        DeleteRequestCommand command = new DeleteRequestCommand(1L);
        when(requestRepository.existsById(1L)).thenReturn(true);
        doNothing().when(requestRepository).deleteById(1L);

        // Act
        requestCommandService.handle(command);

        // Assert
        verify(requestRepository, times(1)).existsById(1L);
        verify(requestRepository, times(1)).deleteById(1L);
    }

    @Test
    void handleDeleteRequest_WhenRequestDoesNotExist_ThrowsException() {
        // Arrange
        DeleteRequestCommand command = new DeleteRequestCommand(999L);
        when(requestRepository.existsById(999L)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertEquals("Request with id 999 does not exist", exception.getMessage());
        verify(requestRepository, times(1)).existsById(999L);
        verify(requestRepository, never()).deleteById(any());
    }

    @Test
    void handleDeleteRequest_WhenDeleteFails_ThrowsException() {
        // Arrange
        DeleteRequestCommand command = new DeleteRequestCommand(1L);
        when(requestRepository.existsById(1L)).thenReturn(true);
        doThrow(new RuntimeException("Database error")).when(requestRepository).deleteById(1L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Error while deleting request"));
        verify(requestRepository, times(1)).deleteById(1L);
    }

    // TESTS FOR DeleteAllRequestsByTaskIdCommand

    @Test
    void handleDeleteAllRequestsByTaskId_WhenValidCommand_DeletesAllRequests() {
        // Arrange
        DeleteAllRequestsByTaskIdCommand command = new DeleteAllRequestsByTaskIdCommand(100L);
        when(tasksContextFacade.getTaskById(100L)).thenReturn(Optional.of(testTask));
        doNothing().when(requestRepository).deleteByTaskId(100L);

        // Act
        requestCommandService.handle(command);

        // Assert
        verify(tasksContextFacade, times(1)).getTaskById(100L);
        verify(requestRepository, times(1)).deleteByTaskId(100L);
    }

    @Test
    void handleDeleteAllRequestsByTaskId_WhenTaskDoesNotExist_ThrowsException() {
        // Arrange
        DeleteAllRequestsByTaskIdCommand command = new DeleteAllRequestsByTaskIdCommand(999L);
        when(tasksContextFacade.getTaskById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertEquals("Task with id 999 does not exist", exception.getMessage());
        verify(tasksContextFacade, times(1)).getTaskById(999L);
        verify(requestRepository, never()).deleteByTaskId(any());
    }

    @Test
    void handleDeleteAllRequestsByTaskId_WhenDeleteFails_ThrowsException() {
        // Arrange
        DeleteAllRequestsByTaskIdCommand command = new DeleteAllRequestsByTaskIdCommand(100L);
        when(tasksContextFacade.getTaskById(100L)).thenReturn(Optional.of(testTask));
        doThrow(new RuntimeException("Database error")).when(requestRepository).deleteByTaskId(100L);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> requestCommandService.handle(command)
        );
        assertTrue(exception.getMessage().contains("Error while deleting requests for task"));
        verify(requestRepository, times(1)).deleteByTaskId(100L);
    }

    // ============================================================
    // REQUEST QUERY SERVICE TESTS
    // ============================================================

    // TESTS FOR GetAllRequestsQuery

    @Test
    void handleGetAllRequests_WhenRequestsExist_ReturnsRequestList() {
        // Arrange
        GetAllRequestsQuery query = new GetAllRequestsQuery();
        List<Request> expectedRequests = Arrays.asList(testRequest1, testRequest2, testRequest3);
        when(requestRepository.findAll()).thenReturn(expectedRequests);

        // Act
        List<Request> result = requestQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(testRequest1));
        assertTrue(result.contains(testRequest2));
        assertTrue(result.contains(testRequest3));
        verify(requestRepository, times(1)).findAll();
    }

    @Test
    void handleGetAllRequests_WhenNoRequestsExist_ReturnsEmptyList() {
        // Arrange
        GetAllRequestsQuery query = new GetAllRequestsQuery();
        when(requestRepository.findAll()).thenReturn(new ArrayList<>());

        // Act
        List<Request> result = requestQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(requestRepository, times(1)).findAll();
    }

    // TESTS FOR GetRequestsByTaskIdQuery

    @Test
    void handleGetRequestsByTaskId_WhenRequestsExist_ReturnsRequestList() {
        // Arrange
        GetRequestsByTaskIdQuery query = new GetRequestsByTaskIdQuery(100L);
        List<Request> expectedRequests = Arrays.asList(testRequest1, testRequest2, testRequest3);
        when(requestRepository.findByTaskId(100L)).thenReturn(expectedRequests);

        // Act
        List<Request> result = requestQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertTrue(result.contains(testRequest1));
        assertTrue(result.contains(testRequest2));
        assertTrue(result.contains(testRequest3));
        verify(requestRepository, times(1)).findByTaskId(100L);
    }

    @Test
    void handleGetRequestsByTaskId_WhenNoRequestsExist_ReturnsEmptyList() {
        // Arrange
        GetRequestsByTaskIdQuery query = new GetRequestsByTaskIdQuery(999L);
        when(requestRepository.findByTaskId(999L)).thenReturn(new ArrayList<>());

        // Act
        List<Request> result = requestQueryService.handle(query);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(requestRepository, times(1)).findByTaskId(999L);
    }

    // TESTS FOR GetRequestByIdQuery

    @Test
    void handleGetRequestById_WhenRequestExists_ReturnsRequest() {
        // Arrange
        GetRequestByIdQuery query = new GetRequestByIdQuery(1L);
        when(requestRepository.findById(1L)).thenReturn(Optional.of(testRequest1));

        // Act
        Optional<Request> result = requestQueryService.handle(query);

        // Assert
        assertTrue(result.isPresent());
        assertEquals(testRequest1.getId(), result.get().getId());
        assertEquals("Request 1 description", result.get().getDescription());
        assertEquals("SUBMISSION", result.get().getRequestType());
        assertEquals("PENDING", result.get().getRequestStatus());
        verify(requestRepository, times(1)).findById(1L);
    }

    @Test
    void handleGetRequestById_WhenRequestDoesNotExist_ReturnsEmpty() {
        // Arrange
        GetRequestByIdQuery query = new GetRequestByIdQuery(999L);
        when(requestRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Request> result = requestQueryService.handle(query);

        // Assert
        assertFalse(result.isPresent());
        verify(requestRepository, times(1)).findById(999L);
    }
}
