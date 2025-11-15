package com.lotus.lotusSPM.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for method-level rate limiting.
 * Can be applied to controller methods to enforce specific rate limits.
 *
 * Usage:
 * @RateLimit(capacity = 10, refillTokens = 10, refillDuration = 60)
 * public ResponseEntity<?> someEndpoint() { ... }
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    /**
     * Maximum capacity of the rate limit bucket
     */
    int capacity() default 100;

    /**
     * Number of tokens to refill
     */
    int refillTokens() default 100;

    /**
     * Refill duration in seconds
     */
    long refillDuration() default 60;

    /**
     * Type of rate limit
     */
    RateLimitType type() default RateLimitType.USER;

    enum RateLimitType {
        USER,    // Per authenticated user
        IP,      // Per IP address
        GLOBAL   // Global limit
    }
}
