package com.example.clinicapp.dto;

import jakarta.validation.constraints.NotNull;

public class DoctorDto {

    @NotNull(message = "Doctor ID is required")
    private Long id;
    private String name;
    private String specialty;
    private String contactNumber;
    private String email;

    // Default constructor
    public DoctorDto() {}

    // Parameterized constructor
    public DoctorDto(Long id, String name, String specialty, String contactNumber, String email) {
        this.id = id;
        this.name = name;
        this.specialty = specialty;
        this.contactNumber = contactNumber;
        this.email = email;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
