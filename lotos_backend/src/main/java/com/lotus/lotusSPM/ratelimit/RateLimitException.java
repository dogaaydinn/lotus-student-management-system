package com.lotus.lotusSPM.ratelimit;

/**
 * Exception thrown when rate limit is exceeded.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
public class RateLimitException extends RuntimeException {

    private final String key;
    private final int retryAfterSeconds;

    public RateLimitException(String message, String key, int retryAfterSeconds) {
        super(message);
        this.key = key;
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public String getKey() {
        return key;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}
