package com.example.clinicapp.repository;

import com.example.clinicapp.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("SELECT p FROM Patient p WHERE p.name = :name AND p.phoneNumber = :phoneNumber")
    Optional<Patient> findByNameAndPhoneNumber(@Param("name") String name, @Param("phoneNumber") String phoneNumber);
}
