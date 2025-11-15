package com.lotus.lotusSPM.dao.custom;

import com.lotus.lotusSPM.dto.StudentSummaryDTO;
import com.lotus.lotusSPM.model.Student;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * Custom repository for optimized Student queries.
 * Prevents N+1 queries with JOIN FETCH and uses DTO projections.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
public interface CustomStudentRepository {

    /**
     * Find student by ID with all associations loaded (prevents N+1)
     * Use JOIN FETCH to load applicationForms in single query
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.applicationForms " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithApplications(@Param("id") Long id);

    /**
     * Find student by ID with documents loaded
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.documents " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithDocuments(@Param("id") Long id);

    /**
     * Find student with all associations (use carefully - can be expensive)
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "LEFT JOIN FETCH s.applicationForms " +
           "LEFT JOIN FETCH s.documents " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithAllAssociations(@Param("id") Long id);

    /**
     * Find active students by faculty and department
     * Uses composite index: idx_student_faculty_dept
     */
    @Query("SELECT s FROM Student s " +
           "WHERE s.faculty = :faculty " +
           "AND s.department = :department " +
           "AND s.active = true " +
           "ORDER BY s.username")
    Page<Student> findActiveByFacultyAndDepartment(
            @Param("faculty") String faculty,
            @Param("department") String department,
            Pageable pageable);

    /**
     * Full-text search across student fields
     * Uses full-text index: idx_student_fulltext
     */
    @Query(value = "SELECT s.* FROM students s " +
                   "WHERE MATCH(s.username, s.email, s.faculty, s.department) " +
                   "AGAINST (:searchTerm IN NATURAL LANGUAGE MODE) " +
                   "AND s.active = true",
           nativeQuery = true)
    List<Student> fullTextSearch(@Param("searchTerm") String searchTerm);

    /**
     * Find students with DTO projection for better performance
     * Only selects needed fields instead of full entity
     */
    @Query("SELECT new com.lotus.lotusSPM.dto.StudentSummaryDTO(" +
           "s.id, s.username, s.email, s.faculty, s.department, s.active) " +
           "FROM Student s " +
           "WHERE s.active = true")
    Page<StudentSummaryDTO> findAllSummaries(Pageable pageable);

    /**
     * Find students by faculty with DTO projection
     */
    @Query("SELECT new com.lotus.lotusSPM.dto.StudentSummaryDTO(" +
           "s.id, s.username, s.email, s.faculty, s.department, s.active) " +
           "FROM Student s " +
           "WHERE s.faculty = :faculty AND s.active = true")
    Page<StudentSummaryDTO> findByFacultySummaries(
            @Param("faculty") String faculty,
            Pageable pageable);

    /**
     * Count active students by faculty
     * Uses index: idx_student_faculty_active
     */
    @Query("SELECT COUNT(s) FROM Student s " +
           "WHERE s.faculty = :faculty AND s.active = true")
    long countActiveByFaculty(@Param("faculty") String faculty);

    /**
     * Find students with pending applications
     * Uses JOIN to avoid N+1 queries
     */
    @Query("SELECT DISTINCT s FROM Student s " +
           "JOIN s.applicationForms af " +
           "WHERE af.status = 'PENDING' " +
           "ORDER BY s.username")
    List<Student> findStudentsWithPendingApplications();

    /**
     * Find students without any applications
     */
    @Query("SELECT s FROM Student s " +
           "WHERE s.id NOT IN (SELECT af.student.id FROM ApplicationForm af) " +
           "AND s.active = true")
    Page<Student> findStudentsWithoutApplications(Pageable pageable);

    /**
     * Get student count by department
     * Optimized aggregation query
     */
    @Query("SELECT s.department, COUNT(s) FROM Student s " +
           "WHERE s.active = true " +
           "GROUP BY s.department " +
           "ORDER BY COUNT(s) DESC")
    List<Object[]> getStudentCountByDepartment();
}
