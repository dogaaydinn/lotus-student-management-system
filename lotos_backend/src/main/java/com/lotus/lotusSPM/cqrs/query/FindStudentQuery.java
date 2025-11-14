package com.lotus.lotusSPM.cqrs.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Query to find a student by ID.
 * Enterprise Pattern: CQRS Query for read operations.
 * Queries are optimized for reading and don't modify state.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FindStudentQuery {
    private String studentId;
}
