package com.platform.saas.security;

import com.platform.saas.model.Tenant;
import com.platform.saas.repository.TenantRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;

/**
 * Servlet filter that extracts tenant context from the request subdomain.
 *
 * This filter:
 * 1. Extracts the subdomain from the request host (e.g., "acme" from "acme.platform.com")
 * 2. Looks up the tenant in the database by subdomain
 * 3. Stores the tenant ID in TenantContext for the duration of the request
 * 4. Clears the context after request processing
 *
 * The tenant context is then available to all downstream components via TenantContext.getTenantId().
 *
 * URL Format:
 * - Production: https://{subdomain}.platform.com/api/...
 * - Development: http://{subdomain}.localhost:3000/api/...
 * - Localhost: http://localhost:8080/api/... (tenant extracted from X-Tenant-Subdomain header)
 */
@Component
@Slf4j
public class TenantContextFilter extends OncePerRequestFilter {

    @Autowired
    private TenantRepository tenantRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // Extract subdomain from request
            String subdomain = extractSubdomain(request);

            if (subdomain != null && !subdomain.isEmpty()) {
                log.debug("Processing request for subdomain: {}", subdomain);

                // Lookup tenant by subdomain
                Optional<Tenant> tenantOpt = tenantRepository.findBySubdomain(subdomain);

                if (tenantOpt.isPresent()) {
                    Tenant tenant = tenantOpt.get();

                    // Check if tenant is active
                    if (!tenant.isActive()) {
                        log.warn("Tenant is inactive: {}", subdomain);
                        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Tenant account is inactive");
                        return;
                    }

                    // Set tenant context
                    TenantContext.setTenantId(tenant.getId());
                    TenantContext.setTenantSubdomain(subdomain);

                    log.debug("Tenant context set: tenantId={}, subdomain={}", tenant.getId(), subdomain);
                } else {
                    // Tenant not found
                    log.warn("Tenant not found for subdomain: {}", subdomain);

                    // Allow public endpoints to proceed without tenant context
                    if (isPublicEndpoint(request)) {
                        log.debug("Public endpoint accessed without tenant context: {}", request.getRequestURI());
                    } else {
                        response.sendError(HttpServletResponse.SC_NOT_FOUND, "Tenant not found");
                        return;
                    }
                }
            } else {
                // No subdomain found
                log.debug("No subdomain extracted from request: {}", request.getRequestURL());

                // Allow public endpoints
                if (!isPublicEndpoint(request)) {
                    log.warn("Non-public endpoint accessed without tenant subdomain: {}", request.getRequestURI());
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Tenant subdomain required");
                    return;
                }
            }

            // Continue filter chain
            filterChain.doFilter(request, response);

        } finally {
            // Always clear tenant context after request processing
            TenantContext.clear();
        }
    }

    /**
     * Extract subdomain from HTTP request.
     *
     * Extraction strategies:
     * 1. Check X-Tenant-Subdomain header (for localhost development)
     * 2. Extract from Host header subdomain (for production)
     *
     * @param request The HTTP request
     * @return The subdomain, or null if not found
     */
    private String extractSubdomain(HttpServletRequest request) {
        // Strategy 1: Check X-Tenant-Subdomain header (for local development)
        String subdomainHeader = request.getHeader("X-Tenant-Subdomain");
        if (subdomainHeader != null && !subdomainHeader.isEmpty()) {
            log.debug("Subdomain extracted from X-Tenant-Subdomain header: {}", subdomainHeader);
            return subdomainHeader.toLowerCase().trim();
        }

        // Strategy 2: Extract from Host header
        String host = request.getHeader("Host");
        if (host == null || host.isEmpty()) {
            log.debug("No Host header present in request");
            return null;
        }

        // Remove port if present (e.g., localhost:8080 -> localhost)
        String hostWithoutPort = host.split(":")[0];

        // Split by dots to get subdomain
        String[] parts = hostWithoutPort.split("\\.");

        // Check if subdomain exists
        // Example: acme.platform.com -> ["acme", "platform", "com"]
        // Example: localhost -> ["localhost"]
        if (parts.length >= 2) {
            // Skip common development hosts
            if (hostWithoutPort.equals("localhost") ||
                hostWithoutPort.equals("127.0.0.1") ||
                hostWithoutPort.endsWith(".local")) {
                log.debug("Development host detected, no subdomain: {}", hostWithoutPort);
                return null;
            }

            // First part is the subdomain
            String subdomain = parts[0].toLowerCase().trim();
            log.debug("Subdomain extracted from Host header: {} (host: {})", subdomain, host);
            return subdomain;
        }

        log.debug("No subdomain found in Host header: {}", host);
        return null;
    }

    /**
     * Check if the request is for a public endpoint that doesn't require tenant context.
     *
     * @param request The HTTP request
     * @return true if endpoint is public, false otherwise
     */
    private boolean isPublicEndpoint(HttpServletRequest request) {
        String uri = request.getRequestURI();

        // Public endpoints
        return uri.startsWith("/api/health") ||
               uri.startsWith("/api/auth/signup") ||
               uri.startsWith("/api/auth/login") ||
               uri.startsWith("/api/auth/oauth") ||
               uri.startsWith("/actuator/health") ||
               uri.startsWith("/actuator/info") ||
               uri.startsWith("/api/internal"); // Internal Lambda API
    }

    /**
     * Skip filter for static resources.
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/static") ||
               path.startsWith("/public") ||
               path.endsWith(".js") ||
               path.endsWith(".css") ||
               path.endsWith(".ico");
    }
}
