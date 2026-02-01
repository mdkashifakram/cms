package com.example.clinicapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Audit Log Entity
 * 
 * Tracks all security-relevant actions for HIPAA §164.312(b) compliance.
 * Records who accessed what data, when, and from where.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_username", columnList = "username"),
    @Index(name = "idx_audit_timestamp", columnList = "timestamp"),
    @Index(name = "idx_audit_action", columnList = "action"),
    @Index(name = "idx_audit_resource", columnList = "resourceType, resourceId")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String resourceType;

    private Long resourceId;

    @Column(nullable = false)
    private String ipAddress;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(nullable = false)
    private String method; // HTTP method: GET, POST, PUT, DELETE

    @Column
    private String userAgent;

    @Column
    private Integer responseStatus;

    @Column
    private Long durationMs;

    // Constructors
    public AuditLog() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLog(String username, String action, String resourceType, Long resourceId, 
                   String ipAddress, String method) {
        this();
        this.username = username;
        this.action = action;
        this.resourceType = resourceType;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.method = method;
    }

    // Builder pattern for convenience
    public static AuditLogBuilder builder() {
        return new AuditLogBuilder();
    }

    public static class AuditLogBuilder {
        private final AuditLog log = new AuditLog();

        public AuditLogBuilder username(String username) {
            log.username = username;
            return this;
        }

        public AuditLogBuilder action(String action) {
            log.action = action;
            return this;
        }

        public AuditLogBuilder resourceType(String resourceType) {
            log.resourceType = resourceType;
            return this;
        }

        public AuditLogBuilder resourceId(Long resourceId) {
            log.resourceId = resourceId;
            return this;
        }

        public AuditLogBuilder ipAddress(String ipAddress) {
            log.ipAddress = ipAddress;
            return this;
        }

        public AuditLogBuilder method(String method) {
            log.method = method;
            return this;
        }

        public AuditLogBuilder details(String details) {
            log.details = details;
            return this;
        }

        public AuditLogBuilder userAgent(String userAgent) {
            log.userAgent = userAgent;
            return this;
        }

        public AuditLogBuilder responseStatus(Integer status) {
            log.responseStatus = status;
            return this;
        }

        public AuditLogBuilder durationMs(Long duration) {
            log.durationMs = duration;
            return this;
        }

        public AuditLog build() {
            return log;
        }
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public Integer getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(Integer responseStatus) {
        this.responseStatus = responseStatus;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }

    @Override
    public String toString() {
        return "AuditLog{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", action='" + action + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", timestamp=" + timestamp +
                '}';
    }
}
