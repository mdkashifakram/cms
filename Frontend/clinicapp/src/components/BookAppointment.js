import React, { useState } from 'react';
import './BookAppointment.css';
import PatientDetailsPopup from './PatientDetailsPopup';
import RegistrationSuccessPopup from './RegistrationSuccessPopup';
import BookAppointmentPopup from './BookAppointmentPopup';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const BookAppointment = () => {
  // Security Remediation: Removed localStorage - using HttpOnly cookies
  const [formData, setFormData] = useState({
    salutation: '',
    name: '',
    phoneNumber: '',
    gender: '',
    age: '',
    address: '',
    city: '',
    pin: '',
  });

  const [existingPatientData, setExistingPatientData] = useState({
    existingPatientId: '',
    existingPatientName: '',
    existingPhoneNumber: '',
  });

  const [errors, setErrors] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false); // To handle loading state
  const [showPopup, setShowPopup] = useState(false); // For showing the popup
  const [fetchedPatientDetails, setFetchedPatientDetails] = useState(null); // For holding fetched data

  // State for registration success popup
  const [showSuccessPopup, setShowSuccessPopup] = useState(false);
  const [registeredPatientData, setRegisteredPatientData] = useState(null);
  const [showNewAppointmentPopup, setShowNewAppointmentPopup] = useState(false);


  // Handle input change and clear errors for new patients
  const handleInputChange = (e) => {
    const { name, value } = e.target;

    setFormData((prevData) => ({
      ...prevData,
      [name]: value,
    }));

    if (errors[name]) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        [name]: '',
      }));
    }
  };

  // Handle input change and clear errors for existing patients
  const handleExistingInputChange = (e) => {
    const { name, value } = e.target;

    setExistingPatientData((prevData) => ({
      ...prevData,
      [name]: value,
    }));

    if (errors[name]) {
      setErrors((prevErrors) => ({
        ...prevErrors,
        [name]: '',
      }));
    }
  };

  // Validation logic for both new and existing patients
  const validateForm = (section) => {
    const newErrors = {};

    if (section === 'new') {
      if (!formData.salutation) newErrors.salutation = 'Salutation is required!';
      if (!formData.name) newErrors.name = 'Patient name is required!';
      if (!formData.phoneNumber) newErrors.phoneNumber = 'Phone number is required!';
      if (!formData.gender) newErrors.gender = 'Gender is required!';
      if (!formData.age) newErrors.age = 'Age or DOB is required!';
    } else if (section === 'existing') {
      // Support search by Patient ID OR (Name + Phone)
      const hasPatientId = existingPatientData.existingPatientId.trim() !== '';
      const hasNameAndPhone = existingPatientData.existingPatientName.trim() !== '' &&
                              existingPatientData.existingPhoneNumber.trim() !== '';

      if (!hasPatientId && !hasNameAndPhone) {
        newErrors.existingSearch = 'Enter Patient ID OR both Name and Phone Number';
      }
    }

    return newErrors;
  };

  // Handle form submission for new patient registration
  const handleSubmit = async (e) => {
    e.preventDefault();

    const validationErrors = validateForm('new');
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    // Submit form data to the backend
    try {
      setIsSubmitting(true);
      logger.log('Submitting form data');
      // Combine salutation and name before submission
      const submissionData = {
        ...formData,
        name: `${formData.salutation} ${formData.name}`.trim(),
      };
      // Remove salutation from submission as it's now part of name
      delete submissionData.salutation;

      // Make API call to register new patient
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.PATIENTS.ALL}`, {
        method: 'POST',
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify(submissionData),
      });

      const data = await response.json();
      logger.log('Form data submitted');
      if (response.ok) {
        logger.log('Patient registered successfully');
        // Show success popup with patient ID instead of alert
        setRegisteredPatientData(data);
        setShowSuccessPopup(true);

        // Reset form after successful submission
        setFormData({
          salutation: '',
          name: '',
          phoneNumber: '',
          gender: '',
          age: '',
          address: '',
          city: '',
          pin: '',
        });
      } else {
        logger.error('Error registering patient:', data?.message);
        alert('Error registering patient: ' + (data.message || 'Unknown error'));
      }
    } catch (error) {
      logger.error('Error:', error.message);
      alert('Error registering patient: ' + error.message);
    } finally {
      setIsSubmitting(false);
    }
  };

  // Handle search for existing patient
  // Supports: Patient ID search OR Name+Phone search
  // CMS-012: Using POST instead of GET to protect patient PII from URL exposure
  const handleSearchPatient = async (e) => {
    e.preventDefault();

    const validationErrors = validateForm('existing');
    if (Object.keys(validationErrors).length > 0) {
      setErrors(validationErrors);
      return;
    }

    // Proceed with search functionality for existing patient
    logger.log('Searching for existing patient...');

    // Build search request based on what user provided
    const searchRequest = {};
    if (existingPatientData.existingPatientId.trim() !== '') {
      searchRequest.patientId = parseInt(existingPatientData.existingPatientId, 10);
    } else {
      searchRequest.name = existingPatientData.existingPatientName;
      searchRequest.phoneNumber = existingPatientData.existingPhoneNumber;
    }

    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.PATIENTS.SEARCH}`, {
        method: 'POST',
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify(searchRequest)
      });

      const data = await response.json();

      if (response.ok) {
        logger.log('Patient found');
        setFetchedPatientDetails(data);
        setShowPopup(true); // Show popup with patient details
      } else {
        alert(typeof data === 'string' ? data : 'Patient not found!');
      }
    } catch (error) {
      logger.error('Error fetching patient data:', error.message);
      alert('Error fetching patient details');
    }
  };

  const closePopup = () => {
    setShowPopup(false);
    setExistingPatientData({
      existingPatientId: '',
      existingPatientName: '',
      existingPhoneNumber: ''
    });
  };

  // Handlers for registration success popup
  const closeSuccessPopup = () => {
    setShowSuccessPopup(false);
    setRegisteredPatientData(null);
  };

  const handleCreateAppointmentFromSuccess = () => {
    setShowSuccessPopup(false);
    setShowNewAppointmentPopup(true);
  };

  const closeNewAppointmentPopup = () => {
    setShowNewAppointmentPopup(false);
    setRegisteredPatientData(null);
  };

  return (
    <div className="container">
      {/* Section 1: New Patient Registration */}
      <div className="section-one">
        <h2 className="section-header">New Patient</h2>
        <div className="form-container">
          <div className="form-group name-with-salutation">
            <label>Patient Name*</label>
            <div className="name-input-group">
              <select
                name="salutation"
                value={formData.salutation}
                onChange={handleInputChange}
                className="salutation-select"
              >
                <option value="">Title</option>
                <option value="Mr.">Mr.</option>
                <option value="Mrs.">Mrs.</option>
                <option value="Ms.">Ms.</option>
                <option value="Dr.">Dr.</option>
                <option value="Master">Master</option>
                <option value="Baby">Baby</option>
              </select>
              <input
                type="text"
                name="name"
                placeholder="Enter Patient Name"
                value={formData.name}
                onChange={handleInputChange}
                className="name-input"
              />
            </div>
            <div className="name-input-group">
              {errors.salutation && <span className="error-message">{errors.salutation}</span>}
              {errors.name && <span className="error-message">{errors.name}</span>}
            </div>
          </div>

          <div className="form-group gender-full-row">
            <label>Gender*</label>
            <div className="form-group-gender gender-group full-width">
              <input
                type="radio"
                id="male"
                name="gender"
                value="M"
                checked={formData.gender === 'M'}
                onChange={handleInputChange}
              />
              <label htmlFor="male">M</label>
              <input
                type="radio"
                id="female"
                name="gender"
                value="F"
                checked={formData.gender === 'F'}
                onChange={handleInputChange}
              />
              <label htmlFor="female">F</label>
              <input
                type="radio"
                id="other"
                name="gender"
                value="Other"
                checked={formData.gender === 'Other'}
                onChange={handleInputChange}
              />
              <label htmlFor="other">Other</label>
            </div>
            {errors.gender && <span className="error-message">{errors.gender}</span>}
          </div>

          <div className="form-group">
            <label>Phone Number*</label>
            <input
              type="text"
              name="phoneNumber"
              placeholder="Enter Phone Number"
              value={formData.phoneNumber}
              onChange={handleInputChange}
            />
            {errors.phoneNumber && <span className="error-message">{errors.phoneNumber}</span>}
          </div>

          <div className="form-group">
            <label>Age or DOB*</label>
            <input
              type="text"
              name="age"
              placeholder="Enter Age or DOB"
              value={formData.age}
              onChange={handleInputChange}
            />
            {errors.age && <span className="error-message">{errors.age}</span>}
          </div>

          
        </div>

        <div className="add form-group">
          <label>Address</label>
          <input
            type="text"
            name="address"
            placeholder="Enter Address"
            value={formData.address}
            onChange={handleInputChange}
          />
        </div>
        <div className="form-container">
          <div className="form-group">
            <label>City</label>
            <input
              type="text"
              name="city"
              placeholder="Enter City"
              value={formData.city}
              onChange={handleInputChange}
            />
          </div>

          <div className="form-group">
            <label>Pin Code</label>
            <input
              type="text"
              name="pin"
              placeholder="Enter Pin Code"
              value={formData.pin}
              onChange={handleInputChange}
            />
          </div>
        </div>

        <div className="button-container">
          <button onClick={handleSubmit} disabled={isSubmitting}>
            {isSubmitting ? 'Submitting...' : 'Register & Create Appointment'}
          </button>
        </div>
      </div>

      {/* Section 2: Existing Patient Search */}
      <div className="section-two">
        <h2 className="section-header">Existing Patient</h2>

        <div className="search-mode-info">
          <p>Search by Patient ID <strong>OR</strong> Name + Phone Number</p>
        </div>

        <div className="form-container-exist">
          <div className="form-group width">
            <label>Patient ID</label>
            <input
              type="text"
              name="existingPatientId"
              placeholder="Enter Patient ID"
              value={existingPatientData.existingPatientId}
              onChange={handleExistingInputChange}
            />
          </div>

          <div className="or-divider">
            <span>OR</span>
          </div>

          <div className="form-group width">
            <label>Patient Name</label>
            <input
              type="text"
              name="existingPatientName"
              placeholder="Enter Patient Name"
              value={existingPatientData.existingPatientName}
              onChange={handleExistingInputChange}
              disabled={existingPatientData.existingPatientId.trim() !== ''}
            />
          </div>

          <div className="form-group width">
            <label>Phone Number</label>
            <input
              type="text"
              name="existingPhoneNumber"
              placeholder="Enter Phone Number"
              value={existingPatientData.existingPhoneNumber}
              onChange={handleExistingInputChange}
              disabled={existingPatientData.existingPatientId.trim() !== ''}
            />
          </div>
        </div>

        {errors.existingSearch && <span className="error-message search-error">{errors.existingSearch}</span>}

        <div className="button-container">
          <button onClick={handleSearchPatient}>Search Patient</button>
        </div>

        {/* Popup for patient details */}
        {showPopup && (
          <PatientDetailsPopup
            patientData={fetchedPatientDetails}
            onClose={closePopup}
          />
        )}

      </div>

      {/* Success popup after registration */}
      {showSuccessPopup && registeredPatientData && (
        <RegistrationSuccessPopup
          patientData={registeredPatientData}
          onClose={closeSuccessPopup}
          onCreateAppointment={handleCreateAppointmentFromSuccess}
        />
      )}

      {/* Appointment popup from new registration */}
      {showNewAppointmentPopup && registeredPatientData && (
        <BookAppointmentPopup
          patientData={registeredPatientData}
          onClose={closeNewAppointmentPopup}
        />
      )}
    </div>
  );
};

export default BookAppointment;
