package com.example.clinicapp.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinicapp.entity.Appointment;

public interface AppointmentRepository extends JpaRepository<Appointment, Long>{
	List<Appointment> findByAppointmentTimeBetween(LocalDateTime start, LocalDateTime end);
	List<Appointment> findByStatus(String status);
}
