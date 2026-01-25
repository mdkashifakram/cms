package com.example.clinicapp.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public class PatientDto {

    private Long id;
    
    @NotBlank(message = "Name is mandatory")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @NotBlank(message = "Phone number is mandatory")
    @Size(min = 10, max = 15, message = "Phone number must be between 10 and 15 digits")
    @Pattern(regexp = "^[0-9]*$", message = "Phone number must be numeric")
    private String phoneNumber;

    @NotBlank(message = "Gender is mandatory")
    private String gender;

    @NotBlank(message = "Age is mandatory")
    @Pattern(regexp = "^[0-9]*$", message = "Age must be numeric")
    private String age;

    @NotBlank(message = "Address is mandatory")
    private String address;

    @NotBlank(message = "City is mandatory")
    private String city;

    @NotBlank(message = "Pin is mandatory")
    @Size(min = 6, max = 6, message = "Pin must be exactly 6 digits")
    @Pattern(regexp = "^[0-9]*$", message = "Pin must be numeric")
    private String pin;

    private LocalDateTime timeSlot;

    // Constructors
    public PatientDto() {}

    public PatientDto(Long id, String name, String phoneNumber, String gender, String age, 
                      String address, String city, String pin, LocalDateTime timeSlot) {
        this.id = id;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.gender = gender;
        this.age = age;
        this.address = address;
        this.city = city;
        this.pin = pin;
        this.timeSlot = timeSlot;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }

    public LocalDateTime getTimeSlot() { return timeSlot; }
    public void setTimeSlot(LocalDateTime timeSlot) { this.timeSlot = timeSlot; }
}