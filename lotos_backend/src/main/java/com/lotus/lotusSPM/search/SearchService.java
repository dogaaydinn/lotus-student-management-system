package com.lotus.lotusSPM.search;

import com.lotus.lotusSPM.model.Opportunity;
import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.search.document.OpportunityDocument;
import com.lotus.lotusSPM.search.document.StudentDocument;
import com.lotus.lotusSPM.search.repository.OpportunitySearchRepository;
import com.lotus.lotusSPM.search.repository.StudentSearchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Criteria;
import org.springframework.data.elasticsearch.core.query.CriteriaQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Search Service for advanced search operations.
 *
 * Enterprise Pattern: Search Service Layer
 *
 * Provides:
 * - Indexing operations
 * - Search operations
 * - Auto-complete
 * - Faceted search
 * - Real-time indexing
 * - Bulk indexing
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final StudentSearchRepository studentSearchRepository;
    private final OpportunitySearchRepository opportunitySearchRepository;
    private final ElasticsearchOperations elasticsearchOperations;

    // ==================== STUDENT SEARCH ====================

    /**
     * Index a student for search.
     */
    public void indexStudent(Student student) {
        StudentDocument doc = convertToStudentDocument(student);
        studentSearchRepository.save(doc);
        log.info("Indexed student: {}", student.getUsername());
    }

    /**
     * Bulk index students.
     */
    public void bulkIndexStudents(List<Student> students) {
        List<StudentDocument> documents = students.stream()
            .map(this::convertToStudentDocument)
            .collect(Collectors.toList());

        studentSearchRepository.saveAll(documents);
        log.info("Bulk indexed {} students", students.size());
    }

    /**
     * Search students with multi-field query.
     */
    public Page<StudentDocument> searchStudents(String query, Pageable pageable) {
        return studentSearchRepository.searchAcrossFields(query, pageable);
    }

    /**
     * Auto-complete for student names.
     */
    public List<StudentDocument> autocompleteStudents(String prefix) {
        return studentSearchRepository.findBySuggestContaining(prefix);
    }

    /**
     * Filter students by faculty and department.
     */
    public Page<StudentDocument> filterStudents(String faculty, String department, Pageable pageable) {
        CriteriaQuery query = new CriteriaQuery(
            new Criteria("faculty").is(faculty)
                .and("department").is(department)
        );

        SearchHits<StudentDocument> searchHits = elasticsearchOperations.search(
            query, StudentDocument.class
        );

        return studentSearchRepository.findByFaculty(faculty, pageable);
    }

    // ==================== OPPORTUNITY SEARCH ====================

    /**
     * Index an opportunity for search.
     */
    public void indexOpportunity(Opportunity opportunity) {
        OpportunityDocument doc = convertToOpportunityDocument(opportunity);
        opportunitySearchRepository.save(doc);
        log.info("Indexed opportunity: {}", opportunity.getTitle());
    }

    /**
     * Bulk index opportunities.
     */
    public void bulkIndexOpportunities(List<Opportunity> opportunities) {
        List<OpportunityDocument> documents = opportunities.stream()
            .map(this::convertToOpportunityDocument)
            .collect(Collectors.toList());

        opportunitySearchRepository.saveAll(documents);
        log.info("Bulk indexed {} opportunities", opportunities.size());
    }

    /**
     * Search opportunities with relevance scoring.
     */
    public Page<OpportunityDocument> searchOpportunities(String query, Pageable pageable) {
        return opportunitySearchRepository.searchOpportunities(query, pageable);
    }

    /**
     * Search opportunities by skills.
     */
    public Page<OpportunityDocument> searchBySkills(String[] skills, Pageable pageable) {
        return opportunitySearchRepository.findBySkills(skills, pageable);
    }

    /**
     * Auto-complete for opportunity titles.
     */
    public List<OpportunityDocument> autocompleteOpportunities(String prefix) {
        return opportunitySearchRepository.findBySuggestContaining(prefix);
    }

    /**
     * Get popular opportunities.
     */
    public Page<OpportunityDocument> getPopularOpportunities(Pageable pageable) {
        return opportunitySearchRepository.findByOrderByApplicationsCountDesc(pageable);
    }

    // ==================== HELPER METHODS ====================

    private StudentDocument convertToStudentDocument(Student student) {
        StudentDocument doc = new StudentDocument();
        doc.setId(String.valueOf(student.getId()));
        doc.setUsername(student.getUsername());
        doc.setName(student.getName());
        doc.setSurname(student.getSurname());
        doc.setEmail(student.getEmail());
        doc.setFaculty(student.getFaculty());
        doc.setDepartment(student.getDepartment());
        doc.setInternshipStatus(student.getInternshipStatus());
        doc.setFullName(student.getName() + " " + student.getSurname());
        doc.setSuggest(student.getName() + " " + student.getSurname());
        return doc;
    }

    private OpportunityDocument convertToOpportunityDocument(Opportunity opportunity) {
        OpportunityDocument doc = new OpportunityDocument();
        doc.setId(String.valueOf(opportunity.getId()));
        doc.setTitle(opportunity.getTitle());
        doc.setDescription(opportunity.getDescription());
        doc.setCompany(opportunity.getCompany());
        doc.setLocation(opportunity.getLocation());
        doc.setRequirements(opportunity.getRequirements());
        doc.setSalary(opportunity.getSalary());
        doc.setSuggest(opportunity.getTitle() + " " + opportunity.getCompany());

        // Extract skills from requirements (simple split)
        if (opportunity.getRequirements() != null) {
            String[] skills = opportunity.getRequirements()
                .split("[,;\\n]");
            doc.setSkills(skills);
        }

        return doc;
    }
}
