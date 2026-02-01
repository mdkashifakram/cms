package com.example.clinicapp.controller;

import com.example.clinicapp.dto.JwtResponse;
import com.example.clinicapp.dto.LoginRequest;
import com.example.clinicapp.dto.RegisterRequest;
import com.example.clinicapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    // CMS-004: Cookie configuration
    @Value("${jwt.expiration:1800000}")
    private long jwtExpirationMs;

    @Value("${app.cookie.secure:false}")
    private boolean secureCookie;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * CMS-004: Login with HttpOnly Cookie
     * 
     * Sets JWT in an HttpOnly cookie to prevent XSS attacks from stealing the token.
     * The token is no longer accessible via JavaScript (localStorage).
     * 
     * Cookie attributes:
     * - HttpOnly: Prevents JavaScript access
     * - Secure: Only sent over HTTPS (when enabled)
     * - SameSite=Strict: Prevents CSRF attacks
     * - Path=/: Available for all endpoints
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest,
                                   HttpServletResponse response) {
        String token = authService.authenticate(loginRequest);
        
        // CMS-004: Set HttpOnly cookie instead of returning token in body
        Cookie authCookie = new Cookie("authToken", token);
        authCookie.setHttpOnly(true);           // Prevents XSS access
        authCookie.setSecure(secureCookie);     // Use HTTPS only in production
        authCookie.setPath("/");                // Available for all paths
        authCookie.setMaxAge((int) (jwtExpirationMs / 1000)); // Convert ms to seconds
        
        // SameSite attribute (prevents CSRF)
        // Production (Secure): None (allows cross-site for subdomains)
        // Development (Insecure): Strict (default protection)
        response.addCookie(authCookie);
        String sameSite = secureCookie ? "None" : "Strict";
        response.setHeader("Set-Cookie", 
            response.getHeader("Set-Cookie") + "; SameSite=" + sameSite);
        
        // Return success message (no token in body for security)
        // Frontend should check response status, not token
        return ResponseEntity.ok(Map.of(
            "message", "Login successful",
            "tokenType", "cookie"
        ));
    }

    /**
     * CMS-004: Legacy login endpoint for backward compatibility
     * 
     * @deprecated Use POST /auth/login which sets HttpOnly cookie
     */
    @Deprecated
    @PostMapping("/login/legacy")
    public ResponseEntity<JwtResponse> loginLegacy(@Valid @RequestBody LoginRequest loginRequest) {
        String token = authService.authenticate(loginRequest);
        return ResponseEntity.ok(new JwtResponse(token));
    }

    /**
     * CMS-PRIV-001: Validate current user session via HttpOnly cookie
     *
     * Used by frontend PrivateRoute component to check authentication status.
     * Validates the HttpOnly cookie automatically sent with the request.
     *
     * Returns user information and roles if authenticated, 401 if not.
     */
    @GetMapping("/validate")
    public ResponseEntity<?> validateSession(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("authenticated", false));
        }

        // Check if authentication is anonymous
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ANONYMOUS"))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("authenticated", false));
        }

        // Extract user details and roles
        Map<String, Object> response = new HashMap<>();
        response.put("authenticated", true);
        response.put("username", authentication.getName());

        // Extract roles from authorities
        List<String> roles = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());
        response.put("roles", roles);

        // Get doctor info if user is a doctor
        Map<String, Object> doctorInfo = authService.getDoctorInfoForUser(authentication.getName());
        if (doctorInfo != null) {
            response.put("doctor", doctorInfo);
        }

        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')") // Only admins can register new users
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest request) {
        String response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @CookieValue(name = "authToken", required = false) String cookieToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {
        
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        // Get token from cookie or header
        String token = null;
        if (cookieToken != null && !cookieToken.isEmpty()) {
            token = cookieToken;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }
        
        if (token != null) {
            authService.logout(token, username);
        }
        
        // CMS-004: Clear the auth cookie
        Cookie clearCookie = new Cookie("authToken", "");
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(secureCookie);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0); // Immediately expire
        response.addCookie(clearCookie);
        
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @CookieValue(name = "authToken", required = false) String cookieToken,
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            HttpServletResponse response) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // Get token from cookie or header
        String token = null;
        if (cookieToken != null && !cookieToken.isEmpty()) {
            token = cookieToken;
        } else if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
        }

        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("No authentication token found");
        }

        authService.resetPassword(username, oldPassword, newPassword, token);
        
        // CMS-004: Clear the cookie on password reset (force re-login)
        Cookie clearCookie = new Cookie("authToken", "");
        clearCookie.setHttpOnly(true);
        clearCookie.setSecure(secureCookie);
        clearCookie.setPath("/");
        clearCookie.setMaxAge(0);
        response.addCookie(clearCookie);
        
        return ResponseEntity.ok("Password reset successfully. Please login again.");
    }
}