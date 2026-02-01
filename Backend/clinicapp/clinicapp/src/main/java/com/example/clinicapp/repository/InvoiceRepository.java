package com.example.clinicapp.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.clinicapp.entity.Invoice;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByPatientId(Long patientId);

    List<Invoice> findByDoctorId(Long doctorId);

    Optional<Invoice> findByPrescriptionId(Long prescriptionId);

    Optional<Invoice> findByAppointmentId(Long appointmentId);

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    // For invoice number generation - count invoices created with a specific prefix
    @Query("SELECT COUNT(i) FROM Invoice i WHERE i.invoiceNumber LIKE :prefix%")
    Long countByInvoiceNumberPrefix(@Param("prefix") String prefix);

    // Find all invoices ordered by date descending
    List<Invoice> findAllByOrderByInvoiceDateDesc();

    // Find invoices by status
    List<Invoice> findByStatus(String status);
}
