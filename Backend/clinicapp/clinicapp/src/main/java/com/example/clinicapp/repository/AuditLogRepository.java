package com.example.clinicapp.repository;

import com.example.clinicapp.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Audit Log Repository
 * 
 * Provides queries for audit log retrieval and security monitoring.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    // Find by username
    List<AuditLog> findByUsernameOrderByTimestampDesc(String username);

    Page<AuditLog> findByUsernameOrderByTimestampDesc(String username, Pageable pageable);

    // Find by action type
    List<AuditLog> findByActionOrderByTimestampDesc(String action);

    // Find by resource
    List<AuditLog> findByResourceTypeAndResourceIdOrderByTimestampDesc(
        String resourceType, Long resourceId);

    // Find by time range
    List<AuditLog> findByTimestampBetweenOrderByTimestampDesc(
        LocalDateTime start, LocalDateTime end);

    // Find by username and time range
    List<AuditLog> findByUsernameAndTimestampBetweenOrderByTimestampDesc(
        String username, LocalDateTime start, LocalDateTime end);

    // Security monitoring: Failed login attempts
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' " +
           "AND a.ipAddress = :ip AND a.timestamp > :since")
    long countFailedLoginsByIpSince(@Param("ip") String ipAddress, 
                                     @Param("since") LocalDateTime since);

    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.action = 'LOGIN_FAILED' " +
           "AND a.username = :username AND a.timestamp > :since")
    long countFailedLoginsByUsernameSince(@Param("username") String username,
                                           @Param("since") LocalDateTime since);

    // Security monitoring: Suspicious activity detection
    @Query("SELECT a FROM AuditLog a WHERE a.username = :username " +
           "AND a.timestamp > :since ORDER BY a.timestamp DESC")
    List<AuditLog> findRecentActivityByUsername(@Param("username") String username,
                                                 @Param("since") LocalDateTime since);

    // Count actions in time period (for rate limiting/anomaly detection)
    @Query("SELECT COUNT(a) FROM AuditLog a WHERE a.username = :username " +
           "AND a.action = :action AND a.timestamp > :since")
    long countActionsByUserSince(@Param("username") String username,
                                  @Param("action") String action,
                                  @Param("since") LocalDateTime since);

    // Find all patient data access
    @Query("SELECT a FROM AuditLog a WHERE a.resourceType = 'PATIENT' " +
           "AND a.resourceId = :patientId ORDER BY a.timestamp DESC")
    List<AuditLog> findAllPatientAccess(@Param("patientId") Long patientId);

    // Delete old audit logs (for cleanup, if configured)
    void deleteByTimestampBefore(LocalDateTime before);
}
