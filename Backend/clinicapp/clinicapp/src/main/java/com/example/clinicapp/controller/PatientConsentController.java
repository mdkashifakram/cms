package com.example.clinicapp.controller;

import com.example.clinicapp.entity.PatientConsent;
import com.example.clinicapp.entity.PatientConsent.ConsentType;
import com.example.clinicapp.service.AuditService;
import com.example.clinicapp.service.PatientConsentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Patient Consent Controller
 * 
 * REST API for managing patient consent (HIPAA/GDPR compliance).
 * 
 * Endpoints:
 * - GET  /api/consent/{patientId}         - Get all consents for patient
 * - GET  /api/consent/{patientId}/summary - Get consent summary
 * - POST /api/consent/{patientId}/grant   - Grant consent
 * - POST /api/consent/{patientId}/revoke  - Revoke consent
 * - GET  /api/consent/stats               - Get consent statistics (admin)
 */
@RestController
@RequestMapping("/api/consent")
public class PatientConsentController {

    @Autowired
    private PatientConsentService consentService;

    @Autowired
    private AuditService auditService;

    /**
     * Get all consents for a patient
     * CMS-CONS-001: Only authenticated doctors and admins can view consent records
     */
    @GetMapping("/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<PatientConsent>> getPatientConsents(@PathVariable Long patientId) {
        List<PatientConsent> consents = consentService.getPatientConsents(patientId);

        auditService.log("CONSENT_VIEW", "PATIENT", patientId,
            "Viewed consent records");

        return ResponseEntity.ok(consents);
    }

    /**
     * Get consent summary for a patient (which consents are active)
     * CMS-CONS-002: Only authenticated doctors and admins can view consent summary
     */
    @GetMapping("/{patientId}/summary")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Map<ConsentType, Boolean>> getConsentSummary(@PathVariable Long patientId) {
        Map<ConsentType, Boolean> summary = consentService.getConsentSummary(patientId);
        return ResponseEntity.ok(summary);
    }

    /**
     * Grant consent for a patient
     */
    @PostMapping("/{patientId}/grant")
    public ResponseEntity<PatientConsent> grantConsent(
            @PathVariable Long patientId,
            @RequestParam ConsentType consentType,
            @RequestParam(required = false) String notes) {
        
        PatientConsent consent = consentService.grantConsent(patientId, consentType, notes);
        return ResponseEntity.ok(consent);
    }

    /**
     * Grant multiple consents at once (for initial registration)
     */
    @PostMapping("/{patientId}/grant-multiple")
    public ResponseEntity<String> grantMultipleConsents(
            @PathVariable Long patientId,
            @RequestBody List<ConsentType> consentTypes) {
        
        consentService.grantInitialConsents(patientId, consentTypes);
        return ResponseEntity.ok("Consents granted successfully");
    }

    /**
     * Revoke consent for a patient
     */
    @PostMapping("/{patientId}/revoke")
    public ResponseEntity<PatientConsent> revokeConsent(
            @PathVariable Long patientId,
            @RequestParam ConsentType consentType) {
        
        PatientConsent consent = consentService.revokeConsent(patientId, consentType);
        return ResponseEntity.ok(consent);
    }

    /**
     * Check if patient has active consent
     * CMS-CONS-003: Only authenticated doctors and admins can check consent status
     */
    @GetMapping("/{patientId}/check")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Boolean> checkConsent(
            @PathVariable Long patientId,
            @RequestParam ConsentType consentType) {

        boolean hasConsent = consentService.hasActiveConsent(patientId, consentType);
        return ResponseEntity.ok(hasConsent);
    }

    /**
     * Get consent statistics (admin only)
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getConsentStatistics() {
        Map<String, Long> stats = consentService.getConsentStatistics();
        return ResponseEntity.ok(stats);
    }

    /**
     * Get patients needing consent renewal (admin only)
     */
    @GetMapping("/renewal-needed")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Long>> getPatientsNeedingRenewal() {
        List<Long> patientIds = consentService.getPatientsNeedingRenewal();
        return ResponseEntity.ok(patientIds);
    }

    /**
     * Get available consent types
     * CMS-CONS-004: Only authenticated users can view consent types
     */
    @GetMapping("/types")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    public ResponseEntity<ConsentType[]> getConsentTypes() {
        return ResponseEntity.ok(ConsentType.values());
    }
}
