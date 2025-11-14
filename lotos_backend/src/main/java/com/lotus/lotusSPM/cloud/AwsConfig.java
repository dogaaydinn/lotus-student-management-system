package com.lotus.lotusSPM.cloud;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sqs.SqsClient;

/**
 * AWS Services Configuration.
 *
 * Enterprise Pattern: Cloud Integration / Infrastructure as a Service
 *
 * Services:
 * - S3: Object storage for documents, images, backups
 * - SNS: Notifications (email, SMS, push)
 * - SQS: Message queuing for async processing
 * - CloudWatch: Monitoring and logging
 * - Lambda: Serverless function execution
 *
 * Use Cases:
 * - Document storage (transcripts, resumes)
 * - Image storage (profile pictures)
 * - Backup and disaster recovery
 * - Email/SMS notifications
 * - Async job processing
 * - Event-driven workflows
 */
@Configuration
public class AwsConfig {

    @Value("${aws.access-key-id:}")
    private String accessKeyId;

    @Value("${aws.secret-access-key:}")
    private String secretAccessKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    /**
     * S3 Client for object storage.
     */
    @Bean
    public S3Client s3Client() {
        if (accessKeyId.isEmpty()) {
            // Use default credentials chain (IAM role, env vars, etc.)
            return S3Client.builder()
                .region(Region.of(region))
                .build();
        }

        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ))
            .build();
    }

    /**
     * SNS Client for notifications.
     */
    @Bean
    public SnsClient snsClient() {
        if (accessKeyId.isEmpty()) {
            return SnsClient.builder()
                .region(Region.of(region))
                .build();
        }

        return SnsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ))
            .build();
    }

    /**
     * SQS Client for message queuing.
     */
    @Bean
    public SqsClient sqsClient() {
        if (accessKeyId.isEmpty()) {
            return SqsClient.builder()
                .region(Region.of(region))
                .build();
        }

        return SqsClient.builder()
            .region(Region.of(region))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(accessKeyId, secretAccessKey)
            ))
            .build();
    }
}
