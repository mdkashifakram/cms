package com.example.clinicapp.service;

import com.example.clinicapp.dto.LoginRequest;
import com.example.clinicapp.dto.RegisterRequest;
import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.entity.Role;
import com.example.clinicapp.entity.RevokedToken;
import com.example.clinicapp.entity.User;
import com.example.clinicapp.repository.RoleRepository;
import com.example.clinicapp.repository.RevokedTokenRepository;
import com.example.clinicapp.repository.UserRepository;
import com.example.clinicapp.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       RevokedTokenRepository revokedTokenRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       CustomUserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.revokedTokenRepository = revokedTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Transactional
    public String register(RegisterRequest request) {
        // Check if username exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        // Check if email exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Get roles from request or default to USER
        Set<Role> roles = request.getRoles().stream()
                .map(roleName -> roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName)))
                .collect(Collectors.toSet());

        if (roles.isEmpty()) {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Default role USER not found"));
            roles.add(defaultRole);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setRoles(roles);
        user.setPasswordSetAt(LocalDateTime.now());
        user.setAccountEnabled(true);
        user.setAccountLocked(false);

        userRepository.save(user);
        logger.info("User registered successfully: {}", user.getUsername());
        
        return "User registered successfully!";
    }

    public String authenticate(LoginRequest loginRequest) {
        logger.info("User attempting to authenticate: {}", loginRequest.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()));

            UserDetails userDetails = userDetailsService.loadUserByUsername(loginRequest.getUsername());
            String token = jwtUtil.generateToken(userDetails);
            
            logger.info("User authenticated successfully: {}", loginRequest.getUsername());
            return token;
            
        } catch (BadCredentialsException e) {
            logger.error("Authentication failed for user: {}", loginRequest.getUsername());
            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public void logout(String token, String username) {
        if (token != null && username != null) {
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(24); // Token validity period
            RevokedToken revokedToken = new RevokedToken(token, expiryDate, username, "LOGOUT");
            revokedTokenRepository.save(revokedToken);
            logger.info("User logged out: {}", username);
        }
    }

    @Transactional
    public void resetPassword(String username, String oldPassword, String newPassword, String currentToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate old password
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            logger.warn("Password reset failed for user {}: incorrect old password", username);
            throw new RuntimeException("Current password is incorrect");
        }

        // Revoke current token
        if (currentToken != null) {
            LocalDateTime expiryDate = LocalDateTime.now().plusHours(24);
            RevokedToken revokedToken = new RevokedToken(
                    currentToken, expiryDate, username, "PASSWORD_RESET");
            revokedTokenRepository.save(revokedToken);
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setPasswordSetAt(LocalDateTime.now());
        userRepository.save(user);

        logger.info("Password reset for user: {}", username);
    }

    /**
     * Get doctor information for a user if they are a doctor
     * Returns null if user is not a doctor or doctor info not found
     */
    public Map<String, Object> getDoctorInfoForUser(String username) {
        return userRepository.findByUsername(username)
                .map(User::getDoctor)
                .map(doctor -> {
                    Map<String, Object> doctorInfo = new HashMap<>();
                    doctorInfo.put("id", doctor.getId());
                    doctorInfo.put("name", doctor.getName());
                    doctorInfo.put("specialty", doctor.getSpecialty());
                    return doctorInfo;
                })
                .orElse(null);
    }
}