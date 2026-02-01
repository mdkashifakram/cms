/**
 * CMS-006: Private Route Component (SECURE)
 *
 * Protects routes by validating authentication with backend.
 * Uses HttpOnly cookies for authentication - NO localStorage.
 * Redirects unauthenticated users to login page.
 * Optionally checks for specific role requirements.
 *
 * Security: CMS-PRIV-001 - Removed localStorage token access
 */
import React, { useState, useEffect } from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { API_CONFIG } from '../config/api';
import logger from '../utils/logger';

/**
 * PrivateRoute component
 * @param {React.ReactNode} children - Child components to render if authenticated
 * @param {string|string[]} requiredRole - Optional role(s) required to access this route
 */
const PrivateRoute = ({ children, requiredRole = null }) => {
    const location = useLocation();
    const [authState, setAuthState] = useState({
        loading: true,
        authenticated: false,
        userRoles: []
    });

    useEffect(() => {
        const validateAuth = async () => {
            try {
                // Validate authentication via backend endpoint
                // HttpOnly cookie is sent automatically with credentials: 'include'
                const response = await fetch(`${API_CONFIG.BASE_URL}/auth/validate`, {
                    method: 'GET',
                    credentials: 'include',
                    headers: {
                        'Content-Type': 'application/json'
                    }
                });

                if (response.ok) {
                    const data = await response.json();
                    setAuthState({
                        loading: false,
                        authenticated: true,
                        userRoles: data.roles || []
                    });
                } else {
                    setAuthState({
                        loading: false,
                        authenticated: false,
                        userRoles: []
                    });
                }
            } catch (error) {
                logger.error('Authentication validation failed');
                setAuthState({
                    loading: false,
                    authenticated: false,
                    userRoles: []
                });
            }
        };

        validateAuth();
    }, []);

    // Show loading state while checking authentication
    if (authState.loading) {
        return <div>Loading...</div>;
    }

    // Not authenticated - redirect to login
    if (!authState.authenticated) {
        return <Navigate to="/" state={{ from: location }} replace />;
    }

    // Check role requirement if specified
    if (requiredRole) {
        const requiredRoles = Array.isArray(requiredRole) ? requiredRole : [requiredRole];

        // Check if user has at least one of the required roles
        const hasRequiredRole = requiredRoles.some(role =>
            authState.userRoles.includes(role) || authState.userRoles.includes(`ROLE_${role}`)
        );

        if (!hasRequiredRole) {
            logger.warn('Access denied: User lacks required role(s)');
            return <Navigate to="/dashboard" state={{ unauthorized: true }} replace />;
        }
    }

    // All checks passed - render children
    return children;
};

export default PrivateRoute;
