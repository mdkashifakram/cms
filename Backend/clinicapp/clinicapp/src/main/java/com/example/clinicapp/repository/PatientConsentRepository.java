package com.example.clinicapp.repository;

import com.example.clinicapp.entity.PatientConsent;
import com.example.clinicapp.entity.PatientConsent.ConsentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Patient Consent Repository
 * 
 * Provides queries for consent management and compliance reporting.
 */
@Repository
public interface PatientConsentRepository extends JpaRepository<PatientConsent, Long> {

    // Find all consents for a patient
    List<PatientConsent> findByPatientId(Long patientId);

    // Find specific consent type for a patient
    Optional<PatientConsent> findByPatientIdAndConsentType(Long patientId, ConsentType consentType);

    // Find all granted consents for a patient
    List<PatientConsent> findByPatientIdAndGrantedTrue(Long patientId);

    // Find all revoked consents for a patient
    @Query("SELECT c FROM PatientConsent c WHERE c.patientId = :patientId AND c.revokedAt IS NOT NULL")
    List<PatientConsent> findRevokedByPatientId(@Param("patientId") Long patientId);

    // Check if patient has granted a specific consent
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM PatientConsent c " +
           "WHERE c.patientId = :patientId AND c.consentType = :consentType " +
           "AND c.granted = true AND c.revokedAt IS NULL")
    boolean hasActiveConsent(@Param("patientId") Long patientId, 
                             @Param("consentType") ConsentType consentType);

    // Get all patients who need consent renewal (e.g., consent older than 1 year)
    @Query("SELECT DISTINCT c.patientId FROM PatientConsent c " +
           "WHERE c.granted = true AND c.grantedAt < :expiryDate")
    List<Long> findPatientsNeedingConsentRenewal(@Param("expiryDate") java.time.LocalDateTime expiryDate);

    // Count patients by consent type (for reporting)
    @Query("SELECT c.consentType, COUNT(c) FROM PatientConsent c " +
           "WHERE c.granted = true AND c.revokedAt IS NULL " +
           "GROUP BY c.consentType")
    List<Object[]> countActiveConsentsByType();

    // Delete all consents for a patient (GDPR right to erasure)
    void deleteByPatientId(Long patientId);
}
