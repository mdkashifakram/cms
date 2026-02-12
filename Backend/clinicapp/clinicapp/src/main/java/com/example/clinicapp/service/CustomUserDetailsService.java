package com.example.clinicapp.service;

import com.example.clinicapp.entity.User;
import com.example.clinicapp.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));

        // Check if account is locked
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new RuntimeException("Account is locked");
        }

        // Check if account is enabled
        if (Boolean.FALSE.equals(user.getAccountEnabled())) {
            throw new RuntimeException("Account is disabled");
        }

        // Check password expiry (30 days)
        if (user.getPasswordSetAt() != null) {
            long daysSinceSet = ChronoUnit.DAYS.between(user.getPasswordSetAt(), LocalDateTime.now());
            if (daysSinceSet >= 90) { // 90 days for production, adjust as needed
                throw new RuntimeException("Password has expired. Please reset your password.");
            }
        }

        // Convert roles to authorities with ROLE_ prefix
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(Boolean.TRUE.equals(user.getAccountLocked()))
                .credentialsExpired(false)
                .disabled(Boolean.FALSE.equals(user.getAccountEnabled()))
                .build();
    }
}