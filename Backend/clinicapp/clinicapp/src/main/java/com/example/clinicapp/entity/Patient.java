package com.example.clinicapp.entity;

import com.example.clinicapp.dto.PatientDto;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


@Entity
@Table(name = "patients")
public class Patient {
	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "patient_seq")
	@SequenceGenerator(name = "patient_seq", sequenceName = "patients_id_seq", allocationSize = 1, initialValue = 1000)
	private Long id;
	private String name;
	private Integer age;
	private String phoneNumber;
	private String gender;
    private String address;
    private String city;
    private String pin;
	public Patient() {
		// TODO Auto-generated constructor stub
	}
	public Patient(PatientDto patientDto) {
        this.name = patientDto.getName();
        this.phoneNumber = patientDto.getPhoneNumber();
        this.gender = patientDto.getGender();
        this.age = Integer.parseInt(patientDto.getAge());
        this.address = patientDto.getAddress();
        this.city = patientDto.getCity();
        this.pin = patientDto.getPin();
    }
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
	public Integer getAge() {
		return age;
	}
	public void setAge(Integer age) {
		this.age = age;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
	}
	public String getPin() {
		return pin;
	}
	public void setPin(String pin) {
		this.pin = pin;
	}
	
	
}
