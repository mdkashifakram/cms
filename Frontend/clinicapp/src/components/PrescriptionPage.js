import React, { useState } from 'react';
import PrescriptionSidebar from './PrescriptionSidebar';
import PrescriptionAdvanced from './PrescriptionForm';
import PastPrescriptions from './PastPrescription';
import Reports from './Reports';

const PrescriptionPage = () => {
  const [activeTab, setActiveTab] = useState('form'); // default tab

  const renderTab = () => {
    switch (activeTab) {
      case 'form':
        return <PrescriptionAdvanced />;
      case 'history':
        return <PastPrescriptions />;
      case 'reports':
        return <Reports />;
      default:
        return <PrescriptionAdvanced />;
    }
  };

  return (
    <div className="flex h-screen">
      <PrescriptionSidebar activeTab={activeTab} onTabChange={setActiveTab} />
      <div className="flex flex-1 flex-col min-w-0">{renderTab()}</div>
    </div>
  );
};

export default PrescriptionPage;
