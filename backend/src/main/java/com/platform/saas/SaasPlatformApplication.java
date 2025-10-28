package com.platform.saas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main application class for the Multi-Tenant SaaS Platform.
 *
 * This Spring Boot application provides a REST API for multi-tenant project management
 * with OAuth2 authentication, role-based access control, and event-driven automation.
 */
@SpringBootApplication
public class SaasPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(SaasPlatformApplication.class, args);
    }
}
