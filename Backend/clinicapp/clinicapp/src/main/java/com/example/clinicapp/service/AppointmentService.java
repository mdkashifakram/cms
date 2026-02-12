package com.example.clinicapp.service;

import com.example.clinicapp.dto.AppointmentDto;
import com.example.clinicapp.entity.Appointment;
import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.entity.Patient;
import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final EmailService emailService;
    private final MobileService mobileService;

    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository, EmailService emailService,MobileService mobileService) {
        this.appointmentRepository = appointmentRepository;
        this.emailService = emailService;
        this.mobileService=mobileService;
    }
    
    // Saving appointments
    public Appointment saveAppointment(Appointment appointment) {
        List<Appointment> existingAppointments = appointmentRepository.findByAppointmentTimeBetween(
                appointment.getAppointmentTime().minusMinutes(1),
                appointment.getAppointmentTime().plusMinutes(1)
        );
        if (!existingAppointments.isEmpty()) {
            throw new IllegalStateException("The time slot is already booked");
        }
        Appointment savedAppointment = appointmentRepository.save(appointment);

        // Send email notification for new appointment
        emailService.sendEmail(
                appointment.getPatientEmail(),
                "Appointment Confirmation",
                "Your appointment has been successfully booked for " + appointment.getAppointmentTime()
        );
       //mobileService.MobileNotification(appointment.getContact(),appointment.getPatientName(),"Appointment is Confirmed");
        return savedAppointment;
    }

    
    public Appointment getAppointmentById(Long id) {
        return appointmentRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Appointment not found with ID: " + id));
    }
    
    

    // Checking the appointment status for the day
    public List<AppointmentDto> getAppointmentsForDay(LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);
        List<Appointment> appointments = appointmentRepository.findByAppointmentTimeBetween(startOfDay, endOfDay);
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Convert Appointment entity to AppointmentDto
    private AppointmentDto convertToDto(Appointment appointment) {
        if (appointment == null) {
            return null;
        }

        AppointmentDto dto = new AppointmentDto();
        dto.setId(appointment.getId());
        dto.setPatientName(appointment.getPatientName());
        dto.setPatientEmail(appointment.getPatientEmail());
        dto.setStatus(appointment.getStatus());
        dto.setDetails(appointment.getDetails());
        dto.setAppointmentTime(appointment.getAppointmentTime());

        // Extract patient information
        Patient patient = appointment.getPatient();
        if (patient != null) {
            dto.setPatientId(patient.getId());
            dto.setPatientAge(patient.getAge());
            dto.setPatientGender(patient.getGender());
            dto.setPatientPhone(patient.getPhoneNumber());
        }

        // Extract doctor information
        Doctor doctor = appointment.getDoctor();
        if (doctor != null) {
            dto.setDoctorId(doctor.getId());
            dto.setDoctorName(doctor.getName());
            dto.setDoctorSpecialty(doctor.getSpecialty());
            dto.setDoctorEmail(doctor.getEmail());
            dto.setDoctorPhone(doctor.getContactNumber());
        }

        // Extract prescription ID if exists
        Prescription prescription = appointment.getPrescription();
        if (prescription != null) {
            dto.setPrescriptionId(prescription.getId());
        }

        return dto;
    }

    // Update the appointment
    public Appointment updateAppointment(Long appointmentId, Appointment updatedAppointment) {
        Appointment existingAppointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found!"));

        existingAppointment.setAppointmentTime(updatedAppointment.getAppointmentTime());
        existingAppointment.setDetails(updatedAppointment.getDetails());
        Appointment updated = appointmentRepository.save(existingAppointment);

        // Send email notification for updated appointment
        emailService.sendEmail(
                updated.getPatientEmail(),
                "Appointment Updated",
                "Your appointment has been updated to " + updated.getAppointmentTime()
        );
        //mobileService.MobileNotification(updatedAppointment.getContact(),updatedAppointment.getPatientName(),"Appointment is Updated. Now scheduled appointment time is : "+updatedAppointment.getAppointmentTime());
        return updated;
    }

    
    // Cancel appointment
    public void cancelAppointment(Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new IllegalArgumentException("Appointment not found!"));

        appointmentRepository.deleteById(appointmentId);

        // Send email notification for canceled appointment
        emailService.sendEmail(
                appointment.getPatientEmail(),
                "Appointment Cancellation",
                "Your appointment scheduled for " + appointment.getAppointmentTime() + " has been canceled."
        );
        //mobileService.MobileNotification(appointment.getContact(),appointment.getPatientName(),"Appointment is Cancelled");
    }

    // Check time slot availability
    public boolean isTimeSlotAvailable(LocalDateTime time) {
        List<Appointment> existingAppointments = appointmentRepository.findByAppointmentTimeBetween(
                time.minusMinutes(1), time.plusMinutes(1));
        return existingAppointments.isEmpty();
    }

    // Get appointments by status
    public List<Appointment> getAppointmentByStatus(String status) {
        return appointmentRepository.findByStatus(status);
    }

    // ==================== CONSULTS METHODS ====================

    // Get all completed appointments (consults) - ordered by most recent first
    public List<AppointmentDto> getCompletedAppointments() {
        List<Appointment> completed = appointmentRepository.findByStatusOrderByAppointmentTimeDesc("Completed");
        return completed.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get completed appointments by patient ID
    public List<AppointmentDto> getCompletedAppointmentsByPatientId(Long patientId) {
        List<Appointment> completed = appointmentRepository.findByPatient_IdAndStatus(patientId, "Completed");
        return completed.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get all appointments by patient ID (for history)
    public List<AppointmentDto> getAppointmentsByPatientId(Long patientId) {
        List<Appointment> appointments = appointmentRepository.findByPatient_Id(patientId);
        return appointments.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
