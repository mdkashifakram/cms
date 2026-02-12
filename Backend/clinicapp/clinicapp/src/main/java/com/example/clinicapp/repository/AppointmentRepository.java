package com.example.clinicapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.clinicapp.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>{
	List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);

	// Case-insensitive status lookup
	List<Appointment> findByStatusIgnoreCase(String status);

	// For Consults: Find completed appointments by patient ID (case-insensitive)
	@Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId AND LOWER(a.status) = LOWER(:status)")
	List<Appointment> findByPatient_IdAndStatusIgnoreCase(@Param("patientId") Long patientId, @Param("status") String status);

	// For Consults: Find all appointments by patient ID
	List<Appointment> findByPatient_Id(Long patientId);

	// For Consults: Find completed appointments ordered by time (most recent first, case-insensitive)
	@Query("SELECT a FROM Appointment a WHERE LOWER(a.status) = LOWER(:status) ORDER BY a.appointmentTime DESC")
	List<Appointment> findByStatusIgnoreCaseOrderByAppointmentTimeDesc(@Param("status") String status);
}
