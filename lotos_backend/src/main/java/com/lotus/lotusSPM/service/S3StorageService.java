package com.lotus.lotusSPM.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * S3 Storage Service
 * Handles file upload/download operations with AWS S3
 */
@Service
public class S3StorageService {

    private static final Logger log = LoggerFactory.getLogger(S3StorageService.class);

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.s3.bucket-name:lotus-spm-files}")
    private String bucketName;

    private final AmazonS3 amazonS3;

    @Autowired
    public S3StorageService(AmazonS3 amazonS3) {
        this.amazonS3 = amazonS3;
    }

    /**
     * Upload file to S3
     * @param file MultipartFile to upload
     * @param folder Folder path in S3 bucket
     * @return S3 object key
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (!s3Enabled || amazonS3 == null) {
            log.warn("S3 is disabled. Cannot upload file: {}", file.getOriginalFilename());
            return null;
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : "";
        String key = folder + "/" + UUID.randomUUID().toString() + extension;

        try {
            // Create bucket if it doesn't exist
            if (!amazonS3.doesBucketExistV2(bucketName)) {
                log.info("Creating S3 bucket: {}", bucketName);
                amazonS3.createBucket(bucketName);
            }

            // Upload file
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try (InputStream inputStream = file.getInputStream()) {
                PutObjectRequest putObjectRequest = new PutObjectRequest(
                        bucketName,
                        key,
                        inputStream,
                        metadata
                );

                // Make file publicly readable (optional)
                // putObjectRequest.withCannedAcl(CannedAccessControlList.PublicRead);

                amazonS3.putObject(putObjectRequest);
                log.info("File uploaded successfully to S3: {}", key);
                return key;
            }
        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new IOException("Failed to upload file to S3", e);
        }
    }

    /**
     * Download file from S3
     * @param key S3 object key
     * @return S3Object
     */
    public S3Object downloadFile(String key) {
        if (!s3Enabled || amazonS3 == null) {
            log.warn("S3 is disabled. Cannot download file: {}", key);
            return null;
        }

        try {
            S3Object s3Object = amazonS3.getObject(bucketName, key);
            log.info("File downloaded successfully from S3: {}", key);
            return s3Object;
        } catch (Exception e) {
            log.error("Failed to download file from S3: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Delete file from S3
     * @param key S3 object key
     */
    public void deleteFile(String key) {
        if (!s3Enabled || amazonS3 == null) {
            log.warn("S3 is disabled. Cannot delete file: {}", key);
            return;
        }

        try {
            amazonS3.deleteObject(bucketName, key);
            log.info("File deleted successfully from S3: {}", key);
        } catch (Exception e) {
            log.error("Failed to delete file from S3: {}", e.getMessage(), e);
        }
    }

    /**
     * Generate presigned URL for temporary access
     * @param key S3 object key
     * @param expirationMinutes Expiration time in minutes
     * @return Presigned URL
     */
    public String generatePresignedUrl(String key, int expirationMinutes) {
        if (!s3Enabled || amazonS3 == null) {
            log.warn("S3 is disabled. Cannot generate presigned URL for: {}", key);
            return null;
        }

        try {
            java.util.Date expiration = new java.util.Date();
            long expTimeMillis = expiration.getTime();
            expTimeMillis += 1000L * 60 * expirationMinutes;
            expiration.setTime(expTimeMillis);

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(bucketName, key);
            generatePresignedUrlRequest.setExpiration(expiration);

            java.net.URL url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest);
            log.info("Generated presigned URL for: {}", key);
            return url.toString();
        } catch (Exception e) {
            log.error("Failed to generate presigned URL: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Check if file exists in S3
     * @param key S3 object key
     * @return true if file exists
     */
    public boolean fileExists(String key) {
        if (!s3Enabled || amazonS3 == null) {
            return false;
        }

        try {
            return amazonS3.doesObjectExist(bucketName, key);
        } catch (Exception e) {
            log.error("Failed to check if file exists in S3: {}", e.getMessage(), e);
            return false;
        }
    }
}
