package com.platform.saas.security;

import com.platform.saas.model.User;
import com.platform.saas.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for JwtUserInfoExtractor.
 *
 * Sprint 4.5 - Security Testing
 * Target: 77 missed instructions → ~62 covered (80% of class)
 *
 * Test Categories:
 * 1. extractUserInfo() - existing user
 * 2. extractUserInfo() - new user (creates user)
 * 3. extractUserInfo() - various claim scenarios
 * 4. extractCognitoUserId() - simple extraction
 * 5. extractEmail() - simple extraction
 * 6. Edge cases - null claims, missing claims
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUserInfoExtractor Tests")
class JwtUserInfoExtractorTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private JwtUserInfoExtractor jwtUserInfoExtractor;

    private String cognitoUserId;
    private String email;
    private String name;
    private UUID userId;
    private User mockUser;

    @BeforeEach
    void setUp() {
        cognitoUserId = "cognito-user-123";
        email = "test@example.com";
        name = "Test User";
        userId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setCognitoUserId(cognitoUserId);
        mockUser.setEmail(email);
        mockUser.setName(name);
    }

    // ==================== extractUserInfo() - Existing User ====================

    @Test
    @DisplayName("Should extract user info for existing user")
    void extractUserInfo_ExistingUser_ReturnsUserInfo() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, true);
        when(userService.getUserByCognitoId(cognitoUserId)).thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCognitoUserId()).isEqualTo(cognitoUserId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(userService).getUserByCognitoId(cognitoUserId);
        verify(userService).updateLastLogin(userId);
        verify(userService, never()).createUserFromCognito(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle email_verified as false")
    void extractUserInfo_EmailNotVerified_ReturnsFalse() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, false);
        when(userService.getUserByCognitoId(cognitoUserId)).thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result.isEmailVerified()).isFalse();

        verify(userService).getUserByCognitoId(cognitoUserId);
        verify(userService).updateLastLogin(userId);
    }

    @Test
    @DisplayName("Should handle missing email_verified claim as false")
    void extractUserInfo_MissingEmailVerified_DefaultsToFalse() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, null);
        when(userService.getUserByCognitoId(cognitoUserId)).thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result.isEmailVerified()).isFalse();

        verify(userService).getUserByCognitoId(cognitoUserId);
    }

    // ==================== extractUserInfo() - New User ====================

    @Test
    @DisplayName("Should create new user when user doesn't exist")
    void extractUserInfo_NewUser_CreatesUser() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, true);

        // Simulate user not found
        when(userService.getUserByCognitoId(cognitoUserId))
                .thenThrow(new RuntimeException("User not found"));

        // Simulate user creation
        when(userService.createUserFromCognito(cognitoUserId, email, name))
                .thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCognitoUserId()).isEqualTo(cognitoUserId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(userService).getUserByCognitoId(cognitoUserId);
        verify(userService).createUserFromCognito(cognitoUserId, email, name);
        verify(userService).updateLastLogin(userId);
    }

    @Test
    @DisplayName("Should create user with null name")
    void extractUserInfo_NewUserNullName_CreatesUser() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, null, true);

        when(userService.getUserByCognitoId(cognitoUserId))
                .thenThrow(new RuntimeException("User not found"));

        mockUser.setName(null);
        when(userService.createUserFromCognito(cognitoUserId, email, null))
                .thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isNull();

        verify(userService).createUserFromCognito(cognitoUserId, email, null);
        verify(userService).updateLastLogin(userId);
    }

    // ==================== extractUserInfo() - Edge Cases ====================

    @Test
    @DisplayName("Should handle JWT with minimal claims")
    void extractUserInfo_MinimalClaims_ExtractsSubjectOnly() {
        // Given - JWT with only subject claim
        Map<String, Object> claims = new HashMap<>();
        Jwt jwt = createJwtWithClaims(cognitoUserId, claims);

        when(userService.getUserByCognitoId(cognitoUserId)).thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result.getCognitoUserId()).isEqualTo(cognitoUserId);
        assertThat(result.getEmail()).isNull();
        assertThat(result.getName()).isNull();
        assertThat(result.isEmailVerified()).isFalse();

        verify(userService).getUserByCognitoId(cognitoUserId);
    }

    @Test
    @DisplayName("Should handle JWT with empty string claims")
    void extractUserInfo_EmptyStringClaims_ExtractsEmptyStrings() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, "", "", true);
        when(userService.getUserByCognitoId(cognitoUserId)).thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result.getEmail()).isEmpty();
        assertThat(result.getName()).isEmpty();

        verify(userService).getUserByCognitoId(cognitoUserId);
    }

    // ==================== extractCognitoUserId() Tests ====================

    @Test
    @DisplayName("Should extract Cognito user ID from JWT")
    void extractCognitoUserId_ValidJwt_ReturnsUserId() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, true);

        // When
        String result = jwtUserInfoExtractor.extractCognitoUserId(jwt);

        // Then
        assertThat(result).isEqualTo(cognitoUserId);
    }

    @Test
    @DisplayName("Should extract Cognito user ID with special characters")
    void extractCognitoUserId_SpecialCharacters_ReturnsUserId() {
        // Given
        String specialUserId = "cognito-user-123-abc-xyz";
        Jwt jwt = createJwt(specialUserId, email, name, true);

        // When
        String result = jwtUserInfoExtractor.extractCognitoUserId(jwt);

        // Then
        assertThat(result).isEqualTo(specialUserId);
    }

    // ==================== extractEmail() Tests ====================

    @Test
    @DisplayName("Should extract email from JWT")
    void extractEmail_ValidJwt_ReturnsEmail() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, true);

        // When
        String result = jwtUserInfoExtractor.extractEmail(jwt);

        // Then
        assertThat(result).isEqualTo(email);
    }

    @Test
    @DisplayName("Should return null when email claim is missing")
    void extractEmail_MissingClaim_ReturnsNull() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        Jwt jwt = createJwtWithClaims(cognitoUserId, claims);

        // When
        String result = jwtUserInfoExtractor.extractEmail(jwt);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should extract email with various formats")
    void extractEmail_VariousFormats_ReturnsEmail() {
        // Given - test various email formats
        String[] emails = {
            "simple@example.com",
            "user.name+tag@example.co.uk",
            "user_name@sub.domain.example.com"
        };

        for (String testEmail : emails) {
            Jwt jwt = createJwt(cognitoUserId, testEmail, name, true);

            // When
            String result = jwtUserInfoExtractor.extractEmail(jwt);

            // Then
            assertThat(result).isEqualTo(testEmail);
        }
    }

    // ==================== Integration Scenarios ====================

    @Test
    @DisplayName("Should handle full user flow with all claims")
    void extractUserInfo_FullFlow_ExtractsAllData() {
        // Given
        Jwt jwt = createJwt(cognitoUserId, email, name, true);
        when(userService.getUserByCognitoId(cognitoUserId)).thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then - Verify all data extracted correctly
        assertThat(result.getCognitoUserId()).isEqualTo(cognitoUserId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isEqualTo(name);
        assertThat(result.isEmailVerified()).isTrue();
        assertThat(result.getUserId()).isEqualTo(userId);

        // Verify service interactions
        verify(userService).getUserByCognitoId(cognitoUserId);
        verify(userService).updateLastLogin(userId);
        verify(userService, never()).createUserFromCognito(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle user creation with partial claims")
    void extractUserInfo_NewUserPartialClaims_CreatesUserSuccessfully() {
        // Given - JWT with subject and email only (no name)
        Jwt jwt = createJwt(cognitoUserId, email, null, false);

        when(userService.getUserByCognitoId(cognitoUserId))
                .thenThrow(new RuntimeException("User not found"));

        mockUser.setName(null);
        when(userService.createUserFromCognito(cognitoUserId, email, null))
                .thenReturn(mockUser);

        // When
        JwtUserInfo result = jwtUserInfoExtractor.extractUserInfo(jwt);

        // Then
        assertThat(result.getCognitoUserId()).isEqualTo(cognitoUserId);
        assertThat(result.getEmail()).isEqualTo(email);
        assertThat(result.getName()).isNull();
        assertThat(result.isEmailVerified()).isFalse();
        assertThat(result.getUserId()).isEqualTo(userId);

        verify(userService).createUserFromCognito(cognitoUserId, email, null);
        verify(userService).updateLastLogin(userId);
    }

    // ==================== Helper Methods ====================

    /**
     * Create a JWT token with standard claims.
     */
    private Jwt createJwt(String subject, String email, String name, Boolean emailVerified) {
        Map<String, Object> claims = new HashMap<>();
        if (email != null) {
            claims.put("email", email);
        }
        if (name != null) {
            claims.put("name", name);
        }
        if (emailVerified != null) {
            claims.put("email_verified", emailVerified);
        }

        return createJwtWithClaims(subject, claims);
    }

    /**
     * Create a JWT token with custom claims.
     */
    private Jwt createJwtWithClaims(String subject, Map<String, Object> claims) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        // JWT requires at least one claim - add sub if claims is empty
        if (claims.isEmpty()) {
            claims.put("sub", subject);
        }

        return new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(3600),
            headers,
            claims
        ) {
            @Override
            public String getSubject() {
                return subject;
            }
        };
    }
}
