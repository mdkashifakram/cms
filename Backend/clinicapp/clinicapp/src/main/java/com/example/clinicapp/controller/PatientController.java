package com.example.clinicapp.controller;

import com.example.clinicapp.dto.PatientSearchRequest;
import com.example.clinicapp.entity.Patient;
import com.example.clinicapp.service.PatientService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/patients")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public List<Patient> getAllPatients() {
        return patientService.getAllPatients();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<Patient> getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(patientService.getPatientById(id));
    }

    /**
     * CMS-012: POST-based patient search (RECOMMENDED)
     *
     * Supports two search modes:
     * 1. By Patient ID - provide patientId in request body
     * 2. By Name + Phone - provide both name and phoneNumber
     *
     * Uses request body instead of URL parameters to protect patient PII
     * from being logged in server access logs, proxy logs, and browser history.
     */
    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<?> searchPatient(@RequestBody PatientSearchRequest request) {
        // Validate request - either patientId OR (name + phoneNumber) required
        if (!request.isValid()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Invalid search criteria. Provide either patientId OR both name and phoneNumber.");
        }

        switch (request.getSearchMode()) {
            case BY_ID:
                try {
                    Patient patient = patientService.getPatientById(request.getPatientId());
                    return ResponseEntity.ok(patient);
                } catch (IllegalArgumentException e) {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Patient not found with ID: " + request.getPatientId());
                }

            case BY_NAME_PHONE:
                Optional<Patient> patientOpt = patientService.getPatientByNameandPhoneNumber(
                    request.getName(),
                    request.getPhoneNumber()
                );
                if (patientOpt.isPresent()) {
                    return ResponseEntity.ok(patientOpt.get());
                } else {
                    return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Patient not found with provided name and phone number");
                }

            default:
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid search criteria");
        }
    }

    /**
     * @deprecated Use POST /patients/search instead.
     * This endpoint exposes patient PII in URL query parameters.
     * Kept for backward compatibility - will be removed in next major version.
     */
    @Deprecated
    @GetMapping("/searchpatient")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<?> getPatientByNameAndPhoneNumber(
            @RequestParam String name,
            @RequestParam String phoneNumber) {
        Optional<Patient> patient = patientService.getPatientByNameandPhoneNumber(name, phoneNumber);

        if (patient.isPresent()) {
            return new ResponseEntity<>(patient.get(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Patient not found", HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<?> createPatient(@Valid @RequestBody Patient patient) {
        Patient createdPatient = patientService.savePatient(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPatient);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<Patient> updatePatient(@PathVariable Long id, @Valid @RequestBody Patient patient) {
        return ResponseEntity.ok(patientService.updatePatient(id, patient));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}