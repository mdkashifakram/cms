package com.example.clinicapp.service;

import com.example.clinicapp.entity.AuditLog;
import com.example.clinicapp.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Service
 * 
 * Provides centralized audit logging for HIPAA §164.312(b) compliance.
 * Logs all security-relevant events asynchronously to avoid performance impact.
 */
@Service
public class AuditService {

    private static final Logger logger = LoggerFactory.getLogger(AuditService.class);

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Log an audit event asynchronously
     */
    @Async
    public void log(String action, String resourceType, Long resourceId, String details) {
        try {
            String username = getCurrentUsername();
            String ipAddress = getClientIpAddress();
            String method = getHttpMethod();
            String userAgent = getUserAgent();

            AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .resourceType(resourceType)
                .resourceId(resourceId)
                .ipAddress(ipAddress)
                .method(method)
                .details(details)
                .userAgent(userAgent)
                .build();

            auditLogRepository.save(auditLog);
            logger.debug("Audit log saved: {} - {} - {}", action, resourceType, username);
        } catch (Exception e) {
            logger.error("Failed to save audit log: {}", e.getMessage());
        }
    }

    /**
     * Log without resource ID
     */
    public void log(String action, String resourceType, String details) {
        log(action, resourceType, null, details);
    }

    /**
     * Log authentication events
     */
    public void logAuthEvent(String action, String username, String details) {
        try {
            String ipAddress = getClientIpAddress();
            String userAgent = getUserAgent();

            AuditLog auditLog = AuditLog.builder()
                .username(username)
                .action(action)
                .resourceType("AUTH")
                .ipAddress(ipAddress)
                .method("POST")
                .details(details)
                .userAgent(userAgent)
                .build();

            auditLogRepository.save(auditLog);
            logger.info("Auth event logged: {} for user {}", action, username);
        } catch (Exception e) {
            logger.error("Failed to log auth event: {}", e.getMessage());
        }
    }

    /**
     * Log patient data access (HIPAA requirement)
     */
    @Async
    public void logPatientAccess(Long patientId, String action, String details) {
        log(action, "PATIENT", patientId, details);
    }

    /**
     * Log prescription access
     */
    @Async
    public void logPrescriptionAccess(Long prescriptionId, String action, String details) {
        log(action, "PRESCRIPTION", prescriptionId, details);
    }

    /**
     * Get audit trail for a patient (for compliance reporting)
     */
    public List<AuditLog> getPatientAuditTrail(Long patientId) {
        return auditLogRepository.findAllPatientAccess(patientId);
    }

    /**
     * Get user activity for security review
     */
    public List<AuditLog> getUserActivity(String username, LocalDateTime since) {
        return auditLogRepository.findRecentActivityByUsername(username, since);
    }

    /**
     * Check for suspicious activity (multiple failed logins)
     */
    public boolean isSuspiciousActivity(String username, String ipAddress) {
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        
        long failedByUsername = auditLogRepository.countFailedLoginsByUsernameSince(
            username, oneHourAgo);
        long failedByIp = auditLogRepository.countFailedLoginsByIpSince(
            ipAddress, oneHourAgo);

        // Flag as suspicious if > 5 failed attempts
        return failedByUsername > 5 || failedByIp > 10;
    }

    // Helper methods
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            return auth.getName();
        }
        return "anonymous";
    }

    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                
                // Check for forwarded headers (proxy/load balancer)
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
        } catch (Exception e) {
            logger.warn("Could not get client IP address: {}", e.getMessage());
        }
        return "unknown";
    }

    private String getHttpMethod() {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                return attrs.getRequest().getMethod();
            }
        } catch (Exception e) {
            // Ignore
        }
        return "UNKNOWN";
    }

    private String getUserAgent() {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                String userAgent = attrs.getRequest().getHeader("User-Agent");
                // Truncate to avoid very long user agent strings
                if (userAgent != null && userAgent.length() > 255) {
                    return userAgent.substring(0, 255);
                }
                return userAgent;
            }
        } catch (Exception e) {
            // Ignore
        }
        return null;
    }
}
