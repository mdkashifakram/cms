package com.example.clinicapp.service;

import com.example.clinicapp.entity.PatientConsent;
import com.example.clinicapp.entity.PatientConsent.ConsentType;
import com.example.clinicapp.repository.PatientConsentRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Patient Consent Service
 * 
 * Manages patient consent for HIPAA/GDPR compliance.
 * Provides methods for granting, revoking, and checking consent status.
 */
@Service
public class PatientConsentService {

    private static final Logger logger = LoggerFactory.getLogger(PatientConsentService.class);
    private static final String CURRENT_CONSENT_VERSION = "1.0";

    @Autowired
    private PatientConsentRepository consentRepository;

    @Autowired
    private AuditService auditService;

    /**
     * Grant consent for a patient
     */
    @Transactional
    public PatientConsent grantConsent(Long patientId, ConsentType consentType, String notes) {
        String ipAddress = getClientIpAddress();
        
        PatientConsent consent = consentRepository
            .findByPatientIdAndConsentType(patientId, consentType)
            .orElse(new PatientConsent(patientId, consentType));

        consent.grant(ipAddress, CURRENT_CONSENT_VERSION);
        consent.setNotes(notes);

        PatientConsent saved = consentRepository.save(consent);
        
        // Audit log
        auditService.log("CONSENT_GRANTED", "PATIENT_CONSENT", saved.getId(),
            String.format("Patient %d granted %s consent", patientId, consentType));

        logger.info("Consent granted: Patient={}, Type={}", patientId, consentType);
        return saved;
    }

    /**
     * Revoke consent for a patient
     */
    @Transactional
    public PatientConsent revokeConsent(Long patientId, ConsentType consentType) {
        Optional<PatientConsent> consentOpt = consentRepository
            .findByPatientIdAndConsentType(patientId, consentType);

        if (consentOpt.isEmpty()) {
            throw new IllegalArgumentException("Consent not found for patient " + patientId);
        }

        PatientConsent consent = consentOpt.get();
        consent.revoke();

        PatientConsent saved = consentRepository.save(consent);
        
        // Audit log
        auditService.log("CONSENT_REVOKED", "PATIENT_CONSENT", saved.getId(),
            String.format("Patient %d revoked %s consent", patientId, consentType));

        logger.info("Consent revoked: Patient={}, Type={}", patientId, consentType);
        return saved;
    }

    /**
     * Check if patient has active consent
     */
    public boolean hasActiveConsent(Long patientId, ConsentType consentType) {
        return consentRepository.hasActiveConsent(patientId, consentType);
    }

    /**
     * Get all consents for a patient
     */
    public List<PatientConsent> getPatientConsents(Long patientId) {
        return consentRepository.findByPatientId(patientId);
    }

    /**
     * Get consent status summary for a patient
     */
    public Map<ConsentType, Boolean> getConsentSummary(Long patientId) {
        List<PatientConsent> consents = consentRepository.findByPatientId(patientId);
        Map<ConsentType, Boolean> summary = new HashMap<>();
        
        for (ConsentType type : ConsentType.values()) {
            summary.put(type, false);
        }
        
        for (PatientConsent consent : consents) {
            summary.put(consent.getConsentType(), consent.isActive());
        }
        
        return summary;
    }

    /**
     * Grant multiple consents at once (for initial patient registration)
     */
    @Transactional
    public void grantInitialConsents(Long patientId, List<ConsentType> consentTypes) {
        String ipAddress = getClientIpAddress();
        
        for (ConsentType type : consentTypes) {
            PatientConsent consent = new PatientConsent(patientId, type);
            consent.grant(ipAddress, CURRENT_CONSENT_VERSION);
            consentRepository.save(consent);
        }
        
        auditService.log("INITIAL_CONSENTS_GRANTED", "PATIENT", patientId,
            String.format("Granted %d initial consents", consentTypes.size()));
    }

    /**
     * Delete all patient data (GDPR Right to Erasure)
     * This should be called as part of a larger data deletion process
     */
    @Transactional
    public void deletePatientConsents(Long patientId) {
        consentRepository.deleteByPatientId(patientId);
        
        auditService.log("CONSENTS_DELETED", "PATIENT", patientId,
            "All consents deleted for GDPR compliance");
        
        logger.info("All consents deleted for patient: {}", patientId);
    }

    /**
     * Get patients who need consent renewal
     */
    public List<Long> getPatientsNeedingRenewal() {
        return consentRepository.findPatientsNeedingConsentRenewal(java.time.LocalDateTime.now().minusDays(365));
    }

    /**
     * Get consent statistics for compliance reporting
     */
    public Map<String, Long> getConsentStatistics() {
        List<Object[]> rawStats = consentRepository.countActiveConsentsByType();
        Map<String, Long> stats = new HashMap<>();
        
        for (Object[] row : rawStats) {
            ConsentType type = (ConsentType) row[0];
            Long count = (Long) row[1];
            stats.put(type.name(), count);
        }
        
        return stats;
    }

    // Helper method
    private String getClientIpAddress() {
        try {
            ServletRequestAttributes attrs = 
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest request = attrs.getRequest();
                String xForwardedFor = request.getHeader("X-Forwarded-For");
                if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
                    return xForwardedFor.split(",")[0].trim();
                }
                return request.getRemoteAddr();
            }
        } catch (Exception e) {
            logger.warn("Could not get client IP address");
        }
        return "unknown";
    }
}
