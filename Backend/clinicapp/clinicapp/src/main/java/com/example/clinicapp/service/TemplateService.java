package com.example.clinicapp.service;

import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.entity.PrescriptionTemplate;
import com.example.clinicapp.repository.PrescriptionRepository;
import com.example.clinicapp.repository.PrescriptionTemplateRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TemplateService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionTemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    public TemplateService(PrescriptionRepository prescriptionRepository,
                           PrescriptionTemplateRepository templateRepository,
                           ObjectMapper objectMapper) {
        this.prescriptionRepository = prescriptionRepository;
        this.templateRepository = templateRepository;
        this.objectMapper = objectMapper;
    }

    // Load last section for patient
    public String loadPrev(Long patientId, String section) throws JsonProcessingException {
        Optional<Prescription> lastPrescriptionOpt =
                prescriptionRepository.findByPatientId(patientId).stream()
                        .sorted((a,b) -> b.getIssuedAt().compareTo(a.getIssuedAt()))
                        .findFirst();

        if (lastPrescriptionOpt.isEmpty()) return null;

        Prescription last = lastPrescriptionOpt.get();
        Object sectionData = extractSection(last, section);
        return objectMapper.writeValueAsString(sectionData);
    }

    // Load doctor's saved template
    public String loadTemplate(Long doctorId, String section, String templateName) {
        return templateRepository.findByDoctorIdAndSectionAndTemplateName(doctorId, section, templateName)
                .map(PrescriptionTemplate::getDataJson)
                .orElse("[]");
    }

    // Save template
    // --- we will search with template name;
    public void saveTemplate(Long doctorId, String section, String templateName, Object sectionData) throws JsonProcessingException {
        String json = objectMapper.writeValueAsString(sectionData);
        PrescriptionTemplate template = new PrescriptionTemplate();
        template.setDoctorId(doctorId);
        template.setSection(section);
        template.setTemplateName(templateName);
        template.setDataJson(json);
        template.setCreatedAt(LocalDateTime.now());
        templateRepository.save(template);
    }

    // Search template names
    public List<String> searchTemplateNames(Long doctorId, String section, String query) {
        List<PrescriptionTemplate> templates;
        
        if (query == null || query.trim().isEmpty()) {
            templates = templateRepository.findByDoctorIdAndSection(doctorId, section);
        } else {
            templates = templateRepository.findByDoctorIdAndSectionAndTemplateNameContainingIgnoreCase(doctorId, section, query);
        }
        
        return templates.stream()
                .map(PrescriptionTemplate::getTemplateName)
                .distinct()
                .collect(Collectors.toList());
    }


    private Object extractSection(Prescription prescription, String section) {
        switch (section.toLowerCase()) {
            case "diagnosis": return prescription.getDiagnosis();
            case "medicines": return prescription.getMedicines();
            default: return null;
        }
    }
}
