package com.lotus.lotusSPM.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Lightweight DTO for Student summaries.
 * Used in list views to avoid loading full entity with associations.
 * Improves performance by selecting only needed fields.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentSummaryDTO {

    private Long id;
    private String username;
    private String email;
    private String faculty;
    private String department;
    private Boolean active;

    /**
     * Create summary from full Student entity
     */
    public static StudentSummaryDTO from(com.lotus.lotusSPM.model.Student student) {
        return new StudentSummaryDTO(
            student.getId(),
            student.getUsername(),
            student.getEmail(),
            student.getFaculty(),
            student.getDepartment(),
            student.getActive()
        );
    }
}
