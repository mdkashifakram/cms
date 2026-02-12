package com.example.clinicapp.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * UserContext Utility
 * 
 * Provides helper methods to access the current authenticated user's context.
 * Used for implementing row-level security and authorization checks.
 * 
 * Security Remediation: Implements user context checking for authorization
 */
@Component
public class UserContext {

    /**
     * Get the current authenticated username
     */
    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            return ((UserDetails) authentication.getPrincipal()).getUsername();
        }
        if (authentication != null && authentication.getPrincipal() instanceof String) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }

    /**
     * Check if current user has a specific role
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            return false;
        }
        return authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_" + role) 
                           || auth.getAuthority().equals(role));
    }

    /**
     * Check if current user is an Admin
     */
    public boolean isAdmin() {
        return hasRole("ADMIN");
    }

    /**
     * Check if current user is a Doctor
     */
    public boolean isDoctor() {
        return hasRole("DOCTOR");
    }

    /**
     * Check if current user is a Receptionist
     */
    public boolean isReceptionist() {
        return hasRole("RECEPTIONIST");
    }

    /**
     * Check if current user is a Nurse
     */
    public boolean isNurse() {
        return hasRole("NURSE");
    }

    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String... roles) {
        for (String role : roles) {
            if (hasRole(role)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if user is authenticated
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.isAuthenticated() 
            && !"anonymousUser".equals(authentication.getPrincipal());
    }
}
