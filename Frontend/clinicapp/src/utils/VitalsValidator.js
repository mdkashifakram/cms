/**
 * CMS-009: Medical Vital Signs Validator
 * 
 * Validates vital signs against medically acceptable ranges.
 * Prevents invalid medical records from being created.
 */

// Medical constraints for vital signs
const VITALS_CONSTRAINTS = {
    temperature: {
        min: 35,
        max: 42,
        unit: '°C',
        description: 'Body temperature'
    },
    bloodPressure: {
        pattern: /^(\d{2,3})\/(\d{2,3})$/,
        systolicMin: 70,
        systolicMax: 250,
        diastolicMin: 40,
        diastolicMax: 150,
        description: 'Blood pressure (systolic/diastolic)'
    },
    pulse: {
        min: 40,
        max: 200,
        unit: 'bpm',
        description: 'Heart rate'
    },
    spo2: {
        min: 70,
        max: 100,
        unit: '%',
        description: 'Blood oxygen saturation'
    },
    height: {
        min: 30,
        max: 250,
        unit: 'cm',
        description: 'Height'
    },
    weight: {
        min: 0.5,
        max: 500,
        unit: 'kg',
        description: 'Weight'
    },
    bmi: {
        min: 10,
        max: 60,
        description: 'Body Mass Index'
    }
};

/**
 * Validates a single vital sign value
 * @param {string} field - Name of the vital sign field
 * @param {string|number} value - Value to validate
 * @returns {string|null} - Error message or null if valid
 */
export const validateVitalField = (field, value) => {
    if (!value || value === '') {
        return null; // Empty values are allowed (optional fields)
    }

    const constraint = VITALS_CONSTRAINTS[field];
    if (!constraint) {
        return null; // Unknown field, skip validation
    }

    // Special handling for blood pressure (format validation)
    if (field === 'bloodPressure' || field === 'bp') {
        const match = String(value).match(VITALS_CONSTRAINTS.bloodPressure.pattern);
        if (!match) {
            return 'Blood pressure format must be systolic/diastolic (e.g., 120/80)';
        }

        const systolic = parseInt(match[1], 10);
        const diastolic = parseInt(match[2], 10);

        if (systolic < VITALS_CONSTRAINTS.bloodPressure.systolicMin ||
            systolic > VITALS_CONSTRAINTS.bloodPressure.systolicMax) {
            return `Systolic pressure must be between ${VITALS_CONSTRAINTS.bloodPressure.systolicMin}-${VITALS_CONSTRAINTS.bloodPressure.systolicMax}`;
        }

        if (diastolic < VITALS_CONSTRAINTS.bloodPressure.diastolicMin ||
            diastolic > VITALS_CONSTRAINTS.bloodPressure.diastolicMax) {
            return `Diastolic pressure must be between ${VITALS_CONSTRAINTS.bloodPressure.diastolicMin}-${VITALS_CONSTRAINTS.bloodPressure.diastolicMax}`;
        }

        if (systolic <= diastolic) {
            return 'Systolic pressure must be greater than diastolic';
        }

        return null;
    }

    // Numeric validation for other fields
    const numValue = parseFloat(value);

    if (isNaN(numValue)) {
        return `${constraint.description || field} must be a valid number`;
    }

    if (numValue < constraint.min || numValue > constraint.max) {
        return `${constraint.description || field} must be between ${constraint.min}-${constraint.max}${constraint.unit || ''}`;
    }

    return null;
};

/**
 * Validates all vital signs in a vitals object
 * @param {object} vitals - Object containing vital sign values
 * @returns {object} - Object with field names as keys and error messages as values
 */
export const validateVitals = (vitals) => {
    const errors = {};

    // Map of vitals object keys to constraint keys
    const fieldMapping = {
        temp: 'temperature',
        temperature: 'temperature',
        bp: 'bloodPressure',
        bloodPressure: 'bloodPressure',
        pulse: 'pulse',
        heartRate: 'pulse',
        spo2: 'spo2',
        oxygenSaturation: 'spo2',
        height: 'height',
        weight: 'weight',
        bmi: 'bmi'
    };

    for (const [key, value] of Object.entries(vitals)) {
        const constraintKey = fieldMapping[key] || key;
        const error = validateVitalField(constraintKey, value);

        if (error) {
            errors[key] = error;
        }
    }

    // Auto-calculate and validate BMI if height and weight provided
    if (vitals.height && vitals.weight && !errors.height && !errors.weight) {
        const heightM = parseFloat(vitals.height) / 100; // cm to m
        const weightKg = parseFloat(vitals.weight);
        const calculatedBMI = (weightKg / (heightM * heightM)).toFixed(1);

        // If BMI was provided, check if it matches calculation
        if (vitals.bmi && !errors.bmi) {
            const providedBMI = parseFloat(vitals.bmi);
            const variance = Math.abs(providedBMI - parseFloat(calculatedBMI));

            if (variance > 1) {
                errors.bmi = `BMI should be approximately ${calculatedBMI} based on height/weight`;
            }
        }
    }

    return errors;
};

/**
 * Checks if vitals object has any validation errors
 * @param {object} vitals - Object containing vital sign values
 * @returns {boolean} - True if valid, false if has errors
 */
export const areVitalsValid = (vitals) => {
    const errors = validateVitals(vitals);
    return Object.keys(errors).length === 0;
};

/**
 * Calculate BMI from height and weight
 * @param {number} heightCm - Height in centimeters
 * @param {number} weightKg - Weight in kilograms
 * @returns {string|null} - BMI value or null if invalid inputs
 */
export const calculateBMI = (heightCm, weightKg) => {
    const height = parseFloat(heightCm);
    const weight = parseFloat(weightKg);

    if (isNaN(height) || isNaN(weight) || height <= 0 || weight <= 0) {
        return null;
    }

    const heightM = height / 100;
    return (weight / (heightM * heightM)).toFixed(1);
};

/**
 * Get BMI category based on value
 * @param {number} bmi - BMI value
 * @returns {string} - Category description
 */
export const getBMICategory = (bmi) => {
    const value = parseFloat(bmi);

    if (isNaN(value)) return 'Unknown';
    if (value < 18.5) return 'Underweight';
    if (value < 25) return 'Normal';
    if (value < 30) return 'Overweight';
    return 'Obese';
};

export default {
    validateVitals,
    validateVitalField,
    areVitalsValid,
    calculateBMI,
    getBMICategory,
    VITALS_CONSTRAINTS
};
