package com.example.clinicapp.repository;

import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.entity.PrescriptionMedicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PrescriptionMedicineRepository extends JpaRepository<PrescriptionMedicine, Long> {
    void deleteByPrescription(Prescription prescription);
}
