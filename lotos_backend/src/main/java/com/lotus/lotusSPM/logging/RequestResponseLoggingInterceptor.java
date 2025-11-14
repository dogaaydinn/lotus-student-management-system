package com.lotus.lotusSPM.logging;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import lombok.extern.slf4j.Slf4j;

/**
 * Logging interceptor for request/response tracking
 * Provides observability into API usage patterns and performance
 */
@Component
@Slf4j
public class RequestResponseLoggingInterceptor implements HandlerInterceptor {

    private static final String REQUEST_ID = "requestId";
    private static final String START_TIME = "startTime";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String requestId = UUID.randomUUID().toString();
        request.setAttribute(REQUEST_ID, requestId);
        request.setAttribute(START_TIME, System.currentTimeMillis());

        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String clientIp = getClientIp(request);
        String userAgent = request.getHeader("User-Agent");

        log.info("[{}] --> {} {} {} | IP: {} | UA: {}",
                requestId, method, uri,
                queryString != null ? "?" + queryString : "",
                clientIp,
                userAgent != null ? userAgent.substring(0, Math.min(50, userAgent.length())) : "N/A");

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                          ModelAndView modelAndView) throws Exception {
        // Can add custom logic here if needed
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                               Exception ex) throws Exception {

        String requestId = (String) request.getAttribute(REQUEST_ID);
        Long startTime = (Long) request.getAttribute(START_TIME);

        if (startTime != null) {
            long duration = System.currentTimeMillis() - startTime;
            int status = response.getStatus();
            String method = request.getMethod();
            String uri = request.getRequestURI();

            if (ex != null) {
                log.error("[{}] <-- {} {} | Status: {} | Duration: {}ms | Error: {}",
                        requestId, method, uri, status, duration, ex.getMessage(), ex);
            } else if (status >= 500) {
                log.error("[{}] <-- {} {} | Status: {} | Duration: {}ms",
                        requestId, method, uri, status, duration);
            } else if (status >= 400) {
                log.warn("[{}] <-- {} {} | Status: {} | Duration: {}ms",
                        requestId, method, uri, status, duration);
            } else if (duration > 1000) {
                log.warn("[{}] <-- {} {} | Status: {} | Duration: {}ms [SLOW]",
                        requestId, method, uri, status, duration);
            } else {
                log.info("[{}] <-- {} {} | Status: {} | Duration: {}ms",
                        requestId, method, uri, status, duration);
            }
        }
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
