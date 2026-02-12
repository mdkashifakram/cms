package com.example.clinicapp.dto;

public class ReferralDto {
    private String doctor;
    private String speciality;
    private String phone;
    private String email;

    public ReferralDto() {}

    public ReferralDto(String doctor, String speciality, String phone, String email) {
        this.doctor = doctor;
        this.speciality = speciality;
        this.phone = phone;
        this.email = email;
    }

    public String getDoctor() {
        return doctor;
    }

    public void setDoctor(String doctor) {
        this.doctor = doctor;
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