import React, { useState, useEffect } from 'react';
import './UpdateAppointment.css';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const UpdateAppointment = ({ selectedAppointment, isModalOpen, handleCloseModal, onUpdateSuccess }) => {
  // State for form fields - initialized from selectedAppointment
  const [appointmentTime, setAppointmentTime] = useState('');
  const [details, setDetails] = useState('');
  const [status, setStatus] = useState('');
  const [selectedDuration, setSelectedDuration] = useState('10 min');
  const [appointmentType, setAppointmentType] = useState('in-person');
  const [skipBilling, setSkipBilling] = useState(false);
  const [isUpdating, setIsUpdating] = useState(false);

  // Initialize form values when selectedAppointment changes
  useEffect(() => {
    if (selectedAppointment) {
      setAppointmentTime(selectedAppointment.appointmentTime || '');
      setDetails(selectedAppointment.details || 'Follow-Up Consultation');
      setStatus(selectedAppointment.status || 'Booked');
    }
  }, [selectedAppointment]);

  if (!isModalOpen || !selectedAppointment) return null;

  const handleDurationChange = (e) => {
    setSelectedDuration(e.target.value);
  };

  const handleAppointmentTimeChange = (e) => {
    setAppointmentTime(e.target.value);
  };

  const handleDetailsChange = (e) => {
    setDetails(e.target.value);
  };

  const handleStatusChange = (e) => {
    setStatus(e.target.value);
  };

  const handleTypeChange = (e) => {
    setAppointmentType(e.target.value);
  };

  const handleSkipBillingChange = (e) => {
    setSkipBilling(e.target.checked);
  };

  // Generate duration options (10 min to 50 min)
  const durationOptions = Array.from({ length: 5 }, (v, i) => `${(i + 1) * 10} min`);

  const handleUpdate = async () => {
    if (!appointmentTime) {
      alert('Please select a date and time for the appointment.');
      return;
    }

    setIsUpdating(true);

    const appointmentData = {
      id: selectedAppointment.id,
      patientName: selectedAppointment.patientName,
      appointmentTime,
      details,
      status,
      doctor: selectedAppointment.doctor,
      patient: selectedAppointment.patient
    };

    logger.log('Updating appointment...');

    try {
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.APPOINTMENTS.UPDATE(selectedAppointment.id)}`,
        {
          method: 'PUT',
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS,
          body: JSON.stringify(appointmentData),
        }
      );

      if (response.ok) {
        logger.log('Appointment updated successfully');
        alert('Appointment updated successfully!');
        if (onUpdateSuccess) {
          onUpdateSuccess();
        }
        handleCloseModal();
      } else {
        const errorData = await response.json().catch(() => ({}));
        logger.error('Failed to update appointment:', errorData);
        alert('Failed to update appointment: ' + (errorData.message || 'Unknown error'));
      }
    } catch (error) {
      logger.error('Error updating appointment:', error.message);
      alert('An error occurred while updating the appointment.');
    } finally {
      setIsUpdating(false);
    }
  };

  return (
    <div className="modal-overlay" onClick={handleCloseModal}>
      <div className='popup-card-pop'>
        <div onClick={(e) => e.stopPropagation()}>
          <div className="modal-header-pop">
            <h2>{selectedAppointment.patientName}</h2>
            <button className="popup-close-button" onClick={handleCloseModal}>X</button>
          </div>

          <div className="modal-details">
            {selectedAppointment.age && <p><strong>Age:</strong> {selectedAppointment.age}</p>}
            <p><strong>Patient ID:</strong> {selectedAppointment.patient?.id || selectedAppointment.id}</p>
          </div>

          <div className="form-container">
            <div className="form-group">
              <label>Doctor Name</label>
              <input type="text" value="Dr. Aswini Rana" readOnly />
            </div>

            <div className="form-group">
              <label>Date & Time of Appointment</label>
              <input
                type="datetime-local"
                value={appointmentTime}
                onChange={handleAppointmentTimeChange}
              />
            </div>

            <div className="form-group">
              <label>Service</label>
              <select value={details} onChange={handleDetailsChange}>
                <option value="Follow-Up Consultation">Follow-Up Consultation</option>
                <option value="Initial Consultation">Initial Consultation</option>
              </select>
            </div>

            <div className="form-group">
              <label>Booking Status</label>
              <select value={status} onChange={handleStatusChange}>
                <option value="Booked">Booked</option>
                <option value="Pending">Pending</option>
                <option value="Completed">Completed</option>
              </select>
            </div>

            <div className="form-group">
              <label>Type</label>
              <div>
                <input
                  type="radio"
                  id="update-in-person"
                  name="update-type"
                  value="in-person"
                  checked={appointmentType === 'in-person'}
                  onChange={handleTypeChange}
                />
                <label htmlFor="update-in-person">In-Person</label>
              </div>
            </div>

            <div className="form-group">
              <label>Duration</label>
              <select value={selectedDuration} onChange={handleDurationChange}>
                {durationOptions.map((duration, index) => (
                  <option key={index} value={duration}>
                    {duration}
                  </option>
                ))}
              </select>
            </div>

            <div className="form-group">
              <label>
                <input
                  type="checkbox"
                  checked={skipBilling}
                  onChange={handleSkipBillingChange}
                /> Skip Billing
              </label>
            </div>

            <div className="button-container">
              <button onClick={handleCloseModal}>Cancel</button>
              <button
                className="book-appointment-btn"
                onClick={handleUpdate}
                disabled={isUpdating}
              >
                {isUpdating ? 'Updating...' : 'Update Appointment'}
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default UpdateAppointment;
