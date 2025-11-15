package com.lotus.lotusSPM.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        // Use String serialization for keys
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Use JSON serialization for values
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        template.setValueSerializer(jackson2JsonRedisSerializer);
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        template.afterPropertiesSet();
        return template;
    }

    @Bean
    public CacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.activateDefaultTyping(
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            ObjectMapper.DefaultTyping.NON_FINAL,
            JsonTypeInfo.As.PROPERTY
        );

        GenericJackson2JsonRedisSerializer jackson2JsonRedisSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // Default cache configuration (5 minutes TTL)
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofMinutes(5))
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())
            )
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(jackson2JsonRedisSerializer)
            )
            .disableCachingNullValues();

        // Multi-level caching strategies with different TTLs
        java.util.Map<String, RedisCacheConfiguration> cacheConfigurations = new java.util.HashMap<>();

        // Short-lived cache (5 minutes) - Frequently accessed, frequently updated
        cacheConfigurations.put("students", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("opportunities", defaultConfig.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("documents", defaultConfig.entryTtl(Duration.ofMinutes(5)));

        // Medium-lived cache (30 minutes) - User sessions, permissions
        cacheConfigurations.put("userSessions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("permissions", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("coordinators", defaultConfig.entryTtl(Duration.ofMinutes(30)));

        // Long-lived cache (1 hour) - Analytics, reports, rarely changing data
        cacheConfigurations.put("analytics", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("reports", defaultConfig.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("applicationForms", defaultConfig.entryTtl(Duration.ofHours(1)));

        // Very long-lived cache (24 hours) - Static data, configurations
        cacheConfigurations.put("configurations", defaultConfig.entryTtl(Duration.ofHours(24)));
        cacheConfigurations.put("staticData", defaultConfig.entryTtl(Duration.ofHours(24)));

        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
}
