import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './Consults.css';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const Consults = () => {
  const navigate = useNavigate();
  const [consults, setConsults] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [filteredConsults, setFilteredConsults] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Fetch all completed appointments (consults)
  const fetchConsults = async () => {
    setIsLoading(true);
    try {
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CONSULTS.ALL}`,
        {
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS,
        }
      );

      if (response.ok) {
        const data = await response.json();
        logger.log('Fetched consults count:', data?.length);
        setConsults(data);
        setFilteredConsults(data);
      } else {
        logger.error('Failed to fetch consults');
        setConsults([]);
        setFilteredConsults([]);
      }
    } catch (error) {
      logger.error('Error loading consults:', error.message);
      setConsults([]);
      setFilteredConsults([]);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchConsults();
  }, []);

  // Search by Patient ID
  const handleSearchChange = (event) => {
    const searchValue = event.target.value;
    setSearchTerm(searchValue);

    if (searchValue.trim() === '') {
      setFilteredConsults(consults);
    } else {
      const filtered = consults.filter((consult) =>
        consult.patientId?.toString().includes(searchValue) ||
        consult.id?.toString().includes(searchValue)
      );
      setFilteredConsults(filtered);
    }
  };

  // Search by Patient ID via API
  const handleSearchById = async () => {
    if (!searchTerm.trim()) {
      fetchConsults();
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.CONSULTS.BY_PATIENT(searchTerm)}`,
        {
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS,
        }
      );

      if (response.ok) {
        const data = await response.json();
        logger.log('Search results:', data?.length);
        setFilteredConsults(Array.isArray(data) ? data : [data]);
      } else {
        logger.log('No consults found for patient ID:', searchTerm);
        setFilteredConsults([]);
      }
    } catch (error) {
      logger.error('Error searching consults:', error.message);
      setFilteredConsults([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleRefresh = () => {
    setSearchTerm('');
    fetchConsults();
  };

  // Handle Past Visit button click - opens latest prescription
  const handlePastVisitClick = async (consult) => {
    try {
      // Get prescription for this appointment
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_APPOINTMENT(consult.id)}`,
        {
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS,
        }
      );

      if (response.ok) {
        const { prescriptionId } = await response.json();

        if (prescriptionId) {
          // Fetch full prescription details
          const prescriptionResp = await fetch(
            `${API_CONFIG.BASE_URL}${API_ENDPOINTS.PRESCRIPTIONS.BY_ID(prescriptionId)}`,
            {
              credentials: API_CONFIG.CREDENTIALS,
              headers: API_CONFIG.HEADERS,
            }
          );
          const fullPrescription = await prescriptionResp.json();

          // Navigate to prescription form in view/edit mode
          navigate('/prescription-form', {
            state: {
              prescriptionId: fullPrescription.id,
              mode: 'view',
              appointment: consult,
              patientInfo: {
                id: fullPrescription.patient?.id || consult.patientId,
                name: fullPrescription.patient?.name || consult.patientName,
                age: fullPrescription.patient?.age || consult.patientAge,
              },
              doctorInfo: {
                id: fullPrescription.doctor?.id || consult.doctorId,
                name: fullPrescription.doctor?.name || consult.doctorName,
                specialization: fullPrescription.doctor?.specialty || consult.doctorSpecialty
              },
              existingPrescription: fullPrescription
            }
          });
        } else {
          alert('No prescription found for this visit.');
        }
      } else {
        alert('No prescription found for this visit.');
      }
    } catch (error) {
      logger.error('Error fetching prescription:', error.message);
      alert('Error loading prescription details.');
    }
  };

  // Format date for display
  const formatDate = (dateTime) => {
    if (!dateTime) return '-';
    const date = new Date(dateTime);
    return date.toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="consults-container">
      <div className="consults-header">
        <input
          type="text"
          placeholder="Search by Patient ID"
          className="search-bar"
          value={searchTerm}
          onChange={handleSearchChange}
          onKeyPress={(e) => e.key === 'Enter' && handleSearchById()}
        />

        <div className="consults-footer">
          <button className="search-btn" onClick={handleSearchById}>
            Search
          </button>
          <button className="refresh-btn" onClick={handleRefresh}>
            Refresh
          </button>
        </div>
      </div>

      {isLoading ? (
        <div className="loading-message">Loading...</div>
      ) : (
        <table className="consults-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>Patient Name</th>
              <th>Past Visit</th>
              <th>Last Visited</th>
              <th>Purpose</th>
            </tr>
          </thead>
          <tbody>
            {filteredConsults.length > 0 ? (
              filteredConsults.map((consult) => (
                <tr key={consult.id}>
                  <td>{consult.patientId || consult.id}</td>
                  <td>{consult.patientName}</td>
                  <td>
                    <button
                      className="pastVisit-btn"
                      onClick={() => handlePastVisitClick(consult)}
                    >
                      Past Visit
                    </button>
                  </td>
                  <td>{formatDate(consult.appointmentTime)}</td>
                  <td>{consult.details || '-'}</td>
                </tr>
              ))
            ) : (
              <tr>
                <td colSpan="5" className="no-records-found">
                  No consultation records found
                </td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

export default Consults;
