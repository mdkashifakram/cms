package com.example.clinicapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Patient Consent Entity
 * 
 * Tracks patient consent for HIPAA/GDPR compliance.
 * Records when patients grant or revoke consent for data processing.
 * 
 * HIPAA Requirements:
 * - §164.508: Uses and disclosures for which an authorization is required
 * - §164.522: Rights to request privacy protection
 * 
 * GDPR Requirements:
 * - Article 6: Lawfulness of processing (consent)
 * - Article 7: Conditions for consent
 * - Article 17: Right to erasure ("right to be forgotten")
 */
@Entity
@Table(name = "patient_consents", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"patient_id", "consent_type"})
})
public class PatientConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "patient_id", nullable = false)
    private Long patientId;

    @Column(name = "consent_type", nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private ConsentType consentType;

    @Column(nullable = false)
    private Boolean granted = false;

    @Column(name = "granted_at")
    private LocalDateTime grantedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "consent_version", length = 20)
    private String consentVersion;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Types of consent that can be tracked
     */
    public enum ConsentType {
        // HIPAA Consents
        TREATMENT,              // Consent to receive treatment
        PAYMENT,                // Consent for billing/payment processing
        HEALTHCARE_OPERATIONS,  // Consent for healthcare operations
        DISCLOSURE_TO_FAMILY,   // Consent to share info with family
        RESEARCH,               // Consent for research participation
        MARKETING,              // Consent for marketing communications
        
        // GDPR Consents
        DATA_PROCESSING,        // General data processing consent
        DATA_RETENTION,         // Consent for data retention period
        THIRD_PARTY_SHARING,    // Consent for sharing with third parties
        AUTOMATED_DECISIONS,    // Consent for automated decision making
        CROSS_BORDER_TRANSFER,  // Consent for data transfer outside region
        
        // Communication
        EMAIL_NOTIFICATIONS,    // Consent for email notifications
        SMS_NOTIFICATIONS,      // Consent for SMS notifications
        APPOINTMENT_REMINDERS   // Consent for appointment reminders
    }

    // Constructors
    public PatientConsent() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public PatientConsent(Long patientId, ConsentType consentType) {
        this();
        this.patientId = patientId;
        this.consentType = consentType;
    }

    // Business methods
    
    /**
     * Grant consent
     */
    public void grant(String ipAddress, String version) {
        this.granted = true;
        this.grantedAt = LocalDateTime.now();
        this.revokedAt = null;
        this.ipAddress = ipAddress;
        this.consentVersion = version;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Revoke consent
     */
    public void revoke() {
        this.granted = false;
        this.revokedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Check if consent is currently active
     */
    public boolean isActive() {
        return granted && revokedAt == null;
    }

    // PrePersist and PreUpdate hooks
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public ConsentType getConsentType() {
        return consentType;
    }

    public void setConsentType(ConsentType consentType) {
        this.consentType = consentType;
    }

    public Boolean getGranted() {
        return granted;
    }

    public void setGranted(Boolean granted) {
        this.granted = granted;
    }

    public LocalDateTime getGrantedAt() {
        return grantedAt;
    }

    public void setGrantedAt(LocalDateTime grantedAt) {
        this.grantedAt = grantedAt;
    }

    public LocalDateTime getRevokedAt() {
        return revokedAt;
    }

    public void setRevokedAt(LocalDateTime revokedAt) {
        this.revokedAt = revokedAt;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getConsentVersion() {
        return consentVersion;
    }

    public void setConsentVersion(String consentVersion) {
        this.consentVersion = consentVersion;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "PatientConsent{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", consentType=" + consentType +
                ", granted=" + granted +
                ", grantedAt=" + grantedAt +
                '}';
    }
}
