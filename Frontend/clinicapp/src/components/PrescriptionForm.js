import React, { useState, useEffect } from "react";
import './PrescriptionForm.css';
import DoctorPadTemplate from './DoctorpadTemplate';
import { useLocation } from 'react-router-dom';
import { useRef } from 'react';
import ReactToPrint from 'react-to-print';
import { IconButton, Tooltip, Popper, Paper, ClickAwayListener, Autocomplete, TextField } from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import HistoryIcon from "@mui/icons-material/History";
import ClearIcon from "@mui/icons-material/Clear";
import SearchIcon from "@mui/icons-material/Search";
import DeleteIcon from '@mui/icons-material/Delete';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';
import { searchMedicine } from '../utils/medicineSearch';

const PrescriptionAdvanced = () => {
  // Security Remediation: Removed localStorage - using HttpOnly cookies
  const componentRef = useRef();
  const location = useLocation();
  // Get data from router state
  const {
    prescriptionId: existingPrescriptionId = null,
    mode = "create",
    patientInfo = {},
    doctorInfo = {},
    appointment = {}
  } = location.state || {};

  // Track prescription ID and mode
  const [prescriptionId, setPrescriptionId] = useState(existingPrescriptionId);
  const [isEditMode, setIsEditMode] = useState(mode === "edit");

  // Unified prescription state with all fields
  const [prescription, setPrescription] = useState({
    // Use doctorInfo from navigation state (passed from appointments or auth)
    doctor: {
      id: doctorInfo.id || null,
      name: doctorInfo.name || "Unknown Doctor",
      specialty: doctorInfo.specialization || doctorInfo.specialty || ""
    },
    patient: {
      id: patientInfo.id || null,
      name: patientInfo.name || "",
      age: patientInfo.age || ""
    },
    appointmentId: appointment.id || null,
    vitals: { temp: "", bp: "", pulse: "", spo2: "", bmi: "", height: "", weight: "", waistHip: "" },
    complaints: "",
    pastHistory: "",
    diagnosis: [],
    medicines: [{ type: "", medicine: "", dosage: "", when: "", frequency: "", duration: "", durationNumber: "", durationUnit: "", notes: "" }],
    advice: "",
    testRequested: "",
    nextVisit: {
      number: "",
      unit: "",
      date: ""
    },
    referredTo: [{ doctor: "", speciality: "", phone: "", email: "" }],
    pastMedications: "",
    generalExamination: ""
  });

  // Load doctor info from auth if not provided
  useEffect(() => {
    const loadDoctorInfo = async () => {
      // Only fetch if doctor ID is not already set
      if (!doctorInfo.id) {
        try {
          const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.VALIDATE}`, {
            credentials: API_CONFIG.CREDENTIALS,
            headers: API_CONFIG.HEADERS,
          });

          if (response.ok) {
            const data = await response.json();
            if (data.doctor) {
              setPrescription(prev => ({
                ...prev,
                doctor: {
                  id: data.doctor.id,
                  name: data.doctor.name,
                  specialty: data.doctor.specialty
                }
              }));
            }
          }
        } catch (error) {
          logger.error('Error loading doctor info:', error);
        }
      }
    };

    loadDoctorInfo();
  }, []);

  // Load existing prescription if in edit mode
  useEffect(() => {
    if (prescriptionId) {
      setIsEditMode(true); // ensure we switch to edit mode
      loadExistingPrescription(prescriptionId);
    }
  }, [prescriptionId]);


  //Load existing prescription data
  const loadExistingPrescription = async (id) => {
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_ID(id)}`, {
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
      });

      if (response.ok) {
        const data = await response.json();

        // Map flat backend fields to frontend vitals object
        setPrescription(prev => ({
          ...prev,
          id: data.id,
          doctor: data.doctor,
          patient: data.patient,
          vitals: {
            temp: data.temperature || "",
            bp: data.bloodPressure || "",
            pulse: data.pulse || "",
            spo2: data.spo2 || "",
            height: data.height || "",
            weight: data.weight || "",
            bmi: data.bmi || "",
            waistHip: data.waistHip || "",
          },
          complaints: data.complaints || "",
          pastHistory: data.pastHistory || "",
          diagnosis: data.diagnosis || [],
          advice: data.advice || "",
          testRequested: data.testRequested || "",
          pastMedications: data.pastMedications || "",
          generalExamination: data.generalExamination || "",
          medicines: (data.medicines || []).map(med => {
            const parts = (med.duration || "").match(/^(\d+)\s*(Days|Weeks|Months)$/i);
            return {
              ...med,
              durationNumber: parts ? parts[1] : "",
              durationUnit: parts ? parts[2] : "",
            };
          }),
          referredTo: data.referrals || [],
          nextVisit: {
            number: data.nextVisitNumber || "",
            unit: data.nextVisitUnit || "",
            date: data.nextVisitDate || "",
          }
        }));
      }
    } catch (err) {
      logger.error("Error loading prescription");
    }
  };


  // Check if prescription already exists when component mounts OR load existing one
  useEffect(() => {
    const loadPrescriptionData = async () => {
      // If prescriptionId is passed from navigation, load it directly
      if (existingPrescriptionId && isEditMode) {
        await loadExistingPrescription(existingPrescriptionId);
      }
      // Otherwise check if prescription exists for this appointment
      else if (appointment.id && !existingPrescriptionId) {
        try {
          const response = await fetch(
            `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_APPOINTMENT(appointment.id)}`,
            {
              credentials: API_CONFIG.CREDENTIALS,
              headers: API_CONFIG.HEADERS,
            }
          );
          if (response.ok) {
            const data = await response.json();
            logger.log("Existing prescription found");
            if (data && data.id) {
              setPrescriptionId(data.id);
              setIsEditMode(true);
              setPrescription(data);
            }
          }
        } catch (error) {
          logger.error("Error checking existing prescription");
        }
      }
    };

    loadPrescriptionData();
  }, []); // Empty dependency array - run only once on mount

  const [showSavePrompt, setShowSavePrompt] = useState(false);
  const [templateName, setTemplateName] = useState("");
  const [searchTemplate, setSearchTemplate] = useState("");
  const [patientId, setPatientId] = useState(null);
  const [anchorEl, setAnchorEl] = useState(null);
  const [open, setOpen] = useState(false);
  const [loadOpen, setLoadOpen] = useState(false);
  const [loadAnchorEl, setLoadAnchorEl] = useState(null);
  const [suggestions, setSuggestions] = useState([]);
  const [pastOpen, setPastOpen] = useState(false);
  const [pastAnchorEl, setPastAnchorEl] = useState(null);
  const [pastTemplateName, setPastTemplateName] = useState("");
  const [pastLoadOpen, setPastLoadOpen] = useState(false);
  const [pastLoadAnchorEl, setPastLoadAnchorEl] = useState(null);
  const [searchPastTemplate, setSearchPastTemplate] = useState("");
  const [pastSuggestions, setPastSuggestions] = useState([]);
  const [search, setSearch] = useState("");
  const [compositions, setCompositions] = useState({});
  const [medicineSuggestions, setMedicineSuggestions] = useState({});
  const medicineDebounceRefs = useRef({});

  // Handle medicine input change with debounced autocomplete suggestions
  const handleMedicineInputChange = (index, value) => {
    // Update the medicine name in state
    handleInputChange({ target: { value } }, "medicine", index, "medicines");

    // Clear existing debounce for this row
    if (medicineDebounceRefs.current[index]) {
      clearTimeout(medicineDebounceRefs.current[index]);
    }

    if (!value || value.length < 2) {
      setMedicineSuggestions(prev => ({ ...prev, [index]: [] }));
      setCompositions(prev => {
        const next = { ...prev };
        delete next[index];
        return next;
      });
      return;
    }

    medicineDebounceRefs.current[index] = setTimeout(async () => {
      const results = await searchMedicine(value, 15);
      setMedicineSuggestions(prev => ({ ...prev, [index]: results }));
    }, 200);
  };

  // Handle medicine selection from autocomplete dropdown
  const handleMedicineSelect = (index, selectedMedicine) => {
    if (!selectedMedicine) {
      setCompositions(prev => {
        const next = { ...prev };
        delete next[index];
        return next;
      });
      return;
    }

    const name = typeof selectedMedicine === 'string' ? selectedMedicine : selectedMedicine.name;
    handleInputChange({ target: { value: name } }, "medicine", index, "medicines");

    if (selectedMedicine.composition) {
      setCompositions(prev => ({ ...prev, [index]: selectedMedicine.composition }));
    }
    setMedicineSuggestions(prev => ({ ...prev, [index]: [] }));
  };

  const dropdownOptions = {
    type: ["TAB.", "CAP.", "INJ.", "SYR.", "DROP"],
    dosage: ["0-0-1", "0-1-0", "1-0-0", "1-0-1", "1-1-1", "2-2-2", "0-0-2", "0-2-0", "2-0-0", "2-0-2"],
    when: ["Before Food", "After Food", "Bed Time", "Morning"],
    frequency: ["daily", "weekly", "monthly"],
    speciality: ["Cardiologist", "Neurologist", "Dermatologist", "General Physician"]
  };

  const units = ["Days", "Weeks", "Months"];

  // Unified input handler
  const handleInputChange = (e, field, index = null, section = null) => {
    const value = e.target.value;

    if (section === "medicines") {
      setPrescription(prev => ({
        ...prev,
        medicines: prev.medicines.map((med, i) =>
          i === index ? { ...med, [field]: value } : med
        )
      }));
    } else if (section === "referredTo") {
      setPrescription(prev => ({
        ...prev,
        referredTo: prev.referredTo.map((ref, i) =>
          i === index ? { ...ref, [field]: value } : ref
        )
      }));
    } else if (section === "doctor" || section === "patient") {
      setPrescription(prev => ({
        ...prev,
        [section]: { ...prev[section], [field]: value }
      }));
    } else if (section === "vitals") {
      setPrescription(prev => ({
        ...prev,
        vitals: { ...prev.vitals, [field]: value }
      }));
    } else if (section === "nextVisit") {
      setPrescription(prev => ({
        ...prev,
        nextVisit: { ...prev.nextVisit, [field]: value }
      }));
    } else {
      setPrescription(prev => ({ ...prev, [field]: value }));
    }
  };

  // Add medication row
  const addMedication = () => {
    setPrescription(prev => ({
      ...prev,
      medicines: [
        ...prev.medicines,
        { type: "", medicine: "", dosage: "", when: "", frequency: "", duration: "", durationNumber: "", durationUnit: "", notes: "" }
      ]
    }));
  };

  // Add referral row
  const addReferral = () => {
    setPrescription(prev => ({
      ...prev,
      referredTo: [
        ...prev.referredTo,
        { doctor: "", speciality: "", phone: "", email: "" }
      ]
    }));
  };

  // Delete referral
  const handleDeleteReferralMedicine = (index) => {
    setPrescription(prev => ({
      ...prev,
      medicines: prev.medicines.filter((_, i) => i !== index)
    }));
  };

  // Delete referral
  const handleDeleteReferral = (index) => {
    setPrescription(prev => ({
      ...prev,
      referredTo: prev.referredTo.filter((_, i) => i !== index)
    }));
  };

  // Clear functions
  const handleClearVitals = () => {
    setPrescription(prev => ({
      ...prev,
      vitals: { temp: "", bp: "", pulse: "", spo2: "", bmi: "", height: "", weight: "", waistHip: "" }
    }));
  };

  const handleClearComplaints = () => {
    setPrescription(prev => ({ ...prev, complaints: "" }));
  };

  const handleClearPast = () => {
    setPrescription(prev => ({ ...prev, pastHistory: "" }));
  };

  const handleClear = () => {
    setPrescription(prev => ({ ...prev, diagnosis: [] }));
  };

  const handleClearRows = () => {
    setPrescription(prev => ({
      ...prev,
      medicines: [{ type: "", medicine: "", dosage: "", when: "", frequency: "", duration: "", durationNumber: "", durationUnit: "", notes: "" }]
    }));
  };

  const handleClearAdvice = () => {
    setPrescription(prev => ({ ...prev, advice: "" }));
  };

  const handleClearTestInvestigations = () => {
    setPrescription(prev => ({ ...prev, testRequested: "" }));
  };

  const handleClearNextVisit = () => {
    setPrescription(prev => ({
      ...prev,
      nextVisit: { number: "", unit: "", date: "" }
    }));
  };

  const handleClearReferrals = () => {
    setPrescription(prev => ({
      ...prev,
      referredTo: [{ doctor: "", speciality: "", phone: "", email: "" }]
    }));
  };


  const handleClearPastMed = () => {
    setPrescription(prev => ({ ...prev, pastMedications: "" }));
  };

  // UPDATE existing prescription (for nurse updating vitals, etc.)
  const handleUpdate = async () => {
    if (!prescriptionId) {
      alert("No prescription ID found. Cannot update.");
      return;
    }

    logger.log("Updating Prescription ID:", prescriptionId);
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_ID(prescriptionId)}`, {
        method: "PATCH",
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify(prescription),
      });
      if (response.ok) {
        const data = await response.json();
        logger.log("Prescription updated successfully");
        alert("Prescription updated successfully!");
      } else {
        logger.error("Update failed:", response.statusText);
        alert("Failed to update prescription");
      }
    } catch (error) {
      logger.error("Error updating prescription");
      alert("Error updating prescription");
    }
  };

  // CREATE new prescription (generates new ID)
  const handleSubmit = async () => {
    logger.log("Creating New Prescription");
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.GENERATE}`, {
        method: "POST",
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify(prescription),
      });
      if (response.ok) {
        const data = await response.json();
        logger.log("Prescription created successfully");
        // Set the new prescription ID and switch to edit mode
        if (data.id || data.prescriptionId) {
          setPrescriptionId(data.id || data.prescriptionId);
          setIsEditMode(true);
        }
        alert("Prescription created successfully!");
      } else {
        logger.error("Creation failed:", response.statusText);
        alert("Failed to create prescription");
      }
    } catch (error) {
      logger.error("Error creating prescription");
      alert("Error creating prescription");
    }
  };

  // Determine which button to show
  //---->last working function
  const handleSaveOrUpdate = () => {
    if (prescriptionId) {
      handleUpdate(); // PATCH existing prescription
    } else {
      handleSubmit(); // POST new prescription
    }
  };


  // Template handlers
  const handleSaveTemplate = async () => {
    if (!templateName || prescription.diagnosis.length === 0) return alert("Enter template name & diagnosis");
    await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.TEMPLATES.SAVE}?doctorId=1&section=diagnosis&templateName=${encodeURIComponent(templateName)}`, {
      method: "POST",
      credentials: API_CONFIG.CREDENTIALS,
      headers: API_CONFIG.HEADERS,
      body: JSON.stringify({
        templateName,
        diagnosis: prescription.diagnosis,
        patientId,
      }),
    });
    alert("Template Saved!");
    setShowSavePrompt(false);
    setTemplateName("");
  };

  const handleLoadTemplate = async (name) => {
    const res = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.TEMPLATES.LOAD}?doctorId=1&section=diagnosis&templateName=${encodeURIComponent(name)}`, {
      credentials: API_CONFIG.CREDENTIALS,
      headers: API_CONFIG.HEADERS,
    });
    const data = await res.json();
    setPrescription(prev => ({ ...prev, diagnosis: data.diagnosis || [] }));
    logger.log("Template loaded");
  };

  const handleLoadPrevious = async () => {
    const res = await fetch(`/api/patients/${patientId}/diagnosis/latest`);
    const data = await res.json();
    setPrescription(prev => ({ ...prev, diagnosis: data.diagnosis || [] }));
  };

  // Past History handlers
  const handleSavePastTemplate = async () => {
    if (!pastTemplateName.trim() || !prescription.pastHistory.trim()) return;

    try {
      const res = await fetch(`${API_CONFIG.BASE_URL}/api/past-history/templates`, {
        method: "POST",
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify({
          name: pastTemplateName,
          pastHistory: prescription.pastHistory,
        }),
      });

      if (res.ok) {
        alert("Past History Template Saved!");
        setPastOpen(false);
        setPastTemplateName("");
        const updated = await res.json();
        setPastSuggestions(updated.templates || []);
      }
    } catch (err) {
      logger.error("Error saving past history template");
    }
  };

  const handleLoadPastTemplate = async (name) => {
    try {
      const res = await fetch(`/api/past-history/templates/${name}`);
      const data = await res.json();
      if (data?.pastHistory) {
        setPrescription(prev => ({ ...prev, pastHistory: data.pastHistory }));
      }
    } catch (err) {
      logger.error("Error loading past history template");
    }
  };

  const handleLoadPastPrevious = async () => {
    try {
      const res = await fetch("/api/past-history/previous");
      const data = await res.json();
      if (data?.pastHistory) {
        setPrescription(prev => ({ ...prev, pastHistory: data.pastHistory }));
      }
    } catch (err) {
      logger.error("Error loading previous past history");
    }
  };

  // Diagnosis handlers
  const handleAddDiagnosis = async (term) => {
    if (!term) return;
    if (prescription.diagnosis.includes(term)) return;

    setPrescription(prev => ({
      ...prev,
      diagnosis: [...prev.diagnosis, term]
    }));

    if (!suggestions.includes(term)) {
      await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.DIAGNOSIS.ADD}`, {
        method: "POST",
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify({ term }),
      });
    }
  };

  const handleRemoveDiagnosis = (term) => {
    setPrescription(prev => ({
      ...prev,
      diagnosis: prev.diagnosis.filter(item => item !== term)
    }));
  };

  // Fetch suggestions
  useEffect(() => {
    if (!searchTemplate.trim()) {
      setSuggestions([]);
      return;
    }

    const delayDebounce = setTimeout(() => {
      fetch(`${API_CONFIG.BASE_URL}/api/templates?query=${searchTemplate}`, {
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
      })
        .then((res) => res.json())
        .then((data) => setSuggestions(data))
        .catch(() => setSuggestions([]));
    }, 400);

    return () => clearTimeout(delayDebounce);
  }, [searchTemplate]);

  useEffect(() => {
    if (search.trim()) {
      fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.DIAGNOSIS.SEARCH}?query=${encodeURIComponent(search)}`, {
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
      })
        .then(res => res.json())
        .then(data => setSuggestions(data))
        .catch(() => setSuggestions([]));
    } else {
      setSuggestions([]);
    }
  }, [search]);

  return (
    <div className="prescription-container">
      <div className="flex flex-col mb-8 mt-4">
        <span>
          <strong>Doctor:</strong> {prescription.doctor.name} | <strong>ID:</strong> {"1234"}
        </span>
        <span>
          <strong>Patient:</strong> {prescription.patient.name} | <strong>Age:</strong> ({prescription.patient.age}) years | ID: {prescription.patient.id}
        </span>
        {prescriptionId && (
          <span className="text-blue-600 font-semibold">
            <strong>Prescription ID:</strong> {prescriptionId} (Edit Mode)
          </span>
        )}
      </div>

      <div>
        <div className="left-section">
          {/* Vitals */}
          <div className="flex gap-24">
            <div>
              <h3 className="font-sans text-lg">Vitals</h3>
              <div className="flex justify-center">
                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "white",
                      "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                    onClick={handleClearVitals}>
                    <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>

            <div className="grid grid-cols-4 gap-2 flex-1">
              <div className="flex items-center">
                <label className="text-sm">
                  Temperature
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.temp}
                      onChange={(e) => handleInputChange(e, "temp", null, "vitals")}
                    />
                    <span className="px-2 text-sm">F</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  BP
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.bp}
                      onChange={(e) => handleInputChange(e, "bp", null, "vitals")}
                    />
                    <span className="px-2 text-sm">mmHg</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  Pulse
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.pulse}
                      onChange={(e) => handleInputChange(e, "pulse", null, "vitals")}
                    />
                    <span className="px-2 text-sm">bpm</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  SpO₂
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.spo2}
                      onChange={(e) => handleInputChange(e, "spo2", null, "vitals")}
                    />
                    <span className="px-2 text-sm">%</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  Height
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.height}
                      onChange={(e) => handleInputChange(e, "height", null, "vitals")}
                    />
                    <span className="px-2 text-sm">cm</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  Weight
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.weight}
                      onChange={(e) => handleInputChange(e, "weight", null, "vitals")}
                    />
                    <span className="px-2 text-sm">kg</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  BMI
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.bmi}
                      onChange={(e) => handleInputChange(e, "bmi", null, "vitals")}
                    />
                    <span className="px-2 text-sm">kg/m²</span>
                  </div>
                </label>
              </div>

              <div className="flex items-center">
                <label className="text-sm">
                  Waist/Hip
                  <div className="flex flex-1 items-center rounded-md overflow-hidden mt-2">
                    <input
                      type="text"
                      className="flex-1 px-2 py-1 outline-none"
                      value={prescription.vitals.waistHip}
                      onChange={(e) => handleInputChange(e, "waistHip", null, "vitals")}
                    />
                  </div>
                </label>
              </div>
            </div>
          </div>

          {/* Complaints */}
          <div className="flex gap-12 pt-8">
            <div>
              <h3 className="font-sans text-lg">Complaints</h3>
              <div className="flex pl-3">
                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "white",
                      "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                    onClick={handleClearComplaints}>
                    <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>
            <input
              type="text"
              value={prescription.complaints}
              onChange={(e) => handleInputChange(e, "complaints")}
            />
          </div>

          {/* Past History */}
          <div className="flex gap-4 pt-8">
            <div className="">
              <h3 className="w-32 font-sans text-lg">Past History</h3>
              <div style={{ display: "flex", gap: "5px" }}>
                <Tooltip title="Save Template">
                  <IconButton
                    sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                    onClick={(e) => {
                      setPastAnchorEl(e.currentTarget);
                      setPastOpen((prev) => !prev);
                    }}
                  >
                    <SaveIcon sx={{ fontSize: 18, color: "#fff" }} />
                  </IconButton>
                </Tooltip>

                <Popper open={pastOpen} anchorEl={pastAnchorEl} placement="bottom-start">
                  <ClickAwayListener onClickAway={() => setPastOpen(false)}>
                    <Paper sx={{ p: "10px", width: "250px", mt: 1.25 }}>
                      <input
                        type="text"
                        placeholder="Template Name"
                        value={pastTemplateName}
                        onChange={(e) => setPastTemplateName(e.target.value)}
                        className="border rounded px-2 py-1 w-full"
                      />
                      <div className="flex gap-2 mt-2">
                        <button onClick={handleSavePastTemplate} className="px-3 py-1 bg-green-600 text-white rounded">
                          Save
                        </button>
                        <button onClick={() => setPastOpen(false)} className="px-3 py-1 border rounded">
                          Cancel
                        </button>
                      </div>
                    </Paper>
                  </ClickAwayListener>
                </Popper>

                <Tooltip title="Load Template">
                  <IconButton
                    sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                    onClick={(e) => {
                      setPastLoadAnchorEl(e.currentTarget);
                      setPastLoadOpen((prev) => !prev);
                    }}
                  >
                    <SearchIcon sx={{ fontSize: 18, color: "#fff" }} />
                  </IconButton>
                </Tooltip>

                <Popper open={pastLoadOpen} anchorEl={pastLoadAnchorEl} placement="bottom-start">
                  <ClickAwayListener onClickAway={() => setPastLoadOpen(false)}>
                    <Paper sx={{ p: "10px", width: "250px", mt: 1.25 }}>
                      <Autocomplete
                        freeSolo
                        options={pastSuggestions}
                        value={searchPastTemplate}
                        onInputChange={(event, newValue) => setSearchPastTemplate(newValue)}
                        onChange={(event, newValue) => {
                          if (newValue) {
                            setSearchPastTemplate(newValue);
                            handleLoadPastTemplate(newValue);
                            setPastLoadOpen(false);
                          }
                        }}
                        renderInput={(params) => (
                          <TextField
                            {...params}
                            placeholder="Search Template..."
                            size="small"
                            fullWidth
                            onKeyDown={(e) => {
                              if (e.key === "Enter") {
                                e.preventDefault();
                                handleLoadPastTemplate(searchPastTemplate);
                                setPastLoadOpen(false);
                              }
                            }}
                          />
                        )}
                      />
                    </Paper>
                  </ClickAwayListener>
                </Popper>

                <Tooltip title="Load Previous">
                  <IconButton
                    sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                    onClick={handleLoadPastPrevious}
                  >
                    <HistoryIcon sx={{ fontSize: 18, color: "#fff" }} />
                  </IconButton>
                </Tooltip>

                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                    onClick={handleClearPast}
                  >
                    <ClearIcon sx={{ fontSize: 18, color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>

            <textarea
              value={prescription.pastHistory}
              onChange={(e) => handleInputChange(e, "pastHistory")}
              className="flex-1 border rounded p-2"
            />
          </div>

          {/* Diagnosis */}
          <div className="flex gap-4 pt-8">
            <div className="">
              <h3 className="w-32 font-sans text-lg">Diagnosis</h3>
              <div style={{ display: "flex", gap: "5px" }}>
                <Tooltip title="Save Template">
                  <IconButton
                    sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                    onClick={(e) => {
                      setAnchorEl(e.currentTarget);
                      setOpen((prev) => !prev);
                    }}
                  >
                    <SaveIcon sx={{ fontSize: 18, color: "#fff" }} />
                  </IconButton>
                </Tooltip>

                <Popper open={open} anchorEl={anchorEl} placement="bottom-start">
                  <ClickAwayListener onClickAway={() => setOpen(false)}>
                    <Paper sx={{ p: "10px", width: "250px", mt: 1.25 }}>
                      <input
                        type="text"
                        placeholder="Template Name"
                        value={templateName}
                        onChange={(e) => setTemplateName(e.target.value)}
                        className="border rounded px-2 py-1 w-full"
                      />
                      <div className="flex gap-2 mt-2">
                        <button onClick={handleSaveTemplate} className="px-3 py-1 bg-green-600 text-white rounded">
                          Save
                        </button>
                        <button onClick={() => setOpen(false)} className="px-3 py-1 border rounded">
                          Cancel
                        </button>
                      </div>
                    </Paper>
                  </ClickAwayListener>
                </Popper>

                <Tooltip title="Load Template">
                  <IconButton
                    sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                    onClick={(e) => {
                      setLoadAnchorEl(e.currentTarget);
                      setLoadOpen((prev) => !prev);
                    }}
                  >
                    <SearchIcon sx={{ fontSize: 18, color: "#fff" }} />
                  </IconButton>
                </Tooltip>

                <Popper open={loadOpen} anchorEl={loadAnchorEl} placement="bottom-start">
                  <ClickAwayListener onClickAway={() => setLoadOpen(false)}>
                    <Paper sx={{ p: "10px", width: "250px", mt: 1.25 }}>
                      <Autocomplete
                        freeSolo
                        options={suggestions}
                        value={searchTemplate}
                        onInputChange={(event, newValue) => setSearchTemplate(newValue)}
                        onChange={(event, newValue) => {
                          if (newValue) {
                            setSearchTemplate(newValue);
                            handleLoadTemplate(newValue);
                            setLoadOpen(false);
                          }
                        }}
                        renderInput={(params) => (
                          <TextField
                            {...params}
                            placeholder="Search Template..."
                            size="small"
                            fullWidth
                            onKeyDown={(e) => {
                              if (e.key === "Enter") {
                                e.preventDefault();
                                handleLoadTemplate(searchTemplate);
                                setLoadOpen(false);
                              }
                            }}
                          />
                        )}
                      />
                    </Paper>
                  </ClickAwayListener>
                </Popper>

                <Tooltip title="Load Previous">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "#fff !important",
                      "&:hover": { backgroundColor: "#f0f0f0 !important" },
                    }}
                    onClick={handleLoadPrevious}>
                    <HistoryIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>

                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "white",
                      "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                    onClick={handleClear}>
                    <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>

            <div className="space-y-2 w-8/12">
              <div className="border rounded p-2 flex flex-wrap gap-1">
                {prescription.diagnosis.map((d, i) => (
                  <span
                    key={i}
                    className="bg-yellow-100 px-2 py-1 rounded cursor-pointer"
                    onClick={() => handleRemoveDiagnosis(d)}
                  >
                    {d} ✕
                  </span>
                ))}

                <input
                  type="text"
                  value={search}
                  onChange={(e) => setSearch(e.target.value)}
                  onKeyDown={(e) => {
                    if (e.key === "Enter" && search.trim()) {
                      e.preventDefault();
                      handleAddDiagnosis(search.trim());
                      setSearch("");
                    }
                  }}
                  placeholder="Type diagnosis..."
                  className="outline-none flex-1"
                />
              </div>

              {suggestions.length > 0 && search && (
                <div className="border rounded bg-white shadow p-1 max-h-40 overflow-y-auto">
                  {suggestions.map((s, idx) => (
                    <div
                      key={idx}
                      className="p-1 hover:bg-gray-100 cursor-pointer"
                      onClick={() => {
                        handleAddDiagnosis(s);
                        setSearch("");
                      }}
                    >
                      {s}
                    </div>
                  ))}
                </div>
              )}
            </div>
          </div>

          {/* Medications Table */}
          <div className="p-4">
            <table className="w-full border-collapse border border-gray-300 mt-8">
              <thead>
                <tr className="bg-gray-100">
                  <th className="border p-2">#</th>
                  <th className="border p-2">Type</th>
                  <th className="border p-2">Medicine</th>
                  <th className="border p-2">Dosage</th>
                  <th className="border p-2">When</th>
                  <th className="border p-2">Frequency</th>
                  <th className="border p-2">Duration</th>
                  <th className="border p-2">Notes</th>
                  <th className="border p-2">Action</th>
                </tr>
              </thead>
              <tbody>
                {prescription.medicines.map((row, index) => (
                  <tr key={index} className="align-top">
                    <td className="border p-2 text-center">{index + 1}</td>

                    <td className="border p-2">
                      <select
                        value={row.type}
                        onChange={(e) => handleInputChange(e, "type", index, "medicines")}
                        className="w-full border p-1"
                      >
                        <option value="">Select</option>
                        {dropdownOptions.type.map((opt, i) => (
                          <option key={i} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>

                    <td className="border p-2 align-top" style={{ minWidth: '200px' }}>
                      <Autocomplete
                        freeSolo
                        fullWidth
                        options={medicineSuggestions[index] || []}
                        getOptionLabel={(option) => typeof option === 'string' ? option : option.name}
                        inputValue={row.medicine}
                        onInputChange={(event, newValue, reason) => {
                          if (reason === 'input') {
                            handleMedicineInputChange(index, newValue);
                          }
                        }}
                        onChange={(event, newValue) => {
                          handleMedicineSelect(index, newValue);
                        }}
                        renderOption={(props, option) => (
                          <li {...props} key={option.name + option.composition}>
                            <div>
                              <div className="font-medium">{option.name}</div>
                              <div className="text-xs text-gray-500">{option.composition}</div>
                            </div>
                          </li>
                        )}
                        renderInput={(params) => (
                          <TextField
                            {...params}
                            placeholder="Medicine name"
                            size="small"
                            variant="outlined"
                            sx={{ minWidth: '180px', '& .MuiOutlinedInput-root': { padding: '2px' } }}
                          />
                        )}
                        size="small"
                        disableClearable
                        filterOptions={(x) => x}
                      />
                      {compositions[index] && (
                        <div className="text-xs text-gray-500 mt-1 italic leading-tight">{compositions[index]}</div>
                      )}
                    </td>

                    <td className="border p-2">
                      <select
                        value={row.dosage}
                        onChange={(e) => handleInputChange(e, "dosage", index, "medicines")}
                        className="w-full border p-1"
                      >
                        <option value="">Select</option>
                        {dropdownOptions.dosage.map((opt, i) => (
                          <option key={i} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>

                    <td className="border p-2">
                      <select
                        value={row.when}
                        onChange={(e) => handleInputChange(e, "when", index, "medicines")}
                        className="w-full border p-1"
                      >
                        <option value="">Select</option>
                        {dropdownOptions.when.map((opt, i) => (
                          <option key={i} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>

                    <td className="border p-2">
                      <select
                        value={row.frequency}
                        onChange={(e) => handleInputChange(e, "frequency", index, "medicines")}
                        className="w-full border p-1"
                      >
                        <option value="">Select</option>
                        {dropdownOptions.frequency.map((opt, i) => (
                          <option key={i} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>

                    <td className="border p-2">
                      <div className="flex gap-1 items-center">
                        <input
                          type="number"
                          min="1"
                          value={row.durationNumber || ""}
                          onChange={(e) => {
                            const num = e.target.value;
                            const unit = row.durationUnit || "";
                            handleInputChange({ target: { value: num } }, "durationNumber", index, "medicines");
                            handleInputChange({ target: { value: num && unit ? `${num} ${unit}` : "" } }, "duration", index, "medicines");
                          }}
                          placeholder="No of"
                          className="w-16 border p-1 text-center"
                        />
                        <select
                          value={row.durationUnit || ""}
                          onChange={(e) => {
                            const unit = e.target.value;
                            const num = row.durationNumber || "";
                            handleInputChange({ target: { value: unit } }, "durationUnit", index, "medicines");
                            handleInputChange({ target: { value: num && unit ? `${num} ${unit}` : "" } }, "duration", index, "medicines");
                          }}
                          className="flex-1 border p-1"
                        >
                          <option value="">Select</option>
                          <option value="Days">Days</option>
                          <option value="Weeks">Weeks</option>
                          <option value="Months">Months</option>
                        </select>
                      </div>
                    </td>

                    <td className="border p-2">
                      <input
                        type="text"
                        value={row.notes}
                        onChange={(e) => handleInputChange(e, "notes", index, "medicines")}
                        placeholder="Notes"
                        className="w-full border p-1"
                      />
                    </td>
                    <td className="border p-2 text-center">
                      <Tooltip title="Delete Referral">
                        <IconButton
                          sx={{
                            width: "25px",
                            height: "25px",
                            backgroundColor: "white",
                            "&:hover": { backgroundColor: "#f0f0f0" },
                          }}
                          onClick={() => handleDeleteReferralMedicine(index)}
                        >
                          <DeleteIcon sx={{ fontSize: "18px", color: "#fff" }} />
                        </IconButton>
                      </Tooltip>
                    </td>
                  </tr>
                ))}

                <tr>
                  <td colSpan="9" className="border p-2 text-center text-blue-600 cursor-pointer"
                    onClick={addMedication}
                  >
                    + Add Medicine
                  </td>
                </tr>
              </tbody>
            </table>

            <div className="flex gap-3 mt-3 justify-end">
              <Tooltip title="Save Template">
                <IconButton
                  sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                >
                  <SaveIcon sx={{ fontSize: 18, color: "#fff" }} />
                </IconButton>
              </Tooltip>

              <Tooltip title="Load Template">
                <IconButton
                  sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                >
                  <SearchIcon sx={{ fontSize: 18, color: "#fff" }} />
                </IconButton>
              </Tooltip>

              <Tooltip title="Load Previous">
                <IconButton
                  sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                >
                  <HistoryIcon sx={{ fontSize: 18, color: "#fff" }} />
                </IconButton>
              </Tooltip>

              <Tooltip title="Clear Template">
                <IconButton
                  sx={{ width: 25, height: 25, backgroundColor: "white", "&:hover": { backgroundColor: "#f0f0f0" } }}
                  onClick={handleClearRows}
                >
                  <ClearIcon sx={{ fontSize: 18, color: "#fff" }} />
                </IconButton>
              </Tooltip>
            </div>
          </div>

          {/* Advice */}
          <div className="flex gap-2 pt-8">
            <div>
              <h3 className="w-32 font-sans text-lg">Advice</h3>
              <div className="flex pl-3">
                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "white",
                      "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                    onClick={handleClearAdvice}>
                    <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>
            <textarea
              value={prescription.advice}
              onChange={(e) => handleInputChange(e, "advice")}
            ></textarea>
          </div>

          {/* Test Requested */}
          <div className="flex gap-4 pt-8">
            <div className="w-36">
              <div>
                <h3 className="font-sans text-lg">Test Requested</h3>
                <div className="flex pl-3">
                  <Tooltip title="Clear Template">
                    <IconButton
                      sx={{
                        width: "25px",
                        height: "25px",
                        backgroundColor: "white",
                        "&:hover": { backgroundColor: "#f0f0f0" },
                      }}
                      onClick={handleClearTestInvestigations}>
                      <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                    </IconButton>
                  </Tooltip>
                </div>
              </div>
            </div>
            <input
              type="text"
              value={prescription.testRequested}
              onChange={(e) => handleInputChange(e, "testRequested")}
            />
          </div>

          {/* Next Visit */}
          <div className="flex gap-4 pt-8">
            <div>
              <h3 className="w-32 font-sans text-lg mt-2">Next Visit</h3>
              <div className="flex pl-3">
                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "white",
                      "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                    onClick={handleClearNextVisit}>
                    <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>

            <div className="flex items-center space-x-2">
              <input
                type="number"
                placeholder="No of"
                value={prescription.nextVisit.number}
                onChange={(e) => handleInputChange(e, "number", null, "nextVisit")}
                className="border rounded px-3 py-1 w-24"
              />

              <fieldset className="inline-flex overflow-hidden rounded-md border border-gray-300">
                {units.map((u, i) => (
                  <div key={u} className="flex">
                    <input
                      id={`unit-${u}`}
                      type="radio"
                      name="unit"
                      value={u}
                      checked={prescription.nextVisit.unit === u}
                      onChange={(e) => handleInputChange(e, "unit", null, "nextVisit")}
                      className="peer sr-only"
                    />
                    <label
                      htmlFor={`unit-${u}`}
                      className={`select-none cursor-pointer px-3 py-1 -mb-0 border-r border-gray-300
bg-white text-black
peer-checked:bg-blue-500 peer-checked:text-white peer-checked:border-blue-500
${i === 0 ? "rounded-l-md" : ""} ${i === units.length - 1 ? "rounded-r-md border-r-0" : ""}`}
                    >
                      {u}
                    </label>
                  </div>
                ))}
              </fieldset>

              <span className="text-gray-500">Or</span>

              <input
                type="date"
                value={prescription.nextVisit.date}
                onChange={(e) => handleInputChange(e, "date", null, "nextVisit")}
                className="border rounded px-3 py-1"
              />

              {prescription.nextVisit.number && prescription.nextVisit.unit && (
                <span className="ml-3 text-sm text-gray-700 font-medium">
                  {prescription.nextVisit.number} {prescription.nextVisit.unit}
                </span>
              )}
            </div>
          </div>

          {/* Referred To */}
          <div className="flex gap-4 pt-8">
            <div>
              <h3 className="w-32 font-sans text-lg mt-2">Referred To</h3>
              <div className="flex pl-3">
                <Tooltip title="Clear Template">
                  <IconButton
                    sx={{
                      width: "25px",
                      height: "25px",
                      backgroundColor: "white",
                      "&:hover": { backgroundColor: "#f0f0f0" },
                    }}
                    onClick={handleClearReferrals}>
                    <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                  </IconButton>
                </Tooltip>
              </div>
            </div>

            <table className="mb-6">
              <thead>
                <tr className="bg-gray-100">
                  <th className="border p-2">Doctor Name</th>
                  <th className="border p-2">Speciality</th>
                  <th className="border p-2">Phone No</th>
                  <th className="border p-2">Email</th>
                  <th className="border p-2">Action</th>
                </tr>
              </thead>
              <tbody>
                {prescription.referredTo.map((ref, index) => (
                  <tr key={index}>
                    <td className="border p-2">
                      <input
                        type="text"
                        value={ref.doctor}
                        onChange={(e) => handleInputChange(e, "doctor", index, "referredTo")}
                        placeholder="Doctor Name"
                        className="w-full border p-1"
                      />
                    </td>
                    <td className="border p-2">
                      <select
                        value={ref.speciality}
                        onChange={(e) => handleInputChange(e, "speciality", index, "referredTo")}
                        className="w-full border p-1"
                      >
                        <option value="">Select</option>
                        {dropdownOptions.speciality.map((opt, i) => (
                          <option key={i} value={opt}>{opt}</option>
                        ))}
                      </select>
                    </td>
                    <td className="border p-2">
                      <input
                        type="text"
                        value={ref.phone}
                        onChange={(e) => handleInputChange(e, "phone", index, "referredTo")}
                        placeholder="+91 Number"
                        className="w-full border p-1"
                      />
                    </td>
                    <td className="border p-2">
                      <input
                        type="email"
                        value={ref.email}
                        onChange={(e) => handleInputChange(e, "email", index, "referredTo")}
                        placeholder="Email"
                        className="w-full border p-1"
                      />
                    </td>
                    <td className="border p-2 text-center">
                      <Tooltip title="Delete Referral">
                        <IconButton
                          sx={{
                            width: "25px",
                            height: "25px",
                            backgroundColor: "white",
                            "&:hover": { backgroundColor: "#f0f0f0" },
                          }}
                          onClick={() => handleDeleteReferral(index)}
                        >
                          <DeleteIcon sx={{ fontSize: "18px", color: "#fff" }} />
                        </IconButton>
                      </Tooltip>
                    </td>
                  </tr>
                ))}
                <tr>
                  <td colSpan="5" className="border p-2 text-center text-blue-600 cursor-pointer" onClick={addReferral}>
                    + Add Referral
                  </td>
                </tr>
              </tbody>
            </table>
          </div>

          {/* Past Medication */}
          <div className="flex gap-2 pt-8">
            <div className="w-40">
              <div>
                <h3 className="font-sans text-lg">Past Medication</h3>
                <div className="flex pl-3">
                  <Tooltip title="Clear Template">
                    <IconButton
                      sx={{
                        width: "25px",
                        height: "25px",
                        backgroundColor: "white",
                        "&:hover": { backgroundColor: "#f0f0f0" },
                      }}
                      onClick={handleClearPastMed}>
                      <ClearIcon sx={{ fontSize: "18px", color: "#fff" }} />
                    </IconButton>
                  </Tooltip>
                </div>
              </div>
            </div>
            <input
              type="text"
              value={prescription.pastMedications}
              onChange={(e) => handleInputChange(e, "pastMedications")}
            />
          </div>

          {/* General Examination */}
          <div className="flex gap-5 pt-8">
            <div>
              <h3 className="w-32 font-sans text-lg">General Examinations</h3>
              <div className="flex pl-3"></div>
            </div>
            <textarea
              value={prescription.generalExamination}
              onChange={(e) => handleInputChange(e, "generalExamination")}
            ></textarea>
          </div>
        </div>
      </div>

      <div className="max-w-4xl ml-auto px-4 flex justify-end">
        <div className="flex gap-4">
          {/* Dynamic button based on mode */}
          <button
            className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
            onClick={handleSaveOrUpdate}
          >
            {isEditMode && prescriptionId ? "Update" : "Save"}
          </button>
          <button
            className="px-4 py-2 bg-gray-300 rounded hover:bg-gray-400"
            onClick={() =>
              setPrescription({
                doctor: { id: 1, name: "Dr. John Doe", specialization: "General Physician" },
                patient: { id: "1234", name: "Jane Smith", age: "56" },
                vitals: { temp: "", bp: "", pulse: "", spo2: "", bmi: "", height: "", weight: "", waistHip: "" },
                complaints: "",
                pastHistory: "",
                diagnosis: [],
                medicines: [{ type: "", medicine: "", dosage: "", when: "", frequency: "", duration: "", durationNumber: "", durationUnit: "", notes: "" }],
                advice: "",
                testRequested: "",
                nextVisit: { number: "", unit: "", date: "" },
                referredTo: [{ doctor: "", speciality: "", phone: "", email: "" }],
                pastMedications: "",
                generalExamination: ""
              })
            }
          >
            Clear
          </button>
          <ReactToPrint
            trigger={() => <button className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700">Print</button>}
            content={() => componentRef.current}
          />

          <div style={{ display: 'none' }}>
            <DoctorPadTemplate ref={componentRef} formData={prescription} />
          </div>
        </div>
      </div>
    </div>
  );
};

export default PrescriptionAdvanced;