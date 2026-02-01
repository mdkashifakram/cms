import React, { useState, useEffect, useRef } from 'react';
import ReactToPrint from 'react-to-print';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  IconButton,
  Tooltip,
  CircularProgress
} from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import PrintIcon from '@mui/icons-material/Print';
import WhatsAppIcon from '@mui/icons-material/WhatsApp';
import AddIcon from '@mui/icons-material/Add';
import DeleteIcon from '@mui/icons-material/Delete';
import SaveIcon from '@mui/icons-material/Save';
import InvoiceTemplate from './InvoiceTemplate';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';
import logger from '../utils/logger';

const InvoiceModal = ({ open, onClose, prescriptionId, prescription }) => {
  const invoiceTemplateRef = useRef();
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [existingInvoice, setExistingInvoice] = useState(null);

  const [invoice, setInvoice] = useState({
    id: null,
    invoiceNumber: '',
    invoiceDate: new Date().toISOString().split('T')[0],
    items: [{ description: 'Consultation Fee', quantity: 1, unitPrice: '', lineTotal: 0 }],
    taxPercentage: 0,
    notes: '',
    subtotal: 0,
    taxAmount: 0,
    grandTotal: 0
  });

  // Load existing invoice if available
  useEffect(() => {
    if (open && prescriptionId) {
      loadExistingInvoice();
    }
  }, [open, prescriptionId]);

  // Reset form when modal closes
  useEffect(() => {
    if (!open) {
      setExistingInvoice(null);
      setInvoice({
        id: null,
        invoiceNumber: '',
        invoiceDate: new Date().toISOString().split('T')[0],
        items: [{ description: 'Consultation Fee', quantity: 1, unitPrice: '', lineTotal: 0 }],
        taxPercentage: 0,
        notes: '',
        subtotal: 0,
        taxAmount: 0,
        grandTotal: 0
      });
    }
  }, [open]);

  const loadExistingInvoice = async () => {
    setLoading(true);
    try {
      const response = await fetch(
        `${API_CONFIG.BASE_URL}${API_ENDPOINTS.INVOICES.BY_PRESCRIPTION(prescriptionId)}`,
        {
          credentials: API_CONFIG.CREDENTIALS,
          headers: API_CONFIG.HEADERS
        }
      );

      if (response.ok) {
        const data = await response.json();
        setExistingInvoice(data);
        setInvoice({
          id: data.id,
          invoiceNumber: data.invoiceNumber,
          invoiceDate: data.invoiceDate ? data.invoiceDate.split('T')[0] : new Date().toISOString().split('T')[0],
          items: data.items && data.items.length > 0
            ? data.items.map(item => ({
                description: item.description,
                quantity: item.quantity,
                unitPrice: item.unitPrice,
                lineTotal: item.lineTotal
              }))
            : [{ description: 'Consultation Fee', quantity: 1, unitPrice: '', lineTotal: 0 }],
          taxPercentage: data.taxPercentage || 0,
          notes: data.notes || '',
          subtotal: data.subtotal || 0,
          taxAmount: data.taxAmount || 0,
          grandTotal: data.grandTotal || 0
        });
      }
    } catch (error) {
      logger.error('Error loading invoice:', error);
    } finally {
      setLoading(false);
    }
  };

  // Calculate line total when quantity or unit price changes
  const calculateLineTotal = (quantity, unitPrice) => {
    const qty = parseInt(quantity) || 0;
    const price = parseFloat(unitPrice) || 0;
    return qty * price;
  };

  // Calculate all totals
  const calculateTotals = (items, taxPercentage) => {
    const subtotal = items.reduce((sum, item) => {
      const lineTotal = calculateLineTotal(item.quantity, item.unitPrice);
      return sum + lineTotal;
    }, 0);

    const taxPct = parseFloat(taxPercentage) || 0;
    const taxAmount = (subtotal * taxPct) / 100;
    const grandTotal = subtotal + taxAmount;

    return { subtotal, taxAmount, grandTotal };
  };

  // Handle item change
  const handleItemChange = (index, field, value) => {
    const updatedItems = [...invoice.items];
    updatedItems[index] = {
      ...updatedItems[index],
      [field]: value
    };

    // Recalculate line total
    if (field === 'quantity' || field === 'unitPrice') {
      updatedItems[index].lineTotal = calculateLineTotal(
        field === 'quantity' ? value : updatedItems[index].quantity,
        field === 'unitPrice' ? value : updatedItems[index].unitPrice
      );
    }

    const totals = calculateTotals(updatedItems, invoice.taxPercentage);

    setInvoice(prev => ({
      ...prev,
      items: updatedItems,
      ...totals
    }));
  };

  // Handle tax percentage change
  const handleTaxChange = (value) => {
    const totals = calculateTotals(invoice.items, value);
    setInvoice(prev => ({
      ...prev,
      taxPercentage: value,
      ...totals
    }));
  };

  // Add new item row
  const addItem = () => {
    setInvoice(prev => ({
      ...prev,
      items: [...prev.items, { description: '', quantity: 1, unitPrice: '', lineTotal: 0 }]
    }));
  };

  // Remove item row
  const removeItem = (index) => {
    if (invoice.items.length <= 1) return;

    const updatedItems = invoice.items.filter((_, i) => i !== index);
    const totals = calculateTotals(updatedItems, invoice.taxPercentage);

    setInvoice(prev => ({
      ...prev,
      items: updatedItems,
      ...totals
    }));
  };

  // Save invoice
  const handleSave = async () => {
    // Validate items
    const hasValidItems = invoice.items.some(
      item => item.description && parseFloat(item.unitPrice) > 0
    );

    if (!hasValidItems) {
      alert('Please add at least one item with a valid price');
      return;
    }

    setSaving(true);

    try {
      const payload = {
        patientId: prescription.patient?.id,
        doctorId: prescription.doctor?.id,
        appointmentId: prescription.appointmentId,
        prescriptionId: prescriptionId,
        items: invoice.items.filter(item => item.description && parseFloat(item.unitPrice) > 0).map(item => ({
          description: item.description,
          quantity: parseInt(item.quantity) || 1,
          unitPrice: parseFloat(item.unitPrice) || 0
        })),
        taxPercentage: parseFloat(invoice.taxPercentage) || 0,
        notes: invoice.notes
      };

      let response;

      if (existingInvoice && existingInvoice.id) {
        // Update existing invoice
        response = await fetch(
          `${API_CONFIG.BASE_URL}${API_ENDPOINTS.INVOICES.UPDATE(existingInvoice.id)}`,
          {
            method: 'PATCH',
            credentials: API_CONFIG.CREDENTIALS,
            headers: API_CONFIG.HEADERS,
            body: JSON.stringify(payload)
          }
        );
      } else {
        // Create new invoice
        response = await fetch(
          `${API_CONFIG.BASE_URL}${API_ENDPOINTS.INVOICES.CREATE}`,
          {
            method: 'POST',
            credentials: API_CONFIG.CREDENTIALS,
            headers: API_CONFIG.HEADERS,
            body: JSON.stringify(payload)
          }
        );
      }

      if (response.ok) {
        const data = await response.json();
        setExistingInvoice(data);
        setInvoice(prev => ({
          ...prev,
          id: data.id,
          invoiceNumber: data.invoiceNumber,
          subtotal: data.subtotal,
          taxAmount: data.taxAmount,
          grandTotal: data.grandTotal
        }));
        alert('Invoice saved successfully!');
      } else {
        const errorData = await response.json();
        alert(errorData.message || 'Failed to save invoice');
      }
    } catch (error) {
      logger.error('Error saving invoice:', error);
      alert('Error saving invoice');
    } finally {
      setSaving(false);
    }
  };

  // Send via WhatsApp
  const handleWhatsApp = () => {
    const phone = prescription.patient?.phoneNumber || '';
    const cleanPhone = phone.replace(/\D/g, '');
    const phoneWithCountry = cleanPhone.startsWith('91') ? cleanPhone : '91' + cleanPhone;

    const itemsText = invoice.items
      .filter(item => item.description && parseFloat(item.unitPrice) > 0)
      .map((item, i) => `${i + 1}. ${item.description} x${item.quantity} = Rs.${parseFloat(item.lineTotal).toFixed(2)}`)
      .join('\n');

    const message = encodeURIComponent(
      `*INVOICE*\n` +
      `Invoice #: ${invoice.invoiceNumber || 'DRAFT'}\n` +
      `Date: ${invoice.invoiceDate}\n\n` +
      `*Patient:* ${prescription.patient?.name || 'N/A'}\n` +
      `*Doctor:* Dr. ${prescription.doctor?.name || 'N/A'}\n\n` +
      `*Items:*\n` +
      `-------------------\n` +
      `${itemsText}\n` +
      `-------------------\n` +
      `Subtotal: Rs.${parseFloat(invoice.subtotal).toFixed(2)}\n` +
      (parseFloat(invoice.taxPercentage) > 0
        ? `Tax (${invoice.taxPercentage}%): Rs.${parseFloat(invoice.taxAmount).toFixed(2)}\n`
        : '') +
      `*Grand Total: Rs.${parseFloat(invoice.grandTotal).toFixed(2)}*\n` +
      (invoice.notes ? `\nNote: ${invoice.notes}` : '')
    );

    window.open(`https://wa.me/${phoneWithCountry}?text=${message}`, '_blank');
  };

  // Format currency for display
  const formatCurrency = (value) => {
    const num = parseFloat(value) || 0;
    return num.toFixed(2);
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle className="flex justify-between items-center">
        <span className="text-lg font-semibold">
          {existingInvoice ? `Invoice: ${existingInvoice.invoiceNumber}` : 'Generate Invoice'}
        </span>
        <IconButton onClick={onClose} size="small">
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        {loading ? (
          <div className="flex justify-center items-center py-8">
            <CircularProgress />
          </div>
        ) : (
          <div className="space-y-4">
            {/* Patient & Doctor Info (Read-only) */}
            <div className="grid grid-cols-2 gap-4 p-3 bg-gray-50 rounded">
              <div>
                <p className="text-sm text-gray-600 mb-1">Patient</p>
                <p className="font-medium">{prescription.patient?.name || 'N/A'}</p>
                <p className="text-sm text-gray-500">
                  {prescription.patient?.age && `${prescription.patient.age}y`}
                  {prescription.patient?.phoneNumber && ` | ${prescription.patient.phoneNumber}`}
                </p>
              </div>
              <div className="text-right">
                <p className="text-sm text-gray-600 mb-1">Doctor</p>
                <p className="font-medium">Dr. {prescription.doctor?.name || 'N/A'}</p>
                <p className="text-sm text-gray-500">{prescription.doctor?.specialty || ''}</p>
              </div>
            </div>

            {/* Invoice Number & Date */}
            <div className="grid grid-cols-2 gap-4">
              <TextField
                label="Invoice Number"
                value={invoice.invoiceNumber || 'Auto-generated on save'}
                disabled
                size="small"
                fullWidth
              />
              <TextField
                label="Invoice Date"
                type="date"
                value={invoice.invoiceDate}
                onChange={(e) => setInvoice(prev => ({ ...prev, invoiceDate: e.target.value }))}
                size="small"
                fullWidth
                InputLabelProps={{ shrink: true }}
              />
            </div>

            {/* Line Items */}
            <div>
              <div className="flex justify-between items-center mb-2">
                <h3 className="font-medium">Line Items</h3>
                <Button
                  startIcon={<AddIcon />}
                  size="small"
                  onClick={addItem}
                >
                  Add Item
                </Button>
              </div>

              <table className="w-full border-collapse">
                <thead>
                  <tr className="bg-gray-100">
                    <th className="border p-2 text-left text-sm">#</th>
                    <th className="border p-2 text-left text-sm">Description</th>
                    <th className="border p-2 text-center text-sm w-20">Qty</th>
                    <th className="border p-2 text-right text-sm w-28">Unit Price</th>
                    <th className="border p-2 text-right text-sm w-28">Total</th>
                    <th className="border p-2 text-center text-sm w-12"></th>
                  </tr>
                </thead>
                <tbody>
                  {invoice.items.map((item, index) => (
                    <tr key={index}>
                      <td className="border p-2 text-center">{index + 1}</td>
                      <td className="border p-1">
                        <input
                          type="text"
                          value={item.description}
                          onChange={(e) => handleItemChange(index, 'description', e.target.value)}
                          placeholder="Item description"
                          className="w-full px-2 py-1 border rounded"
                        />
                      </td>
                      <td className="border p-1">
                        <input
                          type="number"
                          value={item.quantity}
                          onChange={(e) => handleItemChange(index, 'quantity', e.target.value)}
                          min="1"
                          className="w-full px-2 py-1 border rounded text-center"
                        />
                      </td>
                      <td className="border p-1">
                        <input
                          type="number"
                          value={item.unitPrice}
                          onChange={(e) => handleItemChange(index, 'unitPrice', e.target.value)}
                          placeholder="0.00"
                          min="0"
                          step="0.01"
                          className="w-full px-2 py-1 border rounded text-right"
                        />
                      </td>
                      <td className="border p-2 text-right">
                        Rs. {formatCurrency(item.lineTotal)}
                      </td>
                      <td className="border p-1 text-center">
                        <Tooltip title="Remove item">
                          <IconButton
                            size="small"
                            onClick={() => removeItem(index)}
                            disabled={invoice.items.length <= 1}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>

            {/* Totals */}
            <div className="flex justify-end">
              <div className="w-64 space-y-2">
                <div className="flex justify-between">
                  <span>Subtotal:</span>
                  <span>Rs. {formatCurrency(invoice.subtotal)}</span>
                </div>
                <div className="flex justify-between items-center">
                  <span>Tax (%):</span>
                  <input
                    type="number"
                    value={invoice.taxPercentage}
                    onChange={(e) => handleTaxChange(e.target.value)}
                    min="0"
                    max="100"
                    step="0.1"
                    className="w-20 px-2 py-1 border rounded text-right"
                  />
                </div>
                <div className="flex justify-between">
                  <span>Tax Amount:</span>
                  <span>Rs. {formatCurrency(invoice.taxAmount)}</span>
                </div>
                <div className="flex justify-between font-bold text-lg border-t pt-2">
                  <span>Grand Total:</span>
                  <span>Rs. {formatCurrency(invoice.grandTotal)}</span>
                </div>
              </div>
            </div>

            {/* Notes */}
            <TextField
              label="Notes / Disclaimer"
              value={invoice.notes}
              onChange={(e) => setInvoice(prev => ({ ...prev, notes: e.target.value }))}
              multiline
              rows={2}
              fullWidth
              placeholder="Optional notes or disclaimer"
            />
          </div>
        )}

        {/* Hidden Invoice Template for Printing */}
        <div style={{ display: 'none' }}>
          <InvoiceTemplate
            ref={invoiceTemplateRef}
            invoiceData={invoice}
            patientData={prescription.patient}
            doctorData={prescription.doctor}
          />
        </div>
      </DialogContent>

      <DialogActions className="p-3">
        <Button onClick={onClose} color="inherit">
          Cancel
        </Button>

        <Button
          startIcon={saving ? <CircularProgress size={16} /> : <SaveIcon />}
          onClick={handleSave}
          variant="contained"
          color="primary"
          disabled={saving || loading}
        >
          {existingInvoice ? 'Update' : 'Save'}
        </Button>

        <ReactToPrint
          trigger={() => (
            <Button
              startIcon={<PrintIcon />}
              variant="outlined"
              color="primary"
              disabled={loading}
            >
              Print
            </Button>
          )}
          content={() => invoiceTemplateRef.current}
        />

        <Button
          startIcon={<WhatsAppIcon />}
          onClick={handleWhatsApp}
          variant="contained"
          color="success"
          disabled={loading || !prescription.patient?.phoneNumber}
        >
          WhatsApp
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default InvoiceModal;
