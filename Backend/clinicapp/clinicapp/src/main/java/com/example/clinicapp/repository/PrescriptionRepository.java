package com.example.clinicapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinicapp.entity.Prescription;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByAppointmentId(Long appointmentId);

    List<Prescription> findByDoctorId(Long doctorId);
    
    List<Prescription> findByPatientId(Long patientId);
}
