package com.lotus.lotusSPM.ratelimit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RateLimitInterceptor
 * Tests rate limiting logic for DDoS protection
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RateLimitInterceptor Security Tests")
class RateLimitInterceptorTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @InjectMocks
    private RateLimitInterceptor rateLimitInterceptor;

    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private Object handler;

    @BeforeEach
    void setUp() {
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        handler = new Object();

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("Should allow request when under rate limit")
    void preHandle_WhenUnderLimit_ShouldAllowRequest() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.value());
        verify(redisTemplate.opsForValue(), times(2)).increment(anyString());
        verify(redisTemplate, times(2)).expire(anyString(), anyLong(), any(TimeUnit.class));
    }

    @Test
    @DisplayName("Should block request when minute rate limit exceeded")
    void preHandle_WhenMinuteLimitExceeded_ShouldBlockRequest() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(contains(":minute:"))).thenReturn(101L); // Over 100 limit
        when(valueOperations.increment(contains(":hour:"))).thenReturn(101L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("100");
        assertThat(response.getHeader("X-RateLimit-Remaining")).isEqualTo("0");
        assertThat(response.getHeader("Retry-After")).isEqualTo("60");
    }

    @Test
    @DisplayName("Should block request when hour rate limit exceeded")
    void preHandle_WhenHourLimitExceeded_ShouldBlockRequest() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(contains(":minute:"))).thenReturn(50L); // Under minute limit
        when(valueOperations.increment(contains(":hour:"))).thenReturn(1001L); // Over 1000 limit

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isFalse();
        assertThat(response.getStatus()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS.value());
        assertThat(response.getHeader("X-RateLimit-Limit")).isEqualTo("1000");
        assertThat(response.getHeader("Retry-After")).isEqualTo("3600");
    }

    @Test
    @DisplayName("Should use X-Forwarded-For header when present")
    void preHandle_WithXForwardedFor_ShouldUseForwardedIP() throws Exception {
        // Given
        request.addHeader("X-Forwarded-For", "203.0.113.195, 70.41.3.18");
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        // Verify it uses the forwarded IP (first in list)
        verify(valueOperations, atLeastOnce()).increment(contains("203.0.113.195"));
    }

    @Test
    @DisplayName("Should use X-Real-IP header when X-Forwarded-For absent")
    void preHandle_WithXRealIP_ShouldUseRealIP() throws Exception {
        // Given
        request.addHeader("X-Real-IP", "203.0.113.195");
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        verify(valueOperations, atLeastOnce()).increment(contains("203.0.113.195"));
    }

    @Test
    @DisplayName("Should use remote address when no forwarding headers")
    void preHandle_WithoutForwardingHeaders_ShouldUseRemoteAddr() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.100");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        verify(valueOperations, atLeastOnce()).increment(contains("192.168.1.100"));
    }

    @Test
    @DisplayName("Should set rate limit headers on successful request")
    void preHandle_WhenAllowed_ShouldSetRateLimitHeaders() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(contains(":minute:"))).thenReturn(25L);
        when(valueOperations.increment(contains(":hour:"))).thenReturn(150L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        assertThat(response.getHeader("X-RateLimit-Limit-Minute")).isEqualTo("100");
        assertThat(response.getHeader("X-RateLimit-Remaining-Minute")).isEqualTo("75"); // 100 - 25
        assertThat(response.getHeader("X-RateLimit-Limit-Hour")).isEqualTo("1000");
        assertThat(response.getHeader("X-RateLimit-Remaining-Hour")).isEqualTo("850"); // 1000 - 150
    }

    @Test
    @DisplayName("Should handle first request and set expiry")
    void preHandle_FirstRequest_ShouldSetExpiry() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        verify(redisTemplate).expire(contains(":minute:"), eq(1L), eq(TimeUnit.MINUTES));
        verify(redisTemplate).expire(contains(":hour:"), eq(1L), eq(TimeUnit.HOURS));
    }

    @Test
    @DisplayName("Should use authenticated username when available")
    void preHandle_WithAuthenticatedUser_ShouldUseUsername() throws Exception {
        // Given
        request.setRemoteUser("john123");
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(anyString())).thenReturn(1L);

        // When
        boolean result = rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(result).isTrue();
        verify(valueOperations, atLeastOnce()).increment(contains("user:john123"));
    }

    @Test
    @DisplayName("Should handle concurrent requests correctly")
    void preHandle_ConcurrentRequests_ShouldTrackSeparately() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(anyString()))
                .thenReturn(1L)   // First minute request
                .thenReturn(2L)   // First hour request
                .thenReturn(2L)   // Second minute request
                .thenReturn(3L);  // Second hour request

        // When
        rateLimitInterceptor.preHandle(request, response, handler);
        rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        verify(valueOperations, times(4)).increment(anyString());
    }

    @Test
    @DisplayName("Should return error message in response body when rate limited")
    void preHandle_WhenRateLimited_ShouldReturnErrorMessage() throws Exception {
        // Given
        request.setRemoteAddr("192.168.1.1");
        when(valueOperations.increment(contains(":minute:"))).thenReturn(101L);
        when(valueOperations.increment(contains(":hour:"))).thenReturn(101L);

        // When
        rateLimitInterceptor.preHandle(request, response, handler);

        // Then
        assertThat(response.getContentAsString()).contains("Too many requests");
        assertThat(response.getContentType()).isEqualTo("application/json");
    }
}
