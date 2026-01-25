import React from 'react';

const PrescriptionSidebar = ({ activeTab, onTabChange }) => {
  const menuItems = [
    { key: 'form', label: 'New Prescription' },
    { key: 'history', label: 'Past Prescriptions' },
    { key: 'reports', label: 'Reports' },
  ];

  return (
    <div className="w-52 bg-gray-100 border-r h-screen p-4 flex-shrink-0">
      <ul className="space-y-2">
        {menuItems.map((item) => (
          <li
            key={item.key}
            onClick={() => onTabChange(item.key)}
            className={`cursor-pointer px-4 py-2 rounded hover:bg-blue-100 ${
              activeTab === item.key ? 'bg-blue-200 font-bold' : ''
            }`}
          >
            {item.label}
          </li>
        ))}
      </ul>
    </div>
  );
};

export default PrescriptionSidebar;
