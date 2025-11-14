package com.lotus.lotusSPM.search.repository;

import com.lotus.lotusSPM.search.document.StudentDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Elasticsearch Repository for Student search operations.
 *
 * Enterprise Pattern: Repository Pattern for Search
 *
 * Provides:
 * - Full-text search queries
 * - Custom query methods
 * - Aggregations
 * - Suggestions
 * - Fuzzy matching
 */
@Repository
public interface StudentSearchRepository extends ElasticsearchRepository<StudentDocument, String> {

    /**
     * Search students by name (full-text search).
     */
    Page<StudentDocument> findByNameContainingOrSurnameContaining(
        String name, String surname, Pageable pageable);

    /**
     * Search students by faculty.
     */
    Page<StudentDocument> findByFaculty(String faculty, Pageable pageable);

    /**
     * Search students by department.
     */
    Page<StudentDocument> findByDepartment(String department, Pageable pageable);

    /**
     * Search students by internship status.
     */
    Page<StudentDocument> findByInternshipStatus(String status, Pageable pageable);

    /**
     * Custom multi-field search query.
     * Searches across name, surname, email, faculty, department.
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"name^2\", \"surname^2\", \"email\", \"faculty\", \"department\"], \"fuzziness\": \"AUTO\"}}")
    Page<StudentDocument> searchAcrossFields(String query, Pageable pageable);

    /**
     * Find students by email domain.
     */
    List<StudentDocument> findByEmailContaining(String domain);

    /**
     * Auto-complete suggestions.
     */
    List<StudentDocument> findBySuggestContaining(String prefix);
}
