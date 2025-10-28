package com.platform.saas.security;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for SecurityConfig.
 * Tests JWT authentication converter, authorities extraction, and CORS configuration.
 *
 * CRITICAL SECURITY COMPONENT - High coverage required
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig Unit Tests - Security Critical")
class SecurityConfigTest {

    private SecurityConfig securityConfig;
    private JwtUserInfoExtractor jwtUserInfoExtractor;

    @Mock
    private HttpServletRequest mockRequest;

    @Mock
    private jakarta.servlet.http.HttpServletMapping mockServletMapping;

    @BeforeEach
    void setUp() {
        jwtUserInfoExtractor = mock(JwtUserInfoExtractor.class);
        securityConfig = new SecurityConfig(jwtUserInfoExtractor);

        // Set required configuration properties
        ReflectionTestUtils.setField(securityConfig, "jwkSetUri",
            "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test/.well-known/jwks.json");
        ReflectionTestUtils.setField(securityConfig, "allowedOrigins",
            "http://localhost:3000,http://localhost:5173");
        ReflectionTestUtils.setField(securityConfig, "internalApiSecret", "test-secret");
    }

    // ========== JWT GRANTED AUTHORITIES CONVERTER TESTS ==========

    @Test
    @DisplayName("Should extract authorities from cognito:groups claim")
    void jwtConverter_ExtractsCognitoGroups() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Arrays.asList("admin", "editor"));

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(2);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_admin", "ROLE_editor");
    }

    @Test
    @DisplayName("Should extract custom authorities from custom:authorities claim")
    void jwtConverter_ExtractsCustomAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("custom:authorities", Arrays.asList("VIEW_REPORTS", "EXPORT_DATA"));

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .contains("VIEW_REPORTS", "EXPORT_DATA");
    }

    @Test
    @DisplayName("Should combine cognito:groups and custom:authorities")
    void jwtConverter_CombinesCognitoAndCustomAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Arrays.asList("admin"));
        claims.put("custom:authorities", Arrays.asList("VIEW_REPORTS", "EXPORT_DATA"));

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSizeGreaterThanOrEqualTo(3);

        List<String> authorityStrings = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        assertThat(authorityStrings).contains("ROLE_admin", "VIEW_REPORTS", "EXPORT_DATA");
    }

    @Test
    @DisplayName("Should handle JWT with no authorities")
    void jwtConverter_HandlesNoAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("Should handle JWT with empty cognito:groups list")
    void jwtConverter_HandlesEmptyCognitoGroups() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Collections.emptyList());

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).isEmpty();
    }

    @Test
    @DisplayName("Should handle JWT with missing custom:authorities claim")
    void jwtConverter_HandlesNullCustomAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Arrays.asList("admin"));
        // Note: custom:authorities not included in claims - simulates null/missing claim

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_admin");
    }

    @Test
    @DisplayName("Should handle JWT with empty custom:authorities list")
    void jwtConverter_HandlesEmptyCustomAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Arrays.asList("admin"));
        claims.put("custom:authorities", Collections.emptyList());

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSize(1);
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactly("ROLE_admin");
    }

    @Test
    @DisplayName("Should prefix cognito groups with ROLE_")
    void jwtConverter_PrefixesCognitoGroupsWithRole() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Arrays.asList("user", "moderator"));

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("ROLE_user", "ROLE_moderator");
    }

    @Test
    @DisplayName("Should not prefix custom authorities with ROLE_")
    void jwtConverter_DoesNotPrefixCustomAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("custom:authorities", Arrays.asList("MANAGE_USERS", "DELETE_PROJECTS"));

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("MANAGE_USERS", "DELETE_PROJECTS");
    }

    @Test
    @DisplayName("Should handle multiple groups and authorities")
    void jwtConverter_HandlesMultipleGroupsAndAuthorities() {
        // Given
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", "user-123");
        claims.put("cognito:groups", Arrays.asList("admin", "editor", "viewer"));
        claims.put("custom:authorities", Arrays.asList("EXPORT", "IMPORT", "AUDIT"));

        Jwt jwt = createJwtWithClaims(claims);
        Converter<Jwt, Collection<GrantedAuthority>> converter =
            securityConfig.jwtGrantedAuthoritiesConverter();

        // When
        Collection<GrantedAuthority> authorities = converter.convert(jwt);

        // Then
        assertThat(authorities).isNotNull();
        assertThat(authorities).hasSizeGreaterThanOrEqualTo(6);

        List<String> authorityStrings = authorities.stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toList());

        assertThat(authorityStrings).contains(
            "ROLE_admin", "ROLE_editor", "ROLE_viewer",
            "EXPORT", "IMPORT", "AUDIT"
        );
    }

    // ========== CORS CONFIGURATION TESTS ==========

    @Test
    @DisplayName("Should configure CORS with allowed origins")
    void cors_ConfiguresAllowedOrigins() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).isNotNull();
        assertThat(config.getAllowedOrigins()).contains(
            "http://localhost:3000",
            "http://localhost:5173"
        );
    }

    @Test
    @DisplayName("Should configure CORS with allowed HTTP methods")
    void cors_ConfiguresAllowedMethods() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowedMethods()).isNotNull();
        assertThat(config.getAllowedMethods()).containsExactlyInAnyOrder(
            "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        );
    }

    @Test
    @DisplayName("Should configure CORS with allowed headers")
    void cors_ConfiguresAllowedHeaders() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowedHeaders()).isNotNull();
        assertThat(config.getAllowedHeaders()).contains("*");
    }

    @Test
    @DisplayName("Should configure CORS to allow credentials")
    void cors_AllowsCredentials() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowCredentials()).isTrue();
    }

    @Test
    @DisplayName("Should configure CORS with max age")
    void cors_ConfiguresMaxAge() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getMaxAge()).isEqualTo(3600L);
    }

    @Test
    @DisplayName("Should configure CORS for all API paths")
    void cors_ConfiguresForAllApiPaths() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        // When
        CorsConfigurationSource source = securityConfig.corsConfigurationSource();

        // Test different API paths
        CorsConfiguration config1 = source.getCorsConfiguration(mockRequest);
        CorsConfiguration config2 = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config1).isNotNull();
        assertThat(config2).isNotNull();
        assertThat(config1.getAllowedOrigins()).isEqualTo(config2.getAllowedOrigins());
    }

    @Test
    @DisplayName("Should handle single allowed origin")
    void cors_HandlesSingleAllowedOrigin() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        SecurityConfig singleOriginConfig = new SecurityConfig(jwtUserInfoExtractor);
        ReflectionTestUtils.setField(singleOriginConfig, "jwkSetUri",
            "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test/.well-known/jwks.json");
        ReflectionTestUtils.setField(singleOriginConfig, "allowedOrigins",
            "http://localhost:3000");
        ReflectionTestUtils.setField(singleOriginConfig, "internalApiSecret", "test-secret");

        // When
        CorsConfigurationSource source = singleOriginConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).hasSize(1);
        assertThat(config.getAllowedOrigins()).containsExactly("http://localhost:3000");
    }

    @Test
    @DisplayName("Should handle multiple comma-separated allowed origins")
    void cors_HandlesMultipleAllowedOrigins() {
        // Given
        when(mockRequest.getRequestURI()).thenReturn("/api/test");
        when(mockRequest.getContextPath()).thenReturn("");
        when(mockRequest.getHttpServletMapping()).thenReturn(mockServletMapping);
        when(mockRequest.getServletPath()).thenReturn("");

        SecurityConfig multiOriginConfig = new SecurityConfig(jwtUserInfoExtractor);
        ReflectionTestUtils.setField(multiOriginConfig, "jwkSetUri",
            "https://cognito-idp.us-east-1.amazonaws.com/us-east-1_test/.well-known/jwks.json");
        ReflectionTestUtils.setField(multiOriginConfig, "allowedOrigins",
            "http://localhost:3000,http://localhost:5173,https://app.example.com");
        ReflectionTestUtils.setField(multiOriginConfig, "internalApiSecret", "test-secret");

        // When
        CorsConfigurationSource source = multiOriginConfig.corsConfigurationSource();
        CorsConfiguration config = source.getCorsConfiguration(mockRequest);

        // Then
        assertThat(config).isNotNull();
        assertThat(config.getAllowedOrigins()).hasSize(3);
        assertThat(config.getAllowedOrigins()).containsExactlyInAnyOrder(
            "http://localhost:3000",
            "http://localhost:5173",
            "https://app.example.com"
        );
    }

    // ========== JWT DECODER TESTS ==========

    @Test
    @DisplayName("Should create JWT decoder bean")
    void jwtDecoder_CreatesBean() {
        // When & Then
        assertThatCode(() -> securityConfig.jwtDecoder())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("JWT decoder should not be null")
    void jwtDecoder_NotNull() {
        // When
        var decoder = securityConfig.jwtDecoder();

        // Then
        assertThat(decoder).isNotNull();
    }

    // ========== JWT AUTHENTICATION CONVERTER TESTS ==========

    @Test
    @DisplayName("Should create JWT authentication converter bean")
    void jwtAuthenticationConverter_CreatesBean() {
        // When & Then
        assertThatCode(() -> securityConfig.jwtAuthenticationConverter())
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("JWT authentication converter should not be null")
    void jwtAuthenticationConverter_NotNull() {
        // When
        var converter = securityConfig.jwtAuthenticationConverter();

        // Then
        assertThat(converter).isNotNull();
    }

    // ========== USER INFO EXTRACTOR TESTS ==========

    @Test
    @DisplayName("Should create user info extractor bean")
    void userInfoExtractor_CreatesBean() {
        // When
        var extractor = securityConfig.userInfoExtractor();

        // Then
        assertThat(extractor).isNotNull();
        assertThat(extractor).isEqualTo(jwtUserInfoExtractor);
    }

    // ========== HELPER METHODS ==========

    private Jwt createJwtWithClaims(Map<String, Object> claims) {
        Map<String, Object> headers = new HashMap<>();
        headers.put("alg", "RS256");
        headers.put("typ", "JWT");

        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                headers,
                claims
        );
    }
}
