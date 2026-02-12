package com.example.clinicapp.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.clinicapp.dto.InvoiceDto;
import com.example.clinicapp.entity.Invoice;
import com.example.clinicapp.service.InvoiceService;

import jakarta.validation.Valid;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/invoices")
public class InvoiceController {

    private final InvoiceService invoiceService;

    public InvoiceController(InvoiceService invoiceService) {
        this.invoiceService = invoiceService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> createInvoice(@Valid @RequestBody InvoiceDto invoiceDto) {
        Invoice invoice = invoiceService.createInvoice(invoiceDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> getInvoiceById(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping("/prescription/{prescriptionId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Invoice> getInvoiceByPrescription(@PathVariable Long prescriptionId) {
        Optional<Invoice> invoice = invoiceService.getInvoiceByPrescriptionId(prescriptionId);
        return invoice.map(ResponseEntity::ok)
                     .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<List<Invoice>> getInvoicesByPatient(@PathVariable Long patientId) {
        List<Invoice> invoices = invoiceService.getInvoicesByPatientId(patientId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Invoice>> getInvoicesByDoctor(@PathVariable Long doctorId) {
        List<Invoice> invoices = invoiceService.getInvoicesByDoctorId(doctorId);
        return ResponseEntity.ok(invoices);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        List<Invoice> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Invoice> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceDto invoiceDto) {
        Invoice updatedInvoice = invoiceService.updateInvoice(id, invoiceDto);
        return ResponseEntity.ok(updatedInvoice);
    }

    @PatchMapping("/{id}/void")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Invoice> voidInvoice(@PathVariable Long id) {
        Invoice voidedInvoice = invoiceService.voidInvoice(id);
        return ResponseEntity.ok(voidedInvoice);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/whatsapp-message")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'RECEPTIONIST')")
    public ResponseEntity<Map<String, String>> getWhatsAppMessage(@PathVariable Long id) {
        Invoice invoice = invoiceService.getInvoiceById(id);
        String message = invoiceService.formatInvoiceForWhatsApp(invoice);

        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("invoiceNumber", invoice.getInvoiceNumber());

        // Get patient phone if available
        if (invoice.getPatient() != null && invoice.getPatient().getPhoneNumber() != null) {
            response.put("phone", invoice.getPatient().getPhoneNumber());
        }

        return ResponseEntity.ok(response);
    }
}
