import React from 'react';
import headerImage from '../assets/Screenshot 2025-10-19 192450.png';
import footerImage from '../assets/Screenshot 2025-10-19 192621.png';

const InvoiceTemplate = React.forwardRef(({ invoiceData, patientData, doctorData }, ref) => {
  // Safely destructure with default values
  const {
    invoiceNumber = '',
    invoiceDate = new Date().toISOString(),
    items = [],
    subtotal = 0,
    taxPercentage = 0,
    taxAmount = 0,
    grandTotal = 0,
    notes = ''
  } = invoiceData || {};

  const patient = patientData || { name: '', age: '', phoneNumber: '' };
  const doctor = doctorData || { name: '', specialty: '' };

  // Format date
  const formatDate = (dateString) => {
    if (!dateString) return new Date().toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    const date = new Date(dateString);
    return date.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
  };

  // Format currency
  const formatCurrency = (amount) => {
    const num = parseFloat(amount) || 0;
    return num.toFixed(2);
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
        {/* Invoice Title */}
        <div className="text-center mb-6">
          <h1 className="text-2xl font-bold text-gray-800 tracking-wide">INVOICE</h1>
        </div>

        {/* Invoice Info Row */}
        <div className="flex justify-between items-start mb-6 pb-3 border-b border-gray-300">
          <div>
            <p className="text-[13px] m-0">
              <span className="font-semibold">Invoice No:</span> {invoiceNumber || 'N/A'}
            </p>
          </div>
          <div className="text-right">
            <p className="text-[13px] m-0">
              <span className="font-semibold">Date:</span> {formatDate(invoiceDate)}
            </p>
          </div>
        </div>

        {/* Patient & Doctor Info */}
        <div className="flex justify-between mb-6">
          {/* Patient Info */}
          <div className="flex-1">
            <h3 className="text-sm font-bold text-gray-800 mb-2">Bill To:</h3>
            <p className="m-0 text-[13px]"><strong>Name:</strong> {patient.name || 'N/A'}</p>
            {patient.age && <p className="m-0 text-[13px]"><strong>Age:</strong> {patient.age} years</p>}
            {patient.phoneNumber && <p className="m-0 text-[13px]"><strong>Phone:</strong> {patient.phoneNumber}</p>}
          </div>

          {/* Doctor Info */}
          <div className="flex-1 text-right">
            <h3 className="text-sm font-bold text-gray-800 mb-2">Treated By:</h3>
            <p className="m-0 text-[13px]"><strong>Dr.</strong> {doctor.name || 'N/A'}</p>
            {doctor.specialty && <p className="m-0 text-[13px]">{doctor.specialty}</p>}
          </div>
        </div>

        {/* Line Items Table */}
        <div className="mb-6">
          <table className="w-full border-collapse text-[13px]">
            <thead>
              <tr className="bg-gray-100 border-b-2 border-gray-300">
                <th className="py-2 px-2 text-left font-bold w-12">#</th>
                <th className="py-2 px-2 text-left font-bold">Description</th>
                <th className="py-2 px-2 text-center font-bold w-20">Qty</th>
                <th className="py-2 px-2 text-right font-bold w-28">Unit Price</th>
                <th className="py-2 px-2 text-right font-bold w-28">Amount</th>
              </tr>
            </thead>
            <tbody>
              {items && items.length > 0 ? (
                items.map((item, index) => (
                  <tr key={index} className="border-b border-gray-200">
                    <td className="py-2.5 px-2 align-top">{index + 1}</td>
                    <td className="py-2.5 px-2 align-top">{item.description || '-'}</td>
                    <td className="py-2.5 px-2 text-center align-top">{item.quantity || 1}</td>
                    <td className="py-2.5 px-2 text-right align-top">Rs. {formatCurrency(item.unitPrice)}</td>
                    <td className="py-2.5 px-2 text-right align-top">Rs. {formatCurrency(item.lineTotal)}</td>
                  </tr>
                ))
              ) : (
                <tr>
                  <td colSpan="5" className="py-4 text-center text-gray-500">No items</td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* Totals Section */}
        <div className="flex justify-end mb-6">
          <div className="w-64">
            <div className="flex justify-between py-2 border-b border-gray-200">
              <span className="font-semibold">Subtotal:</span>
              <span>Rs. {formatCurrency(subtotal)}</span>
            </div>
            {parseFloat(taxPercentage) > 0 && (
              <div className="flex justify-between py-2 border-b border-gray-200">
                <span className="font-semibold">Tax ({taxPercentage}%):</span>
                <span>Rs. {formatCurrency(taxAmount)}</span>
              </div>
            )}
            <div className="flex justify-between py-2 border-b-2 border-gray-800">
              <span className="font-bold text-base">Grand Total:</span>
              <span className="font-bold text-base">Rs. {formatCurrency(grandTotal)}</span>
            </div>
          </div>
        </div>

        {/* Notes/Disclaimer */}
        {notes && (
          <div className="mb-6 p-3 bg-gray-50 border border-gray-200 rounded">
            <h3 className="text-sm font-bold text-gray-800 mb-1">Notes:</h3>
            <p className="m-0 text-[12px] text-gray-600">{notes}</p>
          </div>
        )}

        {/* Payment Info */}
        <div className="mb-4 text-[12px] text-gray-600">
          <p className="m-0">* This is a computer-generated invoice.</p>
          <p className="m-0">* Please retain this invoice for your records.</p>
        </div>

        {/* Footer - Signature Area */}
        <div className="mt-10 pt-5 border-t border-gray-300 flex justify-between">
          <div>
            <p className="text-sm font-bold mb-1">Received By:</p>
            <div className="mt-8 border-t border-gray-400 w-40">
              <p className="text-[11px] text-center mt-1">Patient Signature</p>
            </div>
          </div>
          <div className="text-right">
            <p className="text-sm font-bold mb-1">Authorized By:</p>
            <div className="mt-8 border-t border-gray-400 w-40 ml-auto">
              <p className="text-[11px] text-center mt-1">Clinic Stamp/Signature</p>
            </div>
          </div>
        </div>
      </div>

      {/* Footer Image */}
      <div className="w-full mt-20">
        <img
          src={footerImage}
          alt="Footer"
          className="w-full h-auto object-contain"
        />
      </div>
    </div>
  );
});

export default InvoiceTemplate;
