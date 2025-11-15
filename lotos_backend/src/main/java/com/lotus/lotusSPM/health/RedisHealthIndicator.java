package com.lotus.lotusSPM.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * Custom Health Indicator for Redis
 * Provides detailed health status for Redis connection
 */
@Component("redis")
public class RedisHealthIndicator implements HealthIndicator {

    private final RedisTemplate<String, Object> redisTemplate;

    public RedisHealthIndicator(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public Health health() {
        try {
            RedisConnection connection = redisTemplate.getConnectionFactory().getConnection();

            // Test Redis connection
            String pong = connection.ping();

            if ("PONG".equals(pong)) {
                // Get Redis server info
                Long dbSize = connection.dbSize();

                connection.close();

                return Health.up()
                        .withDetail("status", "Redis is reachable")
                        .withDetail("database_size", dbSize)
                        .withDetail("connection", "active")
                        .build();
            } else {
                return Health.down()
                        .withDetail("status", "Redis ping failed")
                        .withDetail("response", pong)
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Redis connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
