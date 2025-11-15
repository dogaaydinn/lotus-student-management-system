package com.lotus.lotusSPM.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for S3StorageService
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("S3StorageService Tests")
class S3StorageServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private S3StorageService s3StorageService;

    private static final String BUCKET_NAME = "test-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(s3StorageService, "s3Enabled", true);
        ReflectionTestUtils.setField(s3StorageService, "bucketName", BUCKET_NAME);
    }

    @Test
    @DisplayName("Should upload file successfully when S3 is enabled")
    void testUploadFileSuccess() throws IOException {
        // Given
        String originalFilename = "test-document.pdf";
        byte[] fileContent = "test content".getBytes();
        InputStream inputStream = new ByteArrayInputStream(fileContent);

        when(multipartFile.getOriginalFilename()).thenReturn(originalFilename);
        when(multipartFile.getSize()).thenReturn((long) fileContent.length);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getInputStream()).thenReturn(inputStream);
        when(amazonS3.doesBucketExistV2(BUCKET_NAME)).thenReturn(true);

        // When
        String result = s3StorageService.uploadFile(multipartFile, "documents");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).startsWith("documents/");
        assertThat(result).endsWith(".pdf");

        ArgumentCaptor<PutObjectRequest> putRequestCaptor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(amazonS3, times(1)).putObject(putRequestCaptor.capture());

        PutObjectRequest capturedRequest = putRequestCaptor.getValue();
        assertThat(capturedRequest.getBucketName()).isEqualTo(BUCKET_NAME);
        assertThat(capturedRequest.getKey()).startsWith("documents/");
    }

    @Test
    @DisplayName("Should create bucket if it doesn't exist")
    void testCreateBucketIfNotExists() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(amazonS3.doesBucketExistV2(BUCKET_NAME)).thenReturn(false);

        // When
        s3StorageService.uploadFile(multipartFile, "documents");

        // Then
        verify(amazonS3, times(1)).createBucket(BUCKET_NAME);
        verify(amazonS3, times(1)).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("Should return null when S3 is disabled")
    void testUploadFileS3Disabled() throws IOException {
        // Given
        ReflectionTestUtils.setField(s3StorageService, "s3Enabled", false);
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");

        // When
        String result = s3StorageService.uploadFile(multipartFile, "documents");

        // Then
        assertThat(result).isNull();
        verify(amazonS3, never()).putObject(any(PutObjectRequest.class));
    }

    @Test
    @DisplayName("Should throw IOException when upload fails")
    void testUploadFileFailure() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/pdf");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(amazonS3.doesBucketExistV2(BUCKET_NAME)).thenReturn(true);
        when(amazonS3.putObject(any(PutObjectRequest.class)))
                .thenThrow(new RuntimeException("S3 error"));

        // When/Then
        assertThatThrownBy(() -> s3StorageService.uploadFile(multipartFile, "documents"))
                .isInstanceOf(IOException.class)
                .hasMessageContaining("Failed to upload file to S3");
    }

    @Test
    @DisplayName("Should download file successfully")
    void testDownloadFileSuccess() {
        // Given
        S3Object s3Object = new S3Object();
        when(amazonS3.getObject(BUCKET_NAME, "documents/test.pdf")).thenReturn(s3Object);

        // When
        S3Object result = s3StorageService.downloadFile("documents/test.pdf");

        // Then
        assertThat(result).isNotNull();
        verify(amazonS3, times(1)).getObject(BUCKET_NAME, "documents/test.pdf");
    }

    @Test
    @DisplayName("Should return null when download fails")
    void testDownloadFileFailure() {
        // Given
        when(amazonS3.getObject(BUCKET_NAME, "documents/test.pdf"))
                .thenThrow(new RuntimeException("S3 error"));

        // When
        S3Object result = s3StorageService.downloadFile("documents/test.pdf");

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should delete file successfully")
    void testDeleteFileSuccess() {
        // Given
        doNothing().when(amazonS3).deleteObject(BUCKET_NAME, "documents/test.pdf");

        // When
        s3StorageService.deleteFile("documents/test.pdf");

        // Then
        verify(amazonS3, times(1)).deleteObject(BUCKET_NAME, "documents/test.pdf");
    }

    @Test
    @DisplayName("Should handle delete failure gracefully")
    void testDeleteFileFailure() {
        // Given
        doThrow(new RuntimeException("S3 error")).when(amazonS3)
                .deleteObject(BUCKET_NAME, "documents/test.pdf");

        // When/Then - Should not throw exception
        assertThatCode(() -> s3StorageService.deleteFile("documents/test.pdf"))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Should generate presigned URL successfully")
    void testGeneratePresignedUrl() throws Exception {
        // Given
        URL expectedUrl = new URL("https://test-bucket.s3.amazonaws.com/documents/test.pdf?AWSAccessKeyId=...");
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(expectedUrl);

        // When
        String result = s3StorageService.generatePresignedUrl("documents/test.pdf", 60);

        // Then
        assertThat(result).isNotNull();
        assertThat(result).contains("test-bucket");
        verify(amazonS3, times(1)).generatePresignedUrl(any(GeneratePresignedUrlRequest.class));
    }

    @Test
    @DisplayName("Should return null when presigned URL generation fails")
    void testGeneratePresignedUrlFailure() {
        // Given
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenThrow(new RuntimeException("S3 error"));

        // When
        String result = s3StorageService.generatePresignedUrl("documents/test.pdf", 60);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should check if file exists")
    void testFileExists() {
        // Given
        when(amazonS3.doesObjectExist(BUCKET_NAME, "documents/test.pdf")).thenReturn(true);

        // When
        boolean result = s3StorageService.fileExists("documents/test.pdf");

        // Then
        assertThat(result).isTrue();
        verify(amazonS3, times(1)).doesObjectExist(BUCKET_NAME, "documents/test.pdf");
    }

    @Test
    @DisplayName("Should return false when file doesn't exist")
    void testFileNotExists() {
        // Given
        when(amazonS3.doesObjectExist(BUCKET_NAME, "documents/nonexistent.pdf")).thenReturn(false);

        // When
        boolean result = s3StorageService.fileExists("documents/nonexistent.pdf");

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("Should handle file without extension")
    void testUploadFileWithoutExtension() throws IOException {
        // Given
        when(multipartFile.getOriginalFilename()).thenReturn("testfile");
        when(multipartFile.getSize()).thenReturn(1024L);
        when(multipartFile.getContentType()).thenReturn("application/octet-stream");
        when(multipartFile.getInputStream()).thenReturn(new ByteArrayInputStream("test".getBytes()));
        when(amazonS3.doesBucketExistV2(BUCKET_NAME)).thenReturn(true);

        // When
        String result = s3StorageService.uploadFile(multipartFile, "documents");

        // Then
        assertThat(result).isNotNull();
        assertThat(result).startsWith("documents/");
        assertThat(result).doesNotContain("..");
    }

    @Test
    @DisplayName("Should return false when S3 is disabled for fileExists")
    void testFileExistsS3Disabled() {
        // Given
        ReflectionTestUtils.setField(s3StorageService, "s3Enabled", false);

        // When
        boolean result = s3StorageService.fileExists("documents/test.pdf");

        // Then
        assertThat(result).isFalse();
        verify(amazonS3, never()).doesObjectExist(anyString(), anyString());
    }
}
