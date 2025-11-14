package com.lotus.lotusSPM.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;

/**
 * Query to find all students with pagination support.
 * Enterprise Pattern: CQRS Query with pagination for performance.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindAllStudentsQuery {
    private Pageable pageable;
    private String faculty;
    private String department;
    private String internshipStatus;
}
