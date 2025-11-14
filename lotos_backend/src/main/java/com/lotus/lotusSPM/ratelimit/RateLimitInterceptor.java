package com.lotus.lotusSPM.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate Limit Interceptor - Enforces rate limits on API endpoints.
 *
 * Enterprise Pattern: API Gateway Pattern / Throttling
 *
 * Features:
 * - Per-user rate limiting
 * - IP-based rate limiting for anonymous users
 * - Tiered rate limits based on user role
 * - HTTP 429 (Too Many Requests) responses
 * - X-RateLimit headers for client awareness
 * - Distributed rate limiting via Bucket4j
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitInterceptor implements HandlerInterceptor {

    private final RateLimitConfig rateLimitConfig;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String key = resolveKey(request);
        Bucket bucket = resolveBucket(key, request);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            // Request allowed - add rate limit headers
            response.addHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            response.addHeader("X-RateLimit-Retry-After-Seconds", "60");
            return true;
        } else {
            // Rate limit exceeded
            long waitForRefill = TimeUnit.NANOSECONDS.toSeconds(probe.getNanosToWaitForRefill());

            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.addHeader("X-RateLimit-Retry-After-Seconds", String.valueOf(waitForRefill));
            response.getWriter().write(
                "{\"error\": \"Rate limit exceeded\", \"retryAfterSeconds\": " + waitForRefill + "}"
            );

            log.warn("Rate limit exceeded for key: {}, retry after: {} seconds", key, waitForRefill);
            return false;
        }
    }

    /**
     * Resolves the rate limit key (username or IP address).
     */
    private String resolveKey(HttpServletRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return "user:" + auth.getName();
        }

        // Use IP for anonymous users
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return "ip:" + ip;
    }

    /**
     * Resolves the appropriate bucket based on user role.
     */
    private Bucket resolveBucket(String key, HttpServletRequest request) {
        return bucketCache.computeIfAbsent(key, k -> {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated()) {
                if (auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                    return rateLimitConfig.createAdminBucket();
                }
                if (auth.getAuthorities().stream()
                        .anyMatch(a -> a.getAuthority().equals("ROLE_PREMIUM"))) {
                    return rateLimitConfig.createPremiumBucket();
                }
                return rateLimitConfig.createAuthenticatedBucket();
            }

            return rateLimitConfig.createAnonymousBucket();
        });
    }
}
