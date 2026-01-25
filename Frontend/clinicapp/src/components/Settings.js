import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import '../styles/Settings.css';
import logger from '../utils/logger';
import { API_CONFIG, API_ENDPOINTS } from '../config/api';

const Settings = () => {
    const [formData, setFormData] = useState({
        oldPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [errors, setErrors] = useState({});
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));

        // Clear field-specific error on input change
        if (errors[name]) {
            setErrors(prev => ({ ...prev, [name]: '' }));
        }

        // Clear general error
        if (error) {
            setError('');
        }
    };

    const validateForm = () => {
        const newErrors = {};

        // Old password validation
        if (!formData.oldPassword) {
            newErrors.oldPassword = 'Current password is required';
        }

        // New password validation
        if (!formData.newPassword) {
            newErrors.newPassword = 'New password is required';
        } else if (formData.newPassword.length < 8) {
            newErrors.newPassword = 'Password must be at least 8 characters';
        } else if (!/(?=.*[a-z])(?=.*[A-Z])(?=.*\d)/.test(formData.newPassword)) {
            newErrors.newPassword = 'Password must contain uppercase, lowercase, and number';
        }

        // Check if new password is same as old password
        if (formData.newPassword && formData.oldPassword && formData.newPassword === formData.oldPassword) {
            newErrors.newPassword = 'New password must be different from current password';
        }

        // Confirm password validation
        if (!formData.confirmPassword) {
            newErrors.confirmPassword = 'Please confirm your new password';
        } else if (formData.confirmPassword !== formData.newPassword) {
            newErrors.confirmPassword = 'Passwords do not match';
        }

        return newErrors;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();

        // Clear previous messages
        setError('');
        setSuccessMessage('');

        // Validate form
        const validationErrors = validateForm();
        if (Object.keys(validationErrors).length > 0) {
            setErrors(validationErrors);
            return;
        }

        try {
            setIsSubmitting(true);

            const response = await fetch(
                `${API_CONFIG.BASE_URL}${API_ENDPOINTS.AUTH.RESET_PASSWORD}` +
                `?oldPassword=${encodeURIComponent(formData.oldPassword)}` +
                `&newPassword=${encodeURIComponent(formData.newPassword)}`,
                {
                    method: 'POST',
                    headers: API_CONFIG.HEADERS,
                    credentials: API_CONFIG.CREDENTIALS
                }
            );

            if (!response.ok) {
                const errorText = await response.text();
                throw new Error(errorText || 'Failed to reset password');
            }

            const result = await response.text();
            logger.log('Password reset successful');

            setSuccessMessage('Password updated successfully! Redirecting to login...');

            // Clear form
            setFormData({
                oldPassword: '',
                newPassword: '',
                confirmPassword: ''
            });

            // Redirect to login after 2 seconds
            setTimeout(() => {
                navigate('/');
            }, 2000);

        } catch (error) {
            logger.error('Password reset error:', error.message);
            setError(error.message || 'Failed to reset password. Please try again.');
        } finally {
            setIsSubmitting(false);
        }
    };

    const handleCancel = () => {
        navigate('/dashboard');
    };

    return (
        <div className="settings-container">
            <div className="settings-card">
                <h2>Settings</h2>
                <h3>Change Password</h3>

                {successMessage && (
                    <div className="success-message-banner">
                        {successMessage}
                    </div>
                )}

                {error && (
                    <div className="error-message-banner">
                        {error}
                    </div>
                )}

                <form className="settings-form" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label htmlFor="oldPassword">Current Password</label>
                        <input
                            type="password"
                            id="oldPassword"
                            name="oldPassword"
                            value={formData.oldPassword}
                            onChange={handleInputChange}
                            placeholder="Enter your current password"
                            disabled={isSubmitting}
                        />
                        {errors.oldPassword && (
                            <span className="error-message">{errors.oldPassword}</span>
                        )}
                    </div>

                    <div className="form-group">
                        <label htmlFor="newPassword">New Password</label>
                        <input
                            type="password"
                            id="newPassword"
                            name="newPassword"
                            value={formData.newPassword}
                            onChange={handleInputChange}
                            placeholder="Enter your new password"
                            disabled={isSubmitting}
                        />
                        {errors.newPassword && (
                            <span className="error-message">{errors.newPassword}</span>
                        )}
                    </div>

                    <div className="form-group">
                        <label htmlFor="confirmPassword">Confirm New Password</label>
                        <input
                            type="password"
                            id="confirmPassword"
                            name="confirmPassword"
                            value={formData.confirmPassword}
                            onChange={handleInputChange}
                            placeholder="Confirm your new password"
                            disabled={isSubmitting}
                        />
                        {errors.confirmPassword && (
                            <span className="error-message">{errors.confirmPassword}</span>
                        )}
                    </div>

                    <div className="button-group">
                        <button
                            type="submit"
                            className="submit-btn"
                            disabled={isSubmitting}
                        >
                            {isSubmitting ? 'Updating...' : 'Update Password'}
                        </button>
                        <button
                            type="button"
                            className="cancel-btn"
                            onClick={handleCancel}
                            disabled={isSubmitting}
                        >
                            Cancel
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
};

export default Settings;
