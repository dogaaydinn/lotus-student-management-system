package com.lotus.lotusSPM.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.lotus.lotusSPM.logging.RequestResponseLoggingInterceptor;
import com.lotus.lotusSPM.ratelimit.RateLimitInterceptor;

import lombok.extern.slf4j.Slf4j;

/**
 * Web MVC Configuration
 * Configures interceptors, CORS, and other web-related settings
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Autowired
    private RequestResponseLoggingInterceptor loggingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Request/Response logging (first interceptor)
        log.info("Registering request/response logging interceptor");
        registry.addInterceptor(loggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/actuator/**", "/health/**", "/static/**");

        // Rate limiting (second interceptor)
        log.info("Registering rate limit interceptor");
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**", "/student/**", "/students/**",
                                "/coordinator/**", "/admin/**", "/opportunities/**",
                                "/messages/**", "/notifications/**", "/documents/**")
                .excludePathPatterns("/actuator/**", "/health/**");
    }
}
