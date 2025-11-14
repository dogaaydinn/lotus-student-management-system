package com.lotus.lotusSPM.service;

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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentDao studentDao;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student testStudent;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("teststudent");
        testStudent.setPassword("hashedPassword");
        testStudent.setName("Test");
        testStudent.setSurname("Student");
        testStudent.setEmail("test@student.com");
        testStudent.setFaculty("Engineering");
        testStudent.setDepartment("Computer Science");
    }

    @Test
    void testGetAllStudents() {
        // Given
        List<Student> students = Arrays.asList(testStudent, new Student());
        when(studentDao.findAll()).thenReturn(students);

        // When
        List<Student> result = studentService.findStudents();

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(studentDao, times(1)).findAll();
    }

    @Test
    void testGetStudentById() {
        // Given
        when(studentDao.findById(1L)).thenReturn(Optional.of(testStudent));

        // When
        Student result = studentService.findStdById(1L);

        // Then
        assertNotNull(result);
        assertEquals("teststudent", result.getUsername());
        assertEquals("Test", result.getName());
        verify(studentDao, times(1)).findById(1L);
    }

    @Test
    void testGetStudentById_NotFound() {
        // Given
        when(studentDao.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(Exception.class, () -> {
            studentService.findStdById(999L);
        });
        verify(studentDao, times(1)).findById(999L);
    }

    @Test
    void testCreateStudent() {
        // Given
        Student newStudent = new Student();
        newStudent.setUsername("newstudent");
        newStudent.setPassword("hashedPassword");
        newStudent.setName("New");
        newStudent.setSurname("Student");
        newStudent.setEmail("new@student.com");

        when(studentDao.save(any(Student.class))).thenReturn(newStudent);

        // When
        Student result = studentService.createStudent(newStudent);

        // Then
        assertNotNull(result);
        verify(studentDao, times(1)).save(any(Student.class));
    }

    @Test
    void testDeleteStudent() {
        // Given
        when(studentDao.existsById(1L)).thenReturn(true);
        doNothing().when(studentDao).deleteById(1L);

        // When
        studentService.deleteStudent(1L);

        // Then
        verify(studentDao, times(1)).deleteById(1L);
    }

    @Test
    void testFindByUsername() {
        // Given
        when(studentDao.findByUsername("teststudent")).thenReturn(testStudent);

        // When
        Student result = studentService.findByUsername("teststudent");

        // Then
        assertNotNull(result);
        assertEquals("teststudent", result.getUsername());
        verify(studentDao, times(1)).findByUsername("teststudent");
    }

    @Test
    void testFindByUsername_NotFound() {
        // Given
        when(studentDao.findByUsername("nonexistent")).thenReturn(null);

        // When
        Student result = studentService.findByUsername("nonexistent");

        // Then
        assertNull(result);
        verify(studentDao, times(1)).findByUsername("nonexistent");
    }
}
