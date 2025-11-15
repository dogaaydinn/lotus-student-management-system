package com.lotus.lotusSPM.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration for security headers.
 * Implements OWASP recommended security headers for protection against
 * common web vulnerabilities.
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        registrationBean.setOrder(1);
        return registrationBean;
    }

    /**
     * Filter that adds security headers to all HTTP responses
     */
    private static class SecurityHeadersFilter implements Filter {

        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Content Security Policy - Prevents XSS and data injection attacks
            httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self' data:; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none'; " +
                "base-uri 'self'; " +
                "form-action 'self';"
            );

            // HTTP Strict Transport Security - Forces HTTPS
            httpResponse.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains; preload");

            // X-Content-Type-Options - Prevents MIME type sniffing
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // X-Frame-Options - Prevents clickjacking
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // X-XSS-Protection - Enables XSS filter in older browsers
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // Referrer-Policy - Controls referrer information
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Permissions-Policy - Controls browser features
            httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), " +
                "microphone=(), " +
                "camera=(), " +
                "payment=(), " +
                "usb=(), " +
                "magnetometer=(), " +
                "gyroscope=(), " +
                "accelerometer=()");

            // X-Permitted-Cross-Domain-Policies - Restricts Adobe Flash/PDF cross-domain requests
            httpResponse.setHeader("X-Permitted-Cross-Domain-Policies", "none");

            // Cache-Control for sensitive endpoints
            String uri = httpRequest.getRequestURI();
            if (uri.contains("/api/auth") || uri.contains("/api/admin")) {
                httpResponse.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, private");
                httpResponse.setHeader("Pragma", "no-cache");
                httpResponse.setHeader("Expires", "0");
            }

            chain.doFilter(request, response);
        }

        @Override
        public void init(FilterConfig filterConfig) throws ServletException {
            // Initialization logic if needed
        }

        @Override
        public void destroy() {
            // Cleanup logic if needed
        }
    }
}
