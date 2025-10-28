package com.platform.saas.security;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * User information extracted from JWT token.
 * Contains claims from AWS Cognito ID token.
 */
@Data
@Builder
public class JwtUserInfo {

    private String cognitoUserId;  // 'sub' claim
    private String email;          // 'email' claim
    private String name;           // 'name' claim
    private boolean emailVerified; // 'email_verified' claim
    private UUID userId;           // Internal user ID (from database)
}
