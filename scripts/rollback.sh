#!/bin/bash
# Rollback Script
# Multi-Tenant SaaS Platform - Rollback to Previous Version

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
ENVIRONMENT="${1:-dev}"
COMPONENT="${2:-all}"
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

print_usage() {
    echo "Usage: $0 {dev|staging|prod} {backend|frontend|all}"
    echo ""
    echo "Examples:"
    echo "  $0 prod backend    # Rollback only backend in production"
    echo "  $0 staging all     # Rollback both backend and frontend in staging"
    echo ""
}

# Validate inputs
validate_inputs() {
    case $ENVIRONMENT in
        dev|staging|prod)
            ;;
        *)
            print_error "Invalid environment: $ENVIRONMENT"
            print_usage
            exit 1
            ;;
    esac

    case $COMPONENT in
        backend|frontend|all)
            ;;
        *)
            print_error "Invalid component: $COMPONENT"
            print_usage
            exit 1
            ;;
    esac

    print_success "Valid inputs: Environment=$ENVIRONMENT, Component=$COMPONENT"
}

# Confirm rollback
confirm_rollback() {
    print_header "Rollback Confirmation"

    echo ""
    print_warning "⚠️  WARNING: You are about to rollback $COMPONENT in $ENVIRONMENT environment"
    echo ""
    echo "This will:"
    if [ "$COMPONENT" = "backend" ] || [ "$COMPONENT" = "all" ]; then
        echo "  - Revert backend to the previous ECS task definition"
    fi
    if [ "$COMPONENT" = "frontend" ] || [ "$COMPONENT" = "all" ]; then
        echo "  - Restore previous frontend version from S3"
    fi
    echo ""
    echo -n "Are you sure you want to continue? (yes/no): "
    read -r confirmation

    if [ "$confirmation" != "yes" ]; then
        print_info "Rollback cancelled"
        exit 0
    fi

    print_success "Rollback confirmed"
}

# Rollback backend (ECS)
rollback_backend() {
    print_header "Rolling Back Backend"

    # Get ECS details
    local ECS_CLUSTER=$(terraform -chdir=infrastructure/terraform output -raw ecs_cluster_name)
    local ECS_SERVICE=$(terraform -chdir=infrastructure/terraform output -raw ecs_service_name)

    print_info "Current ECS service: $ECS_SERVICE"

    # Get current task definition
    local CURRENT_TASK_DEF=$(aws ecs describe-services \
        --cluster "$ECS_CLUSTER" \
        --services "$ECS_SERVICE" \
        --query 'services[0].taskDefinition' \
        --output text)

    print_info "Current task definition: $CURRENT_TASK_DEF"

    # Extract task family and revision
    local TASK_FAMILY=$(echo "$CURRENT_TASK_DEF" | cut -d':' -f1 | cut -d'/' -f2)
    local CURRENT_REVISION=$(echo "$CURRENT_TASK_DEF" | cut -d':' -f2)
    local PREVIOUS_REVISION=$((CURRENT_REVISION - 1))

    if [ "$PREVIOUS_REVISION" -lt 1 ]; then
        print_error "No previous revision found. Cannot rollback."
        exit 1
    fi

    local PREVIOUS_TASK_DEF="$TASK_FAMILY:$PREVIOUS_REVISION"
    print_info "Rolling back to task definition: $PREVIOUS_TASK_DEF"

    # Update service
    aws ecs update-service \
        --cluster "$ECS_CLUSTER" \
        --service "$ECS_SERVICE" \
        --task-definition "$PREVIOUS_TASK_DEF" \
        --region "$AWS_REGION"

    print_info "Waiting for service to stabilize..."
    aws ecs wait services-stable \
        --cluster "$ECS_CLUSTER" \
        --services "$ECS_SERVICE" \
        --region "$AWS_REGION"

    print_success "Backend rolled back successfully"

    # Verify health
    local API_URL=$(terraform -chdir=infrastructure/terraform output -raw backend_api_url)
    print_info "Verifying backend health..."
    sleep 10  # Wait for service to fully start

    if curl -f "$API_URL/actuator/health" &> /dev/null; then
        print_success "Backend health check passed"
    else
        print_warning "Backend health check failed. Service may still be starting."
    fi
}

# Rollback frontend (S3)
rollback_frontend() {
    print_header "Rolling Back Frontend"

    local FRONTEND_BUCKET=$(terraform -chdir=infrastructure/terraform output -raw frontend_s3_bucket)
    local CF_DIST_ID=$(terraform -chdir=infrastructure/terraform output -raw cloudfront_distribution_id)

    print_info "S3 bucket: $FRONTEND_BUCKET"

    # List object versions for index.html
    print_info "Fetching previous version of index.html..."

    local VERSIONS=$(aws s3api list-object-versions \
        --bucket "$FRONTEND_BUCKET" \
        --prefix "index.html" \
        --query 'Versions[*].[VersionId,LastModified]' \
        --output text)

    if [ -z "$VERSIONS" ]; then
        print_error "No versions found for index.html. Cannot rollback."
        exit 1
    fi

    # Get second version (previous version)
    local PREVIOUS_VERSION=$(echo "$VERSIONS" | sed -n '2p' | awk '{print $1}')

    if [ -z "$PREVIOUS_VERSION" ]; then
        print_error "No previous version found. Cannot rollback."
        exit 1
    fi

    print_info "Rolling back to version: $PREVIOUS_VERSION"

    # Copy previous version to current
    aws s3api copy-object \
        --bucket "$FRONTEND_BUCKET" \
        --copy-source "$FRONTEND_BUCKET/index.html?versionId=$PREVIOUS_VERSION" \
        --key "index.html" \
        --cache-control "no-cache,no-store,must-revalidate"

    print_success "index.html rolled back"

    # Invalidate CloudFront cache
    print_info "Invalidating CloudFront cache..."
    aws cloudfront create-invalidation \
        --distribution-id "$CF_DIST_ID" \
        --paths "/*"

    print_success "Frontend rolled back successfully"

    # Verify availability
    local FRONTEND_URL=$(terraform -chdir=infrastructure/terraform output -raw frontend_url)
    print_info "Verifying frontend availability..."
    sleep 5

    if curl -f "$FRONTEND_URL" &> /dev/null; then
        print_success "Frontend health check passed"
    else
        print_warning "Frontend health check failed. CloudFront cache may still be propagating."
    fi
}

# Rollback summary
print_rollback_summary() {
    print_header "Rollback Summary"

    echo ""
    echo -e "${GREEN}✓ Rollback completed successfully!${NC}"
    echo ""
    echo "Environment: ${BLUE}$ENVIRONMENT${NC}"
    echo "Component(s): ${BLUE}$COMPONENT${NC}"
    echo ""

    if [ "$COMPONENT" = "backend" ] || [ "$COMPONENT" = "all" ]; then
        local API_URL=$(terraform -chdir=infrastructure/terraform output -raw backend_api_url)
        echo "Backend API: ${BLUE}$API_URL${NC}"
    fi

    if [ "$COMPONENT" = "frontend" ] || [ "$COMPONENT" = "all" ]; then
        local FRONTEND_URL=$(terraform -chdir=infrastructure/terraform output -raw frontend_url)
        echo "Frontend: ${BLUE}$FRONTEND_URL${NC}"
    fi

    echo ""
    echo "Next steps:"
    echo "1. Verify the application is working correctly"
    echo "2. Check CloudWatch logs for any errors"
    echo "3. Monitor metrics and alarms"
    echo "4. If issues persist, investigate and fix the root cause"
    echo ""
}

##############################################################################
# Main Execution
##############################################################################

print_header "Rollback $PROJECT_NAME ($COMPONENT) in $ENVIRONMENT"
echo ""

# Validate
validate_inputs
echo ""

# Confirm
confirm_rollback
echo ""

# Rollback components
if [ "$COMPONENT" = "backend" ] || [ "$COMPONENT" = "all" ]; then
    rollback_backend
    echo ""
fi

if [ "$COMPONENT" = "frontend" ] || [ "$COMPONENT" = "all" ]; then
    rollback_frontend
    echo ""
fi

# Summary
print_rollback_summary

print_success "Rollback script completed!"
