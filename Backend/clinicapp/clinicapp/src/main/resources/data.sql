-- =====================================================
-- CMS TEST DATA SEEDING SCRIPT
-- =====================================================
-- Purpose: Populate database with test data for doctors and patients
-- This file is automatically executed by Spring Boot on startup
-- =====================================================

-- =====================================================
-- DOCTORS TABLE
-- =====================================================
-- Insert test doctors with various specialties
INSERT INTO doctors (id, name, specialty, phone, email) VALUES
  (1, 'Dr. John Doe', 'General Physician', '9876543210', 'john.doe@clinic.com'),
  (2, 'Dr. Jane Smith', 'Cardiologist', '9876543211', 'jane.smith@clinic.com'),
  (3, 'Dr. Robert Brown', 'Neurologist', '9876543212', 'robert.brown@clinic.com'),
  (4, 'Dr. Emily Davis', 'Dermatologist', '9876543213', 'emily.davis@clinic.com'),
  (5, 'Dr. Michael Wilson', 'Orthopedic', '9876543214', 'michael.wilson@clinic.com')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- PATIENTS TABLE
-- =====================================================
-- Insert test patients with various demographics
INSERT INTO patients (id, name, age, phone_number, gender, address, city, pin) VALUES
  (1234, 'ISHRAT EKRAM', 56, '9831234567', 'Female', '123 Main Street', 'Kolkata', '700001'),
  (1235, 'Rajesh Kumar', 45, '9831234568', 'Male', '456 Park Avenue', 'Kolkata', '700002'),
  (1236, 'Priya Sharma', 32, '9831234569', 'Female', '789 Lake Road', 'Mumbai', '400001'),
  (1237, 'Amit Patel', 60, '9831234570', 'Male', '321 Hill Street', 'Delhi', '110001'),
  (1238, 'Sunita Reddy', 28, '9831234571', 'Female', '654 River View', 'Bangalore', '560001'),
  (1239, 'Vijay Singh', 50, '9831234572', 'Male', '987 Garden Lane', 'Chennai', '600001'),
  (1240, 'Meena Gupta', 38, '9831234573', 'Female', '147 Ocean Drive', 'Pune', '411001')
ON CONFLICT (id) DO NOTHING;

-- =====================================================
-- RESET SEQUENCES (PostgreSQL specific)
-- =====================================================
-- Ensure auto-increment sequences start after our seeded data
-- Comment out if using MySQL or adjust syntax accordingly

-- For doctors table
SELECT setval('doctors_id_seq', (SELECT MAX(id) FROM doctors) + 1);

-- For patients table
SELECT setval('patients_id_seq', (SELECT MAX(id) FROM patients) + 1);

-- =====================================================
-- VERIFICATION QUERIES (commented out)
-- =====================================================
-- Uncomment below to verify data was inserted
-- SELECT COUNT(*) as doctor_count FROM doctors;
-- SELECT COUNT(*) as patient_count FROM patients;
-- SELECT * FROM doctors ORDER BY id;
-- SELECT * FROM patients ORDER BY id;
