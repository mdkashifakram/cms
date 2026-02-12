import './Navbar.css'
import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const Navbar = () => {
  const navigate = useNavigate();
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split("T")[0]); // Initialize with today's date (YYYY-MM-DD)
  const [isLoggingOut, setIsLoggingOut] = useState(false);

  // Handle date change from date picker
  const handleDateChange = (event) => {
    setSelectedDate(event.target.value); // Update selectedDate when user selects a date
  };

  // Handle logout
  const handleLogout = async () => {
    if (isLoggingOut) return; // Prevent multiple clicks

    setIsLoggingOut(true);
    logger.log('Initiating logout...');

    try {
      const response = await fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.LOGOUT}`, {
        method: 'POST',
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
      });

      if (response.ok) {
        logger.log('Logout successful');
        // Redirect to login page
        navigate('/');
      } else {
        logger.error('Logout failed');
        // Even if logout fails on server, redirect to login
        navigate('/');
      }
    } catch (error) {
      logger.error('Logout error:', error.message);
      // On network error, still redirect to login
      navigate('/');
    } finally {
      setIsLoggingOut(false);
    }
  };

  const handleBookAppointmentClick = () => {
    // Navigate to the BookAppointmentPopup
    navigate('/dashboard/appointments/book');
  };

  const handleAppointmentClick = () => {
    // Navigate to the BookAppointmentPopup
    navigate('/dashboard/appointments');
  };

  const handleSettingsClick = () => {
    // Navigate to Settings page
    navigate('/settings');
  };

  const handleConsultsClick = () => {
    // Navigate to Consults page
    navigate('/dashboard/consults');
  };


  return (
    <nav className="navbar">
      <div className="navbar-left">
        <div className="logo">ASR Clinic</div>
        <ul className="nav-items">
          <li onClick={handleAppointmentClick}>Appointments</li>
          <li onClick={handleConsultsClick}>Consults</li>
          <li onClick={handleBookAppointmentClick}>Book Appointment</li>

        </ul>
      </div>
      <div className="navbar-right">
        <div className="date-picker-container">
          <input
            type="date"
            className="custom-date-picker"
            value={selectedDate}
            onChange={handleDateChange}
            onFocus={(e) => e.target.showPicker()} // Ensure the calendar opens on click or focus
          />

        </div>
        <input className="profile-search" type="text" placeholder="Enter to Search...." />
        <div className="icons">
          <span className="support-icon">Support</span>
          <span
            className="profile-icon"
            onClick={handleSettingsClick}
            style={{ cursor: 'pointer' }}
            title="Settings"
          >
            🟡
          </span>
          <button
            className="logout-btn"
            onClick={handleLogout}
            disabled={isLoggingOut}
            title="Logout"
          >
            {isLoggingOut ? 'Logging out...' : 'Logout'}
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
