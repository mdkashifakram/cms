package com.example.clinicapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.clinicapp.entity.PrescriptionTemplate;

public interface PrescriptionTemplateRepository extends JpaRepository<PrescriptionTemplate, Long> {

    Optional<PrescriptionTemplate> findTopByDoctorIdAndSectionOrderByCreatedAtDesc(Long doctorId, String section);
    Optional<PrescriptionTemplate> findByDoctorIdAndSectionAndTemplateName(Long doctorId, String section, String templateName);
  List<PrescriptionTemplate> findByDoctorIdAndSectionAndTemplateNameContainingIgnoreCase(Long doctorId, String section, String templateName);
    
    List<PrescriptionTemplate> findByDoctorIdAndSection(Long doctorId, String section);

}
