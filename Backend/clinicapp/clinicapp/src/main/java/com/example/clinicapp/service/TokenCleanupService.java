package com.example.clinicapp.service;

import com.example.clinicapp.repository.RevokedTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class TokenCleanupService {
    
    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupService.class);
    private final RevokedTokenRepository revokedTokenRepository;

    public TokenCleanupService(RevokedTokenRepository revokedTokenRepository) {
        this.revokedTokenRepository = revokedTokenRepository;
    }

    @Scheduled(cron = "0 0 2 * * ?") // Run at 2 AM every day
    @Transactional
    public void cleanupExpiredTokens() {
        logger.info("Starting cleanup of expired revoked tokens");
        revokedTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
        logger.info("Expired revoked tokens cleaned up");
    }
}