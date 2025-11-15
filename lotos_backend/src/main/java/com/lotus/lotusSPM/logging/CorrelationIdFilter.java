package com.lotus.lotusSPM.logging;

import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

/**
 * Correlation ID Filter
 * Adds unique correlation ID to each request for distributed tracing
 */
@Component
@Order(1)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String REQUEST_ID_HEADER = "X-Request-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final String USER_ID_MDC_KEY = "userId";
    private static final String REQUEST_URI_MDC_KEY = "requestUri";
    private static final String REQUEST_METHOD_MDC_KEY = "requestMethod";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            // Get or generate correlation ID
            String correlationId = request.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Get or generate request ID
            String requestId = request.getHeader(REQUEST_ID_HEADER);
            if (requestId == null || requestId.isEmpty()) {
                requestId = UUID.randomUUID().toString();
            }

            // Add to MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put(REQUEST_ID_MDC_KEY, requestId);
            MDC.put(REQUEST_URI_MDC_KEY, request.getRequestURI());
            MDC.put(REQUEST_METHOD_MDC_KEY, request.getMethod());

            // Extract user ID from request if available
            String userId = request.getHeader("X-User-ID");
            if (userId != null && !userId.isEmpty()) {
                MDC.put(USER_ID_MDC_KEY, userId);
            }

            // Add to response headers
            response.addHeader(CORRELATION_ID_HEADER, correlationId);
            response.addHeader(REQUEST_ID_HEADER, requestId);

            filterChain.doFilter(request, response);
        } finally {
            // Clean up MDC after request
            MDC.clear();
        }
    }
}
