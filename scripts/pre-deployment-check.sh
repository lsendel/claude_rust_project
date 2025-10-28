#!/bin/bash
# Pre-Deployment Validation Script
# Multi-Tenant SaaS Platform - Deployment Readiness Check

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Counters
ERRORS=0
WARNINGS=0
PASSED=0

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
    ((PASSED++))
}

print_error() {
    echo -e "${RED}✗${NC} $1"
    ((ERRORS++))
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
    ((WARNINGS++))
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

check_command() {
    local cmd=$1
    local name=$2
    local required=$3

    if command -v "$cmd" &> /dev/null; then
        local version=$($cmd --version 2>&1 | head -n 1)
        print_success "$name is installed: $version"
        return 0
    else
        if [ "$required" = "true" ]; then
            print_error "$name is not installed (required)"
        else
            print_warning "$name is not installed (optional)"
        fi
        return 1
    fi
}

check_file() {
    local file=$1
    local name=$2
    local required=$3

    if [ -f "$file" ]; then
        print_success "$name exists: $file"
        return 0
    else
        if [ "$required" = "true" ]; then
            print_error "$name not found: $file (required)"
        else
            print_warning "$name not found: $file (optional)"
        fi
        return 1
    fi
}

check_directory() {
    local dir=$1
    local name=$2

    if [ -d "$dir" ]; then
        print_success "$name exists: $dir"
        return 0
    else
        print_error "$name not found: $dir"
        return 1
    fi
}

check_env_var() {
    local var=$1
    local name=$2
    local required=$3

    if [ -n "${!var}" ]; then
        print_success "$name is set: $var"
        return 0
    else
        if [ "$required" = "true" ]; then
            print_error "$name is not set: $var (required)"
        else
            print_warning "$name is not set: $var (optional)"
        fi
        return 1
    fi
}

##############################################################################
# Main Validation
##############################################################################

print_header "Multi-Tenant SaaS Platform - Pre-Deployment Check"
echo ""

# Check project root
if [ ! -f "package.json" ] && [ ! -f "pom.xml" ] && [ ! -d "infrastructure" ]; then
    print_error "Not running from project root. Please cd to project root."
    exit 1
fi

##############################################################################
# 1. Required Tools
##############################################################################

print_header "1. Required Tools"

check_command "terraform" "Terraform" "true"
check_command "docker" "Docker" "true"
check_command "aws" "AWS CLI" "true"
check_command "node" "Node.js" "true"
check_command "npm" "npm" "true"
check_command "java" "Java" "true"
check_command "mvn" "Maven" "true"
check_command "git" "Git" "true"

echo ""

##############################################################################
# 2. Optional Tools
##############################################################################

print_header "2. Optional Tools (Recommended)"

check_command "gh" "GitHub CLI" "false"
check_command "jq" "jq (JSON processor)" "false"
check_command "curl" "curl" "false"
check_command "psql" "PostgreSQL client" "false"

echo ""

##############################################################################
# 3. Project Structure
##############################################################################

print_header "3. Project Structure"

check_directory "backend" "Backend directory"
check_directory "frontend" "Frontend directory"
check_directory "infrastructure/terraform" "Terraform directory"
check_directory "lambda-functions" "Lambda functions directory"
check_directory ".github/workflows" "GitHub workflows directory"

echo ""

##############################################################################
# 4. Infrastructure Files
##############################################################################

print_header "4. Infrastructure Files"

check_file "infrastructure/terraform/main.tf" "Terraform main configuration" "true"
check_file "infrastructure/terraform/variables.tf" "Terraform variables" "true"
check_file "infrastructure/terraform/vpc.tf" "VPC configuration" "true"
check_file "infrastructure/terraform/rds.tf" "RDS configuration" "true"
check_file "infrastructure/terraform/cognito.tf" "Cognito configuration" "true"
check_file "infrastructure/terraform/ecs.tf" "ECS configuration" "true"
check_file "infrastructure/terraform/s3-cloudfront.tf" "S3/CloudFront configuration" "true"
check_file "infrastructure/terraform/monitoring.tf" "Monitoring configuration" "true"
check_file "infrastructure/terraform/iam.tf" "IAM roles" "true"
check_file "infrastructure/terraform/secrets.tf" "Secrets Manager" "true"

echo ""

##############################################################################
# 5. Docker Files
##############################################################################

print_header "5. Docker Files"

check_file "backend/Dockerfile" "Backend Dockerfile" "true"
check_file "frontend/Dockerfile" "Frontend Dockerfile" "true"
check_file "frontend/nginx.conf" "Nginx configuration" "true"
check_file "docker-compose.yml" "Docker Compose (local dev)" "false"

echo ""

##############################################################################
# 6. CI/CD Files
##############################################################################

print_header "6. CI/CD Files"

check_file ".github/workflows/deploy.yml" "Deployment workflow" "true"
check_file ".github/workflows/backend-ci.yml" "Backend CI workflow" "false"
check_file ".github/workflows/frontend-ci.yml" "Frontend CI workflow" "false"

echo ""

##############################################################################
# 7. Documentation
##############################################################################

print_header "7. Documentation"

check_file "infrastructure/DEPLOYMENT.md" "Deployment guide" "true"
check_file "infrastructure/VALIDATION.md" "Validation checklist" "true"
check_file "PHASE7_COMPLETION_REPORT.md" "Phase 7 completion report" "false"
check_file "README.md" "Project README" "false"

echo ""

##############################################################################
# 8. AWS Configuration
##############################################################################

print_header "8. AWS Configuration"

# Check if AWS CLI is configured
if aws sts get-caller-identity &> /dev/null; then
    AWS_ACCOUNT=$(aws sts get-caller-identity --query Account --output text 2>/dev/null)
    AWS_USER=$(aws sts get-caller-identity --query Arn --output text 2>/dev/null)
    print_success "AWS CLI is configured"
    print_info "Account ID: $AWS_ACCOUNT"
    print_info "User/Role: $AWS_USER"
else
    print_error "AWS CLI is not configured or credentials are invalid"
    print_info "Run: aws configure"
fi

# Check AWS region
AWS_REGION=$(aws configure get region 2>/dev/null || echo "")
if [ -n "$AWS_REGION" ]; then
    print_success "AWS region is set: $AWS_REGION"
else
    print_warning "AWS region is not set (will use us-east-1 default)"
fi

echo ""

##############################################################################
# 9. Terraform State Backend
##############################################################################

print_header "9. Terraform State Backend"

print_info "Checking if S3 bucket exists for Terraform state..."

# Extract bucket name from backend config or use default
STATE_BUCKET="saas-platform-terraform-state"

if aws s3 ls "s3://$STATE_BUCKET" &> /dev/null; then
    print_success "Terraform state bucket exists: $STATE_BUCKET"
else
    print_warning "Terraform state bucket does not exist: $STATE_BUCKET"
    print_info "Create with: aws s3 mb s3://$STATE_BUCKET --region us-east-1"
fi

# Check DynamoDB table for state locking
if aws dynamodb describe-table --table-name terraform-lock &> /dev/null 2>&1; then
    print_success "DynamoDB table exists: terraform-lock"
else
    print_warning "DynamoDB table for state locking does not exist: terraform-lock"
    print_info "Create with: aws dynamodb create-table --table-name terraform-lock --attribute-definitions AttributeName=LockID,AttributeType=S --key-schema AttributeName=LockID,KeyType=HASH --billing-mode PAY_PER_REQUEST"
fi

echo ""

##############################################################################
# 10. ECR Repositories
##############################################################################

print_header "10. ECR Repositories"

if aws ecr describe-repositories --repository-names saas-platform-backend &> /dev/null 2>&1; then
    print_success "ECR repository exists: saas-platform-backend"
else
    print_warning "ECR repository does not exist: saas-platform-backend"
    print_info "Create with: aws ecr create-repository --repository-name saas-platform-backend"
fi

if aws ecr describe-repositories --repository-names saas-platform-frontend &> /dev/null 2>&1; then
    print_success "ECR repository exists: saas-platform-frontend"
else
    print_warning "ECR repository does not exist: saas-platform-frontend"
    print_info "Create with: aws ecr create-repository --repository-name saas-platform-frontend"
fi

echo ""

##############################################################################
# 11. Route53 Hosted Zone
##############################################################################

print_header "11. Route53 Hosted Zone"

print_info "Checking for Route53 hosted zones..."
HOSTED_ZONES=$(aws route53 list-hosted-zones --query "HostedZones[*].Name" --output text 2>/dev/null || echo "")

if [ -n "$HOSTED_ZONES" ]; then
    print_success "Route53 hosted zones found:"
    for zone in $HOSTED_ZONES; do
        print_info "  - $zone"
    done
else
    print_warning "No Route53 hosted zones found"
    print_info "Create a hosted zone for your domain before deployment"
fi

echo ""

##############################################################################
# 12. GitHub Secrets (if gh CLI is available)
##############################################################################

if command -v gh &> /dev/null; then
    print_header "12. GitHub Secrets"

    # Check if we're in a git repo with remote
    if git remote get-url origin &> /dev/null; then
        REPO=$(git remote get-url origin | sed 's/.*github.com[:/]\(.*\)\.git/\1/')
        print_info "Checking GitHub secrets for: $REPO"

        # List of required secrets
        REQUIRED_SECRETS=(
            "AWS_DEPLOY_ROLE_ARN"
            "TF_STATE_BUCKET"
            "DB_PASSWORD"
            "INTERNAL_API_SECRET"
            "API_BASE_URL"
            "FRONTEND_URL"
            "COGNITO_USER_POOL_ID"
            "COGNITO_CLIENT_ID"
        )

        for secret in "${REQUIRED_SECRETS[@]}"; do
            if gh secret list | grep -q "^$secret"; then
                print_success "GitHub secret exists: $secret"
            else
                print_warning "GitHub secret not set: $secret"
            fi
        done
    else
        print_warning "Not in a git repository with remote"
    fi
else
    print_info "GitHub CLI not installed - skipping GitHub secrets check"
fi

echo ""

##############################################################################
# 13. Docker Build Test
##############################################################################

print_header "13. Docker Build Test (Optional)"

print_info "Testing Docker builds (this may take a few minutes)..."

# Test backend Docker build
if [ -f "backend/Dockerfile" ]; then
    if docker build -t backend-test:latest backend &> /dev/null; then
        print_success "Backend Docker build test passed"
    else
        print_error "Backend Docker build test failed"
        print_info "Run manually: cd backend && docker build -t backend-test ."
    fi
else
    print_warning "Backend Dockerfile not found - skipping build test"
fi

# Test frontend Docker build
if [ -f "frontend/Dockerfile" ]; then
    if docker build -t frontend-test:latest frontend &> /dev/null; then
        print_success "Frontend Docker build test passed"
    else
        print_error "Frontend Docker build test failed"
        print_info "Run manually: cd frontend && docker build -t frontend-test ."
    fi
else
    print_warning "Frontend Dockerfile not found - skipping build test"
fi

echo ""

##############################################################################
# 14. Terraform Validation
##############################################################################

print_header "14. Terraform Validation"

cd infrastructure/terraform

if terraform fmt -check -recursive &> /dev/null; then
    print_success "Terraform formatting is correct"
else
    print_warning "Terraform files need formatting"
    print_info "Run: terraform fmt -recursive"
fi

if terraform init -backend=false &> /dev/null; then
    print_success "Terraform initialization succeeded"

    if terraform validate &> /dev/null; then
        print_success "Terraform validation passed"
    else
        print_error "Terraform validation failed"
        print_info "Run: cd infrastructure/terraform && terraform validate"
    fi
else
    print_error "Terraform initialization failed"
fi

cd ../..

echo ""

##############################################################################
# Final Summary
##############################################################################

print_header "Validation Summary"

echo ""
echo -e "${GREEN}Passed:${NC}   $PASSED"
echo -e "${YELLOW}Warnings:${NC} $WARNINGS"
echo -e "${RED}Errors:${NC}   $ERRORS"
echo ""

if [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✓ Pre-deployment validation PASSED${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Review warnings and address if needed"
    echo "2. Set up GitHub secrets (see DEPLOYMENT.md)"
    echo "3. Review infrastructure/terraform/variables.tf"
    echo "4. Run: cd infrastructure/terraform && terraform plan"
    echo "5. Run: cd infrastructure/terraform && terraform apply"
    echo ""
    exit 0
else
    echo -e "${RED}✗ Pre-deployment validation FAILED${NC}"
    echo ""
    echo "Please fix the errors above before proceeding with deployment."
    echo "See infrastructure/DEPLOYMENT.md for detailed setup instructions."
    echo ""
    exit 1
fi
