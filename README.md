# Multi-Tenant SaaS Platform

A comprehensive multi-tenant project management platform with Spring Boot backend, React frontend, and AWS serverless automation.

## Overview

This platform enables organizations to manage projects, tasks, and team collaboration in a secure, scalable multi-tenant environment. Each organization (tenant) has complete data isolation, role-based access control, and customizable automation rules.

## Features

### Core Capabilities

- **Multi-Tenant Architecture**: Complete data isolation with subdomain-based tenant routing
- **OAuth2 Authentication**: Social login via Google, Facebook, and GitHub using AWS Cognito
- **Project Management**: Create, organize, and track projects and tasks
- **Team Collaboration**: Invite team members with role-based permissions (Admin, Editor, Viewer)
- **Automation Rules**: Configure event-driven workflows triggered by project/task events
- **Subscription Tiers**: Free (50 projects), Pro (1,000 projects), Enterprise (unlimited)

### Security & Compliance

- Complete tenant data isolation
- Role-Based Access Control (RBAC)
- Audit logging for all security events
- OAuth2 authentication with JWT tokens
- Penetration testing validated

## Technology Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.5.7
- **Database**: PostgreSQL 15 (AWS RDS)
- **Authentication**: AWS Cognito + Spring Security
- **Testing**: JUnit 5, Mockito, Testcontainers 1.21.3
- **Code Coverage**: JaCoCo 0.8.14 (28% overall, 100% TenantService)

### Frontend
- **Language**: TypeScript 5.x
- **Framework**: React 18.2+
- **Build Tool**: Vite
- **UI Library**: Material-UI / Chakra UI
- **State Management**: React Context API

### Infrastructure
- **Cloud**: AWS (us-east-1)
- **IaC**: Terraform 1.5+
- **Functions**: AWS Lambda (Node.js 18)
- **Events**: Amazon EventBridge
- **CI/CD**: GitHub Actions

## Project Structure

```
├── backend/                  # Spring Boot REST API
│   ├── src/main/java/
│   │   └── com/platform/saas/
│   │       ├── config/       # Security, Cognito, JPA
│   │       ├── controller/   # REST endpoints
│   │       ├── service/      # Business logic
│   │       ├── repository/   # Data access
│   │       ├── model/        # Domain entities
│   │       └── security/     # Authentication
│   └── src/main/resources/
│       └── db/migration/     # Flyway migrations
│
├── frontend/                 # React TypeScript SPA
│   ├── src/
│   │   ├── components/       # Reusable components
│   │   ├── pages/            # Page components
│   │   ├── services/         # API client
│   │   ├── hooks/            # Custom React hooks
│   │   └── context/          # Global state
│   └── public/
│
├── lambda-functions/         # AWS Lambda serverless functions
│   ├── automation-engine/    # Event-driven automation
│   └── cognito-triggers/     # Auth hooks
│
├── infrastructure/           # Infrastructure as Code
│   └── terraform/            # AWS resource definitions
│       ├── cognito.tf        # User pool configuration
│       ├── rds.tf            # PostgreSQL database
│       ├── vpc.tf            # Network configuration
│       └── s3-cloudfront.tf  # Frontend hosting
│
└── specs/                    # Feature specifications
    └── 001-saas-platform/    # Current feature docs
