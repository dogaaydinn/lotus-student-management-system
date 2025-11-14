package com.lotus.lotusSPM.cqrs.aggregate;

import com.lotus.lotusSPM.cqrs.command.CreateStudentCommand;
import com.lotus.lotusSPM.cqrs.command.DeleteStudentCommand;
import com.lotus.lotusSPM.cqrs.command.UpdateStudentCommand;
import com.lotus.lotusSPM.cqrs.event.StudentCreatedEvent;
import com.lotus.lotusSPM.cqrs.event.StudentDeletedEvent;
import com.lotus.lotusSPM.cqrs.event.StudentUpdatedEvent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingHandler;
import org.axonframework.modelling.command.AggregateIdentifier;
import org.axonframework.modelling.command.AggregateLifecycle;
import org.axonframework.spring.stereotype.Aggregate;

import java.time.Instant;

/**
 * Student Aggregate - The root entity in CQRS/Event Sourcing pattern.
 *
 * Enterprise Patterns:
 * 1. Domain-Driven Design (DDD) - Aggregate Root
 * 2. Event Sourcing - State rebuilt from events
 * 3. CQRS - Separate command handling
 *
 * This aggregate ensures consistency boundaries and validates business rules
 * before publishing events. State is derived from event replay.
 */
@Aggregate
@Getter
@NoArgsConstructor
public class StudentAggregate {

    @AggregateIdentifier
    private String studentId;
    private String username;
    private String name;
    private String surname;
    private String email;
    private String faculty;
    private String department;
    private String internshipStatus;
    private boolean deleted;

    /**
     * Command Handler - Validates and publishes StudentCreatedEvent.
     */
    @CommandHandler
    public StudentAggregate(CreateStudentCommand command) {
        // Business validation
        if (command.getUsername() == null || command.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be empty");
        }
        if (command.getEmail() == null || !command.getEmail().contains("@")) {
            throw new IllegalArgumentException("Invalid email format");
        }

        // Publish event - Axon will call @EventSourcingHandler
        AggregateLifecycle.apply(new StudentCreatedEvent(
            command.getStudentId(),
            command.getUsername(),
            command.getName(),
            command.getSurname(),
            command.getEmail(),
            command.getFaculty(),
            command.getDepartment(),
            command.getInternshipStatus(),
            Instant.now()
        ));
    }

    /**
     * Command Handler - Updates student and publishes StudentUpdatedEvent.
     */
    @CommandHandler
    public void handle(UpdateStudentCommand command) {
        if (deleted) {
            throw new IllegalStateException("Cannot update deleted student");
        }

        AggregateLifecycle.apply(new StudentUpdatedEvent(
            command.getStudentId(),
            command.getName(),
            command.getSurname(),
            command.getEmail(),
            command.getFaculty(),
            command.getDepartment(),
            command.getInternshipStatus(),
            Instant.now()
        ));
    }

    /**
     * Command Handler - Soft deletes student and publishes StudentDeletedEvent.
     */
    @CommandHandler
    public void handle(DeleteStudentCommand command) {
        if (deleted) {
            throw new IllegalStateException("Student already deleted");
        }

        AggregateLifecycle.apply(new StudentDeletedEvent(
            command.getStudentId(),
            Instant.now()
        ));
    }

    /**
     * Event Sourcing Handler - Rebuilds state from StudentCreatedEvent.
     */
    @EventSourcingHandler
    public void on(StudentCreatedEvent event) {
        this.studentId = event.getStudentId();
        this.username = event.getUsername();
        this.name = event.getName();
        this.surname = event.getSurname();
        this.email = event.getEmail();
        this.faculty = event.getFaculty();
        this.department = event.getDepartment();
        this.internshipStatus = event.getInternshipStatus();
        this.deleted = false;
    }

    /**
     * Event Sourcing Handler - Updates state from StudentUpdatedEvent.
     */
    @EventSourcingHandler
    public void on(StudentUpdatedEvent event) {
        this.name = event.getName();
        this.surname = event.getSurname();
        this.email = event.getEmail();
        this.faculty = event.getFaculty();
        this.department = event.getDepartment();
        this.internshipStatus = event.getInternshipStatus();
    }

    /**
     * Event Sourcing Handler - Marks aggregate as deleted.
     */
    @EventSourcingHandler
    public void on(StudentDeletedEvent event) {
        this.deleted = true;
    }
}
