package com.lotus.lotusSPM.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting service using Token Bucket algorithm.
 * Supports per-user, per-IP, and global rate limiting.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Service
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    public RateLimitService(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Check if request is allowed based on rate limit
     *
     * @param key Rate limit key (user ID, IP address, etc.)
     * @param capacity Maximum tokens in bucket
     * @param refillTokens Tokens to refill
     * @param refillDuration Duration for refill
     * @return true if request is allowed, false if rate limit exceeded
     */
    public boolean allowRequest(String key, int capacity, int refillTokens, Duration refillDuration) {
        Bucket bucket = bucketCache.computeIfAbsent(key,
            k -> createBucket(capacity, refillTokens, refillDuration));

        boolean allowed = bucket.tryConsume(1);

        if (!allowed) {
            log.warn("Rate limit exceeded for key: {}", key);
        }

        return allowed;
    }

    /**
     * Create a new bucket with given parameters
     */
    private Bucket createBucket(int capacity, int refillTokens, Duration refillDuration) {
        Bandwidth limit = Bandwidth.classic(capacity,
            Refill.intervally(refillTokens, refillDuration));
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }

    /**
     * Check rate limit for user (100 requests per minute)
     */
    public boolean allowUserRequest(String username) {
        String key = "user:" + username;
        return allowRequest(key, 100, 100, Duration.ofMinutes(1));
    }

    /**
     * Check rate limit for IP address (1000 requests per minute)
     */
    public boolean allowIpRequest(String ipAddress) {
        String key = "ip:" + ipAddress;
        return allowRequest(key, 1000, 1000, Duration.ofMinutes(1));
    }

    /**
     * Check rate limit for login attempts (5 per minute)
     */
    public boolean allowLoginAttempt(String username) {
        String key = "login:" + username;
        return allowRequest(key, 5, 5, Duration.ofMinutes(1));
    }

    /**
     * Check rate limit for admin endpoints (50 per minute)
     */
    public boolean allowAdminRequest(String username) {
        String key = "admin:" + username;
        return allowRequest(key, 50, 50, Duration.ofMinutes(1));
    }

    /**
     * Reset rate limit for a specific key
     */
    public void resetRateLimit(String key) {
        bucketCache.remove(key);
        log.info("Rate limit reset for key: {}", key);
    }

    /**
     * Get remaining tokens for a key
     */
    public long getRemainingTokens(String key) {
        Bucket bucket = bucketCache.get(key);
        if (bucket == null) {
            return -1;
        }
        return bucket.getAvailableTokens();
    }
}
