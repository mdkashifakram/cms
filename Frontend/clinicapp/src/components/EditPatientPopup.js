// EditPatientPopup.js
import React, { useState } from 'react';
import './EditPatientPopup.css';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const EditPatientPopup = ({ patientData, onClose, onUpdate }) => {
  const [formData, setFormData] = useState({ ...patientData });
  const [loading, setLoading] = useState(false); // To show a loading state if needed
  const [error, setError] = useState(null);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleUpdate = async () => {
    setLoading(true);
    setError(null);

    try {
      // Security Remediation: Using API config and cookie-based auth
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.PATIENTS.BY_ID(patientData.id)}`, {
        method: 'PUT',
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify(formData),
      });

      if (response.ok) {
        const updatedPatientData = await response.json();
        logger.log('Patient updated successfully');
        onUpdate(updatedPatientData); // Propagate the updated data to the parent
        onClose(); // Close the edit popup after successful update
      } else {
        const errorData = await response.json();
        setError(errorData.message || 'Failed to update patient details');
        logger.error('Error updating patient:', errorData?.message);
      }
    } catch (error) {
      setError('An error occurred while updating the patient details.');
      logger.error('Error updating patient details:', error.message);
    } finally {
      setLoading(false);
    }
  };


  return (
    <div className="edit-popup-overlay">
      <div className="edit-popup-card">
        <div className="edit-popup-header">
          <h2>Edit Patient Details</h2>
          <button className="edit-popup-close-btn" onClick={onClose}>X</button>
        </div>

        <div className="edit-form-container">
          <div className="form-group">
            <label>Patient Name</label>
            <input
              type="text"
              name="name"
              value={formData.name || ''}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input
              type="text"
              name="phoneNumber"
              value={formData.phoneNumber || ''}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label>Gender</label>
            <select
              name="gender"
              value={formData.gender || ''} // Set the default value
              onChange={handleChange} // Handle changes
            >
              <option value="">Select Gender</option> {/* Placeholder */}
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other</option>
            </select>
          </div>

          <div className="form-group">
            <label>Age</label>
            <input
              type="text"
              name="age"
              value={formData.age || ''}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-group add">
          <label>Address</label>
          <input
            type="text"
            name="address"
            value={formData.address || ''}
            onChange={handleChange}
          />
        </div>
        <div className="edit-form-container">
          <div className="form-group">
            <label>City</label>
            <input
              type="text"
              name="city"
              value={formData.city || ''}
              onChange={handleChange}
            />
          </div>

          <div className="form-group">
            <label>Pin Code</label>
            <input
              type="text"
              name="pin"
              value={formData.pin || ''}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="edit-button-container">
          <button className="update-btn" onClick={handleUpdate} disabled={loading}>
            {loading ? 'Updating...' : 'Update'}
          </button>
        </div>
        {error && <p className="edit-error-message">{error}</p>} {/* Error message */}
      </div>
    </div>
  );
};

export default EditPatientPopup;
