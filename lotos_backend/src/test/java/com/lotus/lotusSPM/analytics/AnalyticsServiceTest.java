package com.lotus.lotusSPM.analytics;

import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnalyticsServiceTest {

    @Mock
    private StudentDao studentDao;

    @InjectMocks
    private AnalyticsService analyticsService;

    private List<Student> testStudents;

    @BeforeEach
    void setUp() {
        Student student1 = new Student();
        student1.setId(1L);
        student1.setUsername("student1");
        student1.setFaculty("Engineering");
        student1.setDepartment("Computer Science");

        Student student2 = new Student();
        student2.setId(2L);
        student2.setUsername("student2");
        student2.setFaculty("Engineering");
        student2.setDepartment("Electrical Engineering");

        Student student3 = new Student();
        student3.setId(3L);
        student3.setUsername("student3");
        student3.setFaculty("Business");
        student3.setDepartment("Management");

        testStudents = Arrays.asList(student1, student2, student3);
    }

    @Test
    void testGetEnrollmentStatistics() {
        // Given
        when(studentDao.findAll()).thenReturn(testStudents);

        // When
        Map<String, Object> result = analyticsService.getEnrollmentStatistics();

        // Then
        assertNotNull(result);
        assertTrue(result.containsKey("totalStudents"));
        assertTrue(result.containsKey("byFaculty"));
        assertTrue(result.containsKey("byDepartment"));
        assertEquals(3, result.get("totalStudents"));
        verify(studentDao, times(1)).findAll();
    }

    @Test
    void testGetEnrollmentStatistics_EmptyList() {
        // Given
        when(studentDao.findAll()).thenReturn(Arrays.asList());

        // When
        Map<String, Object> result = analyticsService.getEnrollmentStatistics();

        // Then
        assertNotNull(result);
        assertEquals(0, result.get("totalStudents"));
    }


    @Test
    void testPredictStudentSuccess() {
        // Given
        Long studentId = 1L;

        // When
        Map<String, Object> prediction = analyticsService.predictStudentSuccess(studentId);

        // Then
        assertNotNull(prediction);
        assertTrue(prediction.containsKey("studentId"));
        assertTrue(prediction.containsKey("successProbability"));
        assertTrue(prediction.containsKey("riskLevel"));
        assertTrue(prediction.containsKey("recommendations"));
        assertEquals(studentId, prediction.get("studentId"));
    }

    @Test
    void testPredictStudentSuccess_ValidId() {
        // Given
        Long studentId = 2L;

        // When
        Map<String, Object> prediction = analyticsService.predictStudentSuccess(studentId);

        // Then
        assertNotNull(prediction);
        assertTrue(prediction.containsKey("successProbability"));
    }

    @Test
    void testGetEnrollmentTrend() {
        // When
        List<Map<String, Object>> trends = analyticsService.getEnrollmentTrend(6);

        // Then
        assertNotNull(trends);
    }

    @Test
    void testGetPlacementStatistics() {
        // When
        Map<String, Object> placementData = analyticsService.getPlacementStatistics();

        // Then
        assertNotNull(placementData);
        assertTrue(placementData.containsKey("totalStudents"));
        assertTrue(placementData.containsKey("placedStudents"));
        assertTrue(placementData.containsKey("placementRate"));
    }
}
