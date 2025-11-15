package com.lotus.lotusSPM.dao.custom;

import com.lotus.lotusSPM.model.Opportunities;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Custom repository for optimized Opportunities queries.
 * Prevents N+1 queries and uses efficient indexing.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
public interface CustomOpportunitiesRepository {

    /**
     * Find opportunity by ID with all associations (prevents N+1)
     */
    @Query("SELECT DISTINCT o FROM Opportunities o " +
           "LEFT JOIN FETCH o.applications " +
           "WHERE o.id = :id")
    Optional<Opportunities> findByIdWithApplications(@Param("id") Long id);

    /**
     * Find all active opportunities before deadline
     * Uses composite index: idx_opportunities_active_deadline
     */
    @Query("SELECT o FROM Opportunities o " +
           "WHERE o.status = 'ACTIVE' " +
           "AND o.deadline > :currentDate " +
           "ORDER BY o.deadline ASC")
    Page<Opportunities> findActiveOpportunities(
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable);

    /**
     * Find opportunities by company with status filter
     * Uses composite index: idx_opportunities_company_status
     */
    @Query("SELECT o FROM Opportunities o " +
           "WHERE o.companyName = :companyName " +
           "AND o.status = :status " +
           "ORDER BY o.createdAt DESC")
    List<Opportunities> findByCompanyAndStatus(
            @Param("companyName") String companyName,
            @Param("status") String status);

    /**
     * Full-text search for opportunities
     * Uses full-text index: idx_opportunities_fulltext
     */
    @Query(value = "SELECT o.* FROM opportunities o " +
                   "WHERE MATCH(o.title, o.description, o.company_name) " +
                   "AGAINST (:searchTerm IN NATURAL LANGUAGE MODE) " +
                   "AND o.status = 'ACTIVE' " +
                   "AND o.deadline > CURDATE()",
           nativeQuery = true)
    List<Opportunities> fullTextSearch(@Param("searchTerm") String searchTerm);

    /**
     * Find opportunities by location
     * Uses index: idx_opportunities_location
     */
    @Query("SELECT o FROM Opportunities o " +
           "WHERE o.location LIKE %:location% " +
           "AND o.status = 'ACTIVE' " +
           "AND o.deadline > :currentDate")
    Page<Opportunities> findByLocation(
            @Param("location") String location,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable);

    /**
     * Find expiring opportunities (deadline within next 7 days)
     */
    @Query("SELECT o FROM Opportunities o " +
           "WHERE o.status = 'ACTIVE' " +
           "AND o.deadline BETWEEN :startDate AND :endDate " +
           "ORDER BY o.deadline ASC")
    List<Opportunities> findExpiringOpportunities(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    /**
     * Get opportunity count by status
     */
    @Query("SELECT o.status, COUNT(o) FROM Opportunities o " +
           "GROUP BY o.status")
    List<Object[]> getCountByStatus();

    /**
     * Find top companies by opportunity count
     */
    @Query("SELECT o.companyName, COUNT(o) FROM Opportunities o " +
           "WHERE o.status = 'ACTIVE' " +
           "GROUP BY o.companyName " +
           "ORDER BY COUNT(o) DESC")
    List<Object[]> getTopCompaniesByOpportunityCount(Pageable pageable);

    /**
     * Find opportunities without any applications
     */
    @Query("SELECT o FROM Opportunities o " +
           "WHERE o.id NOT IN (SELECT af.opportunity.id FROM ApplicationForm af) " +
           "AND o.status = 'ACTIVE' " +
           "AND o.deadline > :currentDate")
    Page<Opportunities> findOpportunitiesWithoutApplications(
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable);

    /**
     * Find opportunities with high application count
     */
    @Query("SELECT o FROM Opportunities o " +
           "WHERE SIZE(o.applications) >= :minApplications " +
           "AND o.status = 'ACTIVE' " +
           "ORDER BY SIZE(o.applications) DESC")
    List<Opportunities> findPopularOpportunities(@Param("minApplications") int minApplications);
}
