package com.example.clinicapp.entity;

import java.time.LocalDateTime;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @JsonIgnore // CRITICAL: Never serialize password in JSON responses
    @Column(nullable = false)
    private String password;
    
    @Column(unique = true)
    private String email;
    
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    @Column(name = "password_set_at")
    private LocalDateTime passwordSetAt;
    
    // Link to Doctor entity for doctors
    @ManyToOne
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;
    
    @Column(name = "account_locked")
    private Boolean accountLocked = false;
    
    @Column(name = "account_enabled")
    private Boolean accountEnabled = true;
    
    // Constructors
    public User() {}
    
    public User(String username, String password, String email, Set<Role> roles) {
        this.username = username;
        this.password = password;
        this.email = email;
        this.roles = roles;
        this.passwordSetAt = LocalDateTime.now();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public Set<Role> getRoles() { return roles; }
    public void setRoles(Set<Role> roles) { this.roles = roles; }
    
    public LocalDateTime getPasswordSetAt() { return passwordSetAt; }
    public void setPasswordSetAt(LocalDateTime passwordSetAt) { this.passwordSetAt = passwordSetAt; }
    
    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }
    
    public Boolean getAccountLocked() { return accountLocked; }
    public void setAccountLocked(Boolean accountLocked) { this.accountLocked = accountLocked; }
    
    public Boolean getAccountEnabled() { return accountEnabled; }
    public void setAccountEnabled(Boolean accountEnabled) { this.accountEnabled = accountEnabled; }
}