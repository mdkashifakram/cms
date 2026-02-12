
package com.example.clinicapp.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.example.clinicapp.dto.MedicineDto;
import com.example.clinicapp.dto.PrescriptionDto;
import com.example.clinicapp.dto.ReferralDto;
import com.example.clinicapp.dto.VitalsDto;
import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.entity.Patient;
import com.example.clinicapp.entity.Prescription;
import com.example.clinicapp.entity.PrescriptionMedicine;
import com.example.clinicapp.entity.PrescriptionReferral;
import com.example.clinicapp.exception.DoctorNotFoundException;
import com.example.clinicapp.exception.PatientNotFoundException;
import com.example.clinicapp.exception.PrescriptionNotFoundException;
import com.example.clinicapp.repository.AppointmentRepository;
import com.example.clinicapp.repository.DoctorRepository;
import com.example.clinicapp.repository.PatientRepository;
import com.example.clinicapp.repository.PrescriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PrescriptionService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final PrescriptionRepository prescriptionRepository;
    private final AppointmentRepository appointmentRepository; 
    private final ObjectMapper objectMapper;

    public PrescriptionService(DoctorRepository doctorRepository,
                               PatientRepository patientRepository,
                               PrescriptionRepository prescriptionRepository,
                               AppointmentRepository appointmentRepository,
                               ObjectMapper objectMapper) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.prescriptionRepository = prescriptionRepository;
        this.appointmentRepository=appointmentRepository;
        this.objectMapper = objectMapper;
    }

    // CREATE
    public Prescription createPrescription(PrescriptionDto dto) {
        // Validate DTO
        if (dto == null) {
            throw new IllegalArgumentException("PrescriptionDto cannot be null");
        }
        if (dto.getDoctor() == null || dto.getDoctor().getId() == null) {
            throw new IllegalArgumentException("Doctor information is required");
        }
        if (dto.getPatient() == null) {
            throw new IllegalArgumentException("Patient information is required");
        }

        Doctor doctor = doctorRepository.findById(dto.getDoctor().getId())
                .orElseThrow(() -> new DoctorNotFoundException(
                    "Doctor not found with id: " + dto.getDoctor().getId()));

        Patient patient;
        if (dto.getPatient().getId() != null) {
            patient = patientRepository.findById(dto.getPatient().getId())
                    .orElseThrow(() -> new PatientNotFoundException(
                        "Patient not found with id: " + dto.getPatient().getId()));
        } else {
            patient = new Patient(dto.getPatient());
            patient = patientRepository.save(patient);
        }

        Prescription prescription = new Prescription();
        prescription.setDoctor(doctor);
        prescription.setPatient(patient);
        prescription.setIssuedAt(LocalDateTime.now());

        // Set Appointment if provided
        if (dto.getAppointmentId() != null) {
            prescription.setAppointment(
                appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException(
                        "Appointment not found with id: " + dto.getAppointmentId()))
            );
        }
        
        // Set Vitals
        if (dto.getVitals() != null) {
            VitalsDto vitals = dto.getVitals();
            prescription.setTemperature(vitals.getTemp());
            prescription.setBloodPressure(vitals.getBp());
            prescription.setPulse(vitals.getPulse());
            prescription.setSpo2(vitals.getSpo2());
            prescription.setHeight(vitals.getHeight());
            prescription.setWeight(vitals.getWeight());
            prescription.setBmi(vitals.getBmi());
            prescription.setWaistHip(vitals.getWaistHip());
        }

        // Set Text Sections
        prescription.setComplaints(dto.getComplaints());
        prescription.setPastHistory(dto.getPastHistory());
        prescription.setDiagnosis(dto.getDiagnosis());
        prescription.setAdvice(dto.getAdvice());
        prescription.setTestRequested(dto.getTestRequested());
        prescription.setPastMedications(dto.getPastMedications());
        prescription.setGeneralExamination(dto.getGeneralExamination());

        // Set Next Visit
        if (dto.getNextVisit() != null) {
            prescription.setNextVisitNumber(dto.getNextVisit().getNumber());
            prescription.setNextVisitUnit(dto.getNextVisit().getUnit());
            prescription.setNextVisitDate(dto.getNextVisit().getDate());
        }

        // Set Medicines
        prescription.setMedicines(mapMedicinesToEntities(dto.getMedicines(), prescription));

        // Set Referrals
        prescription.setReferrals(mapReferralsToEntities(dto.getReferredTo(), prescription));

        return prescriptionRepository.save(prescription);
    }

    // READ - Get by ID
    public Long findPrescriptionIdByAppointmentId(Long appointmentId) {
        return appointmentRepository.findById(appointmentId)
                .map(appointment -> appointment.getPrescription())
                .map(prescription -> prescription.getId())
                .orElse(null);
    }
    
    
    public Prescription getPrescriptionById(Long id) {
        return prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                    "Prescription not found with id: " + id));
    }

    // READ - Get by Patient ID
    public List<Prescription> getPrescriptionsByPatientId(Long patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    // READ - Get by Doctor ID
    public List<Prescription> getPrescriptionsByDoctorId(Long doctorId) {
        return prescriptionRepository.findByDoctorId(doctorId);
    }

    // READ - Get All
    public List<Prescription> getAllPrescriptions() {
        return prescriptionRepository.findAll();
    }

    // UPDATE
    public Prescription updatePrescription(Long id, PrescriptionDto dto) {
        Prescription existing = prescriptionRepository.findById(id)
                .orElseThrow(() -> new PrescriptionNotFoundException(
                    "Prescription not found with id: " + id));

     // Update Appointment if provided
        if (dto.getAppointmentId() != null) {
            existing.setAppointment(
                appointmentRepository.findById(dto.getAppointmentId())
                    .orElseThrow(() -> new RuntimeException(
                        "Appointment not found with id: " + dto.getAppointmentId()))
            );
        }
        
        if (dto.getVitals() != null) {
            VitalsDto vitals = dto.getVitals();
            existing.setTemperature(vitals.getTemp());
            existing.setBloodPressure(vitals.getBp());
            existing.setPulse(vitals.getPulse());
            existing.setSpo2(vitals.getSpo2());
            existing.setHeight(vitals.getHeight());
            existing.setWeight(vitals.getWeight());
            existing.setBmi(vitals.getBmi());
            existing.setWaistHip(vitals.getWaistHip());
        }

        existing.setComplaints(dto.getComplaints());
        existing.setPastHistory(dto.getPastHistory());
        existing.setDiagnosis(dto.getDiagnosis());
        existing.setAdvice(dto.getAdvice());
        existing.setTestRequested(dto.getTestRequested());
        existing.setPastMedications(dto.getPastMedications());
        existing.setGeneralExamination(dto.getGeneralExamination());

        if (dto.getNextVisit() != null) {
            existing.setNextVisitNumber(dto.getNextVisit().getNumber());
            existing.setNextVisitUnit(dto.getNextVisit().getUnit());
            existing.setNextVisitDate(dto.getNextVisit().getDate());
        }

		/*
		 * existing.getMedicines().clear();
		 * existing.setMedicines(mapMedicinesToEntities(dto.getMedicines(),
		 * existing));
		 * 
		 * existing.getReferrals().clear();
		 * existing.setReferrals(mapReferralsToEntities(dto.getReferredTo(), existing));
		 */

        existing.getMedicines().clear();
        existing.getMedicines().addAll(mapMedicinesToEntities(dto.getMedicines(), existing));

        existing.getReferrals().clear();
        existing.getReferrals().addAll(mapReferralsToEntities(dto.getReferredTo(), existing));
        return prescriptionRepository.save(existing);
    }

    // DELETE
    public void deletePrescription(Long id) {
        if (!prescriptionRepository.existsById(id)) {
            throw new PrescriptionNotFoundException("Prescription not found with id: " + id);
        }
        prescriptionRepository.deleteById(id);
    }

    // HELPER - Map Medicines
    private List<PrescriptionMedicine> mapMedicinesToEntities(
            List<MedicineDto> medicinesDto, Prescription prescription) {
        if (medicinesDto == null || medicinesDto.isEmpty()) {
            return Collections.emptyList();
        }
        
        return medicinesDto.stream()
                .filter(dto -> dto.getMedicine() != null && !dto.getMedicine().trim().isEmpty())
                .map(dto -> {
                    PrescriptionMedicine medicine = new PrescriptionMedicine();
                    medicine.setMedicineName(dto.getMedicine());
                    medicine.setType(dto.getType());
                    medicine.setDosage(dto.getDosage());
                    medicine.setWhenToTake(dto.getWhen());
                    medicine.setFrequency(dto.getFrequency());
                    medicine.setDuration(dto.getDuration());
                    medicine.setNotes(dto.getNotes());
                    medicine.setPrescription(prescription);
                    return medicine;
                }).collect(Collectors.toList());
    }

    // HELPER - Map Referrals
    private List<PrescriptionReferral> mapReferralsToEntities(
            List<ReferralDto> referralsDto, Prescription prescription) {
        if (referralsDto == null || referralsDto.isEmpty()) {
            return Collections.emptyList();
        }
        
        return referralsDto.stream()
                .filter(dto -> dto.getDoctor() != null && !dto.getDoctor().trim().isEmpty())
                .map(dto -> {
                    PrescriptionReferral referral = new PrescriptionReferral();
                    referral.setDoctorName(dto.getDoctor());
                    referral.setSpeciality(dto.getSpeciality());
                    referral.setPhone(dto.getPhone());
                    referral.setEmail(dto.getEmail());
                    referral.setPrescription(prescription);
                    return referral;
                }).collect(Collectors.toList());
    }
}
