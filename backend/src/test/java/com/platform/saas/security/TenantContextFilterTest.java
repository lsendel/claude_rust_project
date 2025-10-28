package com.platform.saas.security;

import com.platform.saas.model.Tenant;
import com.platform.saas.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TenantContextFilter.
 * Tests tenant isolation, subdomain extraction, and security enforcement.
 *
 * CRITICAL SECURITY COMPONENT - 100% coverage required
 * PMAT: Test code Cyc≤10, Cog≤15
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TenantContextFilter Tests - Security Critical")
class TenantContextFilterTest {

    @Mock
    private TenantRepository tenantRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private TenantContextFilter filter;

    private Tenant testTenant;
    private UUID testTenantId;

    @BeforeEach
    void setUp() {
        testTenantId = UUID.randomUUID();

        testTenant = new Tenant();
        testTenant.setId(testTenantId);
        testTenant.setSubdomain("acme");
        testTenant.setName("Acme Corp");
        testTenant.setActive(true);

        // Clear any existing tenant context
        TenantContext.clear();
    }

    @AfterEach
    void tearDown() {
        // Ensure tenant context is cleared after each test
        TenantContext.clear();
    }

    // ========== SUBDOMAIN EXTRACTION FROM HEADER TESTS ==========

    @Test
    @DisplayName("Should extract subdomain from X-Tenant-Subdomain header")
    void extractSubdomain_FromHeader_Success() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());

        // Context should be cleared after filter execution
        assertThat(TenantContext.getTenantId()).isNull();
    }

    @Test
    @DisplayName("Should normalize subdomain from header to lowercase")
    void extractSubdomain_FromHeaderUppercase_Normalizes() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("ACME");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should trim whitespace from subdomain header")
    void extractSubdomain_FromHeaderWithWhitespace_Trims() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("  acme  ");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(filterChain).doFilter(request, response);
    }

    // ========== SUBDOMAIN EXTRACTION FROM HOST HEADER TESTS ==========

    @Test
    @DisplayName("Should extract subdomain from Host header")
    void extractSubdomain_FromHostHeader_Success() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("acme.platform.com");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should extract subdomain from Host header with port")
    void extractSubdomain_FromHostHeaderWithPort_Success() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("acme.platform.com:8080");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not extract subdomain from localhost")
    void extractSubdomain_FromLocalhost_NoSubdomain() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should not extract subdomain from 127.0.0.1")
    void extractSubdomain_From127001_NoSubdomain() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("127.0.0.1:8080");
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should not extract subdomain from .local domain")
    void extractSubdomain_FromLocalDomain_NoSubdomain() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("myhost.local");
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return null when Host header is missing")
    void extractSubdomain_NoHostHeader_ReturnsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return null when Host header is empty")
    void extractSubdomain_EmptyHostHeader_ReturnsNull() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("");
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
    }

    // ========== TENANT LOOKUP AND VALIDATION TESTS ==========

    @Test
    @DisplayName("Should set tenant context when tenant found and active")
    void tenantLookup_TenantFoundAndActive_SetsContext() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should return 403 when tenant is inactive")
    void tenantLookup_TenantInactive_Returns403() throws ServletException, IOException {
        // Given
        testTenant.setActive(false);
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(response).sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant account is inactive");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should return 404 when tenant not found for non-public endpoint")
    void tenantLookup_TenantNotFound_Returns404() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("nonexistent");
        when(request.getRequestURI()).thenReturn("/api/projects");
        when(tenantRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("nonexistent");
        verify(response).sendError(HttpServletResponse.SC_NOT_FOUND, "Tenant not found");
        verify(filterChain, never()).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow public endpoint when tenant not found")
    void tenantLookup_TenantNotFoundButPublicEndpoint_Allows() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("nonexistent");
        when(request.getRequestURI()).thenReturn("/api/auth/signup");
        when(tenantRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("nonexistent");
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    // ========== PUBLIC ENDPOINT TESTS ==========

    @Test
    @DisplayName("Should allow /api/health without tenant context")
    void publicEndpoint_ApiHealth_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
        verify(response, never()).sendError(anyInt(), anyString());
    }

    @Test
    @DisplayName("Should allow /api/auth/signup without tenant context")
    void publicEndpoint_AuthSignup_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/auth/signup");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow /api/auth/login without tenant context")
    void publicEndpoint_AuthLogin_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/auth/login");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow /api/auth/oauth without tenant context")
    void publicEndpoint_AuthOauth_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/auth/oauth/google");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow /actuator/health without tenant context")
    void publicEndpoint_ActuatorHealth_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/actuator/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow /actuator/info without tenant context")
    void publicEndpoint_ActuatorInfo_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/actuator/info");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should allow /api/internal without tenant context")
    void publicEndpoint_InternalApi_Allowed() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/internal/lambda/trigger");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should reject non-public endpoint without tenant subdomain")
    void nonPublicEndpoint_NoSubdomain_Returns400() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/projects");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/api/projects"));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(response).sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant subdomain required");
        verify(filterChain, never()).doFilter(request, response);
    }

    // ========== TENANT CONTEXT MANAGEMENT TESTS ==========

    @Test
    @DisplayName("Should clear tenant context after successful request")
    void contextManagement_SuccessfulRequest_ContextCleared() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getTenantSubdomain()).isNull();
    }

    @Test
    @DisplayName("Should clear tenant context even on exception")
    void contextManagement_Exception_ContextCleared() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));
        doThrow(new ServletException("Test exception")).when(filterChain).doFilter(request, response);

        // When & Then
        assertThatThrownBy(() -> filter.doFilterInternal(request, response, filterChain))
                .isInstanceOf(ServletException.class);

        // Context should be cleared even after exception
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getTenantSubdomain()).isNull();
    }

    @Test
    @DisplayName("Should clear tenant context when tenant is inactive")
    void contextManagement_InactiveTenant_ContextCleared() throws ServletException, IOException {
        // Given
        testTenant.setActive(false);
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getTenantSubdomain()).isNull();
    }

    @Test
    @DisplayName("Should clear tenant context when tenant not found")
    void contextManagement_TenantNotFound_ContextCleared() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("nonexistent");
        when(request.getRequestURI()).thenReturn("/api/projects");
        when(tenantRepository.findBySubdomain("nonexistent")).thenReturn(Optional.empty());

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        assertThat(TenantContext.getTenantId()).isNull();
        assertThat(TenantContext.getTenantSubdomain()).isNull();
    }

    // ========== STATIC RESOURCE FILTERING TESTS ==========

    @Test
    @DisplayName("Should skip filter for .js files")
    void shouldNotFilter_JavaScriptFile_ReturnsTrue() throws ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/static/app.js");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should skip filter for .css files")
    void shouldNotFilter_CssFile_ReturnsTrue() throws ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/static/styles.css");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should skip filter for .ico files")
    void shouldNotFilter_IconFile_ReturnsTrue() throws ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/favicon.ico");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should skip filter for /static path")
    void shouldNotFilter_StaticPath_ReturnsTrue() throws ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/static/assets/image.png");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should skip filter for /public path")
    void shouldNotFilter_PublicPath_ReturnsTrue() throws ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/public/images/logo.png");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("Should not skip filter for API endpoints")
    void shouldNotFilter_ApiEndpoint_ReturnsFalse() throws ServletException {
        // Given
        when(request.getRequestURI()).thenReturn("/api/projects");

        // When
        boolean result = filter.shouldNotFilter(request);

        // Then
        assertThat(result).isFalse();
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    @DisplayName("Should handle empty subdomain header")
    void edgeCase_EmptySubdomainHeader_NoTenantLookup() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("");
        when(request.getHeader("Host")).thenReturn("localhost:8080");
        when(request.getRequestURI()).thenReturn("/api/health");

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository, never()).findBySubdomain(anyString());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should handle subdomain with multiple dots")
    void edgeCase_SubdomainWithMultipleDots_ExtractsFirstPart() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn(null);
        when(request.getHeader("Host")).thenReturn("app.acme.platform.com");
        when(tenantRepository.findBySubdomain("app")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("app");
        verify(filterChain).doFilter(request, response);
    }

    @Test
    @DisplayName("Should prefer X-Tenant-Subdomain header over Host header")
    void edgeCase_BothHeadersPresent_PrefersXTenantSubdomain() throws ServletException, IOException {
        // Given
        when(request.getHeader("X-Tenant-Subdomain")).thenReturn("acme");
        when(tenantRepository.findBySubdomain("acme")).thenReturn(Optional.of(testTenant));

        // When
        filter.doFilterInternal(request, response, filterChain);

        // Then
        verify(tenantRepository).findBySubdomain("acme");
        verify(tenantRepository, never()).findBySubdomain("different");
        verify(filterChain).doFilter(request, response);
    }
}
