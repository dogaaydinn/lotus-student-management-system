package com.lotus.lotusSPM.versioning;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * API Versioning Configuration.
 *
 * Enterprise Pattern: API Versioning
 *
 * Strategies:
 * 1. URI Versioning: /api/v1/students, /api/v2/students
 * 2. Header Versioning: Accept: application/vnd.lotus.v1+json
 * 3. Parameter Versioning: /api/students?version=1
 * 4. Content Negotiation: Accept header
 *
 * Best Practices:
 * - Maintain backward compatibility
 * - Deprecate old versions gracefully
 * - Document version changes
 * - Support multiple versions simultaneously
 * - Provide migration guides
 *
 * Versioning Policy:
 * - v1: Initial release
 * - v2: Breaking changes with migration path
 * - v3: Major architectural changes
 */
@Configuration
public class ApiVersionConfig implements WebMvcConfigurer {

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        configurer
            .favorParameter(true)
            .parameterName("version")
            .defaultContentType(org.springframework.http.MediaType.APPLICATION_JSON);
    }
}
