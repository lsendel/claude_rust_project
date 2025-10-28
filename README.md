# Multi-Tenant SaaS Platform
**Enterprise-Grade Project Management Platform with AWS Cloud Deployment**

[![Build Status](https://github.com/yourusername/saas-platform/workflows/CI/badge.svg)](https://github.com/yourusername/saas-platform/actions)
[![Coverage](https://codecov.io/gh/yourusername/saas-platform/branch/main/graph/badge.svg)](https://codecov.io/gh/yourusername/saas-platform)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE-MIT)
[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-green.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18+-blue.svg)](https://reactjs.org/)

## Overview

A production-ready, multi-tenant SaaS platform for project and task management built with modern cloud-native technologies. Features include tenant isolation, role-based access control, automation rules, and comprehensive AWS deployment infrastructure.

### Key Features

- ✅ **Multi-Tenant Architecture**: Complete tenant isolation with subdomain routing
- 🔐 **OAuth2 Authentication**: Integration with AWS Cognito (Google, Facebook, GitHub)
- 👥 **Role-Based Access Control**: Admin, Editor, and Viewer roles with fine-grained permissions
- 📊 **Project & Task Management**: Full CRUD operations with dependencies and assignees
- 🤖 **Automation Engine**: Event-driven rules for notifications and webhooks
- 📧 **Email Invitations**: Team member invitation system with AWS SES
- 💰 **Quota Management**: Tenant-specific usage limits with upgrade paths
- ☁️ **Cloud-Native**: Complete AWS infrastructure as code with Terraform
- 📈 **Observability**: CloudWatch logging, X-Ray tracing, and metrics
- 🚀 **CI/CD**: Automated GitHub Actions pipelines for deployment
- 🐳 **Containerized**: Docker images for backend (Spring Boot) and frontend (React + Nginx)
- 🔄 **Auto-Scaling**: ECS Fargate with CPU, memory, and request-based scaling
- 🌍 **CDN**: CloudFront distribution with wildcard SSL certificates

## Quick Start

### Local Development (5 Minutes)

```bash
# 1. Clone the repository
git clone https://github.com/yourusername/saas-platform.git
cd saas-platform

# 2. Start PostgreSQL
docker-compose up -d postgres

# 3. Start backend (in one terminal)
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 4. Start frontend (in another terminal)
cd frontend
npm install
npm run dev

# 5. Open browser
# Frontend: http://localhost:5173
# Backend API: http://localhost:8080
# Health Check: http://localhost:8080/actuator/health
```

**📖 For detailed local setup, see [QUICKSTART.md](QUICKSTART.md)**

## Deployment

### AWS Deployment (Production)

```bash
# One-time setup
./scripts/setup-aws-prerequisites.sh

# Verify deployment readiness
./scripts/pre-deployment-check.sh

# Deploy to environment
./scripts/deploy.sh prod

# Rollback if needed
./scripts/rollback.sh prod all
```

**📖 For detailed deployment instructions, see [infrastructure/DEPLOYMENT.md](infrastructure/DEPLOYMENT.md)**

## Architecture

### Technology Stack

| Component | Technology | Version |
|-----------|-----------|---------|
| **Backend** | Java + Spring Boot | 21 LTS / 3.5.7 |
| **Frontend** | React + TypeScript + Vite | 18+ / 5.x / Latest |
| **Database** | PostgreSQL | 15+ |
| **Authentication** | AWS Cognito | Latest |
| **Container** | Docker + ECS Fargate | Latest |
| **CDN** | CloudFront | Latest |
| **IaC** | Terraform | 1.5+ |
| **CI/CD** | GitHub Actions | Latest |
| **Monitoring** | CloudWatch + X-Ray | Latest |

### System Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                      CloudFront CDN                         │
│                  (*.example.com SSL)                        │
└─────────────────┬───────────────────────────┬───────────────┘
                  │                           │
                  │ (Frontend)                │ (Backend API)
                  ▼                           ▼
          ┌───────────────┐           ┌──────────────────┐
          │  S3 Bucket    │           │      ALB         │
          │  (React SPA)  │           │  (HTTPS/HTTP)    │
          └───────────────┘           └────────┬─────────┘
                                               │
                                               ▼
                                    ┌──────────────────────┐
                                    │   ECS Fargate        │
                                    │  (Spring Boot API)   │
                                    │  Auto-Scaling 2-10   │
                                    └──────────┬───────────┘
                                               │
                      ┌────────────────────────┼────────────────────┐
                      ▼                        ▼                    ▼
              ┌──────────────┐       ┌─────────────┐      ┌────────────┐
              │     RDS      │       │   Cognito   │      │ EventBridge│
              │ (PostgreSQL) │       │   (OAuth2)  │      │  (Events)  │
              └──────────────┘       └─────────────┘      └────────────┘
```

## Project Structure

```
saas-platform/
├── backend/                    # Spring Boot backend API
│   ├── src/main/java/com/platform/saas/
│   │   ├── controller/        # REST API endpoints
│   │   ├── service/           # Business logic
│   │   ├── model/             # JPA entities
│   │   ├── repository/        # Data access layer
│   │   ├── security/          # Authentication & tenant context
│   │   └── config/            # Spring configuration
│   ├── Dockerfile             # Multi-stage Docker build
│   └── pom.xml                # Maven dependencies
│
├── frontend/                  # React + TypeScript frontend
│   ├── src/
│   │   ├── components/        # Reusable UI components
│   │   ├── pages/             # Page components
│   │   ├── services/          # API client services
│   │   └── context/           # React context (auth, tenant)
│   ├── Dockerfile             # Multi-stage Docker build
│   ├── nginx.conf             # Nginx configuration for SPA
│   └── package.json           # npm dependencies
│
├── infrastructure/            # AWS infrastructure as code
│   └── terraform/
│       ├── vpc.tf             # VPC, subnets, security groups
│       ├── rds.tf             # PostgreSQL database
│       ├── cognito.tf         # Authentication
│       ├── ecs.tf             # Container orchestration
│       ├── s3-cloudfront.tf   # Frontend hosting
│       ├── monitoring.tf      # CloudWatch, X-Ray
│       ├── iam.tf             # IAM roles and policies
│       └── secrets.tf         # Secrets Manager
│
├── lambda-functions/          # AWS Lambda functions
│   ├── cognito-triggers/      # Post-authentication triggers
│   └── automation-engine/     # Event-driven automation
│
├── scripts/                   # Deployment and utility scripts
│   ├── pre-deployment-check.sh     # Validate prerequisites
│   ├── setup-aws-prerequisites.sh  # One-time AWS setup
│   ├── deploy.sh                   # Full deployment
│   └── rollback.sh                 # Rollback to previous version
│
├── .github/workflows/         # CI/CD pipelines
│   ├── deploy.yml             # Production deployment
│   ├── backend-ci.yml         # Backend tests and build
│   └── frontend-ci.yml        # Frontend tests and build
│
├── docs/                      # Documentation
│   ├── DEPLOYMENT.md          # Deployment guide
│   ├── VALIDATION.md          # Infrastructure validation
│   └── QUICKSTART.md          # Local development setup
│
└── README.md                  # This file
```

## API Documentation

### REST API Endpoints

| Endpoint | Method | Description | Auth |
|----------|--------|-------------|------|
| `/api/tenants` | POST | Create new tenant | Public |
| `/api/tenants/{id}` | GET | Get tenant details | Tenant Admin |
| `/api/projects` | GET | List projects | Authenticated |
| `/api/projects` | POST | Create project | Admin/Editor |
| `/api/projects/{id}` | PUT | Update project | Admin/Editor |
| `/api/tasks` | GET | List tasks | Authenticated |
| `/api/tasks` | POST | Create task | Admin/Editor |
| `/api/users/invite` | POST | Invite user to tenant | Admin |
| `/api/automation-rules` | GET | List automation rules | Admin |
| `/api/automation-rules` | POST | Create automation rule | Admin |

### Authentication

All API requests (except `/api/tenants` POST) require:

1. **JWT Token** from AWS Cognito in `Authorization` header
2. **Tenant Subdomain** in `X-Tenant-Subdomain` header

Example:

```bash
curl https://api.example.com/api/projects \
  -H "Authorization: Bearer eyJhbGciOiJSUzI1NiIs..." \
  -H "X-Tenant-Subdomain: acme"
```

## Testing

### Backend Tests

```bash
cd backend
mvn test                    # Run all tests
mvn test jacoco:report      # Generate coverage report
```

**Coverage Target**: 80% for new features, minimum 48% overall

### Frontend Tests

```bash
cd frontend
npm test                    # Run all tests
npm test -- --coverage      # Generate coverage report
```

## Monitoring

### CloudWatch Dashboards

- **Performance**: API response times, request counts
- **Errors**: Error rates, failed authentications
- **Resources**: ECS CPU/memory, RDS connections

### Alarms

- API p95 response time > 2 seconds
- Error rate > 1%
- Failed authentication attempts > 10/min

### Logs

```bash
# View backend logs
aws logs tail /aws/ecs/saas-platform-backend-prod --follow

# View Lambda logs
aws logs tail /aws/lambda/automation-engine --follow
```

## Security

### Authentication & Authorization

- **OAuth2** via AWS Cognito (Google, Facebook, GitHub)
- **JWT tokens** for API authentication
- **Role-Based Access Control** (Admin, Editor, Viewer)
- **Tenant isolation** enforced at database and application layers

### Data Protection

- **Encryption at rest**: S3, RDS, Secrets Manager
- **Encryption in transit**: TLS 1.2+ everywhere
- **Secrets management**: AWS Secrets Manager
- **Database backups**: Automated daily snapshots (30-day retention)

### Network Security

- **Private subnets** for ECS and RDS
- **Security groups** with least privilege rules
- **VPC Flow Logs** for network monitoring

## Contributing

We welcome contributions! Please follow these steps:

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

See [CONTRIBUTING.md](CONTRIBUTING.md) for detailed guidelines.

## License

This project is dual-licensed under:

- **MIT License** - See [LICENSE-MIT](LICENSE-MIT)
- **Apache License 2.0** - See [LICENSE-APACHE](LICENSE-APACHE)

## Support

- **Documentation**: See [docs/](docs/) directory
- **Issues**: https://github.com/yourusername/saas-platform/issues
- **Email**: support@yourcompany.com

## Project Status

**Status**: ✅ **Production Ready**

- ✅ Phase 1: Project Setup (Complete)
- ✅ Phase 2: Foundational Infrastructure (Complete)
- ✅ Phase 3: Tenant Sign-Up (Complete)
- ✅ Phase 4: Project/Task Management (Complete)
- ✅ Phase 5: User Invitations & RBAC (Complete)
- ✅ Phase 6: Automation Rules (Complete)
- ✅ Phase 7: Production Deployment (Complete)

**Implementation**: 85/87 tasks complete (97.7%)

---

**Built with ❤️ by the SaaS Platform Team**

For questions or support, please open an issue or contact us at support@yourcompany.com.
