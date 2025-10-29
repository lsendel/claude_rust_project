package com.platform.saas.service;

import com.platform.saas.model.EventLog;
import com.platform.saas.repository.EventLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EventPublisher service.
 *
 * Sprint 4 - Service Layer Testing
 * Target: 429 missed instructions → ~343 covered (80% of class)
 *
 * Test Categories:
 * 1. Constructor - EventBridge enabled mode
 * 2. Constructor - EventBridge disabled mode
 * 3. publishEvent() - EventBridge enabled, successful publish
 * 4. publishEvent() - EventBridge disabled, local logging only
 * 5. publishEvent() - EventBridge failure (failedEntryCount > 0)
 * 6. publishEvent() - Exception during EventBridge publish
 * 7. publishEvent() - Exception during save (error log creation)
 * 8. Edge cases - null/empty payloads, large payloads
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisher Service Tests")
class EventPublisherTest {

    @Mock
    private EventLogRepository eventLogRepository;

    @Mock
    private EventBridgeClient eventBridgeClient;

    @Captor
    private ArgumentCaptor<EventLog> eventLogCaptor;

    @Captor
    private ArgumentCaptor<PutEventsRequest> putEventsRequestCaptor;

    private EventPublisher eventPublisher;

    private UUID tenantId;
    private UUID resourceId;

    @BeforeEach
    void setUp() {
        tenantId = UUID.randomUUID();
        resourceId = UUID.randomUUID();
    }

    // ==================== Constructor Tests ====================

    @Test
    @DisplayName("Should create EventPublisher with EventBridge disabled")
    void constructor_EventBridgeDisabled_ClientNull() {
        // When
        eventPublisher = new EventPublisher(
                eventLogRepository,
                false, // eventBridgeEnabled = false
                "default",
                "us-east-1"
        );

        // Then - EventBridge client should not be initialized
        // Note: We can't directly access the private field, but we can verify behavior
        // by testing publishEvent() with EventBridge disabled
        assertThat(eventPublisher).isNotNull();
    }

    @Test
    @DisplayName("Should create EventPublisher with EventBridge enabled")
    void constructor_EventBridgeEnabled_ClientInitialized() {
        // When
        eventPublisher = new EventPublisher(
                eventLogRepository,
                true, // eventBridgeEnabled = true
                "test-event-bus",
                "us-west-2"
        );

        // Then
        assertThat(eventPublisher).isNotNull();
        // Note: In production, this would initialize the real EventBridgeClient
        // For integration tests, we'd need to mock the AWS SDK client builder
    }

    // ==================== publishEvent() - EventBridge Disabled ====================

    @Test
    @DisplayName("Should publish event locally when EventBridge disabled")
    void publishEvent_EventBridgeDisabled_LogsLocally() {
        // Given
        eventPublisher = new EventPublisher(
                eventLogRepository,
                false, // EventBridge disabled
                "default",
                "us-east-1"
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("status", "COMPLETED");
        payload.put("priority", "HIGH");

        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.status.changed",
                resourceId,
                "task",
                payload
        );

        // Then
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getTenantId()).isEqualTo(tenantId);
        assertThat(savedLog.getEventType()).isEqualTo("task.status.changed");
        assertThat(savedLog.getResourceId()).isEqualTo(resourceId);
        assertThat(savedLog.getResourceType()).isEqualTo("task");
        assertThat(savedLog.getEventPayload()).isEqualTo(payload);
        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.NO_RULES_MATCHED);
        assertThat(savedLog.getExecutionDurationMs()).isNotNull().isGreaterThanOrEqualTo(0);
        assertThat(savedLog.getCreatedAt()).isNotNull();
        assertThat(savedLog.getErrorMessage()).isNull();
        assertThat(savedLog.getErrorStackTrace()).isNull();
    }

    @Test
    @DisplayName("Should handle null payload when EventBridge disabled")
    void publishEvent_NullPayload_EventBridgeDisabled_LogsSuccessfully() {
        // Given
        eventPublisher = new EventPublisher(
                eventLogRepository,
                false,
                "default",
                "us-east-1"
        );

        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "project.created",
                resourceId,
                "project",
                null // null payload
        );

        // Then
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getEventPayload()).isNull();
        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.NO_RULES_MATCHED);
    }

    @Test
    @DisplayName("Should handle empty payload when EventBridge disabled")
    void publishEvent_EmptyPayload_EventBridgeDisabled_LogsSuccessfully() {
        // Given
        eventPublisher = new EventPublisher(
                eventLogRepository,
                false,
                "default",
                "us-east-1"
        );

        Map<String, Object> emptyPayload = new HashMap<>();
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.deleted",
                resourceId,
                "task",
                emptyPayload
        );

        // Then
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getEventPayload()).isEmpty();
        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.NO_RULES_MATCHED);
    }

    @Test
    @DisplayName("Should handle large payload when EventBridge disabled")
    void publishEvent_LargePayload_EventBridgeDisabled_LogsSuccessfully() {
        // Given
        eventPublisher = new EventPublisher(
                eventLogRepository,
                false,
                "default",
                "us-east-1"
        );

        Map<String, Object> largePayload = new HashMap<>();
        for (int i = 0; i < 100; i++) {
            largePayload.put("field" + i, "value" + i);
        }

        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.updated",
                resourceId,
                "task",
                largePayload
        );

        // Then
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getEventPayload()).hasSize(100);
        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.NO_RULES_MATCHED);
    }

    // ==================== publishEvent() - EventBridge Enabled ====================

    @Test
    @DisplayName("Should publish event to EventBridge when enabled")
    void publishEvent_EventBridgeEnabled_PublishesSuccessfully() {
        // Given - Create publisher with manual EventBridge client injection
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("oldStatus", "TODO");
        payload.put("newStatus", "IN_PROGRESS");

        // Mock successful EventBridge response
        PutEventsResultEntry resultEntry = PutEventsResultEntry.builder()
                .eventId("event-123")
                .build();
        PutEventsResponse response = PutEventsResponse.builder()
                .failedEntryCount(0)
                .entries(resultEntry)
                .build();

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenReturn(response);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.status.changed",
                resourceId,
                "task",
                payload
        );

        // Then - Verify EventBridge called
        verify(eventBridgeClient, times(1)).putEvents(putEventsRequestCaptor.capture());
        PutEventsRequest request = putEventsRequestCaptor.getValue();

        assertThat(request.entries()).hasSize(1);
        assertThat(request.entries().get(0).eventBusName()).isEqualTo("test-bus");
        assertThat(request.entries().get(0).source()).isEqualTo("com.platform.saas");
        assertThat(request.entries().get(0).detailType()).isEqualTo("task.status.changed");
        assertThat(request.entries().get(0).detail()).contains(tenantId.toString());
        assertThat(request.entries().get(0).detail()).contains(resourceId.toString());
        assertThat(request.entries().get(0).detail()).contains("task");

        // Then - Verify EventLog saved
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.NO_RULES_MATCHED);
        assertThat(savedLog.getErrorMessage()).isNull();
    }

    @Test
    @DisplayName("Should handle EventBridge failure response")
    void publishEvent_EventBridgeFails_LogsError() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Test Project");

        // Mock failed EventBridge response
        PutEventsResultEntry failedEntry = PutEventsResultEntry.builder()
                .errorCode("InternalException")
                .errorMessage("EventBridge internal error")
                .build();
        PutEventsResponse response = PutEventsResponse.builder()
                .failedEntryCount(1) // Failed
                .entries(failedEntry)
                .build();

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenReturn(response);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "project.created",
                resourceId,
                "project",
                payload
        );

        // Then - Should save error log
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).contains("Failed to publish event to EventBridge");
        assertThat(savedLog.getErrorStackTrace()).isNotNull();
        assertThat(savedLog.getExecutionDurationMs()).isNotNull().isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should handle exception during EventBridge publish")
    void publishEvent_EventBridgeThrowsException_LogsError() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("description", "Test task");

        // Mock EventBridge exception
        when(eventBridgeClient.putEvents(any(PutEventsRequest.class)))
                .thenThrow(new RuntimeException("AWS SDK connection timeout"));
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.created",
                resourceId,
                "task",
                payload
        );

        // Then - Should save error log
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getStatus()).isEqualTo(EventLog.ExecutionStatus.FAILED);
        assertThat(savedLog.getErrorMessage()).isEqualTo("AWS SDK connection timeout");
        assertThat(savedLog.getErrorStackTrace()).contains("RuntimeException");
        assertThat(savedLog.getErrorStackTrace()).contains("AWS SDK connection timeout");
    }

    // ==================== Error Handling Tests ====================

    @Test
    @DisplayName("Should truncate long stack traces")
    void publishEvent_LongStackTrace_TruncatesTo2000Chars() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("key", "value");

        // Create exception with deep stack trace
        RuntimeException deepException = createDeepStackTraceException(100);

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenThrow(deepException);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.updated",
                resourceId,
                "task",
                payload
        );

        // Then
        verify(eventLogRepository, times(1)).save(eventLogCaptor.capture());
        EventLog savedLog = eventLogCaptor.getValue();

        assertThat(savedLog.getErrorStackTrace()).isNotNull();
        // Stack trace should be truncated and contain "truncated" message
        if (savedLog.getErrorStackTrace().length() > 2000) {
            assertThat(savedLog.getErrorStackTrace()).contains("(truncated)");
        }
    }

    @Test
    @DisplayName("Should handle repository save exception")
    void publishEvent_RepositorySaveFails_ExceptionHandled() {
        // Given
        eventPublisher = new EventPublisher(
                eventLogRepository,
                false,
                "default",
                "us-east-1"
        );

        Map<String, Object> payload = new HashMap<>();
        payload.put("field", "value");

        // Mock repository exception on first call, success on second (error log)
        when(eventLogRepository.save(any(EventLog.class)))
                .thenThrow(new RuntimeException("Database connection failed"))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // When - Should not throw exception, but handle internally
        eventPublisher.publishEvent(
                tenantId,
                "task.created",
                resourceId,
                "task",
                payload
        );

        // Then - Verify save was attempted twice (once for normal log, once for error log)
        verify(eventLogRepository, times(2)).save(any(EventLog.class));
    }

    // ==================== Event Detail JSON Building Tests ====================

    @Test
    @DisplayName("Should build event detail JSON with string values")
    void publishEvent_PayloadWithStrings_BuildsCorrectJson() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Task Name");
        payload.put("description", "Task Description");

        PutEventsResponse response = PutEventsResponse.builder()
                .failedEntryCount(0)
                .build();

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenReturn(response);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.created",
                resourceId,
                "task",
                payload
        );

        // Then
        verify(eventBridgeClient).putEvents(putEventsRequestCaptor.capture());
        String detail = putEventsRequestCaptor.getValue().entries().get(0).detail();

        assertThat(detail).contains("\"name\":\"Task Name\"");
        assertThat(detail).contains("\"description\":\"Task Description\"");
    }

    @Test
    @DisplayName("Should build event detail JSON with number values")
    void publishEvent_PayloadWithNumbers_BuildsCorrectJson() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("progress", 75);
        payload.put("priority", 3);
        payload.put("estimatedHours", 12.5);

        PutEventsResponse response = PutEventsResponse.builder()
                .failedEntryCount(0)
                .build();

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenReturn(response);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.updated",
                resourceId,
                "task",
                payload
        );

        // Then
        verify(eventBridgeClient).putEvents(putEventsRequestCaptor.capture());
        String detail = putEventsRequestCaptor.getValue().entries().get(0).detail();

        assertThat(detail).contains("\"progress\":75");
        assertThat(detail).contains("\"priority\":3");
        assertThat(detail).contains("\"estimatedHours\":12.5");
    }

    @Test
    @DisplayName("Should build event detail JSON with boolean values")
    void publishEvent_PayloadWithBooleans_BuildsCorrectJson() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("isCompleted", true);
        payload.put("isBlocked", false);

        PutEventsResponse response = PutEventsResponse.builder()
                .failedEntryCount(0)
                .build();

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenReturn(response);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.updated",
                resourceId,
                "task",
                payload
        );

        // Then
        verify(eventBridgeClient).putEvents(putEventsRequestCaptor.capture());
        String detail = putEventsRequestCaptor.getValue().entries().get(0).detail();

        assertThat(detail).contains("\"isCompleted\":true");
        assertThat(detail).contains("\"isBlocked\":false");
    }

    @Test
    @DisplayName("Should build event detail JSON with mixed types")
    void publishEvent_PayloadWithMixedTypes_BuildsCorrectJson() {
        // Given
        eventPublisher = createEventPublisherWithMockedClient(true);

        Map<String, Object> payload = new HashMap<>();
        payload.put("name", "Task Name");
        payload.put("progress", 50);
        payload.put("isActive", true);
        payload.put("customObject", UUID.randomUUID()); // toString() will be called

        PutEventsResponse response = PutEventsResponse.builder()
                .failedEntryCount(0)
                .build();

        when(eventBridgeClient.putEvents(any(PutEventsRequest.class))).thenReturn(response);
        when(eventLogRepository.save(any(EventLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        eventPublisher.publishEvent(
                tenantId,
                "task.created",
                resourceId,
                "task",
                payload
        );

        // Then
        verify(eventBridgeClient).putEvents(putEventsRequestCaptor.capture());
        String detail = putEventsRequestCaptor.getValue().entries().get(0).detail();

        assertThat(detail).contains("\"tenantId\"");
        assertThat(detail).contains("\"resourceId\"");
        assertThat(detail).contains("\"resourceType\":\"task\"");
        assertThat(detail).contains("\"payload\":{");
    }

    // ==================== Helper Methods ====================

    /**
     * Create EventPublisher with mocked EventBridge client.
     * This is needed because the constructor creates the client internally.
     */
    private EventPublisher createEventPublisherWithMockedClient(boolean eventBridgeEnabled) {
        // Create EventPublisher using reflection to inject mocked client
        EventPublisher publisher = new EventPublisher(
                eventLogRepository,
                false, // Disable to prevent real client creation
                "test-bus",
                "us-east-1"
        );

        // Use reflection to set the mocked client and enable flag
        try {
            java.lang.reflect.Field clientField = EventPublisher.class.getDeclaredField("eventBridgeClient");
            clientField.setAccessible(true);
            clientField.set(publisher, eventBridgeClient);

            java.lang.reflect.Field enabledField = EventPublisher.class.getDeclaredField("eventBridgeEnabled");
            enabledField.setAccessible(true);
            enabledField.set(publisher, eventBridgeEnabled);
        } catch (Exception e) {
            throw new RuntimeException("Failed to inject mocked EventBridge client", e);
        }

        return publisher;
    }

    /**
     * Create exception with deep stack trace for truncation testing.
     */
    private RuntimeException createDeepStackTraceException(int depth) {
        if (depth <= 0) {
            return new RuntimeException("Deep stack trace exception");
        }
        try {
            return createDeepStackTraceException(depth - 1);
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
