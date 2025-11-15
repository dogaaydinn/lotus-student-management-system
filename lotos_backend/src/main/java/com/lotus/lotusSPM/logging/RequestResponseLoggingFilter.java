package com.lotus.lotusSPM.logging;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Enumeration;

/**
 * Filter to log all HTTP requests and responses with performance metrics.
 * Includes correlation IDs for distributed tracing.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Component
@Order(3) // Run after correlation ID and rate limit filters
@Slf4j
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // Skip logging for actuator endpoints
        if (request.getRequestURI().startsWith("/actuator")) {
            chain.doFilter(request, response);
            return;
        }

        // Wrap request and response for content caching
        ContentCachingRequestWrapper wrappedRequest = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper wrappedResponse = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();

        try {
            // Log incoming request
            logRequest(wrappedRequest);

            // Process request
            chain.doFilter(wrappedRequest, wrappedResponse);

        } finally {
            long duration = System.currentTimeMillis() - startTime;

            // Log response
            logResponse(wrappedResponse, duration);

            // Copy response body to actual response
            wrappedResponse.copyBodyToResponse();
        }
    }

    /**
     * Log incoming HTTP request
     */
    private void logRequest(ContentCachingRequestWrapper request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String correlationId = MDC.get("correlationId");

        StringBuilder message = new StringBuilder();
        message.append("HTTP Request: ").append(method).append(" ").append(uri);

        if (queryString != null) {
            message.append("?").append(queryString);
        }

        log.info("{} [Correlation-ID: {}]", message, correlationId);

        // Log headers (excluding sensitive ones)
        if (log.isDebugEnabled()) {
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                if (!isSensitiveHeader(headerName)) {
                    log.debug("Request Header: {} = {}", headerName, request.getHeader(headerName));
                }
            }
        }
    }

    /**
     * Log HTTP response with performance metrics
     */
    private void logResponse(ContentCachingResponseWrapper response, long duration) {
        int status = response.getStatus();
        String correlationId = MDC.get("correlationId");

        String logLevel = status >= 500 ? "ERROR" : status >= 400 ? "WARN" : "INFO";

        String message = String.format("HTTP Response: Status=%d, Duration=%dms [Correlation-ID: %s]",
            status, duration, correlationId);

        if ("ERROR".equals(logLevel)) {
            log.error(message);
        } else if ("WARN".equals(logLevel)) {
            log.warn(message);
        } else {
            log.info(message);
        }

        // Log slow requests
        if (duration > 1000) {
            log.warn("Slow request detected: {}ms [Correlation-ID: {}]", duration, correlationId);
        }

        // Log response headers
        if (log.isDebugEnabled()) {
            response.getHeaderNames().forEach(headerName -> {
                if (!isSensitiveHeader(headerName)) {
                    log.debug("Response Header: {} = {}", headerName, response.getHeader(headerName));
                }
            });
        }
    }

    /**
     * Check if header contains sensitive information
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerName = headerName.toLowerCase();
        return lowerName.contains("authorization") ||
               lowerName.contains("cookie") ||
               lowerName.contains("password") ||
               lowerName.contains("token") ||
               lowerName.contains("secret") ||
               lowerName.contains("api-key");
    }
}
