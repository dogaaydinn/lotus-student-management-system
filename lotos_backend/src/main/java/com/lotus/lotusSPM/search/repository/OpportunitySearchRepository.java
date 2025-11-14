package com.lotus.lotusSPM.search.repository;

import com.lotus.lotusSPM.search.document.OpportunityDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Elasticsearch Repository for Opportunity search operations.
 *
 * Enables sophisticated job/opportunity search with:
 * - Full-text search
 * - Faceted filtering
 * - Skills matching
 * - Location-based search
 * - Relevance scoring
 */
@Repository
public interface OpportunitySearchRepository extends ElasticsearchRepository<OpportunityDocument, String> {

    /**
     * Search opportunities by title or description.
     */
    Page<OpportunityDocument> findByTitleContainingOrDescriptionContaining(
        String title, String description, Pageable pageable);

    /**
     * Search opportunities by company.
     */
    Page<OpportunityDocument> findByCompany(String company, Pageable pageable);

    /**
     * Search opportunities by location.
     */
    Page<OpportunityDocument> findByLocation(String location, Pageable pageable);

    /**
     * Find opportunities with deadline after specified date.
     */
    Page<OpportunityDocument> findByDeadlineAfter(Instant date, Pageable pageable);

    /**
     * Multi-field search with relevance scoring.
     */
    @Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title^3\", \"company^2\", \"description\", \"requirements\"], \"fuzziness\": \"AUTO\"}}")
    Page<OpportunityDocument> searchOpportunities(String query, Pageable pageable);

    /**
     * Find opportunities by skills (array matching).
     */
    @Query("{\"terms\": {\"skills\": ?0}}")
    Page<OpportunityDocument> findBySkills(String[] skills, Pageable pageable);

    /**
     * Auto-complete for opportunity titles.
     */
    List<OpportunityDocument> findBySuggestContaining(String prefix);

    /**
     * Find popular opportunities (sorted by applications count).
     */
    Page<OpportunityDocument> findByOrderByApplicationsCountDesc(Pageable pageable);
}
