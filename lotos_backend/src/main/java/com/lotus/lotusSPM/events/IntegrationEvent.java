package com.lotus.lotusSPM.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for Integration Events.
 *
 * Enterprise Pattern: Event-Driven Architecture - Integration Events
 *
 * Integration events enable communication between bounded contexts
 * or microservices without tight coupling.
 *
 * Use cases:
 * - Service-to-service communication
 * - Data synchronization
 * - Workflow orchestration
 * - System integration
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class IntegrationEvent {

    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    private String serviceName; // Source service
    private String targetService; // Target service (optional)
    private Instant occurredOn = Instant.now();
    private int version = 1;

    public IntegrationEvent(String eventType, String serviceName) {
        this.eventType = eventType;
        this.serviceName = serviceName;
    }
}
