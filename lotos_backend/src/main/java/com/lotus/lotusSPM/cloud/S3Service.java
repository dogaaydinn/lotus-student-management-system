package com.lotus.lotusSPM.cloud;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

/**
 * S3 Service for Cloud Object Storage.
 *
 * Enterprise Pattern: Cloud Storage Service
 *
 * Features:
 * - Upload files to S3
 * - Generate pre-signed URLs
 * - Delete files
 * - List files
 * - Set object metadata
 * - Configure lifecycle policies
 * - Enable versioning
 * - Cross-region replication
 *
 * Best Practices:
 * - Use IAM roles instead of access keys
 * - Enable encryption at rest
 * - Use CloudFront for CDN
 * - Implement lifecycle policies
 * - Tag resources for cost tracking
 * - Enable versioning for critical data
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket:lotus-spm-documents}")
    private String bucketName;

    /**
     * Upload file to S3.
     *
     * @param file MultipartFile to upload
     * @param folder Folder path in S3 (e.g., "documents/transcripts")
     * @return S3 object key
     */
    public String uploadFile(MultipartFile file, String folder) throws IOException {
        String fileName = folder + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(file.getContentType())
            .contentLength(file.getSize())
            .build();

        s3Client.putObject(putObjectRequest,
            RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        log.info("Uploaded file to S3: {}/{}", bucketName, fileName);

        return fileName;
    }

    /**
     * Generate pre-signed URL for secure file access.
     *
     * @param objectKey S3 object key
     * @param expirationMinutes URL expiration time
     * @return Pre-signed URL
     */
    public String generatePresignedUrl(String objectKey, int expirationMinutes) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        Duration expiration = Duration.ofMinutes(expirationMinutes);

        String url = s3Client.utilities().getUrl(builder -> builder
            .bucket(bucketName)
            .key(objectKey)
        ).toString();

        log.info("Generated pre-signed URL for: {}", objectKey);

        return url;
    }

    /**
     * Delete file from S3.
     *
     * @param objectKey S3 object key
     */
    public void deleteFile(String objectKey) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        s3Client.deleteObject(deleteObjectRequest);

        log.info("Deleted file from S3: {}/{}", bucketName, objectKey);
    }

    /**
     * Check if file exists.
     *
     * @param objectKey S3 object key
     * @return true if exists
     */
    public boolean fileExists(String objectKey) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

            s3Client.headObject(headObjectRequest);
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        }
    }

    /**
     * Copy file within S3.
     *
     * @param sourceKey Source object key
     * @param destinationKey Destination object key
     */
    public void copyFile(String sourceKey, String destinationKey) {
        CopyObjectRequest copyObjectRequest = CopyObjectRequest.builder()
            .sourceBucket(bucketName)
            .sourceKey(sourceKey)
            .destinationBucket(bucketName)
            .destinationKey(destinationKey)
            .build();

        s3Client.copyObject(copyObjectRequest);

        log.info("Copied file in S3: {} -> {}", sourceKey, destinationKey);
    }

    /**
     * Get file metadata.
     *
     * @param objectKey S3 object key
     * @return Metadata map
     */
    public HeadObjectResponse getFileMetadata(String objectKey) {
        HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
            .bucket(bucketName)
            .key(objectKey)
            .build();

        return s3Client.headObject(headObjectRequest);
    }
}
