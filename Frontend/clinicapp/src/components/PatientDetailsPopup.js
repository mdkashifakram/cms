import React, { useState } from 'react';
import './PatientDetailsPopup.css';
import BookAppointmentPopup from './BookAppointmentPopup';
import EditPatientPopup from './EditPatientPopup'; // We'll create this next
import { FaEdit } from 'react-icons/fa'; // Import the edit icon

const PatientDetailsPopup = ({ patientData, onClose }) => {
  const [showAppointmentPopup, setShowAppointmentPopup] = useState(false);
  const [showEditPopup, setShowEditPopup] = useState(false);
  const [currentPatientData, setCurrentPatientData] = useState(patientData);

  const handleCreateAppointment = () => {
    setShowAppointmentPopup(true);
  };

  const handleCloseAppointmentPopup = () => {
    setShowAppointmentPopup(false);
  };

  const handleOpenEditPopup = () => {
    setShowEditPopup(true);
  };

  const handleCloseEditPopup = () => {
    setShowEditPopup(false);
  };

  const handleUpdate = (updatedData) => {
    setCurrentPatientData(updatedData); // Update local state with new patient data
    setShowEditPopup(false); // Close the edit popup
  };

  return (
      <div className={`popup-overlay ${showAppointmentPopup || showEditPopup ? 'hide-blur' : ''}`}>
      {!showAppointmentPopup && (
      <div className="popup-card">
          <div className="popup-header">
            <h2>Patient Details</h2>
            <button className="edit-button" onClick={handleOpenEditPopup}>
              <FaEdit />
            </button>
            <button className="popup-close-btn" onClick={onClose}>X</button>
          </div>

        <div className="form-container">
            <div className="form-group">
              <label>Patient Name</label>
              <input
                type="text"
                value={patientData.name || ''}
                readOnly
              />
            </div>

            <div className="form-group">
              <label>Phone Number</label>
              <input
                type="text"
                value={patientData.phoneNumber || ''}
                readOnly
              />
            </div>


            <div className="form-group">
              <label>Gender</label>
              <input
                type="text"
                value={patientData.gender || ''}
                readOnly
              />
            </div>

            <div className="form-group">
              <label>Age</label>
              <input
                type="text"
                value={patientData.age || ''}
                readOnly
              />
            </div>
          </div>
            <div className="add form-group">
              <label>Address</label>
              <input
                type="text"
                value={patientData.address || ''}
                readOnly
              />
            </div>
          
          <div className="form-container">
            <div className="form-group">
              <label>City</label>
              <input
                type="text"
                value={patientData.city || ''}
                readOnly
              />
            </div>


            <div className="form-group">
              <label>Pin Code</label>
              <input
                type="text"
                value={patientData.pin || ''}
                readOnly
              />
            </div>
        </div>

        <div className="button-container">
          <button className="create-appointment-btn"  onClick={handleCreateAppointment}>Create Appointment</button>
        </div>
      </div>
      )}

      {/* Show the appointment popup when "Create Appointment" is clicked */}
      {showAppointmentPopup && (
        <BookAppointmentPopup onClose={handleCloseAppointmentPopup} 
        patientData = {currentPatientData} />
      )}

      {/* Show the edit popup when "Edit" is clicked */}
      {showEditPopup && (
        <EditPatientPopup patientData={patientData} 
        onClose={handleCloseEditPopup} 
        onUpdate={handleUpdate}/>
      )}
    </div> 
  );
};

export default PatientDetailsPopup;
