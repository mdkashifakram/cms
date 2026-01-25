package com.example.clinicapp.entity;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonBackReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    @JsonBackReference("appointment-prescription")
    private Appointment appointment;

    
    private LocalDateTime issuedAt;
    private LocalDateTime lastUpdated; 
    private String status = "DRAFT"; 

    // === VITALS (Embedded as individual columns) ===
    private String temperature;
    private String bloodPressure;
    private String pulse;
    private String spo2;
    private String height;
    private String weight;
    private String bmi;
    private String waistHip;

    // === TEXT SECTIONS ===
    @Column(columnDefinition = "TEXT")
    private String complaints;

    @Column(columnDefinition = "TEXT")
    private String pastHistory;

    @Column(columnDefinition = "TEXT")
    @jakarta.persistence.Convert(converter = com.example.clinicapp.converter.StringListConverter.class)
    private List<String> diagnosis;

    @Column(columnDefinition = "TEXT")
    private String advice; // Added

    @Column(columnDefinition = "TEXT")
    private String testRequested; // Changed from testInvestigations

    @Column(columnDefinition = "TEXT")
    private String pastMedications;

    @Column(columnDefinition = "TEXT")
    private String generalExamination;

    // Next Visit fields
    private String nextVisitNumber;
    private String nextVisitUnit;
    private String nextVisitDate;

    // === MEDICINES (Only relational entity) ===
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionMedicine> medicines;

    // === REFERRALS ===
    @OneToMany(mappedBy = "prescription", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PrescriptionReferral> referrals;

    // Constructors
    public Prescription() {}

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Doctor getDoctor() {
        return doctor;
    }

    public void setDoctor(Doctor doctor) {
        this.doctor = doctor;
    }

    public Patient getPatient() {
        return patient;
    }

    public void setPatient(Patient patient) {
        this.patient = patient;
    }

    public Appointment getAppointment() {
        return appointment;
    }

    public void setAppointment(Appointment appointment) {
        this.appointment = appointment;
    }


    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public void setIssuedAt(LocalDateTime issuedAt) {
        this.issuedAt = issuedAt;
    }

    public LocalDateTime getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(LocalDateTime lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getTemperature() {
        return temperature;
    }

    public void setTemperature(String temperature) {
        this.temperature = temperature;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(String bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public String getPulse() {
        return pulse;
    }

    public void setPulse(String pulse) {
        this.pulse = pulse;
    }

    public String getSpo2() {
        return spo2;
    }

    public void setSpo2(String spo2) {
        this.spo2 = spo2;
    }

    public String getHeight() {
        return height;
    }

    public void setHeight(String height) {
        this.height = height;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getBmi() {
        return bmi;
    }

    public void setBmi(String bmi) {
        this.bmi = bmi;
    }

    public String getWaistHip() {
        return waistHip;
    }

    public void setWaistHip(String waistHip) {
        this.waistHip = waistHip;
    }

    public String getComplaints() {
        return complaints;
    }

    public void setComplaints(String complaints) {
        this.complaints = complaints;
    }

    public String getPastHistory() {
        return pastHistory;
    }

    public void setPastHistory(String pastHistory) {
        this.pastHistory = pastHistory;
    }

    public List<String> getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(List<String> diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getTestRequested() {
        return testRequested;
    }

    public void setTestRequested(String testRequested) {
        this.testRequested = testRequested;
    }

    public String getPastMedications() {
        return pastMedications;
    }

    public void setPastMedications(String pastMedications) {
        this.pastMedications = pastMedications;
    }

    public String getGeneralExamination() {
        return generalExamination;
    }

    public void setGeneralExamination(String generalExamination) {
        this.generalExamination = generalExamination;
    }

    public String getNextVisitNumber() {
        return nextVisitNumber;
    }

    public void setNextVisitNumber(String nextVisitNumber) {
        this.nextVisitNumber = nextVisitNumber;
    }

    public String getNextVisitUnit() {
        return nextVisitUnit;
    }

    public void setNextVisitUnit(String nextVisitUnit) {
        this.nextVisitUnit = nextVisitUnit;
    }

    public String getNextVisitDate() {
        return nextVisitDate;
    }

    public void setNextVisitDate(String nextVisitDate) {
        this.nextVisitDate = nextVisitDate;
    }

    public List<PrescriptionMedicine> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<PrescriptionMedicine> medicines) {
        this.medicines = medicines;
    }

    public List<PrescriptionReferral> getReferrals() {
        return referrals;
    }

    public void setReferrals(List<PrescriptionReferral> referrals) {
        this.referrals = referrals;
    }
    
 // =============================
    // UTILITY METHODS
    // =============================

    @PrePersist
    protected void onCreate() {
        this.issuedAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.lastUpdated = LocalDateTime.now();
    }
    @Override
    public String toString() {
        return "Prescription{id=" + id + ", status='" + status + "', issuedAt=" + issuedAt + "}";
    }

}