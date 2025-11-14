package com.lotus.lotusSPM.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Custom Application Metrics for Business KPIs.
 *
 * Enterprise Pattern: Observability / Business Metrics
 *
 * Metric Types:
 * - Counter: Monotonically increasing value (requests, errors)
 * - Gauge: Current value (active users, queue size)
 * - Timer: Duration and rate (latency, throughput)
 * - Distribution Summary: Statistical distribution
 *
 * Best Practices:
 * - Use tags for dimensionality
 * - Name metrics consistently
 * - Don't over-instrument
 * - Monitor SLIs (Service Level Indicators)
 * - Define SLOs (Service Level Objectives)
 * - Alert on SLO violations
 *
 * Business Metrics:
 * - Student registrations
 * - Application submissions
 * - Opportunity creations
 * - Message exchanges
 * - Document uploads
 * - API latency by endpoint
 * - Error rates by type
 */
@Component
@RequiredArgsConstructor
public class CustomMetrics {

    private final MeterRegistry meterRegistry;

    // Counters
    private Counter studentRegistrationsCounter;
    private Counter applicationSubmissionsCounter;
    private Counter opportunityCreationsCounter;
    private Counter messagesSentCounter;
    private Counter documentsUploadedCounter;
    private Counter apiErrorsCounter;

    // Timers
    private ConcurrentMap<String, Timer> endpointTimers = new ConcurrentHashMap<>();

    @PostConstruct
    public void initializeMetrics() {
        // Initialize counters
        studentRegistrationsCounter = Counter.builder("students.registrations")
            .description("Total student registrations")
            .tag("type", "business")
            .register(meterRegistry);

        applicationSubmissionsCounter = Counter.builder("applications.submissions")
            .description("Total application submissions")
            .tag("type", "business")
            .register(meterRegistry);

        opportunityCreationsCounter = Counter.builder("opportunities.creations")
            .description("Total opportunity creations")
            .tag("type", "business")
            .register(meterRegistry);

        messagesSentCounter = Counter.builder("messages.sent")
            .description("Total messages sent")
            .tag("type", "business")
            .register(meterRegistry);

        documentsUploadedCounter = Counter.builder("documents.uploaded")
            .description("Total documents uploaded")
            .tag("type", "business")
            .register(meterRegistry);

        apiErrorsCounter = Counter.builder("api.errors")
            .description("Total API errors")
            .tag("type", "technical")
            .register(meterRegistry);

        // Initialize gauges
        meterRegistry.gauge("cache.hit.ratio", this, CustomMetrics::calculateCacheHitRatio);
    }

    // ==================== COUNTER METHODS ====================

    public void incrementStudentRegistrations() {
        studentRegistrationsCounter.increment();
    }

    public void incrementApplicationSubmissions() {
        applicationSubmissionsCounter.increment();
    }

    public void incrementOpportunityCreations() {
        opportunityCreationsCounter.increment();
    }

    public void incrementMessagesSent() {
        messagesSentCounter.increment();
    }

    public void incrementDocumentsUploaded() {
        documentsUploadedCounter.increment();
    }

    public void incrementApiErrors(String errorType) {
        Counter.builder("api.errors.by.type")
            .tag("error_type", errorType)
            .register(meterRegistry)
            .increment();
    }

    // ==================== TIMER METHODS ====================

    /**
     * Record endpoint execution time.
     */
    public void recordEndpointDuration(String endpoint, String method, long durationMillis) {
        String timerName = "api.endpoint.duration";
        String key = endpoint + ":" + method;

        Timer timer = endpointTimers.computeIfAbsent(key, k ->
            Timer.builder(timerName)
                .description("API endpoint execution time")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .register(meterRegistry)
        );

        timer.record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Record database query duration.
     */
    public void recordDatabaseQueryDuration(String queryType, long durationMillis) {
        Timer.builder("database.query.duration")
            .description("Database query execution time")
            .tag("query_type", queryType)
            .register(meterRegistry)
            .record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    /**
     * Record cache operation duration.
     */
    public void recordCacheOperationDuration(String operation, long durationMillis) {
        Timer.builder("cache.operation.duration")
            .description("Cache operation execution time")
            .tag("operation", operation)
            .register(meterRegistry)
            .record(durationMillis, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    // ==================== GAUGE METHODS ====================

    private double calculateCacheHitRatio() {
        // Implement cache hit ratio calculation
        return 0.85; // Example: 85% hit ratio
    }

    /**
     * Record active user sessions.
     */
    public void recordActiveUsers(int count) {
        meterRegistry.gauge("users.active", count);
    }

    /**
     * Record queue size.
     */
    public void recordQueueSize(String queueName, int size) {
        meterRegistry.gauge("queue.size",
            java.util.Collections.singletonList(io.micrometer.core.instrument.Tag.of("queue", queueName)),
            size);
    }
}
