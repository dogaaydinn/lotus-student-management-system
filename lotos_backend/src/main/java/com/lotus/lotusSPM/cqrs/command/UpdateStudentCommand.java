package com.lotus.lotusSPM.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

import javax.validation.constraints.Email;

/**
 * Command to update existing student information.
 * Enterprise Pattern: CQRS Command for state modification.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateStudentCommand {

    @TargetAggregateIdentifier
    private String studentId;

    private String name;
    private String surname;

    @Email(message = "Email must be valid")
    private String email;

    private String faculty;
    private String department;
    private String internshipStatus;
}
