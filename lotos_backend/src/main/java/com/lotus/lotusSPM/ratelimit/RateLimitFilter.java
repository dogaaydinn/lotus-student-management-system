package com.lotus.lotusSPM.ratelimit;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to enforce rate limiting on all HTTP requests.
 * Checks both per-IP and per-user rate limits.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Component
@Order(2) // Run after correlation ID filter
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    public RateLimitFilter(RateLimitService rateLimitService) {
        this.rateLimitService = rateLimitService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String ipAddress = getClientIpAddress(request);
        String requestUri = request.getRequestURI();

        // Skip rate limiting for health checks and actuator endpoints
        if (requestUri.startsWith("/actuator")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Check IP-based rate limit first
        if (!rateLimitService.allowIpRequest(ipAddress)) {
            log.warn("Rate limit exceeded for IP: {} on URI: {}", ipAddress, requestUri);
            sendRateLimitError(response, "Too many requests from this IP address");
            return;
        }

        // For authenticated requests, also check user-based rate limit
        String username = request.getRemoteUser();
        if (username != null) {
            // Special handling for login endpoints
            if (requestUri.contains("/login")) {
                if (!rateLimitService.allowLoginAttempt(username)) {
                    log.warn("Login rate limit exceeded for user: {}", username);
                    sendRateLimitError(response, "Too many login attempts. Please try again later.");
                    return;
                }
            }
            // Admin endpoints have stricter limits
            else if (requestUri.contains("/admin")) {
                if (!rateLimitService.allowAdminRequest(username)) {
                    log.warn("Admin rate limit exceeded for user: {}", username);
                    sendRateLimitError(response, "Too many admin requests");
                    return;
                }
            }
            // Regular user endpoints
            else {
                if (!rateLimitService.allowUserRequest(username)) {
                    log.warn("User rate limit exceeded for user: {}", username);
                    sendRateLimitError(response, "Too many requests. Please slow down.");
                    return;
                }
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Send rate limit error response
     */
    private void sendRateLimitError(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType("application/json");
        response.setHeader("X-RateLimit-Retry-After", "60");

        String jsonResponse = String.format(
            "{\"timestamp\":\"%s\",\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"%s\"}",
            java.time.Instant.now().toString(),
            message
        );

        response.getWriter().write(jsonResponse);
    }

    /**
     * Get client IP address from request, considering proxy headers
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take first IP from X-Forwarded-For header
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
}
