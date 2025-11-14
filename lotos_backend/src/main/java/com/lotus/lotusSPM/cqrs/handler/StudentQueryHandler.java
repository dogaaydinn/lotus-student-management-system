package com.lotus.lotusSPM.cqrs.handler;

import com.lotus.lotusSPM.cqrs.query.FindAllStudentsQuery;
import com.lotus.lotusSPM.cqrs.query.FindStudentQuery;
import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.Student;
import lombok.RequiredArgsConstructor;
import org.axonframework.queryhandling.QueryHandler;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import javax.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Query Handler for Student queries.
 * Enterprise Pattern: CQRS Query Side - Optimized for reading.
 *
 * Query handlers are read-only and can be scaled independently
 * from command handlers. They may read from materialized views
 * or read replicas for better performance.
 */
@Component
@RequiredArgsConstructor
public class StudentQueryHandler {

    private final StudentDao studentDao;

    /**
     * Handles FindStudentQuery - Returns a single student by ID.
     */
    @QueryHandler
    public Optional<Student> handle(FindStudentQuery query) {
        return studentDao.findById(Long.parseLong(query.getStudentId()));
    }

    /**
     * Handles FindAllStudentsQuery - Returns paginated filtered results.
     * Uses JPA Specifications for dynamic filtering.
     */
    @QueryHandler
    public Page<Student> handle(FindAllStudentsQuery query) {
        Specification<Student> spec = (root, criteriaQuery, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (query.getFaculty() != null && !query.getFaculty().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("faculty"), query.getFaculty()));
            }

            if (query.getDepartment() != null && !query.getDepartment().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("department"), query.getDepartment()));
            }

            if (query.getInternshipStatus() != null && !query.getInternshipStatus().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("internshipStatus"), query.getInternshipStatus()));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        if (query.getPageable() != null) {
            return studentDao.findAll(spec, query.getPageable());
        } else {
            List<Student> students = studentDao.findAll(spec);
            return new PageImpl<>(students);
        }
    }
}
