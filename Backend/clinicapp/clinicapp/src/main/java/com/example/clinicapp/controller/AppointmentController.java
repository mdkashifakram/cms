

package com.example.clinicapp.controller;


import com.example.clinicapp.dto.AppointmentDto;
import com.example.clinicapp.dto.DoctorDto;
import com.example.clinicapp.entity.Appointment;
import com.example.clinicapp.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentService appointmentService;

    @Autowired
    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/bookAppointment")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public Appointment bookAppointment(@Valid @RequestBody Appointment appointment) {
        return appointmentService.saveAppointment(appointment);
    }

    
    
    // Get a doctor by ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<Appointment> getAppointmentById(@PathVariable Long id) {
        Appointment appointment = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(appointment);
    }
    
    
    @GetMapping("/day")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public List<AppointmentDto> getAppointmentsForDay(@RequestParam("date") LocalDate date) {
        return appointmentService.getAppointmentsForDay(date);
    }

    @PutMapping("/update/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public Appointment updateAppointment(@PathVariable Long id,@Valid  @RequestBody Appointment appointment) {
        return appointmentService.updateAppointment(id, appointment);
    }
    
    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteAppointment(@PathVariable Long id) {
         appointmentService.cancelAppointment(id);
    }
    
    @GetMapping("/availability")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public boolean checkAvailability(@RequestParam("time") LocalDateTime time) {
        return appointmentService.isTimeSlotAvailable(time);
    }

    @GetMapping("/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'DOCTOR', 'ADMIN')")
    public List<Appointment> getAppointmentsByStatus(@RequestParam("status") String status) {
        return appointmentService.getAppointmentByStatus(status);
    }
}
