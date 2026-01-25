import React from 'react';
import '../styles/ClinicCard.css'; // Import CSS file for styling
function ClinicCard({ name, address, onClick }) {
  return (
    <div className="clinic-card" onClick={onClick}>
      <h2>{name}</h2>
      <p>{address}</p>
    </div>
  );
}
export default ClinicCard;