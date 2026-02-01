-- ============================================================
-- CLINIC MANAGEMENT SYSTEM - POSTGRESQL MIGRATION SCRIPT
-- ============================================================
-- Phase 3: Database Migration from MySQL to PostgreSQL
-- 
-- Instructions:
-- 1. Create the PostgreSQL database first:
--    CREATE DATABASE clinic_management;
-- 2. Run this script to create the schema
-- 3. Run migration-data.sql to import data from MySQL
-- ============================================================

-- Enable UUID extension (for potential future use)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- USERS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    account_enabled BOOLEAN DEFAULT TRUE,
    account_locked BOOLEAN DEFAULT FALSE,
    password_set_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- ROLES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS roles (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

-- ============================================================
-- USER_ROLES TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS user_roles (
    user_id BIGINT NOT NULL REFERENCES users(id),
    role_id BIGINT NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

-- ============================================================
-- DOCTORS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS doctors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialty VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255),
    user_id BIGINT REFERENCES users(id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- PATIENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS patients (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(20),
    gender VARCHAR(20),
    age INTEGER,
    address TEXT,
    city VARCHAR(100),
    pin VARCHAR(20),
    email VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- APPOINTMENTS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS appointments (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT REFERENCES patients(id),
    patient_name VARCHAR(255),
    doctor_id BIGINT REFERENCES doctors(id),
    appointment_time TIMESTAMP,
    details TEXT,
    status VARCHAR(50),
    patient_email VARCHAR(255),
    contact VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- PRESCRIPTIONS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS prescriptions (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT REFERENCES patients(id),
    doctor_id BIGINT REFERENCES doctors(id),
    appointment_id BIGINT REFERENCES appointments(id),
    temperature VARCHAR(20),
    blood_pressure VARCHAR(20),
    pulse VARCHAR(20),
    spo2 VARCHAR(20),
    height VARCHAR(20),
    weight VARCHAR(20),
    bmi VARCHAR(20),
    waist_hip VARCHAR(20),
    complaints TEXT,
    past_history TEXT,
    diagnosis TEXT,
    advice TEXT,
    test_requested TEXT,
    past_medications TEXT,
    general_examination TEXT,
    next_visit_number VARCHAR(10),
    next_visit_unit VARCHAR(20),
    next_visit_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- MEDICINES TABLE (prescription medicines)
-- ============================================================
CREATE TABLE IF NOT EXISTS medicines (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT REFERENCES prescriptions(id) ON DELETE CASCADE,
    type VARCHAR(50),
    medicine VARCHAR(255),
    dosage VARCHAR(100),
    frequency VARCHAR(50),
    duration VARCHAR(50),
    when_to_take VARCHAR(50),
    notes TEXT
);

-- ============================================================
-- REFERRALS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS referrals (
    id BIGSERIAL PRIMARY KEY,
    prescription_id BIGINT REFERENCES prescriptions(id) ON DELETE CASCADE,
    doctor_name VARCHAR(255),
    speciality VARCHAR(255),
    phone VARCHAR(20),
    email VARCHAR(255)
);

-- ============================================================
-- TEMPLATES TABLE (diagnosis templates)
-- ============================================================
CREATE TABLE IF NOT EXISTS templates (
    id BIGSERIAL PRIMARY KEY,
    doctor_id BIGINT REFERENCES doctors(id),
    section VARCHAR(100),
    template_name VARCHAR(255),
    content TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- DIAGNOSIS TERMS TABLE
-- ============================================================
CREATE TABLE IF NOT EXISTS diagnosis_terms (
    id BIGSERIAL PRIMARY KEY,
    term VARCHAR(255) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================================
-- REVOKED TOKENS TABLE (for logout/token invalidation)
-- ============================================================
CREATE TABLE IF NOT EXISTS revoked_tokens (
    id BIGSERIAL PRIMARY KEY,
    token TEXT NOT NULL,
    username VARCHAR(255),
    revoked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_revoked_tokens_token ON revoked_tokens(token);

-- ============================================================
-- AUDIT LOGS TABLE (HIPAA Compliance)
-- ============================================================
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    resource_type VARCHAR(100) NOT NULL,
    resource_id BIGINT,
    ip_address VARCHAR(50) NOT NULL,
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    method VARCHAR(10) NOT NULL,
    user_agent VARCHAR(500),
    response_status INTEGER,
    duration_ms BIGINT
);

CREATE INDEX idx_audit_username ON audit_logs(username);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_resource ON audit_logs(resource_type, resource_id);

-- ============================================================
-- PATIENT CONSENT TABLE (HIPAA/GDPR Compliance)
-- ============================================================
CREATE TABLE IF NOT EXISTS patient_consents (
    id BIGSERIAL PRIMARY KEY,
    patient_id BIGINT NOT NULL REFERENCES patients(id),
    consent_type VARCHAR(100) NOT NULL,
    granted BOOLEAN NOT NULL DEFAULT FALSE,
    granted_at TIMESTAMP,
    revoked_at TIMESTAMP,
    ip_address VARCHAR(50),
    consent_version VARCHAR(20),
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(patient_id, consent_type)
);

CREATE INDEX idx_consent_patient ON patient_consents(patient_id);
CREATE INDEX idx_consent_type ON patient_consents(consent_type);

-- ============================================================
-- ENCRYPTION (for sensitive fields like diagnosis)
-- PostgreSQL pgcrypto extension for field-level encryption
-- ============================================================
CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- Function to encrypt sensitive data
CREATE OR REPLACE FUNCTION encrypt_data(data TEXT, key TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN encode(pgp_sym_encrypt(data, key), 'base64');
END;
$$ LANGUAGE plpgsql;

-- Function to decrypt sensitive data
CREATE OR REPLACE FUNCTION decrypt_data(encrypted_data TEXT, key TEXT)
RETURNS TEXT AS $$
BEGIN
    RETURN pgp_sym_decrypt(decode(encrypted_data, 'base64'), key);
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- ============================================================
-- INDEXES FOR PERFORMANCE
-- ============================================================
CREATE INDEX IF NOT EXISTS idx_patients_name ON patients(name);
CREATE INDEX IF NOT EXISTS idx_patients_phone ON patients(phone_number);
CREATE INDEX IF NOT EXISTS idx_appointments_date ON appointments(appointment_time);
CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions(patient_id);
CREATE INDEX IF NOT EXISTS idx_prescriptions_appointment ON prescriptions(appointment_id);

-- ============================================================
-- GRANTS (adjust based on your PostgreSQL user)
-- ============================================================
-- GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO your_app_user;
-- GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO your_app_user;

COMMENT ON TABLE patient_consents IS 'HIPAA/GDPR compliant consent tracking for patients';
COMMENT ON TABLE audit_logs IS 'HIPAA §164.312(b) audit trail for all data access';
