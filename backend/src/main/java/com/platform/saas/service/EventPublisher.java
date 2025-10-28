package com.platform.saas.service;

import com.platform.saas.model.EventLog;
import com.platform.saas.repository.EventLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Service for publishing events to AWS EventBridge and logging them locally.
 * Events trigger automation rules and are stored for audit trail.
 *
 * When EventBridge is disabled, events are only logged locally and can be
 * processed by a local automation engine.
 */
@Service
@Slf4j
public class EventPublisher {

    private final EventBridgeClient eventBridgeClient;
    private final EventLogRepository eventLogRepository;
    private final String eventBusName;
    private final boolean eventBridgeEnabled;

    public EventPublisher(
            EventLogRepository eventLogRepository,
            @Value("${aws.eventbridge.enabled:false}") boolean eventBridgeEnabled,
            @Value("${aws.eventbridge.event-bus-name:default}") String eventBusName,
            @Value("${aws.eventbridge.region:us-east-1}") String region) {

        this.eventLogRepository = eventLogRepository;
        this.eventBridgeEnabled = eventBridgeEnabled;
        this.eventBusName = eventBusName;

        if (eventBridgeEnabled) {
            this.eventBridgeClient = EventBridgeClient.builder()
                    .region(Region.of(region))
                    .credentialsProvider(DefaultCredentialsProvider.create())
                    .build();
            log.info("EventBridge enabled - events will be published to bus: {}", eventBusName);
        } else {
            this.eventBridgeClient = null;
            log.warn("EventBridge is disabled - events will only be logged locally");
        }
    }

    /**
     * Publish an event for automation processing.
     *
     * @param tenantId The tenant ID
     * @param eventType The event type (e.g., "project.created", "task.status.changed")
     * @param resourceId The resource ID that triggered the event
     * @param resourceType The resource type (e.g., "project", "task")
     * @param eventPayload The event payload with details
     */
    public void publishEvent(
            UUID tenantId,
            String eventType,
            UUID resourceId,
            String resourceType,
            Map<String, Object> eventPayload) {

        long startTime = System.currentTimeMillis();

        try {
            // Create event log entry
            EventLog eventLog = EventLog.builder()
                    .tenantId(tenantId)
                    .eventType(eventType)
                    .eventPayload(eventPayload)
                    .resourceId(resourceId)
                    .resourceType(resourceType)
                    .status(EventLog.ExecutionStatus.NO_RULES_MATCHED) // Default, will be updated if rules execute
                    .createdAt(LocalDateTime.now())
                    .build();

            // Publish to EventBridge if enabled
            if (eventBridgeEnabled && eventBridgeClient != null) {
                publishToEventBridge(tenantId, eventType, resourceId, resourceType, eventPayload);
                log.info("Event published to EventBridge: {} for tenant {}", eventType, tenantId);
            } else {
                log.debug("Event logged locally: {} for tenant {} (EventBridge disabled)",
                        eventType, tenantId);
            }

            // Calculate execution duration
            long executionTime = System.currentTimeMillis() - startTime;
            eventLog.setExecutionDurationMs(executionTime);

            // Save event log
            eventLogRepository.save(eventLog);

            log.debug("Event logged: id={}, type={}, tenant={}, resource={}:{}",
                    eventLog.getId(), eventType, tenantId, resourceType, resourceId);

        } catch (Exception e) {
            log.error("Failed to publish event: {} for tenant {}", eventType, tenantId, e);

            // Still save the event log with error status
            long executionTime = System.currentTimeMillis() - startTime;
            EventLog errorLog = EventLog.builder()
                    .tenantId(tenantId)
                    .eventType(eventType)
                    .eventPayload(eventPayload)
                    .resourceId(resourceId)
                    .resourceType(resourceType)
                    .status(EventLog.ExecutionStatus.FAILED)
                    .errorMessage(e.getMessage())
                    .errorStackTrace(getStackTrace(e))
                    .executionDurationMs(executionTime)
                    .createdAt(LocalDateTime.now())
                    .build();

            eventLogRepository.save(errorLog);
        }
    }

    /**
     * Publish event to AWS EventBridge.
     */
    private void publishToEventBridge(
            UUID tenantId,
            String eventType,
            UUID resourceId,
            String resourceType,
            Map<String, Object> eventPayload) {

        // Build event detail JSON
        String detailJson = buildEventDetailJson(tenantId, resourceId, resourceType, eventPayload);

        // Create EventBridge entry
        PutEventsRequestEntry entry = PutEventsRequestEntry.builder()
                .eventBusName(eventBusName)
                .source("com.platform.saas")
                .detailType(eventType)
                .detail(detailJson)
                .time(Instant.now())
                .build();

        // Publish to EventBridge
        PutEventsRequest request = PutEventsRequest.builder()
                .entries(entry)
                .build();

        PutEventsResponse response = eventBridgeClient.putEvents(request);

        if (response.failedEntryCount() > 0) {
            throw new RuntimeException("Failed to publish event to EventBridge: " +
                    response.entries().get(0).errorMessage());
        }
    }

    /**
     * Build event detail JSON for EventBridge.
     */
    private String buildEventDetailJson(
            UUID tenantId,
            UUID resourceId,
            String resourceType,
            Map<String, Object> eventPayload) {

        // Simple JSON construction - in production, use Jackson ObjectMapper
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"tenantId\":\"").append(tenantId).append("\",");
        json.append("\"resourceId\":\"").append(resourceId).append("\",");
        json.append("\"resourceType\":\"").append(resourceType).append("\",");
        json.append("\"timestamp\":\"").append(Instant.now()).append("\",");
        json.append("\"payload\":").append(convertMapToJson(eventPayload));
        json.append("}");

        return json.toString();
    }

    /**
     * Convert map to JSON string (simple implementation).
     * In production, use Jackson ObjectMapper.
     */
    private String convertMapToJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return "{}";
        }

        StringBuilder json = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) json.append(",");
            json.append("\"").append(entry.getKey()).append("\":");

            Object value = entry.getValue();
            if (value instanceof String) {
                json.append("\"").append(value).append("\"");
            } else if (value instanceof Number || value instanceof Boolean) {
                json.append(value);
            } else {
                json.append("\"").append(value != null ? value.toString() : "null").append("\"");
            }

            first = false;
        }
        json.append("}");

        return json.toString();
    }

    /**
     * Extract stack trace as string.
     */
    private String getStackTrace(Exception e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getClass().getName()).append(": ").append(e.getMessage()).append("\n");

        for (StackTraceElement element : e.getStackTrace()) {
            sb.append("\tat ").append(element.toString()).append("\n");
            if (sb.length() > 2000) { // Limit stack trace length
                sb.append("\t... (truncated)");
                break;
            }
        }

        return sb.toString();
    }
}
