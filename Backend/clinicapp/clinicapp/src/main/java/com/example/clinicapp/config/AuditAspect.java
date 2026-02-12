package com.example.clinicapp.config;

import com.example.clinicapp.service.AuditService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * Audit Aspect
 * 
 * Automatically logs all controller method executions for audit trail.
 * Captures method invocations, parameters (sanitized), and execution time.
 */
@Aspect
@Component
public class AuditAspect {

    private static final Logger logger = LoggerFactory.getLogger(AuditAspect.class);

    @Autowired
    private AuditService auditService;

    /**
     * Pointcut for all REST controller methods
     */
    @Pointcut("within(@org.springframework.web.bind.annotation.RestController *)")
    public void restControllerMethods() {}

    /**
     * Pointcut for patient-related operations (HIPAA sensitive)
     */
    @Pointcut("execution(* com.example.clinicapp.controller.PatientController.*(..))")
    public void patientControllerMethods() {}

    /**
     * Pointcut for prescription operations (HIPAA sensitive)
     */
    @Pointcut("execution(* com.example.clinicapp.controller.PrescriptionController.*(..))")
    public void prescriptionControllerMethods() {}

    /**
     * Pointcut for authentication operations
     */
    @Pointcut("execution(* com.example.clinicapp.controller.AuthController.*(..))")
    public void authControllerMethods() {}

    /**
     * Around advice for HIPAA-sensitive operations (Patient & Prescription)
     */
    @Around("patientControllerMethods() || prescriptionControllerMethods()")
    public Object auditSensitiveOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String resourceType = className.replace("Controller", "").toUpperCase();
        
        // Determine action from method name
        String action = determineAction(methodName);
        
        // Extract resource ID if present
        Long resourceId = extractResourceId(joinPoint.getArgs());
        
        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            
            // Log successful operation
            String details = String.format("Method: %s, Duration: %dms", methodName, duration);
            auditService.log(action, resourceType, resourceId, details);
            
            return result;
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            
            // Log failed operation
            String details = String.format("Method: %s, Error: %s, Duration: %dms", 
                methodName, e.getMessage(), duration);
            auditService.log(action + "_FAILED", resourceType, resourceId, details);
            
            throw e;
        }
    }

    /**
     * Around advice for authentication operations
     */
    @Around("authControllerMethods()")
    public Object auditAuthOperations(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        String username = extractUsername(joinPoint.getArgs());
        
        String action = switch (methodName) {
            case "login", "loginLegacy" -> "LOGIN";
            case "logout" -> "LOGOUT";
            case "register" -> "REGISTER";
            case "resetPassword" -> "PASSWORD_RESET";
            default -> "AUTH_" + methodName.toUpperCase();
        };
        
        try {
            Object result = joinPoint.proceed();
            
            // Log successful auth operation
            if (username != null) {
                auditService.logAuthEvent(action + "_SUCCESS", username, 
                    "Authentication operation successful");
            }
            
            return result;
        } catch (Exception e) {
            // Log failed auth operation
            if (username != null) {
                auditService.logAuthEvent(action + "_FAILED", username, 
                    "Error: " + sanitizeErrorMessage(e.getMessage()));
            }
            
            throw e;
        }
    }

    /**
     * Determine action type from method name
     */
    private String determineAction(String methodName) {
        if (methodName.startsWith("get") || methodName.startsWith("find") || 
            methodName.startsWith("search") || methodName.startsWith("list")) {
            return "READ";
        } else if (methodName.startsWith("create") || methodName.startsWith("save") || 
                   methodName.startsWith("add")) {
            return "CREATE";
        } else if (methodName.startsWith("update") || methodName.startsWith("modify") || 
                   methodName.startsWith("edit")) {
            return "UPDATE";
        } else if (methodName.startsWith("delete") || methodName.startsWith("remove")) {
            return "DELETE";
        }
        return "ACCESS";
    }

    /**
     * Extract resource ID from method arguments
     */
    private Long extractResourceId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof Long) {
                return (Long) arg;
            }
        }
        return null;
    }

    /**
     * Extract username from login request arguments
     */
    private String extractUsername(Object[] args) {
        for (Object arg : args) {
            // Check if it's a LoginRequest or similar
            if (arg != null) {
                try {
                    Method getUsername = arg.getClass().getMethod("getUsername");
                    return (String) getUsername.invoke(arg);
                } catch (Exception e) {
                    // Try getEmail if getUsername doesn't exist
                    try {
                        Method getEmail = arg.getClass().getMethod("getEmail");
                        return (String) getEmail.invoke(arg);
                    } catch (Exception ex) {
                        // Ignore
                    }
                }
            }
        }
        return "unknown";
    }

    /**
     * Sanitize error messages to remove sensitive data
     */
    private String sanitizeErrorMessage(String message) {
        if (message == null) return "Unknown error";
        
        // Remove potential sensitive data patterns
        return message
            .replaceAll("password\\s*[:=]\\s*\\S+", "password: [REDACTED]")
            .replaceAll("token\\s*[:=]\\s*\\S+", "token: [REDACTED]")
            .replaceAll("\\b\\d{10,}\\b", "[PHONE_REDACTED]"); // Phone numbers
    }
}
