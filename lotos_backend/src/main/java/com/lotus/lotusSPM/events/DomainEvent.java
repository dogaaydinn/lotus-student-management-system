package com.lotus.lotusSPM.events;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for Domain Events.
 *
 * Enterprise Pattern: Domain-Driven Design (DDD) - Domain Events
 *
 * Domain events represent something that happened in the domain
 * that domain experts care about.
 *
 * Characteristics:
 * - Immutable
 * - Named in past tense (StudentCreated, OrderPlaced)
 * - Contain all necessary information
 * - Have unique identifier
 * - Timestamped
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class DomainEvent {

    private String eventId = UUID.randomUUID().toString();
    private String aggregateId;
    private String eventType;
    private Instant occurredOn = Instant.now();
    private String userId; // Who triggered the event
    private int version = 1; // Event schema version

    public DomainEvent(String aggregateId, String eventType, String userId) {
        this.aggregateId = aggregateId;
        this.eventType = eventType;
        this.userId = userId;
    }
}
