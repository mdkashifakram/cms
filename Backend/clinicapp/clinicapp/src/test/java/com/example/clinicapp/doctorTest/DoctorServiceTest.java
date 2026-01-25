package com.example.clinicapp.doctorTest;

import com.example.clinicapp.dto.DoctorDto;
import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.repository.DoctorRepository;
import com.example.clinicapp.service.DoctorService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    @InjectMocks
    private DoctorService doctorService;

    @Mock
    private DoctorRepository doctorRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Your test methods will go here
    @Test
    void testSaveDoctor() {
        DoctorDto doctorDto = new DoctorDto(null, "Dr. Alisha", "Nueroscientist", "1234567890", "alisha@example.com");
        Doctor doctor = new Doctor("Dr. Smith", "Cardiology", "1234567890");
        Doctor savedDoctor = new Doctor("Dr. Smith", "Cardiology", "1234567890");
        savedDoctor.setId(1L); // Set the ID for testing purposes

        when(doctorRepository.save(any(Doctor.class))).thenReturn(savedDoctor);

        DoctorDto result = doctorService.saveDoctor(doctorDto);

        assertNotNull(result, "The result should not be null.");
        assertEquals(1L, result.getId(), "The doctor ID should be 1L after saving.");
        assertEquals("Dr. Smith", result.getName(), "The doctor name should be 'Dr. Smith'.");
        assertEquals("Cardiology", result.getSpecialty(), "The specialty should be 'Cardiology'.");
        verify(doctorRepository, times(1)).save(any(Doctor.class));
    }

    
    @Test
    void testGetAllDoctors() {
        Doctor doctor1 = new Doctor( "Dr. Smith", "Cardiology", "1234567890");
        doctor1.setId(1L);
        Doctor doctor2 = new Doctor( "Dr. Jones", "Dermatology", "0987654321");
        doctor2.setId(2L);
        when(doctorRepository.findAll()).thenReturn(Arrays.asList(doctor1, doctor2));

        List<DoctorDto> result = doctorService.getAllDoctors();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Dr. Smith", result.get(0).getName());
        assertEquals("Dr. Jones", result.get(1).getName());
        verify(doctorRepository, times(1)).findAll();
    }

    
    @Test
    void testGetDoctorById() {
        Doctor doctor = new Doctor("Dr. Smith", "Cardiology", "1234567890");
        doctor.setId(1L);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        DoctorDto result = doctorService.getDoctorById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Dr. Smith", result.getName());
        verify(doctorRepository, times(1)).findById(1L);
    }

    @Test
    void testGetDoctorById_NotFound() {
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorService.getDoctorById(1L);
        });

        assertEquals("Doctor not found with id: 1", exception.getMessage());
    }


    @Test
    void testGetDoctorByName() {
        Doctor doctor = new Doctor("Dr. Smith", "Cardiology", "1234567890");
        doctor.setId(1L);
        when(doctorRepository.findByName("Dr. Smith")).thenReturn(Optional.of(doctor));

        DoctorDto result = doctorService.getDoctorByName("Dr. Smith");

        assertNotNull(result);
        assertEquals("Dr. Smith", result.getName());
        verify(doctorRepository, times(1)).findByName("Dr. Smith");
    }

    @Test
    void testGetDoctorByName_NotFound() {
        when(doctorRepository.findByName("Dr. Unknown")).thenReturn(Optional.empty());

        Exception exception = assertThrows(RuntimeException.class, () -> {
            doctorService.getDoctorByName("Dr. Unknown");
        });

        assertEquals("Doctor not found with name: Dr. Unknown", exception.getMessage());
    }

    @Test
    void testDeleteDoctor() {
        Long doctorId = 1L;
        doNothing().when(doctorRepository).deleteById(doctorId);

        doctorService.deleteDoctor(doctorId);

        verify(doctorRepository, times(1)).deleteById(doctorId);
    }

    
}
