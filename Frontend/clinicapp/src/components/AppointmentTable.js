import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './AppointmentTable.css';
import UpdateAppointment from './UpdateAppointment';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const AppointmentTable = () => {
  const navigate = useNavigate();
  // Security Remediation: Removed localStorage token - using HttpOnly cookies instead
  const [appointments, setAppointments] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredAppointments, setFilteredAppointments] = useState([]);
  const [selectedDate, setSelectedDate] = useState(new Date().toISOString().split("T")[0]);
  const [selectedAppointment, setSelectedAppointment] = useState(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [openMenuId, setOpenMenuId] = useState(null);
  const [menuOpen, setMenuOpen] = useState(false);

  const fetchAppointmentsByDate = (date) => {
    // Security Remediation: Using API config and cookie-based auth
    fetch(`${API_CONFIG.BASE_URL}${API_ENDPOINTS.APPOINTMENTS.BY_DAY}?date=${date}`, {
      credentials: API_CONFIG.CREDENTIALS,
      headers: API_CONFIG.HEADERS,
    })
      .then(response => response.json())
      .then(data => {
        logger.log('Fetched appointments count:', data?.length);
        setAppointments(data);
        setFilteredAppointments(data);
      })
      .catch(error => logger.error('Error loading appointments:', error.message));
  };

  useEffect(() => {
    fetchAppointmentsByDate(selectedDate);
  }, []);

  const handleSearchChange = (event) => {
    const searchValue = event.target.value.toLowerCase();
    setSearchTerm(searchValue);
    const filtered = appointments.filter((appointment) =>
      appointment.patientName.toLowerCase().includes(searchValue)
    );
    setFilteredAppointments(filtered);
  };

  const handleTodayClick = () => {
    const today = new Date().toISOString().split("T")[0];
    setSelectedDate(today);
    fetchAppointmentsByDate(today);
  };

  const handleSetClick = () => {
    fetchAppointmentsByDate(selectedDate);
  };

  const handleDateChange = (event) => {
    setSelectedDate(event.target.value);
  };

  const handleOpenModal = (appointment) => {
    setSelectedAppointment(appointment);
    setIsModalOpen(true);
    setOpenMenuId(null);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedAppointment(null);
  };

  const toggleMenu = (appointmentId) => {
    setOpenMenuId(openMenuId === appointmentId ? null : appointmentId);
  };

  // Check if patient has existing prescription
  const checkExistingPrescription = async (patientId, appointmentId) => {
    try {
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_APPOINTMENT(appointmentId)}`,
        {
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS,
        }
      );
      logger.log('Prescription check completed');
      if (response.ok) {
        const data = await response.json();
        return data; // Returns prescription with ID if exists
      }
      return null;
    } catch (error) {
      logger.error('Error checking prescription:', error.message);
      return null;
    }
  };

  const handleVisitPadClick = async (appointment) => {
    // 1️⃣ Get prescription ID for this appointment
    const existingPrescriptionResp = await fetch(
      `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_APPOINTMENT(appointment.id)}`,
      {
        credentials: API_CONFIG.CREDENTIALS,
        headers: API_CONFIG.HEADERS,
      }
    );
    const { prescriptionId } = await existingPrescriptionResp.json();

    if (prescriptionId) {
      //Fetch full prescription by ID
      const prescriptionResp = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_ID(prescriptionId)}`,
        {
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS,
        }
      );
      const fullPrescription = await prescriptionResp.json();

      navigate('/prescription-form', {
        state: {
          prescriptionId: fullPrescription.id,
          mode: 'edit',
          appointment,
          patientInfo: {
            id: fullPrescription.patient.id,
            name: fullPrescription.patient.name,
            age: fullPrescription.patient.age,
          },
          doctorInfo: {
            id: fullPrescription.doctor.id,
            name: fullPrescription.doctor.name,
            specialization: fullPrescription.doctor.specialty
          },
          existingPrescription: fullPrescription // optional: pass the object directly
        }
      });
    } else {
      // No prescription exists → create new
      navigate('/prescription-form', {
        state: {
          mode: 'create',
          appointment,
          patientInfo: {
            id: appointment.patientId,
            name: appointment.patientName,
            age: appointment.patientAge || '',
          },
          doctorInfo: {
            id: appointment.doctor?.id || appointment.doctorId,
            name: appointment.doctor?.name || 'Dr. Unknown',
            specialization: appointment.doctor?.specialty || ''
          }
        }
      });
    }
  };


  return (
    <div className="appointment-container">
      <div className="appointment-header">
        <input
          type="text"
          placeholder="Search Appointments"
          className="search-bar"
          value={searchTerm}
          onChange={handleSearchChange}
        />

        <div className="appointment-footer">
          <div className="date-picker-container">
            <input
              type="date"
              className="custom-date-picker"
              value={selectedDate}
              onChange={handleDateChange}
              onFocus={(e) => e.target.showPicker()}
            />
          </div>
          <button className="set-btn" onClick={handleSetClick}>Set</button>
          <button className="today-btn" onClick={handleTodayClick}>Today</button>
          <button className="refresh-btn" onClick={() => window.location.reload()}>Refresh</button>
        </div>
      </div>

      <table className="appointment-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Patient Name</th>
            <th>Visit</th>
            <th>Time</th>
            <th>Status</th>
            <th>Purpose</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {filteredAppointments.length > 0 ? (
            filteredAppointments.map((appointment) => (
              <tr key={appointment.id}>
                <td>{appointment.id}</td>
                <td>{appointment.patientName}</td>
                <td>
                  <button
                    className='visitPad-btn'
                    onClick={() => handleVisitPadClick(appointment)}
                  >
                    Visit Pad
                  </button>
                </td>
                <td>{appointment.appointmentTime}</td>
                <td>{appointment.status}</td>
                <td>{appointment.details}</td>
                <td>
                  <div id="menu-wrap">
                    <div className="dots" onClick={() => toggleMenu(appointment.id)}>
                      <div></div>
                    </div>
                    <div className={`menu ${openMenuId === appointment.id ? 'open' : ''}`}>
                      <ul>
                        <li>
                          <a href="#" className="link" onClick={() => handleOpenModal(appointment)}>Update</a>
                        </li>
                      </ul>
                    </div>
                  </div>
                </td>
              </tr>
            ))
          ) : (
            <tr>
              <td colSpan="7" className="no-records-found">No records found</td>
            </tr>
          )}
        </tbody>
      </table>
      {isModalOpen && (
        <UpdateAppointment
          selectedAppointment={selectedAppointment}
          isModalOpen={isModalOpen}
          handleCloseModal={handleCloseModal}
        />
      )}
    </div>
  );
};

export default AppointmentTable;