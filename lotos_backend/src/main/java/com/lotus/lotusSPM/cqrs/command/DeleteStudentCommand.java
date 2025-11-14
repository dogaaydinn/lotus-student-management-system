package com.lotus.lotusSPM.cqrs.command;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.axonframework.modelling.command.TargetAggregateIdentifier;

/**
 * Command to delete a student from the system.
 * Enterprise Pattern: CQRS Command for deletion operations.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteStudentCommand {

    @TargetAggregateIdentifier
    private String studentId;
}
