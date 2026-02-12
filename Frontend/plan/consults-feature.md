# Consults Feature Implementation Plan

## Overview
The Consults section displays patients who have completed their appointments (consulted with the doctor). It provides a historical view of all completed consultations with the ability to search by Patient ID and view past prescriptions.

---

## Requirements

### Functional Requirements
1. Display list of completed appointments (consultations)
2. Search patients by Patient ID
3. Show columns: ID, Patient Name, Past Visit (button), Last Visited Date, Purpose
4. "Past Visit" button opens the patient's latest prescription
5. No Status column needed (all records are completed)

### Business Logic
- When an appointment is **booked** → visible in **Appointments** tab
- When an appointment is **completed** → visible in **Consults** section
- Consults shows historical/past visits only

---

## Architecture

### Frontend Components

```
Frontend/clinicapp/src/
├── components/
│   ├── Consults.js          # Main consults component
│   └── Consults.css         # Styling
├── config/
│   └── api.js               # API endpoints (CONSULTS section)
└── App.js                   # Route configuration
```

### Backend Components

```
Backend/clinicapp/src/main/java/com/example/clinicapp/
├── controller/
│   └── ConsultController.java    # REST endpoints
├── service/
│   └── AppointmentService.java   # Business logic (added methods)
└── repository/
    └── AppointmentRepository.java # Database queries (added methods)
```

---

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/consults` | Get all completed appointments |
| GET | `/consults/patient/{patientId}` | Get consults by patient ID |
| GET | `/consults/{id}` | Get specific consult by ID |

### Response Format (AppointmentDto)
```json
{
  "id": 1,
  "patientId": 5,
  "patientName": "John Doe",
  "patientAge": "35",
  "patientGender": "M",
  "patientPhone": "1234567890",
  "appointmentTime": "2026-01-26T16:30:00",
  "status": "Completed",
  "details": "Follow-Up Consultation",
  "doctorId": 1,
  "doctorName": "Dr. Smith",
  "doctorSpecialty": "General",
  "prescriptionId": 10
}
```

---

## Database Queries

### New Repository Methods
```java
// Find completed appointments by patient ID
List<Appointment> findByPatient_IdAndStatus(Long patientId, String status);

// Find all appointments by patient ID
List<Appointment> findByPatient_Id(Long patientId);

// Find completed appointments ordered by time (most recent first)
List<Appointment> findByStatusOrderByAppointmentTimeDesc(String status);
```

---

## UI Design

### Table Columns
| Column | Description |
|--------|-------------|
| ID | Patient ID |
| Patient Name | Full name of the patient |
| Past Visit | Button to view latest prescription |
| Last Visited | Date and time of the completed appointment |
| Purpose | Reason/details of the visit |

### Features
- **Search Bar**: Search by Patient ID
- **Search Button**: Trigger API search
- **Refresh Button**: Reload all consults
- **Past Visit Button**: Opens prescription in view mode

---

## Route Configuration

```javascript
// App.js
<Route path="/dashboard/consults" element={
  <PrivateRoute>
    <AppointmentsLayout />
  </PrivateRoute>
} />
```

### Navigation
- Navbar: "Consults" menu item → `/dashboard/consults`

---

## Security

- All endpoints protected with `@PreAuthorize`
- Required roles: `RECEPTIONIST`, `DOCTOR`, `ADMIN`
- Uses HttpOnly cookie authentication

---

## Files Modified/Created

### Frontend (New Files)
- `Frontend/clinicapp/src/components/Consults.js`
- `Frontend/clinicapp/src/components/Consults.css`

### Frontend (Modified)
- `Frontend/clinicapp/src/App.js` - Added route and import
- `Frontend/clinicapp/src/components/Navbar.js` - Added navigation handler
- `Frontend/clinicapp/src/config/api.js` - Added CONSULTS endpoints

### Backend (New Files)
- `Backend/.../controller/ConsultController.java`

### Backend (Modified)
- `Backend/.../service/AppointmentService.java` - Added consult methods
- `Backend/.../repository/AppointmentRepository.java` - Added query methods

---

## Testing Checklist

- [ ] Navigate to Consults from Navbar
- [ ] View all completed appointments
- [ ] Search by Patient ID
- [ ] Click "Past Visit" to view prescription
- [ ] Verify responsive design on different screen sizes
- [ ] Test with no records (empty state)
- [ ] Verify role-based access control

---

## Future Enhancements

1. Add date range filter for consultations
2. Export consultation history to PDF
3. Add pagination for large datasets
4. Show consultation summary/notes
5. Link to patient's complete medical history
