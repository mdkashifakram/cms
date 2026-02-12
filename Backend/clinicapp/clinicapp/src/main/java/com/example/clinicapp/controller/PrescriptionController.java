package com.example.clinicapp.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

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

import com.example.clinicapp.dto.PrescriptionDto;
import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.service.PrescriptionService;

import jakarta.validation.Valid;
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @PostMapping("/create")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Prescription> createPrescription(@Valid @RequestBody PrescriptionDto prescriptionDto) {
        Prescription prescription = prescriptionService.createPrescription(prescriptionDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(prescription);
    }

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Map<String, Long>> findPrescriptionIdByAppointmentId(@PathVariable Long appointmentId) {
        Long prescriptionId = prescriptionService.findPrescriptionIdByAppointmentId(appointmentId);
        if (prescriptionId != null) {
            return ResponseEntity.ok(Collections.singletonMap("prescriptionId", prescriptionId));
        }
        return ResponseEntity.ok(Collections.singletonMap("prescriptionId", null));

    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<Prescription> getPrescriptionById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getPrescriptionById(id);
        return ResponseEntity.ok(prescription);
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'RECEPTIONIST', 'ADMIN')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByPatient(@PathVariable Long patientId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByPatientId(patientId);
        return ResponseEntity.ok(prescriptions);
    }

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Prescription>> getPrescriptionsByDoctor(@PathVariable Long doctorId) {
        List<Prescription> prescriptions = prescriptionService.getPrescriptionsByDoctorId(doctorId);
        return ResponseEntity.ok(prescriptions);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<Prescription> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionDto prescriptionDto) {
        Prescription updatedPrescription = prescriptionService.updatePrescription(id, prescriptionDto);
        return ResponseEntity.ok(updatedPrescription);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePrescription(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<List<Prescription>> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.getAllPrescriptions();
        return ResponseEntity.ok(prescriptions);
    }
}