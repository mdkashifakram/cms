package com.example.clinicapp.dto;

import java.time.LocalDateTime;

public class AppointmentDto {

    private Long id;
    private String patientName;
    private String patientEmail;
    private String status;
    private String details;
    private LocalDateTime appointmentTime;

    // Flattened patient fields
    private Long patientId;
    private Integer patientAge;
    private String patientGender;
    private String patientPhone;

    // Flattened doctor fields
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialty;
    private String doctorEmail;
    private String doctorPhone;

    // Prescription reference
    private Long prescriptionId;

    // Constructors
    public AppointmentDto() {}

    public AppointmentDto(Long id, String patientName, String patientEmail, String status,
                         String details, LocalDateTime appointmentTime,
                         Long patientId, Integer patientAge, String patientGender, String patientPhone,
                         Long doctorId, String doctorName, String doctorSpecialty,
                         String doctorEmail, String doctorPhone, Long prescriptionId) {
        this.id = id;
        this.patientName = patientName;
        this.patientEmail = patientEmail;
        this.status = status;
        this.details = details;
        this.appointmentTime = appointmentTime;
        this.patientId = patientId;
        this.patientAge = patientAge;
        this.patientGender = patientGender;
        this.patientPhone = patientPhone;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.doctorSpecialty = doctorSpecialty;
        this.doctorEmail = doctorEmail;
        this.doctorPhone = doctorPhone;
        this.prescriptionId = prescriptionId;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getPatientEmail() {
        return patientEmail;
    }

    public void setPatientEmail(String patientEmail) {
        this.patientEmail = patientEmail;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public LocalDateTime getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(LocalDateTime appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Integer getPatientAge() {
        return patientAge;
    }

    public void setPatientAge(Integer patientAge) {
        this.patientAge = patientAge;
    }

    public String getPatientGender() {
        return patientGender;
    }

    public void setPatientGender(String patientGender) {
        this.patientGender = patientGender;
    }

    public String getPatientPhone() {
        return patientPhone;
    }

    public void setPatientPhone(String patientPhone) {
        this.patientPhone = patientPhone;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getDoctorSpecialty() {
        return doctorSpecialty;
    }

    public void setDoctorSpecialty(String doctorSpecialty) {
        this.doctorSpecialty = doctorSpecialty;
    }

    public String getDoctorEmail() {
        return doctorEmail;
    }

    public void setDoctorEmail(String doctorEmail) {
        this.doctorEmail = doctorEmail;
    }

    public String getDoctorPhone() {
        return doctorPhone;
    }

    public void setDoctorPhone(String doctorPhone) {
        this.doctorPhone = doctorPhone;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }
}
