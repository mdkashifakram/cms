package com.example.clinicapp.exception;

import org.springframework.security.core.AuthenticationException;

public class PasswordExpiredException extends AuthenticationException {
    public PasswordExpiredException(String msg) {
        super(msg);
    }
}