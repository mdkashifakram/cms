package com.example.clinicapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class PrescriptionTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long doctorId;          // Owner
    private String section;         // "diagnosis", "medicines", "testAdvice", "advice"
    private String templateName;
    
    @Lob
    private String dataJson;        // JSON of section content

    private LocalDateTime createdAt;

    // Constructors
    public PrescriptionTemplate() {}

    public PrescriptionTemplate(Long doctorId, String section, String dataJson, LocalDateTime createdAt, String templateName) {
        this.doctorId = doctorId;
        this.section = section;
        this.dataJson = dataJson;
        this.createdAt = createdAt;
        this.templateName=templateName;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }

    public String getDataJson() { return dataJson; }
    public void setDataJson(String dataJson) { this.dataJson = dataJson; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

	public String getTemplateName() {
		return templateName;
	}

	public void setTemplateName(String templateName) {
		this.templateName = templateName;
	}
    
}
