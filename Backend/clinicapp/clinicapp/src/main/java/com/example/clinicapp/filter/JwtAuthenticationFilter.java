package com.example.clinicapp.filter;

import com.example.clinicapp.repository.RevokedTokenRepository;
import com.example.clinicapp.service.CustomUserDetailsService;
import com.example.clinicapp.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private RevokedTokenRepository revokedTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.debug("Processing request: {} {}", request.getMethod(), requestURI);

        try {
            String jwt = getJwtFromRequest(request);

            if (StringUtils.hasText(jwt)) {
                logger.debug("JWT token found for request: {}", requestURI);

                // Check if token is revoked (skip for public endpoints like /auth/login)
                boolean isPublicEndpoint = requestURI.startsWith("/auth/login") ||
                                          requestURI.startsWith("/auth/register");

                if (!isPublicEndpoint && revokedTokenRepository.existsByToken(jwt)) {
                    logger.warn("Attempt to use revoked token for URI: {}", requestURI);
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.getWriter().write("Token has been revoked");
                    return;
                }

                String username = jwtUtil.extractUsername(jwt);
                logger.debug("Extracted username from JWT: {}", username);

                if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                    logger.debug("Loaded user details for: {}, authorities: {}", username, userDetails.getAuthorities());

                    if (jwtUtil.validateToken(jwt, userDetails)) {
                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(
                                        userDetails,
                                        null,
                                        userDetails.getAuthorities());

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request));

                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        logger.info("Authentication successful for user: {} on URI: {}", username, requestURI);
                    } else {
                        logger.warn("Token validation failed for user: {} on URI: {}", username, requestURI);
                    }
                } else if (username == null) {
                    logger.warn("Could not extract username from JWT for URI: {}", requestURI);
                }
            } else {
                logger.debug("No JWT token found for request: {}", requestURI);
            }
        } catch (Exception e) {
            logger.error("Authentication error on URI: {} - Exception: {}", requestURI, e.getClass().getSimpleName(), e);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * CMS-004: Extract JWT from request
     * 
     * Checks for token in this order:
     * 1. HttpOnly cookie (preferred - secure against XSS)
     * 2. Authorization header (legacy support)
     * 
     * @param request HTTP request
     * @return JWT token or null if not found
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        // CMS-004: First check for token in HttpOnly cookie (secure method)
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            logger.debug("Found {} cookies in request", cookies.length);
            for (Cookie cookie : cookies) {
                if ("authToken".equals(cookie.getName())) {
                    String cookieToken = cookie.getValue();
                    if (StringUtils.hasText(cookieToken)) {
                        logger.debug("JWT successfully extracted from 'authToken' cookie");
                        return cookieToken;
                    } else {
                        logger.warn("'authToken' cookie found but value is empty");
                    }
                }
            }
            logger.debug("No 'authToken' cookie found among {} cookies", cookies.length);
        } else {
            logger.debug("No cookies present in request");
        }

        // Fallback: Check Authorization header (for backward compatibility)
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)) {
            if (bearerToken.startsWith("Bearer ")) {
                logger.debug("JWT successfully extracted from Authorization header");
                return bearerToken.substring(7);
            } else {
                logger.warn("Authorization header present but doesn't start with 'Bearer '");
            }
        } else {
            logger.debug("No Authorization header present");
        }

        logger.debug("No JWT found in request (checked cookie and Authorization header)");
        return null;
    }
}