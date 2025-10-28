package com.platform.saas.security;

import com.platform.saas.model.User;
import com.platform.saas.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * Utility for extracting user information from JWT tokens.
 * Handles Cognito token claims and user synchronization.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtUserInfoExtractor {

    private final UserService userService;

    /**
     * Extract user information from JWT token and ensure user exists in database.
     *
     * @param jwt The JWT token
     * @return User information
     */
    public JwtUserInfo extractUserInfo(Jwt jwt) {
        String cognitoUserId = jwt.getSubject(); // 'sub' claim
        String email = jwt.getClaimAsString("email");
        String name = jwt.getClaimAsString("name");
        Boolean emailVerified = jwt.getClaimAsBoolean("email_verified");

        log.debug("Extracting user info from JWT: cognitoUserId={}, email={}", cognitoUserId, email);

        // Ensure user exists in our database
        User user;
        try {
            user = userService.getUserByCognitoId(cognitoUserId);
            log.debug("Found existing user with ID: {}", user.getId());
        } catch (Exception e) {
            // User doesn't exist, create them
            log.info("Creating new user from JWT: cognitoUserId={}, email={}", cognitoUserId, email);
            user = userService.createUserFromCognito(cognitoUserId, email, name);
        }

        // Update last login
        userService.updateLastLogin(user.getId());

        return JwtUserInfo.builder()
                .cognitoUserId(cognitoUserId)
                .email(email)
                .name(name)
                .emailVerified(emailVerified != null && emailVerified)
                .userId(user.getId())
                .build();
    }

    /**
     * Extract user information from JWT token string.
     *
     * @param token The JWT token string
     * @return User information
     */
    public String extractCognitoUserId(Jwt jwt) {
        return jwt.getSubject();
    }

    /**
     * Extract email from JWT token.
     *
     * @param jwt The JWT token
     * @return The user's email
     */
    public String extractEmail(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }
}
