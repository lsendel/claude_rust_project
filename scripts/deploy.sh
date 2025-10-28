#!/bin/bash
# Deployment Script
# Multi-Tenant SaaS Platform - Deploy to AWS Environment

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
ENVIRONMENT="${1:-dev}"
PROJECT_NAME="${PROJECT_NAME:-saas-platform}"
AWS_REGION="${AWS_REGION:-us-east-1}"

print_header() {
    echo -e "${BLUE}============================================${NC}"
    echo -e "${BLUE}$1${NC}"
    echo -e "${BLUE}============================================${NC}"
}

print_success() {
    echo -e "${GREEN}✓${NC} $1"
}

print_error() {
    echo -e "${RED}✗${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}⚠${NC} $1"
}

print_info() {
    echo -e "${BLUE}ℹ${NC} $1"
}

# Validate environment
validate_environment() {
    case $ENVIRONMENT in
        dev|staging|prod)
            print_success "Valid environment: $ENVIRONMENT"
            ;;
        *)
            print_error "Invalid environment: $ENVIRONMENT"
            echo "Usage: $0 {dev|staging|prod}"
            exit 1
            ;;
    esac
}

# Check prerequisites
check_prerequisites() {
    print_header "Checking Prerequisites"

    local missing=0

    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI not installed"
        ((missing++))
    else
        print_success "AWS CLI installed"
    fi

    if ! command -v terraform &> /dev/null; then
        print_error "Terraform not installed"
        ((missing++))
    else
        print_success "Terraform installed"
    fi

    if ! command -v docker &> /dev/null; then
        print_error "Docker not installed"
        ((missing++))
    else
        print_success "Docker installed"
    fi

    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS credentials not configured"
        ((missing++))
    else
        local AWS_ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
        print_success "AWS authenticated (Account: $AWS_ACCOUNT)"
    fi

    if [ $missing -gt 0 ]; then
        print_error "$missing prerequisite(s) missing"
        exit 1
    fi
}

# Build backend Docker image
build_backend() {
    print_header "Building Backend Docker Image"

    cd backend

    print_info "Running tests..."
    mvn clean test

    print_info "Building JAR..."
    mvn package -DskipTests

    print_info "Building Docker image..."
    docker build -t "$PROJECT_NAME-backend:$ENVIRONMENT" .

    cd ..

    print_success "Backend image built successfully"
}

# Build frontend Docker image
build_frontend() {
    print_header "Building Frontend Docker Image"

    cd frontend

    print_info "Installing dependencies..."
    npm ci

    print_info "Running tests..."
    npm test -- --watchAll=false

    print_info "Building Docker image..."
    docker build -t "$PROJECT_NAME-frontend:$ENVIRONMENT" .

    cd ..

    print_success "Frontend image built successfully"
}

# Push images to ECR
push_to_ecr() {
    print_header "Pushing Images to ECR"

    local AWS_ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
    local ECR_REGISTRY="$AWS_ACCOUNT.dkr.ecr.$AWS_REGION.amazonaws.com"

    # Login to ECR
    print_info "Logging in to ECR..."
    aws ecr get-login-password --region "$AWS_REGION" | \
        docker login --username AWS --password-stdin "$ECR_REGISTRY"

    # Push backend
    print_info "Pushing backend image..."
    docker tag "$PROJECT_NAME-backend:$ENVIRONMENT" \
        "$ECR_REGISTRY/$PROJECT_NAME-backend:$ENVIRONMENT"
    docker push "$ECR_REGISTRY/$PROJECT_NAME-backend:$ENVIRONMENT"
    docker tag "$ECR_REGISTRY/$PROJECT_NAME-backend:$ENVIRONMENT" \
        "$ECR_REGISTRY/$PROJECT_NAME-backend:latest"
    docker push "$ECR_REGISTRY/$PROJECT_NAME-backend:latest"

    # Push frontend
    print_info "Pushing frontend image..."
    docker tag "$PROJECT_NAME-frontend:$ENVIRONMENT" \
        "$ECR_REGISTRY/$PROJECT_NAME-frontend:$ENVIRONMENT"
    docker push "$ECR_REGISTRY/$PROJECT_NAME-frontend:$ENVIRONMENT"
    docker tag "$ECR_REGISTRY/$PROJECT_NAME-frontend:$ENVIRONMENT" \
        "$ECR_REGISTRY/$PROJECT_NAME-frontend:latest"
    docker push "$ECR_REGISTRY/$PROJECT_NAME-frontend:latest"

    print_success "Images pushed to ECR"
}

# Deploy infrastructure with Terraform
deploy_infrastructure() {
    print_header "Deploying Infrastructure with Terraform"

    cd infrastructure/terraform

    print_info "Initializing Terraform..."
    terraform init

    print_info "Validating Terraform configuration..."
    terraform validate

    print_info "Planning infrastructure changes..."
    terraform plan \
        -var="environment=$ENVIRONMENT" \
        -var="backend_image_tag=$ENVIRONMENT" \
        -out=tfplan

    echo ""
    print_warning "Review the plan above. Press Enter to continue or Ctrl+C to abort..."
    read

    print_info "Applying Terraform changes..."
    terraform apply tfplan

    print_success "Infrastructure deployed"

    # Export outputs
    print_info "Exporting Terraform outputs..."
    terraform output -json > /tmp/terraform-outputs.json

    cd ../..
}

# Deploy frontend to S3 and invalidate CloudFront
deploy_frontend_s3() {
    print_header "Deploying Frontend to S3"

    cd frontend

    print_info "Building production frontend..."
    npm run build

    # Get S3 bucket name from Terraform output
    local FRONTEND_BUCKET=$(terraform -chdir=infrastructure/terraform output -raw frontend_s3_bucket)
    local CF_DIST_ID=$(terraform -chdir=infrastructure/terraform output -raw cloudfront_distribution_id)

    print_info "Syncing to S3 bucket: $FRONTEND_BUCKET"
    aws s3 sync dist/ "s3://$FRONTEND_BUCKET/" \
        --delete \
        --cache-control "public,max-age=31536000,immutable" \
        --exclude "index.html"

    aws s3 cp dist/index.html "s3://$FRONTEND_BUCKET/index.html" \
        --cache-control "no-cache,no-store,must-revalidate"

    print_info "Invalidating CloudFront cache..."
    aws cloudfront create-invalidation \
        --distribution-id "$CF_DIST_ID" \
        --paths "/*"

    cd ..

    print_success "Frontend deployed to S3"
}

# Update ECS service
deploy_backend_ecs() {
    print_header "Deploying Backend to ECS"

    # Get ECS details from Terraform output
    local ECS_CLUSTER=$(terraform -chdir=infrastructure/terraform output -raw ecs_cluster_name)
    local ECS_SERVICE=$(terraform -chdir=infrastructure/terraform output -raw ecs_service_name)

    print_info "Forcing ECS service update..."
    aws ecs update-service \
        --cluster "$ECS_CLUSTER" \
        --service "$ECS_SERVICE" \
        --force-new-deployment \
        --region "$AWS_REGION"

    print_info "Waiting for service to stabilize..."
    aws ecs wait services-stable \
        --cluster "$ECS_CLUSTER" \
        --services "$ECS_SERVICE" \
        --region "$AWS_REGION"

    print_success "Backend deployed to ECS"
}

# Run smoke tests
run_smoke_tests() {
    print_header "Running Smoke Tests"

    # Get API URL from Terraform output
    local API_URL=$(terraform -chdir=infrastructure/terraform output -raw backend_api_url)
    local FRONTEND_URL=$(terraform -chdir=infrastructure/terraform output -raw frontend_url)

    print_info "Testing backend health..."
    if curl -f "$API_URL/actuator/health" &> /dev/null; then
        print_success "Backend health check passed"
    else
        print_error "Backend health check failed"
        return 1
    fi

    print_info "Testing frontend availability..."
    if curl -f "$FRONTEND_URL" &> /dev/null; then
        print_success "Frontend health check passed"
    else
        print_error "Frontend health check failed"
        return 1
    fi

    print_success "All smoke tests passed"
}

# Deployment summary
print_deployment_summary() {
    print_header "Deployment Summary"

    local API_URL=$(terraform -chdir=infrastructure/terraform output -raw backend_api_url 2>/dev/null || echo "N/A")
    local FRONTEND_URL=$(terraform -chdir=infrastructure/terraform output -raw frontend_url 2>/dev/null || echo "N/A")

    echo ""
    echo -e "${GREEN}✓ Deployment completed successfully!${NC}"
    echo ""
    echo "Environment: ${BLUE}$ENVIRONMENT${NC}"
    echo "Backend API: ${BLUE}$API_URL${NC}"
    echo "Frontend: ${BLUE}$FRONTEND_URL${NC}"
    echo ""
    echo "Next steps:"
    echo "1. Verify application is working: $FRONTEND_URL"
    echo "2. Check CloudWatch logs for any errors"
    echo "3. Monitor metrics and alarms"
    echo ""
}

##############################################################################
# Main Execution
##############################################################################

print_header "Deploying $PROJECT_NAME to $ENVIRONMENT"
echo ""

# Validate and check
validate_environment
check_prerequisites
echo ""

# Build
build_backend
echo ""

build_frontend
echo ""

# Push
push_to_ecr
echo ""

# Deploy
deploy_infrastructure
echo ""

deploy_frontend_s3
echo ""

deploy_backend_ecs
echo ""

# Test
run_smoke_tests
echo ""

# Summary
print_deployment_summary

print_success "Deployment script completed!"
