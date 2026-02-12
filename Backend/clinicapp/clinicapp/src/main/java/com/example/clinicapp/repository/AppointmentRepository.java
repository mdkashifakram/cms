package com.example.clinicapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinicapp.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>{
	List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);
	List<Appointment> findByStatus(String status);

	// For Consults: Find completed appointments by patient ID
	List<Appointment> findByPatient_IdAndStatus(Long patientId, String status);

	// For Consults: Find all appointments by patient ID
	List<Appointment> findByPatient_Id(Long patientId);

	// For Consults: Find completed appointments ordered by time (most recent first)
	List<Appointment> findByStatusOrderByAppointmentTimeDesc(String status);
}
