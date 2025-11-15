package com.lotus.lotusSPM.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom Metrics Configuration
 * Provides business-specific metrics for monitoring
 */
@Configuration
public class CustomMetricsConfig {

    @Bean
    public Counter studentLoginCounter(MeterRegistry registry) {
        return Counter.builder("lotus.student.login.count")
                .description("Total number of student login attempts")
                .tag("type", "authentication")
                .register(registry);
    }

    @Bean
    public Counter studentLoginSuccessCounter(MeterRegistry registry) {
        return Counter.builder("lotus.student.login.success")
                .description("Number of successful student logins")
                .tag("type", "authentication")
                .register(registry);
    }

    @Bean
    public Counter studentLoginFailureCounter(MeterRegistry registry) {
        return Counter.builder("lotus.student.login.failure")
                .description("Number of failed student login attempts")
                .tag("type", "authentication")
                .register(registry);
    }

    @Bean
    public Counter documentUploadCounter(MeterRegistry registry) {
        return Counter.builder("lotus.document.upload.count")
                .description("Total number of document uploads")
                .tag("type", "document")
                .register(registry);
    }

    @Bean
    public Counter officialLetterGenerationCounter(MeterRegistry registry) {
        return Counter.builder("lotus.official_letter.generation.count")
                .description("Total number of official letters generated")
                .tag("type", "document")
                .register(registry);
    }

    @Bean
    public Timer apiResponseTimer(MeterRegistry registry) {
        return Timer.builder("lotus.api.response.time")
                .description("API response time")
                .tag("type", "performance")
                .register(registry);
    }

    @Bean
    public Counter cacheHitCounter(MeterRegistry registry) {
        return Counter.builder("lotus.cache.hit")
                .description("Cache hit count")
                .tag("type", "cache")
                .register(registry);
    }

    @Bean
    public Counter cacheMissCounter(MeterRegistry registry) {
        return Counter.builder("lotus.cache.miss")
                .description("Cache miss count")
                .tag("type", "cache")
                .register(registry);
    }
}
