package com.lotus.lotusSPM.cqrs.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Event published when a student is deleted.
 * Enterprise Pattern: Event Sourcing - Track deletions for audit trail.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class StudentDeletedEvent {
    private String studentId;
    private Instant timestamp;
}
