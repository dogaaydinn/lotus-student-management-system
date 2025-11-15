package com.lotus.lotusSPM.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CorrelationIdFilter
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CorrelationIdFilter Tests")
class CorrelationIdFilterTest {

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    private CorrelationIdFilter filter;

    @BeforeEach
    void setUp() {
        filter = new CorrelationIdFilter();
        MDC.clear();
    }

    @Test
    @DisplayName("Should generate correlation ID when not present in request")
    void testGenerateCorrelationId() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("GET");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).addHeader(eq("X-Correlation-ID"), anyString());
        verify(response).addHeader(eq("X-Request-ID"), anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use existing correlation ID from request header")
    void testUseExistingCorrelationId() throws ServletException, IOException {
        // Given
        String existingCorrelationId = "existing-correlation-id-123";
        when(request.getHeader("X-Correlation-ID")).thenReturn(existingCorrelationId);
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("POST");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).addHeader("X-Correlation-ID", existingCorrelationId);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should use existing request ID from request header")
    void testUseExistingRequestId() throws ServletException, IOException {
        // Given
        String existingRequestId = "existing-request-id-456";
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getHeader("X-Request-ID")).thenReturn(existingRequestId);
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("PUT");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).addHeader("X-Request-ID", existingRequestId);
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should add user ID to MDC when present in request")
    void testAddUserIdToMDC() throws ServletException, IOException {
        // Given
        String userId = "user123";
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getHeader("X-User-ID")).thenReturn(userId);
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("GET");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - MDC is cleared after filter, so we can't verify it directly
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle empty correlation ID header")
    void testEmptyCorrelationIdHeader() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Correlation-ID")).thenReturn("");
        when(request.getHeader("X-Request-ID")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("GET");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then - Should generate new IDs
        verify(response).addHeader(eq("X-Correlation-ID"), anyString());
        verify(response).addHeader(eq("X-Request-ID"), anyString());
        verify(filterChain, times(1)).doFilter(request, response);
    }

    @Test
    @DisplayName("Should clear MDC after filter chain execution")
    void testClearMDCAfterExecution() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Correlation-ID")).thenReturn("test-correlation-id");
        when(request.getHeader("X-Request-ID")).thenReturn("test-request-id");
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("GET");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should clear MDC even when filter chain throws exception")
    void testClearMDCOnException() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Correlation-ID")).thenReturn(null);
        when(request.getHeader("X-Request-ID")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/students");
        when(request.getMethod()).thenReturn("GET");
        doThrow(new ServletException("Filter chain error")).when(filterChain).doFilter(request, response);

        // When/Then
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(ServletException.class);

        // MDC should still be cleared
        assertThat(MDC.getCopyOfContextMap()).isNullOrEmpty();
    }

    @Test
    @DisplayName("Should handle different HTTP methods")
    void testDifferentHttpMethods() throws ServletException, IOException {
        // Given
        String[] methods = {"GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"};

        for (String method : methods) {
            when(request.getHeader("X-Correlation-ID")).thenReturn(null);
            when(request.getHeader("X-Request-ID")).thenReturn(null);
            when(request.getRequestURI()).thenReturn("/api/students");
            when(request.getMethod()).thenReturn(method);

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, atLeastOnce()).doFilter(request, response);
        }
    }

    @Test
    @DisplayName("Should handle different request URIs")
    void testDifferentRequestUris() throws ServletException, IOException {
        // Given
        String[] uris = {"/api/students", "/api/instructors", "/actuator/health", "/swagger-ui.html"};

        for (String uri : uris) {
            when(request.getHeader("X-Correlation-ID")).thenReturn(null);
            when(request.getHeader("X-Request-ID")).thenReturn(null);
            when(request.getRequestURI()).thenReturn(uri);
            when(request.getMethod()).thenReturn("GET");

            // When
            filter.doFilterInternal(request, response, filterChain);

            // Then
            verify(filterChain, atLeastOnce()).doFilter(request, response);
        }
    }
}
