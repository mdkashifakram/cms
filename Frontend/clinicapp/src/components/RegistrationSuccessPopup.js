import React from 'react';
import './RegistrationSuccessPopup.css';

/**
 * RegistrationSuccessPopup - Displays patient registration success with Patient ID
 *
 * Props:
 * - patientData: Object containing the registered patient details (including id)
 * - onClose: Function to close the popup
 * - onCreateAppointment: Function to proceed to appointment creation
 */
const RegistrationSuccessPopup = ({ patientData, onClose, onCreateAppointment }) => {
  return (
    <div className="success-popup-overlay">
      <div className="success-popup-card">
        <div className="success-popup-header">
          <h2>Registration Successful</h2>
          <button className="success-popup-close-btn" onClick={onClose}>X</button>
        </div>

        <div className="success-content">
          <div className="success-icon">&#10003;</div>

          <div className="patient-id-display">
            <label>Patient ID</label>
            <span className="patient-id-value">{patientData.id}</span>
          </div>

          <div className="patient-summary">
            <p><strong>Name:</strong> {patientData.name}</p>
            <p><strong>Phone:</strong> {patientData.phoneNumber}</p>
            {patientData.gender && <p><strong>Gender:</strong> {patientData.gender}</p>}
            {patientData.age && <p><strong>Age:</strong> {patientData.age}</p>}
          </div>
        </div>

        <div className="success-button-container">
          <button className="close-btn" onClick={onClose}>Close</button>
          <button className="create-appointment-btn" onClick={onCreateAppointment}>
            Create Appointment
          </button>
        </div>
      </div>
    </div>
  );
};

export default RegistrationSuccessPopup;
