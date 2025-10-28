# Deployment Guide
Multi-Tenant SaaS Platform - Production Deployment

## Overview

This guide covers the automated deployment pipeline for the Multi-Tenant SaaS Platform using GitHub Actions, Terraform, AWS ECS, and CloudFront.

## Architecture

- **Frontend**: React SPA → S3 + CloudFront CDN
- **Backend**: Spring Boot API → ECS Fargate + Application Load Balancer
- **Database**: PostgreSQL RDS
- **Authentication**: AWS Cognito
- **Monitoring**: CloudWatch + X-Ray
- **Infrastructure**: Terraform (Infrastructure as Code)

## Prerequisites

### AWS Account Setup

1. **AWS Account**: Production AWS account with appropriate permissions
2. **Route53 Hosted Zone**: Domain configured in Route53
3. **S3 Bucket for Terraform State**:
   ```bash
   aws s3 mb s3://saas-platform-terraform-state --region us-east-1
   aws s3api put-bucket-versioning \
     --bucket saas-platform-terraform-state \
     --versioning-configuration Status=Enabled
   ```

4. **DynamoDB Table for State Locking**:
   ```bash
   aws dynamodb create-table \
     --table-name terraform-lock \
     --attribute-definitions AttributeName=LockID,AttributeType=S \
     --key-schema AttributeName=LockID,KeyType=HASH \
     --billing-mode PAY_PER_REQUEST \
     --region us-east-1
   ```

5. **ECR Repositories**:
   ```bash
   aws ecr create-repository --repository-name saas-platform-backend --region us-east-1
   aws ecr create-repository --repository-name saas-platform-frontend --region us-east-1
   ```

6. **IAM Role for GitHub Actions** (OIDC):
   ```bash
   # Create OIDC provider for GitHub
   aws iam create-open-id-connect-provider \
     --url https://token.actions.githubusercontent.com \
     --client-id-list sts.amazonaws.com \
     --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1

   # Create IAM role with trust policy for GitHub Actions
   # See infrastructure/terraform/iam-github-actions.tf for role definition
   ```

### GitHub Secrets Configuration

Configure the following secrets in GitHub repository settings (`Settings → Secrets and variables → Actions`):

#### Required Secrets

| Secret Name | Description | Example Value |
|------------|-------------|---------------|
| `AWS_DEPLOY_ROLE_ARN` | IAM role ARN for deployment | `arn:aws:iam::123456789012:role/github-actions-deploy` |
| `TF_STATE_BUCKET` | S3 bucket for Terraform state | `saas-platform-terraform-state` |
| `DB_PASSWORD` | RDS database master password | `SecurePassword123!` |
| `INTERNAL_API_SECRET` | Secret for Lambda-Backend auth | `random-secret-key-here` |
| `API_BASE_URL` | Backend API URL | `https://api.example.com` |
| `FRONTEND_URL` | Frontend URL | `https://example.com` |
| `COGNITO_USER_POOL_ID` | Cognito User Pool ID | `us-east-1_XXXXXXXXX` |
| `COGNITO_CLIENT_ID` | Cognito App Client ID | `1234567890abcdefghijk` |

#### Optional OAuth Secrets

| Secret Name | Description |
|------------|-------------|
| `GOOGLE_CLIENT_ID` | Google OAuth Client ID |
| `GOOGLE_CLIENT_SECRET` | Google OAuth Client Secret |
| `GITHUB_CLIENT_ID` | GitHub OAuth Client ID |
| `GITHUB_CLIENT_SECRET` | GitHub OAuth Client Secret |

## Deployment Workflow

### Automated Deployment

The deployment pipeline is triggered automatically on:
- **Push to `main` branch**: Deploys to production
- **Push to `staging` branch**: Deploys to staging environment
- **Pull Requests**: Runs tests and validation only

### Pipeline Stages

1. **Backend Build and Test**
   - Compiles Java 21 Spring Boot application
   - Runs JUnit tests with JaCoCo coverage
   - Uploads coverage reports to Codecov
   - Creates backend JAR artifact

2. **Frontend Build and Test**
   - Installs npm dependencies
   - Runs Jest tests with coverage
   - Builds production React bundle with Vite
   - Creates frontend dist artifact

3. **Terraform Validation**
   - Checks Terraform formatting
   - Validates Terraform configuration
   - Ensures infrastructure code quality

4. **Docker Build and Push**
   - Builds backend Docker image (multi-stage with Maven)
   - Builds frontend Docker image (multi-stage with Node + Nginx)
   - Pushes images to Amazon ECR
   - Tags images with commit SHA and environment

5. **Terraform Deploy**
   - Initializes Terraform with S3 backend
   - Plans infrastructure changes
   - Applies changes to AWS resources
   - Outputs resource identifiers

6. **Frontend Deployment**
   - Syncs built assets to S3 bucket
   - Sets cache headers (1 year for assets, no-cache for index.html)
   - Invalidates CloudFront cache

7. **Backend Deployment**
   - Forces ECS service update with new task definition
   - Waits for service stability
   - Performs rolling deployment with zero downtime

8. **Smoke Tests**
   - Tests backend health endpoint (`/actuator/health`)
   - Tests frontend availability
   - Validates deployment success

## Manual Deployment

### Initial Infrastructure Setup

1. **Configure Terraform Backend** (first-time only):
   ```bash
   cd infrastructure/terraform

   # Edit main.tf to uncomment backend configuration
   # Update bucket name and DynamoDB table

   terraform init
   ```

2. **Create terraform.tfvars**:
   ```hcl
   aws_region          = "us-east-1"
   environment         = "prod"
   project_name        = "saas-platform"
   domain_name         = "example.com"
   db_password         = "SecurePassword123!"
   internal_api_secret = "random-secret-key"

   # Optional OAuth
   google_client_id     = ""
   google_client_secret = ""
   github_client_id     = ""
   github_client_secret = ""
   ```

3. **Deploy Infrastructure**:
   ```bash
   terraform plan
   terraform apply
   ```

### Manual Docker Build and Push

```bash
# Login to ECR
aws ecr get-login-password --region us-east-1 | \
  docker login --username AWS --password-stdin <account-id>.dkr.ecr.us-east-1.amazonaws.com

# Build and push backend
cd backend
docker build -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/saas-platform-backend:latest .
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/saas-platform-backend:latest

# Build and push frontend
cd ../frontend
docker build -t <account-id>.dkr.ecr.us-east-1.amazonaws.com/saas-platform-frontend:latest .
docker push <account-id>.dkr.ecr.us-east-1.amazonaws.com/saas-platform-frontend:latest
```

### Manual Frontend Deployment to S3

```bash
cd frontend
npm run build

# Deploy to S3
aws s3 sync dist/ s3://<frontend-bucket-name>/ \
  --delete \
  --cache-control "public,max-age=31536000,immutable" \
  --exclude "index.html"

aws s3 cp dist/index.html s3://<frontend-bucket-name>/index.html \
  --cache-control "no-cache,no-store,must-revalidate"

# Invalidate CloudFront
aws cloudfront create-invalidation \
  --distribution-id <distribution-id> \
  --paths "/*"
```

### Manual ECS Service Update

```bash
# Force new deployment
aws ecs update-service \
  --cluster saas-platform-prod \
  --service saas-platform-backend \
  --force-new-deployment

# Monitor deployment
aws ecs wait services-stable \
  --cluster saas-platform-prod \
  --services saas-platform-backend
```

## Rollback Procedures

### Rolling Back Backend (ECS)

1. **Identify previous task definition**:
   ```bash
   aws ecs describe-services \
     --cluster saas-platform-prod \
     --services saas-platform-backend \
     --query 'services[0].taskDefinition'
   ```

2. **Update service to previous revision**:
   ```bash
   aws ecs update-service \
     --cluster saas-platform-prod \
     --service saas-platform-backend \
     --task-definition saas-platform-backend-prod:42
   ```

### Rolling Back Frontend (S3)

1. **S3 versioning** is enabled - restore previous version:
   ```bash
   aws s3api list-object-versions \
     --bucket <frontend-bucket> \
     --prefix index.html

   aws s3api copy-object \
     --bucket <frontend-bucket> \
     --copy-source <frontend-bucket>/index.html?versionId=<version-id> \
     --key index.html
   ```

2. **Invalidate CloudFront**:
   ```bash
   aws cloudfront create-invalidation \
     --distribution-id <distribution-id> \
     --paths "/*"
   ```

### Rolling Back Infrastructure (Terraform)

```bash
cd infrastructure/terraform
git checkout <previous-commit>
terraform plan
terraform apply
```

## Monitoring and Troubleshooting

### CloudWatch Logs

- **Backend API Logs**: `/aws/ecs/saas-platform-backend-prod`
- **Lambda Logs**: `/aws/lambda/<function-name>`
- **CloudFront Logs**: S3 bucket `saas-platform-cloudfront-logs-*`
- **ALB Access Logs**: S3 bucket `saas-platform-alb-logs-*`

### X-Ray Tracing

View distributed traces:
```bash
aws xray get-service-graph --start-time <start> --end-time <end>
```

Or use AWS Console: CloudWatch → X-Ray → Service Map

### Health Checks

- **Backend Health**: `https://api.example.com/actuator/health`
- **Frontend**: `https://example.com/health`

### Common Issues

#### 1. ECS Service Fails to Start

**Symptoms**: Tasks start and immediately stop

**Solution**:
```bash
# Check task stopped reason
aws ecs describe-tasks \
  --cluster saas-platform-prod \
  --tasks <task-id> \
  --query 'tasks[0].stoppedReason'

# Check CloudWatch logs
aws logs tail /aws/ecs/saas-platform-backend-prod --follow
```

#### 2. CloudFront Serving Stale Content

**Symptoms**: Frontend not updating after deployment

**Solution**:
```bash
# Force invalidation
aws cloudfront create-invalidation \
  --distribution-id <distribution-id> \
  --paths "/*"
```

#### 3. Database Connection Failures

**Symptoms**: Backend can't connect to RDS

**Solution**:
- Check security group rules
- Verify ECS tasks are in correct subnets
- Check RDS endpoint and credentials in Secrets Manager

```bash
# Test database connectivity from ECS task
aws ecs execute-command \
  --cluster saas-platform-prod \
  --task <task-id> \
  --container backend-api \
  --command "/bin/sh" \
  --interactive
```

#### 4. Terraform State Lock

**Symptoms**: `Error acquiring the state lock`

**Solution**:
```bash
# Force unlock (use with caution)
terraform force-unlock <lock-id>
```

## Cost Optimization

### Production Environment

- **ECS Fargate**: Use Fargate Spot (70% cost savings) for non-critical tasks
- **RDS**: Consider Aurora Serverless for variable workloads
- **CloudFront**: Use PriceClass_100 (North America + Europe only)
- **NAT Gateway**: Use NAT instances for dev/staging

### Scaling Considerations

- **Auto-scaling**: CPU target 70%, Memory target 80%
- **Min Tasks**: 2 (high availability)
- **Max Tasks**: 10 (adjust based on traffic)
- **Scale-in cooldown**: 5 minutes
- **Scale-out cooldown**: 1 minute

## Security Best Practices

1. **Secrets Management**: All secrets in AWS Secrets Manager
2. **IAM Roles**: Least privilege for ECS tasks and Lambda
3. **Encryption**: TLS 1.2+ for all traffic, encryption at rest
4. **Network**: Private subnets for ECS/RDS, public for ALB
5. **Container Security**: Non-root users, minimal images
6. **Monitoring**: CloudWatch alarms for security events

## Compliance

- **Constitution**: v1.0.1 compliant (Java 21, Spring Boot 3.5.7)
- **Coverage**: Target 80% for new features, minimum 48% overall
- **SLA**: 95% of API requests < 2 seconds
- **Availability**: 99.9% uptime target

## Support and Escalation

For deployment issues:
1. Check CloudWatch logs and X-Ray traces
2. Review GitHub Actions workflow logs
3. Consult this deployment guide
4. Escalate to platform team if unresolved

---

**Last Updated**: 2025-10-28
**Version**: 1.0.0
**Maintained by**: SaaS Platform Team
