
import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import AppointmentTable from './components/AppointmentTable';
import BookAppointment from './components/BookAppointment';
import Login from './components/Login';
import Settings from './components/Settings';
import './App.css';
import DashBoard from './components/DashBoard';
import PrescriptionForm from './components/PrescriptionForm';
import PrescriptionSidebar from './components/PrescriptionSidebar'
import PrescriptionPage from './components/PrescriptionPage'
import { useLocation } from 'react-router-dom';
import ChatbotWidget from './components/Chatbot/ChatbotWidget'; // Updated path
import PrivateRoute from './components/PrivateRoute'; // CMS-006: Route Protection


function App() {
  return (
    <Router>
      <Routes>
        {/* Default route to login */}
        <Route path="/" element={<Login />} />

        {/* Settings route - protected */}
        <Route path="/settings" element={
          <PrivateRoute>
            <Settings />
          </PrivateRoute>
        } />

        {/* CMS-006: Protected routes - require authentication */}
        <Route path="/dashboard" element={
          <PrivateRoute>
            <DashBoard />
          </PrivateRoute>
        } />

        {/* Nested routes inside Dashboard - also protected */}
        <Route path="/dashboard/appointments" element={
          <PrivateRoute>
            <AppointmentsLayout />
          </PrivateRoute>
        } />
        <Route path="/dashboard/appointments/book" element={
          <PrivateRoute>
            <AppointmentsLayout />
          </PrivateRoute>
        } />

        {/* Prescription form - requires DOCTOR or ADMIN role */}
        <Route path="/prescription-form" element={
          <PrivateRoute requiredRole={['DOCTOR', 'ADMIN']}>
            <AppointmentsLayout />
          </PrivateRoute>
        } />
      </Routes>
      {/*<ChatbotWidget/>*/}
    </Router>
  );
}

const AppointmentsLayout = () => {
  const location = useLocation(); // Get the current URL location

  return (
    <>
      <Navbar />
      {/* Conditionally render based on the current URL */}
      {location.pathname === '/dashboard/appointments' && <AppointmentTable />}
      {location.pathname === '/dashboard/appointments/book' && <BookAppointment />}
      {location.pathname === '/prescription-form' && <PrescriptionPage />}

    </>
  );
};


export default App;


