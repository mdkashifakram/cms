package com.example.clinicapp.controller;

import com.example.clinicapp.dto.AppointmentDto;
import com.example.clinicapp.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Consults (completed appointments)
 * Provides endpoints for viewing patient consultation history
 */
@RestController
@RequestMapping("/consults")
public class ConsultController {

    private final AppointmentService appointmentService;

    @Autowired
    public ConsultController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    /**
     * Get all completed appointments (consults)
     * Returns list of appointments with status "Completed"
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentDto>> getAllConsults() {
        List<AppointmentDto> consults = appointmentService.getCompletedAppointments();
        return ResponseEntity.ok(consults);
    }

    /**
     * Get completed appointments by patient ID
     * Used for searching patient's consultation history
     */
    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<List<AppointmentDto>> getConsultsByPatientId(@PathVariable Long patientId) {
        List<AppointmentDto> consults = appointmentService.getCompletedAppointmentsByPatientId(patientId);
        return ResponseEntity.ok(consults);
    }

    /**
     * Get a specific consult by appointment ID
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentDto> getConsultById(@PathVariable Long id) {
        // Reuse existing method and convert to DTO
        var appointment = appointmentService.getAppointmentById(id);
        // Check if it's a completed appointment
        if (!"completed".equalsIgnoreCase(appointment.getStatus())) {
            return ResponseEntity.notFound().build();
        }
        // Convert to DTO manually for single entity
        List<AppointmentDto> dtos = appointmentService.getAppointmentsByPatientId(
            appointment.getPatient() != null ? appointment.getPatient().getId() : null
        );
        return dtos.stream()
            .filter(dto -> dto.getId().equals(id))
            .findFirst()
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
