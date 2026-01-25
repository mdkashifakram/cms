import React from 'react';
import { useNavigate } from 'react-router-dom';
import ClinicCard from '../components/ClinicCard';
import '../styles/DashBoard.css'; // Import CSS file for styling
const clinics = [
    { name: 'ASR Clinic', address: 'R-23/A, Block- A, Newtown, Kolkata-700045' },
    { name: 'DTR Clinic', address: 'R-23/A, Block- A, Newtown, Kolkata-700045' },
    { name: 'STAR Clinic', address: 'R-23/A, Block- A, Newtown, Kolkata-700045' },
    { name: 'RAS Clinic', address: 'R-23/A, Block- A, Newtown, Kolkata-700045' },
    { name: 'SAS Clinic', address: 'R-23/A, Block- A, Newtown, Kolkata-700045' },
    { name: 'NUERO Clinic', address: 'R-23/A, Block- A, Newtown, Kolkata-700045' },
   
  ];
  function Dashboard() {
    const navigate = useNavigate();

  const handleCardClick = () => {
    // Navigate to appointments page
    navigate('/dashboard/appointments');
  };

  return (
      <div className="dashboard">
        <div className="dashboard-header">
          <div className="menu-icon">&#9776;</div>
          <h1>DASHBOARD</h1>
          <div className="user-info">
            <span>Dr A Rana</span>
            <div className="user-avatar"></div>
          </div>
          </div>
      <div className="clinic-cards">
        {clinics.map((clinic, index) => (
          <ClinicCard 
          key={index} 
          name={clinic.name}
          address={clinic.address}
          onClick={handleCardClick} />
        ))}
        <div className="add-clinic-card">+</div>
      </div>
    </div>
  );
}
export default Dashboard;