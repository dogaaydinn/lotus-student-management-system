package com.lotus.lotusSPM.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * Command to create a new student in the system.
 * Part of CQRS pattern - represents intent to create a student.
 *
 * Enterprise Pattern: Command Query Responsibility Segregation (CQRS)
 * This separates write operations (commands) from read operations (queries)
 * for better scalability and performance.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateStudentCommand {

    @TargetAggregateIdentifier
    private String studentId;

    @NotBlank(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Surname is required")
    private String surname;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    private String email;

    private String faculty;
    private String department;
    private String internshipStatus;
}
