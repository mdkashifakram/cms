package com.example.clinicapp.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.clinicapp.dto.InvoiceDto;
import com.example.clinicapp.dto.InvoiceItemDto;
import com.example.clinicapp.entity.Appointment;
import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.entity.Invoice;
import com.example.clinicapp.entity.InvoiceItem;
import com.example.clinicapp.entity.Patient;
import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.exception.DoctorNotFoundException;
import com.example.clinicapp.exception.InvoiceNotFoundException;
import com.example.clinicapp.exception.PatientNotFoundException;
import com.example.clinicapp.exception.PrescriptionNotFoundException;
import com.example.clinicapp.repository.AppointmentRepository;
import com.example.clinicapp.repository.DoctorRepository;
import com.example.clinicapp.repository.InvoiceRepository;
import com.example.clinicapp.repository.PatientRepository;
import com.example.clinicapp.repository.PrescriptionRepository;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final AppointmentRepository appointmentRepository;
    private final PrescriptionRepository prescriptionRepository;

    public InvoiceService(InvoiceRepository invoiceRepository,
                          PatientRepository patientRepository,
                          DoctorRepository doctorRepository,
                          AppointmentRepository appointmentRepository,
                          PrescriptionRepository prescriptionRepository) {
        this.invoiceRepository = invoiceRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.appointmentRepository = appointmentRepository;
        this.prescriptionRepository = prescriptionRepository;
    }

    // CREATE
    @Transactional
    public Invoice createInvoice(InvoiceDto dto) {
        // Validate DTO
        if (dto == null) {
            throw new IllegalArgumentException("InvoiceDto cannot be null");
        }
        if (dto.getPatientId() == null) {
            throw new IllegalArgumentException("Patient ID is required");
        }
        if (dto.getDoctorId() == null) {
            throw new IllegalArgumentException("Doctor ID is required");
        }

        // Check if invoice already exists for this prescription
        if (dto.getPrescriptionId() != null) {
            Optional<Invoice> existingInvoice = invoiceRepository.findByPrescriptionId(dto.getPrescriptionId());
            if (existingInvoice.isPresent()) {
                throw new IllegalArgumentException("An invoice already exists for this prescription");
            }
        }

        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new PatientNotFoundException(
                    "Patient not found with id: " + dto.getPatientId()));

        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new DoctorNotFoundException(
                    "Doctor not found with id: " + dto.getDoctorId()));

        Invoice invoice = new Invoice();
        invoice.setPatient(patient);
        invoice.setDoctor(doctor);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setInvoiceDate(LocalDateTime.now());
        invoice.setStatus("GENERATED");

        // Set Appointment if provided
        if (dto.getAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                    .orElse(null);
            invoice.setAppointment(appointment);
        }

        // Set Prescription if provided
        if (dto.getPrescriptionId() != null) {
            Prescription prescription = prescriptionRepository.findById(dto.getPrescriptionId())
                    .orElseThrow(() -> new PrescriptionNotFoundException(
                        "Prescription not found with id: " + dto.getPrescriptionId()));
            invoice.setPrescription(prescription);
        }

        // Set tax percentage
        if (dto.getTaxPercentage() != null) {
            invoice.setTaxPercentage(dto.getTaxPercentage());
        }

        // Set notes
        invoice.setNotes(dto.getNotes());

        // Map line items
        List<InvoiceItem> items = mapItemsToEntities(dto.getItems(), invoice);
        invoice.setItems(items);

        // Calculate totals
        calculateTotals(invoice);

        return invoiceRepository.save(invoice);
    }

    // READ - Get by ID
    public Invoice getInvoiceById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(
                    "Invoice not found with id: " + id));
    }

    // READ - Get by Prescription ID
    public Optional<Invoice> getInvoiceByPrescriptionId(Long prescriptionId) {
        return invoiceRepository.findByPrescriptionId(prescriptionId);
    }

    // READ - Get by Patient ID
    public List<Invoice> getInvoicesByPatientId(Long patientId) {
        return invoiceRepository.findByPatientId(patientId);
    }

    // READ - Get by Doctor ID
    public List<Invoice> getInvoicesByDoctorId(Long doctorId) {
        return invoiceRepository.findByDoctorId(doctorId);
    }

    // READ - Get All
    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAllByOrderByInvoiceDateDesc();
    }

    // READ - Get by Invoice Number
    public Optional<Invoice> getInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber);
    }

    // UPDATE
    @Transactional
    public Invoice updateInvoice(Long id, InvoiceDto dto) {
        Invoice existing = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(
                    "Invoice not found with id: " + id));

        // Update tax percentage
        if (dto.getTaxPercentage() != null) {
            existing.setTaxPercentage(dto.getTaxPercentage());
        }

        // Update notes
        if (dto.getNotes() != null) {
            existing.setNotes(dto.getNotes());
        }

        // Update items
        if (dto.getItems() != null) {
            existing.getItems().clear();
            List<InvoiceItem> newItems = mapItemsToEntities(dto.getItems(), existing);
            existing.getItems().addAll(newItems);
        }

        // Recalculate totals
        calculateTotals(existing);

        return invoiceRepository.save(existing);
    }

    // VOID - Soft delete
    @Transactional
    public Invoice voidInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new InvoiceNotFoundException(
                    "Invoice not found with id: " + id));

        invoice.setStatus("VOID");
        return invoiceRepository.save(invoice);
    }

    // DELETE - Hard delete (Admin only)
    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new InvoiceNotFoundException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    // HELPER - Generate Invoice Number (INV-YYYYMMDD-NNNN)
    private synchronized String generateInvoiceNumber() {
        String datePrefix = "INV-" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        Long count = invoiceRepository.countByInvoiceNumberPrefix(datePrefix);
        return datePrefix + String.format("%04d", count + 1);
    }

    // HELPER - Calculate Totals
    private void calculateTotals(Invoice invoice) {
        BigDecimal subtotal = BigDecimal.ZERO;

        if (invoice.getItems() != null) {
            for (InvoiceItem item : invoice.getItems()) {
                item.calculateLineTotal();
                subtotal = subtotal.add(item.getLineTotal());
            }
        }

        invoice.setSubtotal(subtotal);

        BigDecimal taxPercentage = invoice.getTaxPercentage() != null
            ? invoice.getTaxPercentage()
            : BigDecimal.ZERO;

        BigDecimal taxAmount = subtotal
            .multiply(taxPercentage)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        invoice.setTaxAmount(taxAmount);
        invoice.setGrandTotal(subtotal.add(taxAmount));
    }

    // HELPER - Map Items DTOs to Entities
    private List<InvoiceItem> mapItemsToEntities(List<InvoiceItemDto> itemsDto, Invoice invoice) {
        List<InvoiceItem> items = new ArrayList<>();

        if (itemsDto == null || itemsDto.isEmpty()) {
            return items;
        }

        int order = 0;
        for (InvoiceItemDto dto : itemsDto) {
            if (dto.getDescription() != null && !dto.getDescription().trim().isEmpty()) {
                InvoiceItem item = new InvoiceItem();
                item.setDescription(dto.getDescription());
                item.setQuantity(dto.getQuantity() != null ? dto.getQuantity() : 1);
                item.setUnitPrice(dto.getUnitPrice() != null ? dto.getUnitPrice() : BigDecimal.ZERO);
                item.setItemOrder(order++);
                item.setInvoice(invoice);
                item.calculateLineTotal();
                items.add(item);
            }
        }

        return items;
    }

    // HELPER - Format invoice for WhatsApp message
    public String formatInvoiceForWhatsApp(Invoice invoice) {
        StringBuilder sb = new StringBuilder();

        sb.append("*INVOICE*\n");
        sb.append("Invoice #: ").append(invoice.getInvoiceNumber()).append("\n");
        sb.append("Date: ").append(invoice.getInvoiceDate().toLocalDate()).append("\n");
        sb.append("\n");

        sb.append("*Patient:* ").append(invoice.getPatient().getName()).append("\n");
        sb.append("*Doctor:* Dr. ").append(invoice.getDoctor().getName()).append("\n");
        sb.append("\n");

        sb.append("*Items:*\n");
        sb.append("-------------------\n");

        for (InvoiceItem item : invoice.getItems()) {
            sb.append(item.getDescription())
              .append(" x").append(item.getQuantity())
              .append(" = Rs.").append(item.getLineTotal())
              .append("\n");
        }

        sb.append("-------------------\n");
        sb.append("Subtotal: Rs.").append(invoice.getSubtotal()).append("\n");

        if (invoice.getTaxPercentage().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Tax (").append(invoice.getTaxPercentage()).append("%): Rs.")
              .append(invoice.getTaxAmount()).append("\n");
        }

        sb.append("*Grand Total: Rs.").append(invoice.getGrandTotal()).append("*\n");

        if (invoice.getNotes() != null && !invoice.getNotes().isEmpty()) {
            sb.append("\nNote: ").append(invoice.getNotes());
        }

        return sb.toString();
    }
}
