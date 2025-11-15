package com.lotus.lotusSPM.health;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RedisHealthIndicator
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RedisHealthIndicator Tests")
class RedisHealthIndicatorTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisConnectionFactory connectionFactory;

    @Mock
    private RedisConnection redisConnection;

    private RedisHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new RedisHealthIndicator(redisTemplate);
    }

    @Test
    @DisplayName("Should report UP when Redis is healthy")
    void testHealthyRedis() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");
        when(redisConnection.dbSize()).thenReturn(100L);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("status", "Redis is reachable");
        assertThat(health.getDetails()).containsEntry("database_size", 100L);
        assertThat(health.getDetails()).containsEntry("connection", "active");

        verify(redisConnection, times(1)).ping();
        verify(redisConnection, times(1)).dbSize();
        verify(redisConnection, times(1)).close();
    }

    @Test
    @DisplayName("Should report DOWN when Redis ping fails")
    void testRedisPingFails() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("ERROR");

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "Redis ping failed");
        assertThat(health.getDetails()).containsEntry("response", "ERROR");
    }

    @Test
    @DisplayName("Should report DOWN when Redis connection fails")
    void testRedisConnectionFails() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenThrow(new RuntimeException("Connection refused"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "Redis connection failed");
        assertThat(health.getDetails()).containsKey("error");
        assertThat(health.getDetails().get("error").toString()).contains("Connection refused");
    }

    @Test
    @DisplayName("Should handle null connection factory")
    void testNullConnectionFactory() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(null);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "Redis connection failed");
    }

    @Test
    @DisplayName("Should handle Redis dbSize error gracefully")
    void testDbSizeError() {
        // Given
        when(redisTemplate.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.ping()).thenReturn("PONG");
        when(redisConnection.dbSize()).thenThrow(new RuntimeException("dbSize failed"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsKey("error");
    }
}
