package com.example.clinicapp.repository;

import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.entity.PrescriptionReferral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionReferralRepository extends JpaRepository<PrescriptionReferral, Long> {
    void deleteByPrescription(Prescription prescription);
}