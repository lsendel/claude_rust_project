package com.platform.saas.controller;

import com.platform.saas.security.JwtUserInfo;
import com.platform.saas.security.JwtUserInfoExtractor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API controller for authentication operations.
 * Handles OAuth2 callbacks and user session management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final JwtUserInfoExtractor jwtUserInfoExtractor;

    /**
     * Get current authenticated user information.
     *
     * GET /api/auth/me
     *
     * @param jwt The JWT token from Spring Security
     * @return The current user information
     */
    @GetMapping("/me")
    public ResponseEntity<JwtUserInfo> getCurrentUser(@AuthenticationPrincipal Jwt jwt) {
        log.debug("Getting current user info");

        JwtUserInfo userInfo = jwtUserInfoExtractor.extractUserInfo(jwt);

        return ResponseEntity.ok(userInfo);
    }

    /**
     * Health check endpoint for authenticated users.
     *
     * GET /api/auth/status
     *
     * @return 200 OK if user is authenticated
     */
    @GetMapping("/status")
    public ResponseEntity<Void> checkAuthStatus() {
        log.debug("Auth status check");
        return ResponseEntity.ok().build();
    }
}
