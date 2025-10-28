package com.platform.saas.controller;

import com.platform.saas.model.User;
import com.platform.saas.service.UserService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Internal API controller for service-to-service communication.
 * These endpoints are secured with an API secret and should not be exposed publicly.
 */
@RestController
@RequestMapping("/api/internal")
@RequiredArgsConstructor
@Slf4j
public class InternalApiController {

    private final UserService userService;

    @Value("${app.api.internal-secret}")
    private String internalApiSecret;

    /**
     * DTO for creating a user from Cognito.
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CognitoUserRequest {
        @NotBlank
        private String cognitoUserId;

        @NotBlank
        @Email
        private String email;

        @NotBlank
        private String name;
    }

    /**
     * Create or update a user from Cognito authentication.
     * Called by Cognito Lambda triggers.
     *
     * POST /api/internal/users/from-cognito
     *
     * Security: Requires X-API-Secret header matching internal API secret
     *
     * @param apiSecret The API secret from request header
     * @param request The user information from Cognito
     * @return The created or updated user
     */
    @PostMapping("/users/from-cognito")
    public ResponseEntity<User> createUserFromCognito(
            @RequestHeader("X-API-Secret") String apiSecret,
            @Valid @RequestBody CognitoUserRequest request) {

        // Validate API secret
        if (!internalApiSecret.equals(apiSecret)) {
            log.warn("Invalid internal API secret provided");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        log.info("Creating user from Cognito via internal API: {}", request.getEmail());

        User user = userService.createUserFromCognito(
                request.getCognitoUserId(),
                request.getEmail(),
                request.getName()
        );

        return ResponseEntity.ok(user);
    }

    /**
     * Health check for internal API.
     *
     * GET /api/internal/health
     *
     * @return 200 OK
     */
    @GetMapping("/health")
    public ResponseEntity<Void> health() {
        return ResponseEntity.ok().build();
    }
}
