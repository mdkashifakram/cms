package com.example.clinicapp.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "revoked_tokens", indexes = {
    @Index(name = "idx_token", columnList = "token")
})
public class RevokedToken {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 500)
    private String token;
    
    @Column(nullable = false)
    private LocalDateTime revokedAt;
    
    @Column(nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(nullable = false)
    private String username;
    
    @Column
    private String reason; // "PASSWORD_RESET", "LOGOUT", "ADMIN_REVOKE"
    
    // Constructors
    public RevokedToken() {}
    
    public RevokedToken(String token, LocalDateTime expiresAt, String username, String reason) {
        this.token = token;
        this.revokedAt = LocalDateTime.now();
        this.expiresAt = expiresAt;
        this.username = username;
        this.reason = reason;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    
    public LocalDateTime getRevokedAt() { return revokedAt; }
    public void setRevokedAt(LocalDateTime revokedAt) { this.revokedAt = revokedAt; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}