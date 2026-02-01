package com.example.clinicapp.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "prescription_medicines")
public class PrescriptionMedicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "prescription_id", nullable = false)
    @JsonBackReference
    private Prescription prescription;

    private String medicineName;
    private String type;
    private String dosage;
    private String whenToTake;
    private String frequency;
    private String duration;
    private String notes;

    // Constructors
    public PrescriptionMedicine() {}

    public PrescriptionMedicine(Long id, Prescription prescription, String medicineName, 
                               String type, String dosage, String whenToTake, 
                               String frequency, String duration, String notes) {
        this.id = id;
        this.prescription = prescription;
        this.medicineName = medicineName;
        this.type = type;
        this.dosage = dosage;
        this.whenToTake = whenToTake;
        this.frequency = frequency;
        this.duration = duration;
        this.notes = notes;
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

    public String getMedicineName() {
        return medicineName;
    }

    public void setMedicineName(String medicineName) {
        this.medicineName = medicineName;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getWhenToTake() {
        return whenToTake;
    }

    public void setWhenToTake(String whenToTake) {
        this.whenToTake = whenToTake;
    }

    public String getFrequency() {
        return frequency;
    }

    public void setFrequency(String frequency) {
        this.frequency = frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}