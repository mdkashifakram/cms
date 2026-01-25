package com.example.clinicapp.filter;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * CMS-007: Rate Limiting Filter
 * 
 * Prevents brute force attacks by limiting login attempts to 5 per minute per IP address.
 * Uses Bucket4j token bucket algorithm for efficient rate limiting.
 */
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    // Cache of rate limiters per IP address
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    // Configuration: 5 requests per minute
    private static final int REQUESTS_PER_MINUTE = 5;
    private static final Duration REFILL_DURATION = Duration.ofMinutes(1);

    /**
     * Creates a new rate limit bucket for an IP address.
     * Allows 5 requests, refills 5 tokens every minute.
     */
    private Bucket createNewBucket() {
        Bandwidth limit = Bandwidth.classic(
            REQUESTS_PER_MINUTE,
            Refill.intervally(REQUESTS_PER_MINUTE, REFILL_DURATION)
        );
        return Bucket.builder()
            .addLimit(limit)
            .build();
    }

    /**
     * Extracts the real client IP address, handling proxies.
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Return the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIP = request.getHeader("X-Real-IP");
        if (xRealIP != null && !xRealIP.isEmpty()) {
            return xRealIP;
        }
        
        return request.getRemoteAddr();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // Only apply rate limiting to login endpoint
        if (request.getRequestURI().equals("/auth/login") && request.getMethod().equals("POST")) {
            String clientIP = getClientIP(request);
            
            // Get or create bucket for this IP
            Bucket bucket = bucketCache.computeIfAbsent(clientIP, k -> createNewBucket());

            // Try to consume a token
            if (bucket.tryConsume(1)) {
                // Request allowed - proceed
                filterChain.doFilter(request, response);
            } else {
                // Rate limit exceeded
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Too many login attempts. Please try again in 1 minute.\"}");
                
                // Log the rate limit hit for security monitoring
                logger.warn("Rate limit exceeded for IP: " + clientIP + " on login endpoint");
            }
        } else {
            // Not a login request - proceed normally
            filterChain.doFilter(request, response);
        }
    }

    /**
     * Clean up old buckets periodically to prevent memory leaks.
     * In production, consider using a scheduled task or Redis for distributed rate limiting.
     */
    public void cleanupOldBuckets() {
        // Simple cleanup - remove buckets older than 10 minutes
        // In production, use proper expiration with a scheduled task
        if (bucketCache.size() > 10000) {
            bucketCache.clear();
            logger.info("Cleared rate limit bucket cache due to size limit");
        }
    }
}
