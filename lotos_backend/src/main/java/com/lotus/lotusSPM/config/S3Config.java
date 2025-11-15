package com.lotus.lotusSPM.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * AWS S3 Configuration
 * Provides S3 client for file storage operations
 */
@Configuration
public class S3Config {

    private static final Logger log = LoggerFactory.getLogger(S3Config.class);

    @Value("${aws.s3.enabled:false}")
    private boolean s3Enabled;

    @Value("${aws.s3.access-key:}")
    private String accessKey;

    @Value("${aws.s3.secret-key:}")
    private String secretKey;

    @Value("${aws.s3.region:eu-north-1}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    @Bean
    public AmazonS3 amazonS3Client() {
        if (!s3Enabled) {
            log.info("S3 is disabled. File storage will use local filesystem.");
            return null;
        }

        try {
            AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

            // Use credentials if provided
            if (accessKey != null && !accessKey.isEmpty() && secretKey != null && !secretKey.isEmpty()) {
                BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
                builder.withCredentials(new AWSStaticCredentialsProvider(credentials));
            }

            // Use custom endpoint if provided (for LocalStack or MinIO)
            if (endpoint != null && !endpoint.isEmpty()) {
                builder.withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint, region)
                );
                builder.withPathStyleAccessEnabled(true);
            } else {
                builder.withRegion(region);
            }

            AmazonS3 s3Client = builder.build();
            log.info("S3 client initialized successfully for region: {}", region);
            return s3Client;
        } catch (Exception e) {
            log.error("Failed to initialize S3 client: {}", e.getMessage(), e);
            return null;
        }
    }
}
