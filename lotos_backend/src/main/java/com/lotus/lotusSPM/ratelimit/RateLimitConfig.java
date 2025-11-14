package com.lotus.lotusSPM.ratelimit;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import io.github.bucket4j.grid.ProxyManager;
import io.github.bucket4j.grid.jcache.JCacheProxyManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Cache;
import javax.cache.CacheManager;
import javax.cache.Caching;
import javax.cache.configuration.MutableConfiguration;
import javax.cache.expiry.CreatedExpiryPolicy;
import javax.cache.expiry.Duration;
import javax.cache.spi.CachingProvider;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

/**
 * Rate Limiting Configuration using Token Bucket Algorithm.
 *
 * Enterprise Pattern: Rate Limiting / API Throttling
 *
 * Implements sophisticated rate limiting for:
 * - DDoS protection
 * - Fair usage enforcement
 * - Cost control (external API calls)
 * - SLA enforcement
 *
 * Algorithm: Token Bucket
 * - Tokens refill at fixed rate
 * - Each request consumes tokens
 * - Burst traffic allowed up to bucket capacity
 * - Smooth rate limiting with flexibility
 *
 * Tiers:
 * - Anonymous: 10 requests/minute
 * - Authenticated: 100 requests/minute
 * - Premium: 1000 requests/minute
 * - Admin: Unlimited
 */
@Configuration
public class RateLimitConfig {

    /**
     * JCache configuration for distributed rate limiting.
     */
    @Bean
    public CacheManager jCacheManager() {
        CachingProvider cachingProvider = Caching.getCachingProvider();
        return cachingProvider.getCacheManager();
    }

    /**
     * Bucket cache for rate limit buckets.
     */
    @Bean
    public Cache<String, byte[]> rateLimitCache(CacheManager cacheManager) {
        MutableConfiguration<String, byte[]> config = new MutableConfiguration<>();
        config.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(
            new Duration(TimeUnit.HOURS, 1)
        ));
        config.setStoreByValue(false);

        return cacheManager.createCache("rate-limit-buckets", config);
    }

    /**
     * Proxy manager for distributed bucket management.
     */
    @Bean
    public ProxyManager<String> proxyManager(Cache<String, byte[]> rateLimitCache) {
        return new JCacheProxyManager<>(rateLimitCache);
    }

    /**
     * Create rate limit bucket for anonymous users.
     * 10 requests per minute with burst of 15.
     */
    public Bucket createAnonymousBucket() {
        Bandwidth limit = Bandwidth.classic(
            15, // capacity
            Refill.intervally(10, java.time.Duration.of(1, ChronoUnit.MINUTES))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Create rate limit bucket for authenticated users.
     * 100 requests per minute with burst of 150.
     */
    public Bucket createAuthenticatedBucket() {
        Bandwidth limit = Bandwidth.classic(
            150,
            Refill.intervally(100, java.time.Duration.of(1, ChronoUnit.MINUTES))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Create rate limit bucket for premium users.
     * 1000 requests per minute with burst of 1500.
     */
    public Bucket createPremiumBucket() {
        Bandwidth limit = Bandwidth.classic(
            1500,
            Refill.intervally(1000, java.time.Duration.of(1, ChronoUnit.MINUTES))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }

    /**
     * Create unlimited bucket for admin users.
     */
    public Bucket createAdminBucket() {
        Bandwidth limit = Bandwidth.classic(
            1_000_000,
            Refill.intervally(1_000_000, java.time.Duration.of(1, ChronoUnit.SECONDS))
        );
        return Bucket4j.builder().addLimit(limit).build();
    }
}
