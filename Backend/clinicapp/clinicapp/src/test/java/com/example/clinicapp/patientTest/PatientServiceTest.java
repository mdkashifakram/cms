package com.example.clinicapp.patientTest;


import com.example.clinicapp.entity.Patient;
import com.example.clinicapp.repository.PatientRepository;
import com.example.clinicapp.service.PatientService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PatientServiceTest {

    @InjectMocks
    private PatientService patientService;

    @Mock
    private PatientRepository patientRepository;

    private Patient patient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        patient = new Patient();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setAge(30);
        patient.setPhoneNumber("1234567890");
        patient.setGender("Male");
        patient.setAddress("123 Main St");
        patient.setCity("Kolkata");
        patient.setPin("700001");
    }

    @Test
    void testSavePatient() {
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);
        
        Patient savedPatient = patientService.savePatient(patient);
        
        assertNotNull(savedPatient);
        assertEquals("John Doe", savedPatient.getName());
        verify(patientRepository, times(1)).save(patient);
    }

    @Test
    void testGetPatientById() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        
        Patient foundPatient = patientService.getPatientById(1L);
        
        assertNotNull(foundPatient);
        assertEquals("John Doe", foundPatient.getName());
        verify(patientRepository, times(1)).findById(1L);
    }

    @Test
    void testGetPatientByNameAndPhoneNumber() {
        when(patientRepository.findByNameAndPhoneNumber("John Doe", "1234567890"))
                .thenReturn(Optional.of(patient));
        
        Optional<Patient> foundPatient = patientService.getPatientByNameandPhoneNumber("John Doe", "1234567890");
        
        assertTrue(foundPatient.isPresent());
        assertEquals("John Doe", foundPatient.get().getName());
        verify(patientRepository, times(1)).findByNameAndPhoneNumber("John Doe", "1234567890");
    }

    @Test
    void testUpdatePatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        Patient updatedPatient = new Patient();
        updatedPatient.setName("Jane Doe");
        updatedPatient.setAge(28);
        
        Patient result = patientService.updatePatient(1L, updatedPatient);
        
        assertNotNull(result);
        assertEquals("Jane Doe", result.getName());
        verify(patientRepository, times(1)).findById(1L);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    @Test
    void testDeletePatient() {
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        doNothing().when(patientRepository).deleteById(1L);

        assertDoesNotThrow(() -> patientService.deletePatient(1L));
        verify(patientRepository, times(1)).deleteById(1L);
    }



}
