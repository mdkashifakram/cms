package com.example.clinicapp.service;

import com.example.clinicapp.entity.Patient;
import com.example.clinicapp.repository.PatientRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;

@Service
public class PatientService {
	private final PatientRepository patientRepository;

	public PatientService(PatientRepository patientRepository) {
		this.patientRepository = patientRepository;
	}

	public List<Patient> getAllPatients() {
		return patientRepository.findAll();
	}
	@Cacheable(value="patients",key="#id")
	public Patient getPatientById(Long id) {
		return patientRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Patient not found!"));
	}
	
	public Optional<Patient> getPatientByNameandPhoneNumber(String name, String phoneNumber) {
	    Optional<Patient> patient = patientRepository.findByNameAndPhoneNumber(name, phoneNumber);
	    if (patient == null) {
	        throw new IllegalArgumentException("Patient not found!");
	    }
	    return patient;
	}

	
	
	
	
	@Transactional
	public Patient savePatient(Patient patient) {
		return patientRepository.save(patient);
	}
	@CacheEvict(value = "patients", key = "#id")
	public Patient updatePatient(Long id, Patient updatedPatient) {
		Patient existingPatient = patientRepository.findById(id)
				.orElseThrow(() -> new IllegalArgumentException("Patient not found"));
		existingPatient.setName(updatedPatient.getName());
		existingPatient.setAge(updatedPatient.getAge());
		existingPatient.setPhoneNumber(updatedPatient.getPhoneNumber());
		return patientRepository.save(existingPatient);
	}
	@CacheEvict(value = "patients", key = "#id")
	public void deletePatient(Long id) {
		patientRepository.deleteById(id);
	}

}




//name and phone--need patient details--all the objects of that particular patient