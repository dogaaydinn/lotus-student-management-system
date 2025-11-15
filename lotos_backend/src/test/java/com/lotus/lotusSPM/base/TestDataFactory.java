package com.lotus.lotusSPM.base;

import com.lotus.lotusSPM.model.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Factory class for creating test data objects.
 * Provides consistent test data across all tests.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
public class TestDataFactory {

    /**
     * Create a test student with default values
     */
    public static Student createTestStudent() {
        Student student = new Student();
        student.setUsername("test.student");
        student.setPassword("$2a$12$encoded.password.hash");
        student.setEmail("test.student@lotus.edu");
        student.setFaculty("Engineering");
        student.setDepartment("Computer Science");
        student.setActive(true);
        student.setCreatedAt(LocalDateTime.now());
        student.setUpdatedAt(LocalDateTime.now());
        return student;
    }

    /**
     * Create a test student with custom username
     */
    public static Student createTestStudent(String username) {
        Student student = createTestStudent();
        student.setUsername(username);
        student.setEmail(username + "@lotus.edu");
        return student;
    }

    /**
     * Create a test coordinator
     */
    public static Coordinator createTestCoordinator() {
        Coordinator coordinator = new Coordinator();
        coordinator.setUsername("test.coordinator");
        coordinator.setPassword("$2a$12$encoded.password.hash");
        coordinator.setEmail("test.coordinator@lotus.edu");
        coordinator.setFaculty("Engineering");
        coordinator.setDepartment("Computer Science");
        coordinator.setCreatedAt(LocalDateTime.now());
        coordinator.setUpdatedAt(LocalDateTime.now());
        return coordinator;
    }

    /**
     * Create a test admin
     */
    public static Admin createTestAdmin() {
        Admin admin = new Admin();
        admin.setUsername("test.admin");
        admin.setPassword("$2a$12$encoded.password.hash");
        admin.setEmail("test.admin@lotus.edu");
        admin.setCreatedAt(LocalDateTime.now());
        admin.setUpdatedAt(LocalDateTime.now());
        return admin;
    }

    /**
     * Create a test opportunity
     */
    public static Opportunities createTestOpportunity() {
        Opportunities opportunity = new Opportunities();
        opportunity.setTitle("Software Engineering Intern");
        opportunity.setDescription("Full-stack development internship");
        opportunity.setCompanyName("Tech Corp");
        opportunity.setLocation("San Francisco, CA");
        opportunity.setStatus("ACTIVE");
        opportunity.setDeadline(LocalDate.now().plusDays(30));
        opportunity.setCreatedAt(LocalDateTime.now());
        opportunity.setUpdatedAt(LocalDateTime.now());
        return opportunity;
    }

    /**
     * Create a test application form
     */
    public static ApplicationForm createTestApplication(Student student, Opportunities opportunity) {
        ApplicationForm application = new ApplicationForm();
        application.setStudent(student);
        application.setOpportunity(opportunity);
        application.setStatus("PENDING");
        application.setCoverLetter("I am very interested in this position...");
        application.setSubmittedAt(LocalDateTime.now());
        return application;
    }

    /**
     * Create a test message
     */
    public static Messages createTestMessage(Long senderId, Long receiverId) {
        Messages message = new Messages();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setSubject("Test Message");
        message.setContent("This is a test message content");
        message.setIsRead(false);
        message.setTimestamp(LocalDateTime.now());
        return message;
    }

    /**
     * Create a test document
     */
    public static Documents createTestDocument(Student student) {
        Documents document = new Documents();
        document.setStudent(student);
        document.setFileName("resume.pdf");
        document.setFileType("application/pdf");
        document.setFileSize(1024L);
        document.setDescription("Student resume");
        document.setUploadDate(LocalDateTime.now());
        // Note: In production, don't store actual file data in database
        document.setData(new byte[]{0x00, 0x01, 0x02});
        return document;
    }

    /**
     * Create a test notification
     */
    public static Notifications createTestNotification(Long userId) {
        Notifications notification = new Notifications();
        notification.setUserId(userId);
        notification.setMessage("Test notification message");
        notification.setType("INFO");
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }
}
