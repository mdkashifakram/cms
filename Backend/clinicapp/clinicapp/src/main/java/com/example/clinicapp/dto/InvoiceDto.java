package com.example.clinicapp.dto;

import java.math.BigDecimal;
import java.util.List;

public class InvoiceDto {

    private Long id;
    private Long patientId;
    private Long doctorId;
    private Long appointmentId;
    private Long prescriptionId;
    private List<InvoiceItemDto> items;
    private BigDecimal taxPercentage;
    private String notes;
    private String invoiceDate;

    // Constructors
    public InvoiceDto() {}

    public InvoiceDto(Long patientId, Long doctorId, Long appointmentId, Long prescriptionId,
                      List<InvoiceItemDto> items, BigDecimal taxPercentage, String notes) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.prescriptionId = prescriptionId;
        this.items = items;
        this.taxPercentage = taxPercentage;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPatientId() {
        return patientId;
    }

    public void setPatientId(Long patientId) {
        this.patientId = patientId;
    }

    public Long getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(Long doctorId) {
        this.doctorId = doctorId;
    }

    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }

    public Long getPrescriptionId() {
        return prescriptionId;
    }

    public void setPrescriptionId(Long prescriptionId) {
        this.prescriptionId = prescriptionId;
    }

    public List<InvoiceItemDto> getItems() {
        return items;
    }

    public void setItems(List<InvoiceItemDto> items) {
        this.items = items;
    }

    public BigDecimal getTaxPercentage() {
        return taxPercentage;
    }

    public void setTaxPercentage(BigDecimal taxPercentage) {
        this.taxPercentage = taxPercentage;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }
}
