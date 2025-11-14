package com.lotus.lotusSPM.ratelimit;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Rate limiting interceptor using Redis
 * Implements token bucket algorithm for API rate limiting
 */
@Component
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private static final int MAX_REQUESTS_PER_MINUTE = 100;
    private static final int MAX_REQUESTS_PER_HOUR = 1000;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String clientIp = getClientIp(request);
        String username = request.getRemoteUser();
        String identifier = username != null ? "user:" + username : "ip:" + clientIp;

        // Check minute rate limit
        String minuteKey = "rate_limit:minute:" + identifier;
        Long minuteCount = redisTemplate.opsForValue().increment(minuteKey);

        if (minuteCount == 1) {
            redisTemplate.expire(minuteKey, 1, TimeUnit.MINUTES);
        }

        if (minuteCount != null && minuteCount > MAX_REQUESTS_PER_MINUTE) {
            log.warn("Rate limit exceeded for {} - {} requests in the last minute", identifier, minuteCount);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_MINUTE));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            response.setContentType("application/json");
            return false;
        }

        // Check hour rate limit
        String hourKey = "rate_limit:hour:" + identifier;
        Long hourCount = redisTemplate.opsForValue().increment(hourKey);

        if (hourCount != null && hourCount == 1) {
            redisTemplate.expire(hourKey, 1, TimeUnit.HOURS);
        }

        if (hourCount != null && hourCount > MAX_REQUESTS_PER_HOUR) {
            log.warn("Hourly rate limit exceeded for {} - {} requests in the last hour", identifier, hourCount);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("X-RateLimit-Limit", String.valueOf(MAX_REQUESTS_PER_HOUR));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", "3600");
            response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\"}");
            response.setContentType("application/json");
            return false;
        }

        // Add rate limit headers
        response.setHeader("X-RateLimit-Limit-Minute", String.valueOf(MAX_REQUESTS_PER_MINUTE));
        response.setHeader("X-RateLimit-Remaining-Minute",
                String.valueOf(MAX_REQUESTS_PER_MINUTE - (minuteCount != null ? minuteCount : 0)));
        response.setHeader("X-RateLimit-Limit-Hour", String.valueOf(MAX_REQUESTS_PER_HOUR));
        response.setHeader("X-RateLimit-Remaining-Hour",
                String.valueOf(MAX_REQUESTS_PER_HOUR - (hourCount != null ? hourCount : 0)));

        log.debug("Rate limit check passed for {} - minute: {}/{}, hour: {}/{}",
                identifier, minuteCount, MAX_REQUESTS_PER_MINUTE, hourCount, MAX_REQUESTS_PER_HOUR);

        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
