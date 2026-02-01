/**
 * useVitalsValidation Hook
 * 
 * React hook for integrating VitalsValidator with forms.
 * Provides real-time validation and error display.
 * 
 * Usage:
 * const { errors, validateField, validateAll, isValid } = useVitalsValidation();
 * 
 * // On input change:
 * const handleChange = (e) => {
 *   setValue(e.target.value);
 *   validateField('temperature', e.target.value);
 * };
 * 
 * // Show error:
 * {errors.temperature && <span className="error">{errors.temperature}</span>}
 */
import { useState, useCallback } from 'react';
import { validateVitalField, validateVitals, calculateBMI, areVitalsValid } from './VitalsValidator';

const useVitalsValidation = () => {
    const [errors, setErrors] = useState({});

    /**
     * Validate a single vital field
     * @param {string} field - Field name
     * @param {string} value - Field value
     * @returns {boolean} - True if valid
     */
    const validateField = useCallback((field, value) => {
        const error = validateVitalField(field, value);

        setErrors(prev => {
            if (error) {
                return { ...prev, [field]: error };
            } else {
                const { [field]: removed, ...rest } = prev;
                return rest;
            }
        });

        return !error;
    }, []);

    /**
     * Validate all vitals at once
     * @param {object} vitals - Object containing vital values
     * @returns {boolean} - True if all valid
     */
    const validateAll = useCallback((vitals) => {
        const allErrors = validateVitals(vitals);
        setErrors(allErrors);
        return Object.keys(allErrors).length === 0;
    }, []);

    /**
     * Clear all errors
     */
    const clearErrors = useCallback(() => {
        setErrors({});
    }, []);

    /**
     * Clear specific field error
     */
    const clearFieldError = useCallback((field) => {
        setErrors(prev => {
            const { [field]: removed, ...rest } = prev;
            return rest;
        });
    }, []);

    /**
     * Calculate and validate BMI based on height and weight
     * @param {string} height - Height in cm
     * @param {string} weight - Weight in kg
     * @returns {string|null} - Calculated BMI or null
     */
    const calcBMI = useCallback((height, weight) => {
        return calculateBMI(height, weight);
    }, []);

    /**
     * Check if there are any errors
     */
    const isValid = Object.keys(errors).length === 0;

    /**
     * Check if a specific field has an error
     */
    const hasError = useCallback((field) => {
        return !!errors[field];
    }, [errors]);

    /**
     * Get error message for a field
     */
    const getError = useCallback((field) => {
        return errors[field] || null;
    }, [errors]);

    return {
        errors,
        validateField,
        validateAll,
        clearErrors,
        clearFieldError,
        calcBMI,
        isValid,
        hasError,
        getError
    };
};

export default useVitalsValidation;
