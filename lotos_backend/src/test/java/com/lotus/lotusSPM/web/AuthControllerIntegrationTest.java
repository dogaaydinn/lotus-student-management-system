package com.lotus.lotusSPM.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lotus.lotusSPM.LotusTestBase;
import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.dto.AuthRequest;
import com.lotus.lotusSPM.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthControllerIntegrationTest extends LotusTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private StudentDao studentDao;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        // Clean up
        studentDao.deleteAll();

        // Create test student
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("teststudent");
        testStudent.setPassword(passwordEncoder.encode("password123"));
        testStudent.setName("Test");
        testStudent.setSurname("Student");
        testStudent.setEmail("test@student.com");
        testStudent.setFaculty("Engineering");
        testStudent.setDepartment("Computer Science");
        studentDao.save(testStudent);
    }

    @Test
    void testLogin_Success() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("teststudent", "password123", "STUDENT");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.username").value("teststudent"))
                .andExpect(jsonPath("$.role").value("STUDENT"));
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("teststudent", "wrongpassword", "STUDENT");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testLogin_UserNotFound() throws Exception {
        // Given
        AuthRequest authRequest = new AuthRequest("nonexistent", "password123", "STUDENT");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isUnauthorized());
    }
}
