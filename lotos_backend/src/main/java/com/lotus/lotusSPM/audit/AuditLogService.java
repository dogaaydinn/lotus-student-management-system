package com.lotus.lotusSPM.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

@Service
public class AuditLogService {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogService.class);

    @Autowired(required = false)
    private HttpServletRequest request;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Async
    public void logAction(Long userId, String userType, String username, String action,
                         String entityType, String entityId, Object oldValue, Object newValue) {
        try {
            String oldValueJson = oldValue != null ? objectMapper.writeValueAsString(oldValue) : null;
            String newValueJson = newValue != null ? objectMapper.writeValueAsString(newValue) : null;

            String ipAddress = getClientIp();
            String userAgent = getUserAgent();

            AuditLog auditLog = new AuditLog(
                userId, userType, username, action,
                entityType, entityId, oldValueJson, newValueJson,
                ipAddress, userAgent
            );

            auditLogRepository.save(auditLog);

            logger.info("Audit log created: user={}, action={}, entity={}/{}",
                username, action, entityType, entityId);
        } catch (Exception e) {
            logger.error("Failed to create audit log", e);
        }
    }

    private String getClientIp() {
        if (request == null) {
            return "SYSTEM";
        }

        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }

        // Handle multiple IPs (take first one)
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }

        return ip;
    }

    private String getUserAgent() {
        if (request == null) {
            return "SYSTEM";
        }

        String userAgent = request.getHeader("User-Agent");
        return userAgent != null ? userAgent : "Unknown";
    }
}
