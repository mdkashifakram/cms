package com.example.clinicapp.entity;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;


@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "appointment_seq")
    @SequenceGenerator(name = "appointment_seq", sequenceName = "appointments_id_seq", allocationSize = 1, initialValue = 1000)
    private Long id;

    @NotBlank(message = "Patient name is mandatory")
    @Pattern(regexp = "^[a-zA-Z\\s]+$", message = "Patient name must contain only letters and spaces")
    private String patientName;
    
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = true)
    private Doctor doctor;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = true)
    private Patient patient;

    @OneToOne(mappedBy = "appointment", cascade = CascadeType.ALL)
    @JsonManagedReference("appointment-prescription")
    private Prescription prescription;

    @NotNull(message = "Appointment time is mandatory")
    @Future(message = "Appointment time must be in the future")
    private LocalDateTime appointmentTime;

    @NotBlank(message = "Status is mandatory")
    private String status;

    @Size(max = 500, message = "Details must not exceed 500 characters")
    private String details;
    
    @Email(message = "Email should be valid") 
    private String patientEmail;


    public Appointment() {}

	public Appointment(Long id,
		String patientName,
			Doctor doctor, Patient patient, Prescription prescription,
			LocalDateTime appointmentTime,
			String status,
			String details,
			String patientEmail) {
		
		this.id = id;
		this.patientName = patientName;
		this.doctor = doctor;
		this.patient = patient;
		this.prescription = prescription;
		this.appointmentTime = appointmentTime;
		this.status = status;
		this.details = details;
		this.patientEmail = patientEmail;
	}



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

	public Prescription getPrescription() {
		return prescription;
	}

	public String getPatientName() {
		return patientName;
	}

	public void setPatientName(String patientName) {
		this.patientName = patientName;
	}

	public void setPrescription(Prescription prescription) {
		this.prescription = prescription;
	}

	public LocalDateTime getAppointmentTime() {
		return appointmentTime;
	}

	public void setAppointmentTime(LocalDateTime appointmentTime) {
		this.appointmentTime = appointmentTime;
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

	public String getPatientEmail() {
		return patientEmail;
	}

	public void setPatientEmail(String patientEmail) {
		this.patientEmail = patientEmail;
	}

	

}

