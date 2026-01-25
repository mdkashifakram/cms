package com.example.clinicapp.dto;

public class MedicineDto {
    private String type;
    private String medicine; // Changed from medicineName to medicine
    private String dosage;
    private String when; // Changed from whenToTake to when
    private String frequency;
    private String duration;
    private String notes;

    public MedicineDto() {}

    public MedicineDto(String type, String medicine, String dosage, String when, 
                       String frequency, String duration, String notes) {
        this.type = type;
        this.medicine = medicine;
        this.dosage = dosage;
        this.when = when;
        this.frequency = frequency;
        this.duration = duration;
        this.notes = notes;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getMedicine() {
        return medicine;
    }

    public void setMedicine(String medicine) {
        this.medicine = medicine;
    }

    public String getDosage() {
        return dosage;
    }

    public void setDosage(String dosage) {
        this.dosage = dosage;
    }

    public String getWhen() {
        return when;
    }

    public void setWhen(String when) {
        this.when = when;
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