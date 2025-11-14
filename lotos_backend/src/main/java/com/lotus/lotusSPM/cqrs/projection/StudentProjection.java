package com.lotus.lotusSPM.cqrs.projection;

import com.lotus.lotusSPM.cqrs.event.StudentCreatedEvent;
import com.lotus.lotusSPM.cqrs.event.StudentDeletedEvent;
import com.lotus.lotusSPM.cqrs.event.StudentUpdatedEvent;
import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;

/**
 * Student Projection - Materialized View for Query Side.
 *
 * Enterprise Pattern: CQRS Projection / Read Model
 *
 * Projections listen to events and update read-optimized data stores.
 * This enables:
 * - Denormalized views for fast queries
 * - Multiple specialized views for different use cases
 * - Read/Write scaling independence
 * - Event replay for rebuilding projections
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudentProjection {

    private final StudentDao studentDao;

    /**
     * Updates read model when StudentCreatedEvent is published.
     */
    @EventHandler
    public void on(StudentCreatedEvent event) {
        log.info("Projecting StudentCreatedEvent for ID: {}", event.getStudentId());

        Student student = new Student();
        student.setId(Long.parseLong(event.getStudentId()));
        student.setUsername(event.getUsername());
        student.setName(event.getName());
        student.setSurname(event.getSurname());
        student.setEmail(event.getEmail());
        student.setFaculty(event.getFaculty());
        student.setDepartment(event.getDepartment());
        student.setInternshipStatus(event.getInternshipStatus());

        studentDao.save(student);
    }

    /**
     * Updates read model when StudentUpdatedEvent is published.
     */
    @EventHandler
    public void on(StudentUpdatedEvent event) {
        log.info("Projecting StudentUpdatedEvent for ID: {}", event.getStudentId());

        studentDao.findById(Long.parseLong(event.getStudentId())).ifPresent(student -> {
            student.setName(event.getName());
            student.setSurname(event.getSurname());
            student.setEmail(event.getEmail());
            student.setFaculty(event.getFaculty());
            student.setDepartment(event.getDepartment());
            student.setInternshipStatus(event.getInternshipStatus());
            studentDao.save(student);
        });
    }

    /**
     * Updates read model when StudentDeletedEvent is published.
     */
    @EventHandler
    public void on(StudentDeletedEvent event) {
        log.info("Projecting StudentDeletedEvent for ID: {}", event.getStudentId());

        studentDao.deleteById(Long.parseLong(event.getStudentId()));
    }
}
