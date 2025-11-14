package com.lotus.lotusSPM.graphql;

import com.coxautodev.graphql.tools.GraphQLQueryResolver;
import com.coxautodev.graphql.tools.GraphQLMutationResolver;
import com.lotus.lotusSPM.model.Student;
import com.lotus.lotusSPM.service.base.StudentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Resolver for Student operations.
 *
 * Enterprise Pattern: GraphQL API
 *
 * Benefits over REST:
 * - Client specifies exact data needed (no over-fetching)
 * - Single request for multiple resources (no under-fetching)
 * - Strongly typed schema
 * - Real-time subscriptions
 * - API evolution without versioning
 * - Introspection and documentation
 *
 * This resolver provides:
 * - Queries: Read operations
 * - Mutations: Write operations
 * - Field resolvers: Nested data fetching
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class StudentGraphQLResolver implements GraphQLQueryResolver, GraphQLMutationResolver {

    private final StudentService studentService;

    // ==================== QUERIES ====================

    /**
     * Query: Get student by ID
     * GraphQL: student(id: "123")
     */
    public Student student(String id) {
        log.debug("GraphQL Query: student(id: {})", id);
        return studentService.getStudentById(Long.parseLong(id));
    }

    /**
     * Query: Get student by username
     * GraphQL: studentByUsername(username: "john.doe")
     */
    public Student studentByUsername(String username) {
        log.debug("GraphQL Query: studentByUsername(username: {})", username);
        return studentService.getStudentByUsername(username);
    }

    /**
     * Query: Get paginated and filtered students
     * GraphQL: students(filter: {faculty: "Engineering"}, pagination: {page: 0, size: 20})
     */
    public Map<String, Object> students(
            Map<String, String> filter,
            Map<String, Object> pagination) {

        log.debug("GraphQL Query: students(filter: {}, pagination: {})", filter, pagination);

        // Parse pagination
        int page = pagination != null ? (int) pagination.getOrDefault("page", 0) : 0;
        int size = pagination != null ? (int) pagination.getOrDefault("size", 20) : 20;
        String sortField = pagination != null ? (String) pagination.getOrDefault("sort", "id") : "id";
        String direction = pagination != null ? (String) pagination.getOrDefault("direction", "ASC") : "ASC";

        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(direction)
            ? Sort.Direction.DESC
            : Sort.Direction.ASC;

        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortField));

        // Apply filters
        List<Student> students;
        if (filter != null && filter.containsKey("faculty")) {
            students = studentService.getStudentsByFaculty(filter.get("faculty"));
        } else {
            students = studentService.getAllStudents();
        }

        // Create response with pagination info
        Map<String, Object> response = new HashMap<>();
        response.put("content", students);

        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("totalElements", students.size());
        pageInfo.put("totalPages", (students.size() + size - 1) / size);
        pageInfo.put("currentPage", page);
        pageInfo.put("pageSize", size);
        pageInfo.put("hasNext", (page + 1) * size < students.size());
        pageInfo.put("hasPrevious", page > 0);

        response.put("pageInfo", pageInfo);

        return response;
    }

    // ==================== MUTATIONS ====================

    /**
     * Mutation: Create new student
     * GraphQL: createStudent(input: {username: "john", password: "pass", ...})
     */
    public Student createStudent(Map<String, Object> input) {
        log.info("GraphQL Mutation: createStudent(input: {})", input);

        Student student = new Student();
        student.setUsername((String) input.get("username"));
        student.setPassword((String) input.get("password"));
        student.setName((String) input.get("name"));
        student.setSurname((String) input.get("surname"));
        student.setEmail((String) input.get("email"));
        student.setFaculty((String) input.get("faculty"));
        student.setDepartment((String) input.get("department"));
        student.setInternshipStatus((String) input.get("internshipStatus"));

        return studentService.saveStudent(student);
    }

    /**
     * Mutation: Update existing student
     * GraphQL: updateStudent(id: "123", input: {name: "John Updated"})
     */
    public Student updateStudent(String id, Map<String, Object> input) {
        log.info("GraphQL Mutation: updateStudent(id: {}, input: {})", id, input);

        Student student = studentService.getStudentById(Long.parseLong(id));

        if (input.containsKey("name")) {
            student.setName((String) input.get("name"));
        }
        if (input.containsKey("surname")) {
            student.setSurname((String) input.get("surname"));
        }
        if (input.containsKey("email")) {
            student.setEmail((String) input.get("email"));
        }
        if (input.containsKey("faculty")) {
            student.setFaculty((String) input.get("faculty"));
        }
        if (input.containsKey("department")) {
            student.setDepartment((String) input.get("department"));
        }
        if (input.containsKey("internshipStatus")) {
            student.setInternshipStatus((String) input.get("internshipStatus"));
        }

        return studentService.updateStudent(student);
    }

    /**
     * Mutation: Delete student
     * GraphQL: deleteStudent(id: "123")
     */
    public boolean deleteStudent(String id) {
        log.info("GraphQL Mutation: deleteStudent(id: {})", id);
        studentService.deleteStudent(Long.parseLong(id));
        return true;
    }
}
