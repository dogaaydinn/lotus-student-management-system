package com.lotus.lotusSPM.cqrs.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a student is updated.
 * Enterprise Pattern: Event Sourcing - Track all state modifications.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentUpdatedEvent {
    private String studentId;
    private String name;
    private String surname;
    private String email;
    private String faculty;
    private String department;
    private String internshipStatus;
    private Instant timestamp;
}
