package com.lotus.lotusSPM.cqrs.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a student is created.
 * Enterprise Pattern: Event Sourcing - Capture state changes as events.
 *
 * Events are immutable and represent facts that happened in the past.
 * They form an audit trail and enable event replay for disaster recovery.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentCreatedEvent {
    private String studentId;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String faculty;
    private String department;
    private String internshipStatus;
    private Instant timestamp;
}
