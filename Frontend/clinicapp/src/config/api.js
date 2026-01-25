/**
 * API Configuration
 * 
 * Security Remediation: Centralized API configuration to remove hardcoded URLs
 * and enable environment-based configuration for HTTPS in production.
 */

export const API_CONFIG = {
    BASE_URL: process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080',
    TIMEOUT: 30000,
    CREDENTIALS: 'include',  // Always include cookies for HttpOnly cookie auth
    HEADERS: {
        'Content-Type': 'application/json',
    }
};

export const API_ENDPOINTS = {
    AUTH: {
        LOGIN: '/auth/login',
        LOGOUT: '/auth/logout',
        REGISTER: '/auth/register',
        RESET_PASSWORD: '/auth/reset-password',
        VALIDATE: '/auth/validate',
    },
    APPOINTMENTS: {
        BOOK: '/appointments/bookAppointment',
        BY_ID: (id) => `/appointments/${id}`,
        BY_DAY: '/appointments/day',
        UPDATE: (id) => `/appointments/update/${id}`,
        CANCEL: (id) => `/appointments/cancel/${id}`,
        AVAILABILITY: '/appointments/availability',
        BY_STATUS: '/appointments/status',
    },
    PRESCRIPTIONS: {
        CREATE: '/prescriptions/create',
        GENERATE: '/prescriptions/create',  // Fixed: using correct backend endpoint
        BY_ID: (id) => `/prescriptions/${id}`,
        BY_APPOINTMENT: (id) => `/prescriptions/appointment/${id}`,
        BY_PATIENT: (id) => `/prescriptions/patient/${id}`,
        BY_DOCTOR: (id) => `/prescriptions/doctor/${id}`,
        ALL: '/prescriptions',
    },
    PATIENTS: {
        ALL: '/patients',
        BY_ID: (id) => `/patients/${id}`,
        SEARCH: '/patients/search',
        SEARCH_LEGACY: '/patients/searchpatient',  // Deprecated
    },
    DOCTORS: {
        ALL: '/doctors',
        BY_ID: (id) => `/doctors/${id}`,
    },
    TEMPLATES: {
        SAVE: '/templates/saveTemplate',
        LOAD: '/templates/loadTemplate',
    },
    DIAGNOSIS: {
        SEARCH: '/api/diagnosis',
        ADD: '/api/diagnosis',
    },
    CONSENT: {
        BY_PATIENT: (id) => `/api/consent/${id}`,
        GRANT: (id) => `/api/consent/${id}/grant`,
        REVOKE: (id) => `/api/consent/${id}/revoke`,
        SUMMARY: (id) => `/api/consent/${id}/summary`,
    }
};

/**
 * Helper function for API calls with standardized error handling
 * 
 * @param {string} endpoint - API endpoint (use API_ENDPOINTS constants)
 * @param {object} options - Fetch options (method, body, etc.)
 * @returns {Promise<any>} - Response data
 */
export const apiCall = async (endpoint, options = {}) => {
    const url = `${API_CONFIG.BASE_URL}${endpoint}`;

    const config = {
        credentials: API_CONFIG.CREDENTIALS,
        headers: { ...API_CONFIG.HEADERS },
        ...options,
    };

    // Don't set Content-Type for FormData (browser sets it with boundary)
    if (options.body instanceof FormData) {
        delete config.headers['Content-Type'];
    }

    try {
        const response = await fetch(url, config);

        // Handle non-OK responses
        if (!response.ok) {
            const errorData = await response.json().catch(() => ({}));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        // Return JSON if content exists
        const contentType = response.headers.get('content-type');
        if (contentType && contentType.includes('application/json')) {
            return await response.json();
        }

        return response;
    } catch (error) {
        console.error('API call failed:', error);
        throw error;
    }
};

/**
 * Convenience methods for common HTTP verbs
 */
export const api = {
    get: (endpoint) => apiCall(endpoint, { method: 'GET' }),

    post: (endpoint, data) => apiCall(endpoint, {
        method: 'POST',
        body: JSON.stringify(data),
    }),

    put: (endpoint, data) => apiCall(endpoint, {
        method: 'PUT',
        body: JSON.stringify(data),
    }),

    patch: (endpoint, data) => apiCall(endpoint, {
        method: 'PATCH',
        body: JSON.stringify(data),
    }),

    delete: (endpoint) => apiCall(endpoint, { method: 'DELETE' }),
};

export default API_CONFIG;
