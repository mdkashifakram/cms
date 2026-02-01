import React from 'react';
import headerImage from '../assets/Screenshot 2025-10-19 192450.png';
import footerImage from '../assets/Screenshot 2025-10-19 192621.png';

const DoctorPadTemplate = React.forwardRef(({ formData }, ref) => {
  // Safely destructure with default values
  const {
    doctor = { name: '', id: '', specialization: '' },
    patient = { name: '', age: '', id: '' },
    vitals = {},
    complaints = '',
    pastHistory = '',
    diagnosis = [],
    medications = [],
    advice = '',
    testRequested = '',
    nextVisit = { number: '', unit: '', date: '' },
    referredTo = [],
    pastMedications = '',
    generalExamination = ''
  } = formData || {};

  // Helper to format next visit date
  const getNextVisitDate = () => {
    if (nextVisit.date) {
      const date = new Date(nextVisit.date);
      const options = { day: '2-digit', month: 'short', year: 'numeric', weekday: 'long' };
      return date.toLocaleDateString('en-GB', options).replace(',', ' -');
    } else if (nextVisit.number && nextVisit.unit) {
      const today = new Date();
      let futureDate = new Date(today);
      
      if (nextVisit.unit === 'Days') {
        futureDate.setDate(today.getDate() + parseInt(nextVisit.number));
      } else if (nextVisit.unit === 'Weeks') {
        futureDate.setDate(today.getDate() + (parseInt(nextVisit.number) * 7));
      } else if (nextVisit.unit === 'Months') {
        futureDate.setMonth(today.getMonth() + parseInt(nextVisit.number));
      }
      
      const options = { day: '2-digit', month: 'short', year: 'numeric', weekday: 'long' };
      return futureDate.toLocaleDateString('en-GB', options).replace(',', ' -');
    }
    return '';
  };

  return (
    <div ref={ref} className="max-w-[210mm] mx-auto bg-white font-sans text-[13px] leading-relaxed">
      {/* Header Image */}
      <div className="w-full mb-4">
        <img 
          src={headerImage} 
          alt="Header" 
          className="w-full h-auto object-contain"
        />
      </div>

      {/* Content Container */}
      <div className="px-8">
        {/* Header with Patient Info */}
        <div className="flex justify-between items-start mb-4 pb-3 border-b border-gray-300">
        <div>
          <h2 className="text-base font-bold text-gray-800 mb-1">
            {patient.id}: {patient.name ? patient.name.toUpperCase() : 'N/A'} ({patient.age}y, Male)
          </h2>
        </div>
        <div className="text-right">
          <p className="text-[13px] m-0">
            <span className="font-semibold">Date :</span> {new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' })}
          </p>
        </div>
      </div>

      {/* Vitals - Single Line */}
      {(vitals.bp || vitals.pulse || vitals.weight) && (
        <div className="mb-4 text-[13px]">
          <p className="m-0">
            {vitals.bp && <span><span className="font-semibold">BP</span> {vitals.bp} mmHg</span>}
            {vitals.pulse && <span> | <span className="font-semibold">Pulse</span> {vitals.pulse} bpm</span>}
            {vitals.weight && <span> | <span className="font-semibold">Weight</span> {vitals.weight} kg</span>}
            {vitals.temp && <span> | <span className="font-semibold">Temp</span> {vitals.temp}°F</span>}
            {vitals.spo2 && <span> | <span className="font-semibold">SpO₂</span> {vitals.spo2}%</span>}
          </p>
        </div>
      )}

      {/* Complaints */}
      {complaints && (
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-800 mb-2">Complaints:</h3>
          <div className="pl-5">
            {complaints.split('\n').map((complaint, i) => (
              <p key={i} className="my-1 relative">
                <span className="absolute -left-4 top-0">•</span>
                {complaint.toUpperCase()}
              </p>
            ))}
          </div>
        </div>
      )}

      {/* Past History */}
      {pastHistory && (
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-800 mb-2">Past History:</h3>
          <p className="m-0 uppercase">{pastHistory}</p>
        </div>
      )}

      {/* Diagnosis */}
      {diagnosis && diagnosis.length > 0 && (
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-800 mb-2">Diagnosis:</h3>
          <p className="m-0 uppercase">{diagnosis.join(' , ')}</p>
        </div>
      )}

      {/* Medications Table */}
      {medications && medications.length > 0 && medications.some(m => m.medicine) && (
        <div className="mb-4">
          <table className="w-full border-collapse text-xs">
            <thead>
              <tr className="bg-gray-100 border-b-2 border-gray-300">
                <th className="py-2 px-1 text-left font-bold text-[13px]">Medicine</th>
                <th className="py-2 px-1 text-left font-bold text-[13px]">Dosage</th>
                <th className="py-2 px-1 text-left font-bold text-[13px]">Timing - Freq. - Duration</th>
              </tr>
            </thead>
            <tbody>
              {medications.filter(m => m.medicine).map((med, i) => (
                <tr key={i} className="border-b border-gray-200">
                  <td className="py-2.5 px-1 align-top">
                    <div>
                      <strong>{i + 1}) {med.type} {med.medicine}</strong>
                      {med.notes && (
                        <div className="text-[11px] text-gray-600 mt-1 italic">
                          Notes: {med.notes}
                        </div>
                      )}
                    </div>
                  </td>
                  <td className="py-2.5 px-1 align-top">
                    {med.dosage || '-'}
                  </td>
                  <td className="py-2.5 px-1 align-top">
                    <div>
                      {med.when && <span>{med.when}</span>}
                      {med.frequency && <span> - {med.frequency}</span>}
                      {med.duration && <span> - {med.duration}</span>}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Advice */}
      {advice && (
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-800 mb-2">Advice:</h3>
          <div className="uppercase">
            {advice.split('\n').map((line, i) => (
              <p key={i} className="my-1">{line}</p>
            ))}
          </div>
        </div>
      )}

      {/* Test Requested */}
      {testRequested && (
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-800 mb-2">Tests Prescribed:</h3>
          <p className="m-0 uppercase">[ Next Visit ] {testRequested}</p>
        </div>
      )}

      {/* Next Visit */}
      {(nextVisit.date || (nextVisit.number && nextVisit.unit)) && (
        <div className="mb-4">
          <h3 className="text-sm font-bold text-gray-800 mb-2">Next Visit :</h3>
          <p className="m-0">
            {nextVisit.number && nextVisit.unit && `${nextVisit.number} ${nextVisit.unit.toLowerCase()}`}
            {getNextVisitDate() && ` (${getNextVisitDate()})`}
          </p>
        </div>
      )}

      {/* Footer - Doctor Signature */}
      <div className="mt-10 pt-5 border-t border-gray-300">
        <p className="text-sm font-bold mb-1">
          {doctor.name || 'Dr. Name'}
        </p>
      </div>
      </div>

      {/* Footer Image */}
      <div className="w-full mt-40">
        <img 
          src={footerImage} 
          alt="Footer" 
          className="w-full h-auto object-contain"
        />
      </div>
    </div>
  );
});

export default DoctorPadTemplate;