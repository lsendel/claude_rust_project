package com.platform.saas.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Security configuration for the Multi-Tenant SaaS Platform.
 * Configures JWT authentication, CORS, and request authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    @Value("${app.api.internal-secret}")
    private String internalApiSecret;

    private final JwtUserInfoExtractor jwtUserInfoExtractor;

    /**
     * Configure HTTP security with JWT authentication.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless API
            .csrf(csrf -> csrf.disable())

            // Configure CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // Stateless session management
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Add tenant context filter before authentication
            .addFilterBefore(new TenantContextFilter(), UsernamePasswordAuthenticationFilter.class)

            // Configure authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints
                .requestMatchers(
                    "/api/health",
                    "/api/auth/signup",
                    "/api/auth/login",
                    "/api/auth/oauth/**",
                    "/actuator/health",
                    "/actuator/info"
                ).permitAll()

                // Internal API endpoints (Lambda triggers)
                .requestMatchers("/api/internal/**")
                    .hasAuthority("INTERNAL_API")

                // All other endpoints require authentication
                .anyRequest().authenticated()
            )

            // Configure JWT authentication
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder())
                    .jwtAuthenticationConverter(jwtAuthenticationConverter())
                )
            );

        return http.build();
    }

    /**
     * JWT Decoder bean for validating JWT tokens.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withJwkSetUri(jwkSetUri).build();
    }

    /**
     * JWT Authentication Converter that extracts user information and authorities.
     */
    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter());
        converter.setPrincipalClaimName("sub"); // Use 'sub' claim as principal
        return converter;
    }

    /**
     * Extract authorities from JWT token.
     * Combines Cognito groups with custom authorities.
     */
    @Bean
    public Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter() {
        JwtGrantedAuthoritiesConverter defaultConverter = new JwtGrantedAuthoritiesConverter();
        defaultConverter.setAuthoritiesClaimName("cognito:groups");
        defaultConverter.setAuthorityPrefix("ROLE_");

        return jwt -> {
            // Get default authorities from cognito:groups
            Collection<GrantedAuthority> authorities = defaultConverter.convert(jwt);

            // Extract custom claims for additional authorities
            List<String> customAuthorities = jwt.getClaimAsStringList("custom:authorities");

            if (customAuthorities != null && !customAuthorities.isEmpty()) {
                Collection<GrantedAuthority> customGrantedAuthorities = customAuthorities.stream()
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toList());

                // Combine authorities
                authorities = Stream.concat(
                    authorities.stream(),
                    customGrantedAuthorities.stream()
                ).collect(Collectors.toList());
            }

            return authorities;
        };
    }

    /**
     * CORS configuration to allow requests from frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(allowedOrigins.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Extract user information from JWT for use in controllers.
     * This can be injected as a dependency.
     */
    @Bean
    public JwtUserInfoExtractor userInfoExtractor() {
        return jwtUserInfoExtractor;
    }
}
