package com.lotus.lotusSPM.multitenant;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Intercepts requests and sets tenant context based on subdomain or header
 */
@Component
public class TenantInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String tenantId = extractTenantId(request);

        if (tenantId != null) {
            TenantContext.setCurrentTenant(tenantId);
        }

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        TenantContext.clear();
    }

    private String extractTenantId(HttpServletRequest request) {
        // Try header first (for API clients)
        String tenantId = request.getHeader("X-Tenant-ID");

        if (tenantId == null) {
            // Extract from subdomain
            String host = request.getServerName();
            if (host != null && host.contains(".")) {
                String[] parts = host.split("\\.");
                if (parts.length > 2) {
                    tenantId = parts[0]; // First part is subdomain
                }
            }
        }

        return tenantId;
    }
}
