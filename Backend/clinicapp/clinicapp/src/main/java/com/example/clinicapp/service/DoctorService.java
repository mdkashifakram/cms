package com.example.clinicapp.service;

import com.example.clinicapp.dto.DoctorDto;
import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    // Create or update a doctor
    public DoctorDto saveDoctor(DoctorDto doctorDto) {
        Doctor doctor = new Doctor(doctorDto.getName(), doctorDto.getSpecialty(), doctorDto.getContactNumber());
        Doctor savedDoctor = doctorRepository.save(doctor);
        return mapToDto(savedDoctor);
    }

    // Retrieve all doctors
    public List<DoctorDto> getAllDoctors() {
        return doctorRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // Retrieve a doctor by ID
    public DoctorDto getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Doctor not found with id: " + id));
        return mapToDto(doctor);
    }

    // Retrieve a doctor by name
    public DoctorDto getDoctorByName(String name) {
        Doctor doctor = doctorRepository.findByName(name)
                .orElseThrow(() -> new RuntimeException("Doctor not found with name: " + name));
        return mapToDto(doctor);
    }

    // Delete a doctor by ID
    public void deleteDoctor(Long id) {
        doctorRepository.deleteById(id);
    }

    // Map Doctor to DoctorDto
    private DoctorDto mapToDto(Doctor doctor) {
        return new DoctorDto(doctor.getId(), doctor.getName(), doctor.getSpecialty(), doctor.getContactNumber(), doctor.getEmail());
    }
}


