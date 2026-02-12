package com.example.clinicapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "prescription_referrals")
public class PrescriptionReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonBackReference
    private Prescription prescription;

    private String doctorName;
    private String speciality;
    private String phone;
    private String email;

    // Constructors
    public PrescriptionReferral() {}

    public PrescriptionReferral(Prescription prescription, String doctorName, 
                               String speciality, String phone, String email) {
        this.prescription = prescription;
        this.doctorName = doctorName;
        this.speciality = speciality;
        this.phone = phone;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Prescription getPrescription() {
        return prescription;
    }

    public void setPrescription(Prescription prescription) {
        this.prescription = prescription;
    }

    public String getDoctorName() {
        return doctorName;
    }

    public void setDoctorName(String doctorName) {
        this.doctorName = doctorName;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}