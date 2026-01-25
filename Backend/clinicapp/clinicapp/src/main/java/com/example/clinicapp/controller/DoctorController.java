package com.example.clinicapp.controller;

import com.example.clinicapp.dto.DoctorDto;
import com.example.clinicapp.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // CMS-008: Added authorization - only ADMIN can create doctors
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorDto> createDoctor(@RequestBody DoctorDto doctorDto) {
        DoctorDto savedDoctor = doctorService.saveDoctor(doctorDto);
        return ResponseEntity.ok(savedDoctor);
    }

    // Get all doctors
    // CMS-DOC-001: Only authenticated users can view list of doctors
    @GetMapping
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    public ResponseEntity<List<DoctorDto>> getAllDoctors() {
        List<DoctorDto> doctors = doctorService.getAllDoctors();
        return ResponseEntity.ok(doctors);
    }

    // Get a doctor by ID
    // CMS-DOC-002: Only authenticated users can view doctor details by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    public ResponseEntity<DoctorDto> getDoctorById(@PathVariable Long id) {
        DoctorDto doctor = doctorService.getDoctorById(id);
        return ResponseEntity.ok(doctor);
    }

    // Get a doctor by name
    // CMS-DOC-003: Only authenticated users can view doctor details by name
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN', 'PATIENT')")
    public ResponseEntity<DoctorDto> getDoctorByName(@PathVariable String name) {
        DoctorDto doctor = doctorService.getDoctorByName(name);
        return ResponseEntity.ok(doctor);
    }

    // CMS-008: Added authorization - only ADMIN can delete doctors
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}
