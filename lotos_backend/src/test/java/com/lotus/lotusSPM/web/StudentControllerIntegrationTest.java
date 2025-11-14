package com.lotus.lotusSPM.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for StudentController
 * Tests full API stack including pagination, caching, and error handling
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("StudentController Integration Tests")
class StudentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private ObjectMapper objectMapper;

    private Student testStudent1;
    private Student testStudent2;

    @BeforeEach
    void setUp() {
        // Clear database
        studentDao.deleteAll();

        testStudent1 = new Student();
        testStudent1.setUsername("john123");
        testStudent1.setName("John");
        testStudent1.setSurname("Doe");
        testStudent1.setEmail("john@example.com");
        testStudent1.setPassword("password123");

        testStudent2 = new Student();
        testStudent2.setUsername("jane456");
        testStudent2.setName("Jane");
        testStudent2.setSurname("Smith");
        testStudent2.setEmail("jane@example.com");
        testStudent2.setPassword("password456");

        studentDao.save(testStudent1);
        studentDao.save(testStudent2);
    }

    @Test
    @DisplayName("GET /students should return all students")
    void getStudents_ShouldReturnAllStudents() throws Exception {
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("john123")))
                .andExpect(jsonPath("$[1].username", is("jane456")));
    }

    @Test
    @DisplayName("GET /api/v1/students should return paginated results")
    void getStudentsPaginated_ShouldReturnPage() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .param("page", "0")
                        .param("size", "10")
                        .param("sortBy", "id")
                        .param("sortDir", "asc"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.number", is(0)))
                .andExpect(jsonPath("$.size", is(10)))
                .andExpect(jsonPath("$.content[0].username", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/v1/students with small page size should limit results")
    void getStudentsPaginated_WithSmallPageSize_ShouldLimitResults() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.totalElements", is(2)))
                .andExpect(jsonPath("$.totalPages", is(2)));
    }

    @Test
    @DisplayName("GET /api/v1/students should respect max page size of 100")
    void getStudentsPaginated_WithLargePageSize_ShouldCapAt100() throws Exception {
        // Create 150 students
        for (int i = 3; i <= 150; i++) {
            Student student = new Student();
            student.setUsername("user" + i);
            student.setName("User");
            student.setSurname("" + i);
            student.setEmail("user" + i + "@example.com");
            studentDao.save(student);
        }

        mockMvc.perform(get("/api/v1/students")
                        .param("page", "0")
                        .param("size", "200")) // Request 200, should get max 100
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size", lessThanOrEqualTo(100)));
    }

    @Test
    @DisplayName("GET /api/v1/students with descending sort should work")
    void getStudentsPaginated_WithDescendingSort_ShouldReturnSorted() throws Exception {
        mockMvc.perform(get("/api/v1/students")
                        .param("sortBy", "username")
                        .param("sortDir", "desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].username", is("john123")))
                .andExpect(jsonPath("$.content[1].username", is("jane456")));
    }

    @Test
    @DisplayName("GET /student/{username} should return student when found")
    void getStudentByUsername_WhenExists_ShouldReturnStudent() throws Exception {
        mockMvc.perform(get("/student/john123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("john123")))
                .andExpect(jsonPath("$.name", is("John")))
                .andExpect(jsonPath("$.surname", is("Doe")));
    }

    @Test
    @DisplayName("GET /student/{username} should return 404 when not found")
    void getStudentByUsername_WhenNotFound_ShouldReturn404() throws Exception {
        mockMvc.perform(get("/student/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /student should create new student")
    void createStudent_WithValidData_ShouldCreateAndReturn201() throws Exception {
        Student newStudent = new Student();
        newStudent.setUsername("newuser");
        newStudent.setName("New");
        newStudent.setSurname("User");
        newStudent.setEmail("new@example.com");
        newStudent.setPassword("password");

        mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"));
    }

    @Test
    @DisplayName("DELETE /student/{id} should delete student")
    void deleteStudent_WhenExists_ShouldReturn200() throws Exception {
        Long studentId = testStudent1.getId();

        mockMvc.perform(delete("/student/" + studentId))
                .andExpect(status().isOk());

        // Verify student was deleted
        mockMvc.perform(get("/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].username", is("jane456")));
    }

    @Test
    @DisplayName("POST /student/login should return 200 for valid credentials")
    void login_WithValidCredentials_ShouldReturn200() throws Exception {
        Student loginRequest = new Student();
        loginRequest.setUsername("john123");
        loginRequest.setPassword("password123");

        mockMvc.perform(post("/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /student/login should return 401 for invalid password")
    void login_WithInvalidPassword_ShouldReturn401() throws Exception {
        Student loginRequest = new Student();
        loginRequest.setUsername("john123");
        loginRequest.setPassword("wrongpassword");

        mockMvc.perform(post("/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("POST /student/login should return 401 for non-existent user")
    void login_WithNonExistentUser_ShouldReturn401() throws Exception {
        Student loginRequest = new Student();
        loginRequest.setUsername("nonexistent");
        loginRequest.setPassword("password");

        mockMvc.perform(post("/student/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/students with page 1 should return second page")
    void getStudentsPaginated_SecondPage_ShouldReturnCorrectPage() throws Exception {
        // Create more students for pagination
        for (int i = 3; i <= 25; i++) {
            Student student = new Student();
            student.setUsername("user" + i);
            student.setName("User");
            student.setSurname("" + i);
            student.setEmail("user" + i + "@example.com");
            studentDao.save(student);
        }

        mockMvc.perform(get("/api/v1/students")
                        .param("page", "1")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.number", is(1)))
                .andExpect(jsonPath("$.totalElements", is(25)));
    }

    @Test
    @DisplayName("Integration: Create, retrieve, and delete student workflow")
    void fullWorkflow_CreateRetrieveDelete_ShouldWorkEndToEnd() throws Exception {
        // 1. Create student
        Student newStudent = new Student();
        newStudent.setUsername("workflow");
        newStudent.setName("Workflow");
        newStudent.setSurname("Test");
        newStudent.setEmail("workflow@example.com");
        newStudent.setPassword("pass");

        String location = mockMvc.perform(post("/student")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newStudent)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // 2. Retrieve student
        mockMvc.perform(get("/student/workflow"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("workflow")));

        // 3. Verify in paginated list
        mockMvc.perform(get("/api/v1/students"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.username == 'workflow')]").exists());

        // 4. Delete student
        Long id = studentDao.findByUsername("workflow").getId();
        mockMvc.perform(delete("/student/" + id))
                .andExpect(status().isOk());

        // 5. Verify deletion
        mockMvc.perform(get("/student/workflow"))
                .andExpect(status().isNotFound());
    }
}
