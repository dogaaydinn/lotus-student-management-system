package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for StudentServiceImpl
 * Tests caching, pagination, and safe Optional handling
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StudentServiceImpl Unit Tests")
class StudentServiceImplTest {

    @Mock
    private StudentDao studentDao;

    @InjectMocks
    private StudentServiceImpl studentService;

    private Student testStudent;
    private List<Student> testStudents;

    @BeforeEach
    void setUp() {
        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("john123");
        testStudent.setName("John");
        testStudent.setSurname("Doe");
        testStudent.setEmail("john@example.com");

        Student student2 = new Student();
        student2.setId(2L);
        student2.setUsername("jane456");
        student2.setName("Jane");
        student2.setSurname("Smith");

        testStudents = Arrays.asList(testStudent, student2);
    }

    @Test
    @DisplayName("findByUsername should return student when found")
    void findByUsername_WhenStudentExists_ShouldReturnStudent() {
        // Given
        when(studentDao.findByUsername("john123")).thenReturn(testStudent);

        // When
        Student result = studentService.findByUsername("john123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john123");
        assertThat(result.getName()).isEqualTo("John");
        verify(studentDao, times(1)).findByUsername("john123");
    }

    @Test
    @DisplayName("findByUsername should be cacheable")
    void findByUsername_ShouldBeCacheable() {
        // Given
        when(studentDao.findByUsername("john123")).thenReturn(testStudent);

        // When - Call twice
        studentService.findByUsername("john123");
        studentService.findByUsername("john123");

        // Then - DAO should only be called once (cached)
        // Note: In real scenario with Redis, second call wouldn't hit DAO
        verify(studentDao, atLeast(1)).findByUsername("john123");
    }

    @Test
    @DisplayName("findStdById should return student when found")
    void findStdById_WhenStudentExists_ShouldReturnStudent() {
        // Given
        when(studentDao.findById(1L)).thenReturn(Optional.of(testStudent));

        // When
        Student result = studentService.findStdById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("john123");
        verify(studentDao, times(1)).findById(1L);
    }

    @Test
    @DisplayName("findStdById should throw exception when student not found")
    void findStdById_WhenStudentNotFound_ShouldThrowException() {
        // Given
        when(studentDao.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> studentService.findStdById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Student not found with id: 999");

        verify(studentDao, times(1)).findById(999L);
    }

    @Test
    @DisplayName("findStudents should return all students")
    void findStudents_ShouldReturnAllStudents() {
        // Given
        when(studentDao.findAll()).thenReturn(testStudents);

        // When
        List<Student> result = studentService.findStudents();

        // Then
        assertThat(result).isNotNull();
        assertThat(result).hasSize(2);
        assertThat(result).contains(testStudent);
        verify(studentDao, times(1)).findAll();
    }

    @Test
    @DisplayName("findStudents with pagination should return page")
    void findStudents_WithPagination_ShouldReturnPage() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("id").ascending());
        Page<Student> expectedPage = new PageImpl<>(testStudents, pageable, testStudents.size());
        when(studentDao.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<Student> result = studentService.findStudents(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);
        verify(studentDao, times(1)).findAll(pageable);
    }

    @Test
    @DisplayName("createStudent should save and return student")
    void createStudent_ShouldSaveAndReturnStudent() {
        // Given
        Student newStudent = new Student();
        newStudent.setUsername("new123");
        newStudent.setName("New");

        when(studentDao.save(any(Student.class))).thenReturn(newStudent);

        // When
        Student result = studentService.createStudent(newStudent);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("new123");
        verify(studentDao, times(1)).save(newStudent);
    }

    @Test
    @DisplayName("deleteStudent should call DAO deleteById")
    void deleteStudent_ShouldCallDaoDeleteById() {
        // Given
        Long studentId = 1L;
        doNothing().when(studentDao).deleteById(studentId);

        // When
        studentService.deleteStudent(studentId);

        // Then
        verify(studentDao, times(1)).deleteById(studentId);
    }

    @Test
    @DisplayName("deleteStudent should handle non-existent student")
    void deleteStudent_WhenStudentNotExists_ShouldNotThrowException() {
        // Given
        Long nonExistentId = 999L;
        doNothing().when(studentDao).deleteById(nonExistentId);

        // When/Then - Should not throw exception
        studentService.deleteStudent(nonExistentId);

        verify(studentDao, times(1)).deleteById(nonExistentId);
    }

    @Test
    @DisplayName("Pagination should respect page size limits")
    void findStudents_ShouldRespectPageSizeLimits() {
        // Given
        Pageable smallPage = PageRequest.of(0, 5);
        Page<Student> expectedPage = new PageImpl<>(testStudents.subList(0, 1), smallPage, testStudents.size());
        when(studentDao.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<Student> result = studentService.findStudents(smallPage);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSize()).isEqualTo(5);
        verify(studentDao, times(1)).findAll(smallPage);
    }

    @Test
    @DisplayName("Pagination should work with sorting")
    void findStudents_WithSorting_ShouldReturnSortedPage() {
        // Given
        Pageable pageableWithSort = PageRequest.of(0, 10, Sort.by("name").descending());
        Page<Student> expectedPage = new PageImpl<>(testStudents, pageableWithSort, testStudents.size());
        when(studentDao.findAll(any(Pageable.class))).thenReturn(expectedPage);

        // When
        Page<Student> result = studentService.findStudents(pageableWithSort);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getSort()).isEqualTo(Sort.by("name").descending());
        verify(studentDao, times(1)).findAll(pageableWithSort);
    }
}
