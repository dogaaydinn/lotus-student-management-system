package com.lotus.lotusSPM.events.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Kafka Configuration for High-Throughput Event Streaming.
 *
 * Enterprise Pattern: Event Streaming / Log-Based Messaging
 *
 * Kafka Use Cases:
 * - Event sourcing (append-only log)
 * - Stream processing (real-time analytics)
 * - Log aggregation (centralized logging)
 * - Metrics collection (time-series data)
 * - Change data capture (CDC)
 * - Microservices communication
 *
 * Features:
 * - High throughput (millions of events/sec)
 * - Horizontal scalability
 * - Fault tolerance (replication)
 * - Durability (persistent storage)
 * - Event replay capability
 * - Consumer groups for load balancing
 *
 * Topics:
 * - domain-events: Business events
 * - integration-events: Service integration
 * - audit-logs: Audit trail
 * - metrics: Application metrics
 * - analytics: Business analytics
 */
@Configuration
public class KafkaConfig {

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${spring.kafka.consumer.group-id:lotus-spm-consumer}")
    private String consumerGroupId;

    // Topic names
    public static final String DOMAIN_EVENTS_TOPIC = "domain-events";
    public static final String INTEGRATION_EVENTS_TOPIC = "integration-events";
    public static final String AUDIT_LOGS_TOPIC = "audit-logs";
    public static final String METRICS_TOPIC = "metrics";
    public static final String ANALYTICS_TOPIC = "analytics";
    public static final String NOTIFICATIONS_TOPIC = "notifications";

    // ==================== PRODUCER CONFIGURATION ====================

    /**
     * Producer factory for creating Kafka producers.
     */
    @Bean
    public ProducerFactory<String, Object> producerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        // Performance tuning
        config.put(ProducerConfig.ACKS_CONFIG, "all"); // Wait for all replicas
        config.put(ProducerConfig.RETRIES_CONFIG, 3); // Retry on failure
        config.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384); // Batch size in bytes
        config.put(ProducerConfig.LINGER_MS_CONFIG, 10); // Wait 10ms for batching
        config.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432); // 32MB buffer
        config.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy"); // Compression
        config.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);

        // Idempotence for exactly-once semantics
        config.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);

        return new DefaultKafkaProducerFactory<>(config);
    }

    /**
     * Kafka template for sending messages.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }

    // ==================== CONSUMER CONFIGURATION ====================

    /**
     * Consumer factory for creating Kafka consumers.
     */
    @Bean
    public ConsumerFactory<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, consumerGroupId);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Consumer configuration
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest"); // Read from beginning
        config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false); // Manual commit
        config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 500); // Max records per poll

        // Deserializer configuration
        config.put(JsonDeserializer.TRUSTED_PACKAGES, "com.lotus.lotusSPM.*");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        return new DefaultKafkaConsumerFactory<>(config);
    }

    /**
     * Kafka listener container factory for concurrent processing.
     */
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
            new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(consumerFactory());
        factory.setConcurrency(3); // 3 concurrent consumer threads
        factory.getContainerProperties().setPollTimeout(3000);

        // Error handling
        factory.setCommonErrorHandler(new org.springframework.kafka.listener.DefaultErrorHandler());

        return factory;
    }

    // ==================== TOPIC CONFIGURATION ====================

    /**
     * Domain Events Topic - For business domain events.
     */
    @Bean
    public NewTopic domainEventsTopic() {
        return TopicBuilder.name(DOMAIN_EVENTS_TOPIC)
            .partitions(6) // 6 partitions for parallelism
            .replicas(3) // 3 replicas for fault tolerance
            .compact() // Log compaction for deduplication
            .build();
    }

    /**
     * Integration Events Topic - For cross-service integration.
     */
    @Bean
    public NewTopic integrationEventsTopic() {
        return TopicBuilder.name(INTEGRATION_EVENTS_TOPIC)
            .partitions(3)
            .replicas(3)
            .build();
    }

    /**
     * Audit Logs Topic - For compliance and audit trail.
     */
    @Bean
    public NewTopic auditLogsTopic() {
        return TopicBuilder.name(AUDIT_LOGS_TOPIC)
            .partitions(3)
            .replicas(3)
            .config("retention.ms", String.valueOf(365L * 24 * 60 * 60 * 1000)) // 1 year retention
            .build();
    }

    /**
     * Metrics Topic - For application metrics.
     */
    @Bean
    public NewTopic metricsTopic() {
        return TopicBuilder.name(METRICS_TOPIC)
            .partitions(6)
            .replicas(2)
            .config("retention.ms", String.valueOf(7L * 24 * 60 * 60 * 1000)) // 7 days retention
            .build();
    }

    /**
     * Analytics Topic - For business analytics.
     */
    @Bean
    public NewTopic analyticsTopic() {
        return TopicBuilder.name(ANALYTICS_TOPIC)
            .partitions(6)
            .replicas(3)
            .build();
    }

    /**
     * Notifications Topic - For user notifications.
     */
    @Bean
    public NewTopic notificationsTopic() {
        return TopicBuilder.name(NOTIFICATIONS_TOPIC)
            .partitions(3)
            .replicas(2)
            .build();
    }
}
