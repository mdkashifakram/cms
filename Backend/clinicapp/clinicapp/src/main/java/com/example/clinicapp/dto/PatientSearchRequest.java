package com.example.clinicapp.dto;

import jakarta.validation.constraints.Size;

/**
 * CMS-012: Patient Search Request DTO
 *
 * Supports two search modes:
 * 1. By Patient ID only
 * 2. By Name + Phone Number combination
 *
 * Custom validation ensures either patientId is provided,
 * OR both name and phoneNumber are provided.
 */
public class PatientSearchRequest {

    private Long patientId;

    @Size(max = 100, message = "Patient name cannot exceed 100 characters")
    private String name;

    @Size(min = 10, max = 15, message = "Phone number must be between 10-15 characters")
    private String phoneNumber;

    // Default constructor
    public PatientSearchRequest() {
    }

    // Constructor for ID-based search
    public PatientSearchRequest(Long patientId) {
        this.patientId = patientId;
    }

    // Constructor for name+phone search
    public PatientSearchRequest(String name, String phoneNumber) {
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    // Full constructor
    public PatientSearchRequest(Long patientId, String name, String phoneNumber) {
        this.patientId = patientId;
        this.name = name;
        this.phoneNumber = phoneNumber;
    }

    /**
     * Validates that either patientId is provided,
     * or both name and phoneNumber are provided.
     */
    public boolean isValid() {
        if (patientId != null) {
            return true;
        }
        return name != null && !name.isBlank() &&
               phoneNumber != null && !phoneNumber.isBlank();
    }

    /**
     * Returns the search mode based on provided fields.
     */
    public SearchMode getSearchMode() {
        if (patientId != null) {
            return SearchMode.BY_ID;
        }
        if (name != null && !name.isBlank() &&
            phoneNumber != null && !phoneNumber.isBlank()) {
            return SearchMode.BY_NAME_PHONE;
        }
        return SearchMode.INVALID;
    }

    public enum SearchMode {
        BY_ID,
        BY_NAME_PHONE,
        INVALID
    }

    // Getters and Setters
    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
        // Mask sensitive data in logs
        StringBuilder sb = new StringBuilder("PatientSearchRequest{");
        if (patientId != null) {
            sb.append("patientId=").append(patientId);
        } else {
            sb.append("name='").append(name != null ? name.substring(0, Math.min(2, name.length())) + "***" : "null").append('\'');
            sb.append(", phoneNumber='***").append(phoneNumber != null && phoneNumber.length() > 4 ?
                    phoneNumber.substring(phoneNumber.length() - 4) : "****").append('\'');
        }
        sb.append('}');
        return sb.toString();
    }
}
