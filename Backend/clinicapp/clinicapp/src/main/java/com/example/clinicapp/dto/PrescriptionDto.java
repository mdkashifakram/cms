package com.example.clinicapp.dto;

import java.util.List;

public class PrescriptionDto {
    private DoctorDto doctor;
    private PatientDto patient;
    
    private Long appointmentId;

    // Vitals object
    private VitalsDto vitals;

    // Text sections
    private String complaints;
    private String pastHistory;
    private List<String> diagnosis; // Changed from String to List<String>
    private String advice; // Added
    private String testRequested; // Added (renamed from testInvestigations)
    private String pastMedications;
    private String generalExamination;

    // Medicines list
    private List<MedicineDto> medicines;
    
    // Next Visit
    private NextVisitDto nextVisit; // Added
    
    // Referred To
    private List<ReferralDto> referredTo; // Added

    // Default constructor
    public PrescriptionDto() {}


    
    public PrescriptionDto(DoctorDto doctor, PatientDto patient, Long appointmentId, VitalsDto vitals,
			String complaints, String pastHistory, List<String> diagnosis, String advice, String testRequested,
			String pastMedications, String generalExamination, List<MedicineDto> medicines, NextVisitDto nextVisit,
			List<ReferralDto> referredTo) {
		super();
		this.doctor = doctor;
		this.patient = patient;
		this.appointmentId = appointmentId;
		this.vitals = vitals;
		this.complaints = complaints;
		this.pastHistory = pastHistory;
		this.diagnosis = diagnosis;
		this.advice = advice;
		this.testRequested = testRequested;
		this.pastMedications = pastMedications;
		this.generalExamination = generalExamination;
		this.medicines = medicines;
		this.nextVisit = nextVisit;
		this.referredTo = referredTo;
	}



	// Getters and Setters
    public DoctorDto getDoctor() {
        return doctor;
    }

    public void setDoctor(DoctorDto doctor) {
        this.doctor = doctor;
    }

    public PatientDto getPatient() {
        return patient;
    }

    public void setPatient(PatientDto patient) {
        this.patient = patient;
    }

    public VitalsDto getVitals() {
        return vitals;
    }

    public void setVitals(VitalsDto vitals) {
        this.vitals = vitals;
    }

    public String getComplaints() {
        return complaints;
    }

    public void setComplaints(String complaints) {
        this.complaints = complaints;
    }

    public String getPastHistory() {
        return pastHistory;
    }

    public void setPastHistory(String pastHistory) {
        this.pastHistory = pastHistory;
    }

    public List<String> getDiagnosis() {
        return diagnosis;
    }

    public void setDiagnosis(List<String> diagnosis) {
        this.diagnosis = diagnosis;
    }

    public String getAdvice() {
        return advice;
    }

    public void setAdvice(String advice) {
        this.advice = advice;
    }

    public String getTestRequested() {
        return testRequested;
    }

    public void setTestRequested(String testRequested) {
        this.testRequested = testRequested;
    }

    public String getPastMedications() {
        return pastMedications;
    }

    public void setPastMedications(String pastMedications) {
        this.pastMedications = pastMedications;
    }

    public String getGeneralExamination() {
        return generalExamination;
    }

    public void setGeneralExamination(String generalExamination) {
        this.generalExamination = generalExamination;
    }

    public List<MedicineDto> getMedicines() {
        return medicines;
    }

    public void setMedicines(List<MedicineDto> medicines) {
        this.medicines = medicines;
    }

    public NextVisitDto getNextVisit() {
        return nextVisit;
    }

    public void setNextVisit(NextVisitDto nextVisit) {
        this.nextVisit = nextVisit;
    }

    public List<ReferralDto> getReferredTo() {
        return referredTo;
    }

    public void setReferredTo(List<ReferralDto> referredTo) {
        this.referredTo = referredTo;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
}