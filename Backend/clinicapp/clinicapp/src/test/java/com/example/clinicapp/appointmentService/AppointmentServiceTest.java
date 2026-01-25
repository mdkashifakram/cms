package com.example.clinicapp.appointmentService;


import com.example.clinicapp.dto.AppointmentDto;
import com.example.clinicapp.entity.Appointment;
import com.example.clinicapp.repository.AppointmentRepository;
import com.example.clinicapp.service.AppointmentService;
import com.example.clinicapp.service.EmailService;
import com.example.clinicapp.service.MobileService;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private MobileService mobileService;

    @InjectMocks
    private AppointmentService appointmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
    }

    @Test
    void testSaveAppointmentSuccess() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(LocalDateTime.of(2023, 10, 30, 10, 0));
        appointment.setPatientEmail("test@example.com");

        when(appointmentRepository.findByAppointmentTimeBetween(any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Appointment result = appointmentService.saveAppointment(appointment);

        assertNotNull(result);
        verify(appointmentRepository).save(appointment);
        verify(emailService).sendEmail(
                eq(appointment.getPatientEmail()),
                eq("Appointment Confirmation"),
                contains("Your appointment has been successfully booked")
        );
    }

    @Test
    void testSaveAppointmentTimeSlotUnavailable() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(LocalDateTime.of(2023, 10, 30, 10, 0));

        when(appointmentRepository.findByAppointmentTimeBetween(any(), any()))
                .thenReturn(List.of(new Appointment()));

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            appointmentService.saveAppointment(appointment);
        });

        assertEquals("The time slot is already booked", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void testGetAppointmentsForDay() {
        LocalDate date = LocalDate.of(2023, 10, 30);
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(LocalTime.MAX);

        when(appointmentRepository.findByAppointmentTimeBetween(startOfDay, endOfDay))
                .thenReturn(List.of(new Appointment()));

        List<AppointmentDto> appointments = appointmentService.getAppointmentsForDay(date);

        assertNotNull(appointments);
        verify(appointmentRepository).findByAppointmentTimeBetween(startOfDay, endOfDay);
    }

    @Test
    void testUpdateAppointmentSuccess() {
        Long appointmentId = 1L;
        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentTime(LocalDateTime.of(2023, 10, 30, 10, 0));
        existingAppointment.setPatientEmail("test@example.com");

        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setAppointmentTime(LocalDateTime.of(2023, 10, 30, 11, 0));
        updatedAppointment.setDetails("Updated details");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(existingAppointment));
        when(appointmentRepository.save(existingAppointment)).thenReturn(existingAppointment);

        Appointment result = appointmentService.updateAppointment(appointmentId, updatedAppointment);

        assertNotNull(result);
        assertEquals("Updated details", result.getDetails());
        verify(emailService).sendEmail(
                eq(existingAppointment.getPatientEmail()),
                eq("Appointment Updated"),
                contains("Your appointment has been updated")
        );
    }

    @Test
    void testUpdateAppointmentNotFound() {
        Long appointmentId = 1L;
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.updateAppointment(appointmentId, new Appointment());
        });

        assertEquals("Appointment not found!", exception.getMessage());
        verify(appointmentRepository, never()).save(any());
    }

    @Test
    void testCancelAppointmentSuccess() {
        Long appointmentId = 1L;
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(LocalDateTime.of(2023, 10, 30, 10, 0));
        appointment.setPatientEmail("test@example.com");

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        appointmentService.cancelAppointment(appointmentId);

        verify(appointmentRepository).deleteById(appointmentId);
        verify(emailService).sendEmail(
                eq(appointment.getPatientEmail()),
                eq("Appointment Cancellation"),
                contains("Your appointment scheduled for")
        );
    }

    @Test
    void testCancelAppointmentNotFound() {
        Long appointmentId = 1L;
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            appointmentService.cancelAppointment(appointmentId);
        });

        assertEquals("Appointment not found!", exception.getMessage());
        verify(appointmentRepository, never()).deleteById(any());
    }

    @Test
    void testIsTimeSlotAvailable() {
        LocalDateTime time = LocalDateTime.of(2023, 10, 30, 10, 0);

        when(appointmentRepository.findByAppointmentTimeBetween(time.minusMinutes(1), time.plusMinutes(1)))
                .thenReturn(Collections.emptyList());

        boolean isAvailable = appointmentService.isTimeSlotAvailable(time);

        assertTrue(isAvailable);
        verify(appointmentRepository).findByAppointmentTimeBetween(time.minusMinutes(1), time.plusMinutes(1));
    }

    @Test
    void testGetAppointmentByStatus() {
        String status = "Confirmed";
        when(appointmentRepository.findByStatus(status)).thenReturn(List.of(new Appointment()));

        List<Appointment> appointments = appointmentService.getAppointmentByStatus(status);

        assertNotNull(appointments);
        verify(appointmentRepository).findByStatus(status);
    }
    // 1. Concurrent Appointment Bookings
    @Test
    public void testConcurrentBookings() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(time);

        when(appointmentRepository.findByAppointmentTimeBetween(any(), any())).thenReturn(Collections.emptyList());
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(appointment);

        Thread thread1 = new Thread(() -> appointmentService.saveAppointment(appointment));
        Thread thread2 = new Thread(() -> appointmentService.saveAppointment(appointment));

        thread1.start();
        thread2.start();
        assertDoesNotThrow(() -> {
            thread1.join();
            thread2.join();
        });
    }


    // 4. Large Volume of Appointments in a Day
    @Test
    public void testLargeVolumeAppointmentsForDay() {
        LocalDate date = LocalDate.now();
        List<Appointment> appointments = Collections.nCopies(1000, new Appointment());

        when(appointmentRepository.findByAppointmentTimeBetween(any(), any())).thenReturn(appointments);
        List<AppointmentDto> result = appointmentService.getAppointmentsForDay(date);

        assertEquals(1000, result.size());
    }



    // 7. Overlapping Appointment Times
    @Test
    public void testOverlappingAppointments() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentTime(time);

        when(appointmentRepository.findByAppointmentTimeBetween(any(), any())).thenReturn(List.of(existingAppointment));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> appointmentService.saveAppointment(existingAppointment));
        assertEquals("The time slot is already booked", exception.getMessage());
    }


    // 9. Filter by Multiple Statuses
    @Test
    public void testGetAppointmentByMultipleStatus() {
        List<Appointment> appointments = List.of(new Appointment(), new Appointment());

        when(appointmentRepository.findByStatus(anyString())).thenReturn(appointments);
        List<Appointment> result = appointmentService.getAppointmentByStatus("Confirmed");

        assertEquals(appointments.size(), result.size());
    }



    // 12. Time Zone Handling
    @Test
    public void testTimeZoneHandling() {
        LocalDateTime time = LocalDateTime.now().plusDays(1);
        Appointment appointment = new Appointment();
        appointment.setAppointmentTime(time);

        // Test time zone adjustments here as needed.
    }

    // 13. Bulk Appointment Cancellation
    @Test
    public void testBulkAppointmentCancellation() {
        Long[] ids = {1L, 2L, 3L};
        for (Long id : ids) {
            when(appointmentRepository.findById(id)).thenReturn(Optional.of(new Appointment()));
        }
        for (Long id : ids) {
            appointmentService.cancelAppointment(id);
        }

        verify(appointmentRepository, times(ids.length)).deleteById(anyLong());
    }


    
}
