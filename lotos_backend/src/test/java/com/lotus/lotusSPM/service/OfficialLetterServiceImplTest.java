package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.OfficialLetterDao;
import com.lotus.lotusSPM.dao.StudentDao;
import com.lotus.lotusSPM.model.OfficialLetter;
import com.lotus.lotusSPM.model.Student;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OfficialLetterServiceImpl
 * CRITICAL: Tests path traversal vulnerability fixes (CodeQL Alert #86)
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OfficialLetterServiceImpl Security Tests")
class OfficialLetterServiceImplTest {

    @Mock
    private OfficialLetterDao officialLetterDao;

    @Mock
    private StudentDao studentDao;

    @InjectMocks
    private OfficialLetterServiceImpl officialLetterService;

    @TempDir
    Path tempDir;

    private Student testStudent;
    private OfficialLetter testLetter;

    @BeforeEach
    void setUp() {
        // Set temp directory for testing
        ReflectionTestUtils.setField(officialLetterService, "pdfOutputPath", tempDir.toString());

        testStudent = new Student();
        testStudent.setId(1L);
        testStudent.setUsername("john123");
        testStudent.setName("John");
        testStudent.setSurname("Doe");
        testStudent.setInternshipStatus("completed");

        testLetter = new OfficialLetter();
        testLetter.setId(1L);
        testLetter.setUsername("john123");
        testLetter.setComName("Tech Corp");
        testLetter.setDate("2024-11-14");
    }

    @Test
    @DisplayName("SECURITY: Should sanitize path traversal with ../")
    void createOfficialLetter_WithPathTraversal_ShouldSanitize() {
        // Given - Malicious path traversal attempt
        testLetter.setUsername("../../etc/passwd");
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        assertThat(result).isNotNull();
        // Verify file was created in safe directory (not /etc/passwd)
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);

        // Filename should be sanitized (alphanumeric only)
        String filename = files[0].getName();
        assertThat(filename).matches("OfficialLetter_[a-zA-Z0-9_-]+_\\d+\\.pdf");
        assertThat(filename).doesNotContain("..");
        assertThat(filename).doesNotContain("/");
        assertThat(filename).doesNotContain("\\");
        assertThat(filename).doesNotContain("etc");
        assertThat(filename).doesNotContain("passwd");
    }

    @Test
    @DisplayName("SECURITY: Should sanitize absolute path attempt")
    void createOfficialLetter_WithAbsolutePath_ShouldSanitize() {
        // Given - Absolute path attack
        testLetter.setUsername("/etc/shadow");
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        assertThat(files).hasSize(1);

        String filename = files[0].getName();
        assertThat(filename).doesNotContain("/etc");
        assertThat(filename).doesNotContain("shadow");
        // Should be sanitized to underscores
        assertThat(filename).contains("_etc_shadow");
    }

    @Test
    @DisplayName("SECURITY: Should sanitize Windows path attempt")
    void createOfficialLetter_WithWindowsPath_ShouldSanitize() {
        // Given - Windows path attack
        testLetter.setUsername("C:\\Windows\\System32\\config");
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).isNotNull();
        String filename = files[0].getName();
        assertThat(filename).doesNotContain("\\");
        assertThat(filename).doesNotContain(":");
        assertThat(filename).contains("C__Windows__System32__config");
    }

    @Test
    @DisplayName("SECURITY: Should sanitize special characters")
    void createOfficialLetter_WithSpecialCharacters_ShouldSanitize() {
        // Given - Special characters that could be used in attacks
        testLetter.setUsername("john@#$%^&*(){}[]");
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        String filename = files[0].getName();
        // Should only contain alphanumeric, underscore, hyphen
        assertThat(filename).matches("OfficialLetter_[a-zA-Z0-9_-]+_\\d+\\.pdf");
    }

    @Test
    @DisplayName("SECURITY: Should handle null username safely")
    void createOfficialLetter_WithNullUsername_ShouldUseDefault() {
        // Given
        testLetter.setUsername(null);
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        String filename = files[0].getName();
        assertThat(filename).contains("unknown"); // Default value
    }

    @Test
    @DisplayName("SECURITY: Should handle empty username safely")
    void createOfficialLetter_WithEmptyUsername_ShouldUseDefault() {
        // Given
        testLetter.setUsername("");
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        String filename = files[0].getName();
        assertThat(filename).contains("unknown");
    }

    @Test
    @DisplayName("Should allow valid alphanumeric usernames")
    void createOfficialLetter_WithValidUsername_ShouldSucceed() {
        // Given - Normal valid username
        testLetter.setUsername("john123");
        when(studentDao.findByUsername("john123")).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        assertThat(result).isNotNull();
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).hasSize(1);
        String filename = files[0].getName();
        assertThat(filename).contains("john123");
    }

    @Test
    @DisplayName("Should allow usernames with underscores and hyphens")
    void createOfficialLetter_WithUnderscoreHyphen_ShouldSucceed() {
        // Given
        testLetter.setUsername("john_doe-123");
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        String filename = files[0].getName();
        assertThat(filename).contains("john_doe-123");
    }

    @Test
    @DisplayName("Should create unique filenames with timestamps")
    void createOfficialLetter_MultipleCalls_ShouldCreateUniqueFiles() throws Exception {
        // Given
        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When - Create two letters
        officialLetterService.createOfficialLetter(testLetter);
        Thread.sleep(10); // Ensure different timestamps
        officialLetterService.createOfficialLetter(testLetter);

        // Then
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).hasSize(2); // Two unique files created
    }

    @Test
    @DisplayName("findById should return official letter when found")
    void findById_WhenLetterExists_ShouldReturn() {
        // Given
        when(officialLetterDao.findById(1L)).thenReturn(Optional.of(testLetter));

        // When
        OfficialLetter result = officialLetterService.findById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findById should throw exception when not found")
    void findById_WhenLetterNotFound_ShouldThrowException() {
        // Given
        when(officialLetterDao.findById(999L)).thenReturn(Optional.empty());

        // When/Then
        assertThatThrownBy(() -> officialLetterService.findById(999L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Official letter not found with id: 999");
    }

    @Test
    @DisplayName("getOfficialLetter should return all letters")
    void getOfficialLetter_ShouldReturnAllLetters() {
        // Given
        List<OfficialLetter> letters = Arrays.asList(testLetter);
        when(officialLetterDao.findAll()).thenReturn(letters);

        // When
        List<OfficialLetter> result = officialLetterService.getOfficialLetter();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result).contains(testLetter);
    }

    @Test
    @DisplayName("findByUsername should return letter for username")
    void findByUsername_ShouldReturnLetter() {
        // Given
        when(officialLetterDao.findByUsername("john123")).thenReturn(testLetter);

        // When
        OfficialLetter result = officialLetterService.findByUsername("john123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUsername()).isEqualTo("john123");
    }

    @Test
    @DisplayName("SECURITY: All files should be created within safe directory")
    void createOfficialLetter_AllVariants_ShouldStayInSafeDirectory() {
        // Given - Various attack attempts
        String[] maliciousUsernames = {
            "../../etc/passwd",
            "../../../root",
            "/etc/shadow",
            "C:\\Windows\\System32",
            "....//....//etc/passwd",
            "user/../../../etc/passwd"
        };

        when(studentDao.findByUsername(anyString())).thenReturn(testStudent);
        when(officialLetterDao.save(any(OfficialLetter.class))).thenReturn(testLetter);

        // When
        for (String maliciousUsername : maliciousUsernames) {
            testLetter.setUsername(maliciousUsername);
            officialLetterService.createOfficialLetter(testLetter);
        }

        // Then
        File[] files = tempDir.toFile().listFiles();
        assertThat(files).hasSizeGreaterThanOrEqualTo(maliciousUsernames.length);

        // Verify ALL files are in the safe temp directory
        for (File file : files) {
            assertThat(file.getParentFile().toPath()).isEqualTo(tempDir);
            assertThat(file.getAbsolutePath()).startsWith(tempDir.toString());
        }
    }
}
