package com.example.clinicapp.config;

import com.example.clinicapp.filter.JwtAuthenticationFilter;
import com.example.clinicapp.filter.RateLimitFilter;
import com.example.clinicapp.service.CustomUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final RateLimitFilter rateLimitFilter;
    private final CustomUserDetailsService userDetailsService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                         RateLimitFilter rateLimitFilter,
                         CustomUserDetailsService userDetailsService) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.rateLimitFilter = rateLimitFilter;
        this.userDetailsService = userDetailsService;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // Security Headers Configuration
            .headers(headers -> headers
                // Prevent clickjacking attacks
                .frameOptions(frame -> frame.deny())
                
                // Prevent MIME type sniffing
                .contentTypeOptions(options -> {})
                
                // XSS Protection (legacy browsers)
                .xssProtection(xss -> xss.headerValue(
                    org.springframework.security.web.header.writers.XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))
                
                // Control referrer information
                .referrerPolicy(referrer -> referrer
                    .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                
                // HTTP Strict Transport Security (enable when using HTTPS)
                // .httpStrictTransportSecurity(hsts -> hsts
                //     .maxAgeInSeconds(31536000)
                //     .includeSubDomains(true)
                //     .preload(true))
                
                // Content Security Policy
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data:; " +
                        "font-src 'self'; " +
                        "connect-src 'self' http://localhost:*"))
            )
            
            .authorizeHttpRequests(authz -> authz
                // Public endpoints
                .requestMatchers("/auth/login", "/auth/login/legacy", "/auth/register").permitAll()
                .requestMatchers("/actuator/health").permitAll()
               

                
                // Admin-only endpoints
                .requestMatchers(HttpMethod.DELETE, "/patients/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/doctors/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/appointments/**").hasRole("ADMIN")
                .requestMatchers("/auth/register").hasRole("ADMIN")
                
                // Doctor endpoints
                .requestMatchers("/prescriptions/create").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/prescriptions/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/prescriptions/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/api/diagnosis/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers("/templates/**").hasAnyRole("DOCTOR", "ADMIN")

                // Invoice endpoints
                .requestMatchers("/invoices/create").hasAnyRole("DOCTOR", "ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.POST, "/invoices/**").hasAnyRole("DOCTOR", "ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.PATCH, "/invoices/**").hasAnyRole("DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/invoices/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/invoices/**").authenticated()
                
                // Receptionist/Nurse endpoints
                .requestMatchers("/appointments/**").hasAnyRole("RECEPTIONIST", "DOCTOR", "ADMIN")
                .requestMatchers("/consults/**").hasAnyRole("RECEPTIONIST", "DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/patients/**").hasAnyRole("RECEPTIONIST", "DOCTOR", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/patients/**").hasAnyRole("RECEPTIONIST", "DOCTOR", "ADMIN")
                
                // General authenticated endpoints
                .requestMatchers(HttpMethod.GET, "/patients/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/doctors/**").authenticated()
                .requestMatchers(HttpMethod.GET, "/prescriptions/**").authenticated()
             // All other requests must be authenticated
                .anyRequest().authenticated()
            )
            // Add rate limiting filter before JWT filter
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}