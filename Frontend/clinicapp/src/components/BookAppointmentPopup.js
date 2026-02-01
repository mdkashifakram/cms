import React, { useState } from 'react';
import './BookAppointmentPopup.css'; // Add corresponding CSS styles
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';


const BookAppointmentPopup = ({ patientData, onClose }) => {
  const [selectedDuration, setSelectedDuration] = useState('10 min');
  const [appointmentTime, setAppointmentDate] = useState('');
  const [details, setService] = useState('Follow-Up Consultation');
  const [status, setBookingStatus] = useState('Booked');
  const [appointmentType, setAppointmentType] = useState('in-person');
  const [skipBilling, setSkipBilling] = useState(false);
  const [patientEmail, setpatientEmail] = useState('propagativesolution@gmail.com');
  const [patientName, setpatientName] = useState(patientData.name);
  const [error, setError] = useState(null);

  const handleDurationChange = (e) => {
    setSelectedDuration(e.target.value);
  };

  // Generate an array of options (10 min, 20 min, up to 120 min)
  const durationOptions = Array.from({ length: 5 }, (v, i) => `${(i + 1) * 10} min`);

  const handleAppointmentDateChange = (e) => {
    setAppointmentDate(e.target.value);
  };

  const handleServiceChange = (e) => {
    setService(e.target.value);
  };

  const handleBookingStatusChange = (e) => {
    setBookingStatus(e.target.value);
  };

  const handleTypeChange = (e) => {
    setAppointmentType(e.target.value);
  };

  const handleSkipBillingChange = (e) => {
    setSkipBilling(e.target.checked);
  };

  const handleSubmit = async () => {
    //console.log(patientData);
    const appointmentData = {
      patientName, // data coming from parent
      appointmentTime,
      details,
      status,
      doctor: { id: 1 },  // TODO: Make this dynamic with doctor selection
      patient: { id: patientData.id }
    };
    logger.log('Booking appointment...');
    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.APPOINTMENTS.BOOK}`, {
        method: 'POST',
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
        body: JSON.stringify(appointmentData),
      });
      logger.log(response.ok ? 'Appointment booked' : 'Booking failed');
      if (response.ok) {
        logger.log('Appointment booked successfully');
        alert(`Appointment booked successfully.`);
        onClose(); // Close the popup after success
      } else {

        logger.error('Failed to book appointment');
      }
    } catch (error) {
      alert(`An error occurred while booking appointment.`);
      logger.error('Booking error:', error.message);
    }
  };




  return (
    <div className="popup-overlay">
      <div className="popup-card-pop">
        <div className="popup-header-pop">
          <h2>New Appointment</h2>
          <button className="popup-close-button" onClick={onClose}>X</button>
        </div>

        <div className="form-container">
          <div className="form-group">
            <label>Doctor Name</label>
            <input type="text" value="Dr. Aswini Rana" readOnly />
          </div>

          <div className="form-group">
            <label>Date & Time of Appointment</label>
            <input type="datetime-local" value={appointmentTime} onChange={handleAppointmentDateChange} />
          </div>

          <div className="form-group">
            <label>Service</label>
            <select value={details} onChange={handleServiceChange}>
              <option>Follow-Up Consultation</option>
              <option>Initial Consultation</option>
            </select>
          </div>

          <div className="form-group">
            <label>Booking Status</label>
            <select value={status} onChange={handleBookingStatusChange}>
              <option>Booked</option>
              <option>Pending</option>
            </select>
          </div>

          <div className="form-group">
            <label>Type</label>
            <div>
              <input type="radio" id="in-person" name="type" value="in-person"
                checked={appointmentType === 'in-person'} onChange={handleTypeChange} />
              <label htmlFor="in-person">In-Person</label>
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
              <input type="checkbox" checked={skipBilling} onChange={handleSkipBillingChange} /> Skip Billing
            </label>
          </div>

          <div className="button-container">
            <button onClick={onClose}>Cancel</button>
            <button className="book-appointment-btn" onClick={handleSubmit}>Book Appointment</button>
          </div>
        </div>
      </div>
    </div>
  );
};

export default BookAppointmentPopup;
