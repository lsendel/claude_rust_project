package com.platform.saas.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for OAuth2 authentication with AWS Cognito.
 * Configures JWT token validation, CORS, and endpoint authorization rules.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOrigins;

    /**
     * Configure security filter chain with OAuth2 resource server and JWT validation.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF disabled for API endpoints (using JWT tokens)
                .csrf(csrf -> csrf.disable())

                // Enable CORS with configured origins
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless session management (JWT-based)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no authentication required
                        .requestMatchers(
                                "/api/tenants",                    // Tenant registration
                                "/api/tenants/subdomain/**",       // Subdomain lookup
                                "/api/tenants/validate-subdomain", // Subdomain validation
                                "/api/internal/**",                // Internal API (secured by secret header)
                                "/actuator/health",                // Health check
                                "/actuator/info",                  // Application info
                                "/error"                           // Error handling
                        ).permitAll()

                        // OAuth2 callback endpoints
                        .requestMatchers("/oauth/**", "/login/oauth2/**").permitAll()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Configure OAuth2 Resource Server with JWT
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt.decoder(jwtDecoder()))
                );

        return http.build();
    }

    /**
     * Configure JWT decoder for validating Cognito tokens.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * Configure CORS to allow frontend origin.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Parse allowed origins from configuration
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);

        // Allow common HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Allow common headers
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Tenant-ID", "X-Requested-With"));

        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);

        // Expose headers that client can access
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count", "X-Page-Count"));

        // Cache preflight response for 1 hour
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);

        return source;
    }
}
