package com.example.clinicapp.config;

import com.example.clinicapp.entity.Doctor;
import com.example.clinicapp.entity.Role;
import com.example.clinicapp.entity.User;
import com.example.clinicapp.repository.DoctorRepository;
import com.example.clinicapp.repository.RoleRepository;
import com.example.clinicapp.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Set;

@Configuration
public class AppStartupRunner {

    private static final Logger log = LoggerFactory.getLogger(AppStartupRunner.class);

    @Bean
    public CommandLineRunner init(RoleRepository roleRepository,
                                  UserRepository userRepository,
                                  DoctorRepository doctorRepository,
                                  PasswordEncoder passwordEncoder,
                                  Environment env) {
        return args -> {

            // --- Create roles if missing ---
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role("ADMIN")));
            Role doctorRole = roleRepository.findByName("DOCTOR")
                    .orElseGet(() -> roleRepository.save(new Role("DOCTOR")));
            Role nurseRole = roleRepository.findByName("NURSE")
                    .orElseGet(() -> roleRepository.save(new Role("NURSE")));
            Role receptionistRole = roleRepository.findByName("RECEPTIONIST")
                    .orElseGet(() -> roleRepository.save(new Role("RECEPTIONIST")));
            Role userRole = roleRepository.findByName("USER")
                    .orElseGet(() -> roleRepository.save(new Role("USER")));

            // --- Create default admin if missing ---
            if (userRepository.findByUsername("admin").isEmpty()) {

                // Read password from application.properties or environment variable
                String bootstrapPassword = env.getProperty("BOOTSTRAP_ADMIN_PASSWORD");

                // Detect active profiles
                boolean isDev = Arrays.asList(env.getActiveProfiles()).contains("dev");

                // Dev fallback if no password provided and profile is dev
                if ((bootstrapPassword == null || bootstrapPassword.isBlank()) && isDev) {
                    bootstrapPassword = "DevDefault@123";
                    log.warn("BOOTSTRAP_ADMIN_PASSWORD not set; using local dev fallback password.");
                } else if (bootstrapPassword == null || bootstrapPassword.isBlank()) {
                    // Fail if not dev and password missing
                    log.error("BOOTSTRAP_ADMIN_PASSWORD is not set. Cannot create default admin.");
                    throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD is required for admin creation.");
                }

                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode(bootstrapPassword));
                admin.setEmail("admin@clinic.com");
                admin.setRoles(Set.of(adminRole));
                admin.setPasswordSetAt(LocalDateTime.now());
                admin.setAccountEnabled(true);
                admin.setAccountLocked(false);

                userRepository.save(admin);

                log.info("Admin user created with username=admin.");
                log.warn("IMPORTANT: Change the admin password immediately after first login.");
            }

            // --- Create default doctor if missing ---
            if (doctorRepository.findByName("Aswini Rana").isEmpty()) {
                Doctor doctor = new Doctor();
                doctor.setName("Aswini Rana");
                doctor.setSpecialty("General Physician");
                doctor.setContactNumber("+91-9876543210");
                doctor.setEmail("aswini.rana@clinic.com");

                doctorRepository.save(doctor);

                log.info("Default doctor created: Aswini Rana (General Physician)");
            }
        };
    }
}
