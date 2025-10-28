#!/bin/bash
# AWS Prerequisites Setup Script
# Multi-Tenant SaaS Platform - One-Time AWS Infrastructure Setup

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Configuration
PROJECT_NAME="${PROJECT_NAME:-saas-platform}"
AWS_REGION="${AWS_REGION:-us-east-1}"
STATE_BUCKET="$PROJECT_NAME-terraform-state"
LOCK_TABLE="terraform-lock"

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

# Check AWS CLI is installed and configured
check_aws_cli() {
    if ! command -v aws &> /dev/null; then
        print_error "AWS CLI is not installed"
        echo "Install: https://aws.amazon.com/cli/"
        exit 1
    fi

    if ! aws sts get-caller-identity &> /dev/null; then
        print_error "AWS CLI is not configured or credentials are invalid"
        echo "Run: aws configure"
        exit 1
    fi

    AWS_ACCOUNT=$(aws sts get-caller-identity --query Account --output text)
    print_success "AWS CLI configured (Account: $AWS_ACCOUNT)"
}

# Create S3 bucket for Terraform state
create_state_bucket() {
    print_header "Creating S3 Bucket for Terraform State"

    if aws s3 ls "s3://$STATE_BUCKET" &> /dev/null; then
        print_warning "S3 bucket already exists: $STATE_BUCKET"
    else
        print_info "Creating S3 bucket: $STATE_BUCKET"

        if [ "$AWS_REGION" = "us-east-1" ]; then
            aws s3 mb "s3://$STATE_BUCKET" --region "$AWS_REGION"
        else
            aws s3 mb "s3://$STATE_BUCKET" --region "$AWS_REGION" \
                --create-bucket-configuration LocationConstraint="$AWS_REGION"
        fi

        print_success "S3 bucket created: $STATE_BUCKET"
    fi

    # Enable versioning
    print_info "Enabling versioning on S3 bucket..."
    aws s3api put-bucket-versioning \
        --bucket "$STATE_BUCKET" \
        --versioning-configuration Status=Enabled

    print_success "Versioning enabled"

    # Enable server-side encryption
    print_info "Enabling server-side encryption..."
    aws s3api put-bucket-encryption \
        --bucket "$STATE_BUCKET" \
        --server-side-encryption-configuration '{
            "Rules": [{
                "ApplyServerSideEncryptionByDefault": {
                    "SSEAlgorithm": "AES256"
                }
            }]
        }'

    print_success "Encryption enabled"

    # Block public access
    print_info "Blocking public access..."
    aws s3api put-public-access-block \
        --bucket "$STATE_BUCKET" \
        --public-access-block-configuration \
            BlockPublicAcls=true,IgnorePublicAcls=true,BlockPublicPolicy=true,RestrictPublicBuckets=true

    print_success "Public access blocked"
}

# Create DynamoDB table for state locking
create_lock_table() {
    print_header "Creating DynamoDB Table for State Locking"

    if aws dynamodb describe-table --table-name "$LOCK_TABLE" &> /dev/null 2>&1; then
        print_warning "DynamoDB table already exists: $LOCK_TABLE"
    else
        print_info "Creating DynamoDB table: $LOCK_TABLE"

        aws dynamodb create-table \
            --table-name "$LOCK_TABLE" \
            --attribute-definitions AttributeName=LockID,AttributeType=S \
            --key-schema AttributeName=LockID,KeyType=HASH \
            --billing-mode PAY_PER_REQUEST \
            --region "$AWS_REGION"

        print_success "DynamoDB table created: $LOCK_TABLE"

        print_info "Waiting for table to be active..."
        aws dynamodb wait table-exists --table-name "$LOCK_TABLE"
        print_success "Table is active"
    fi
}

# Create ECR repositories
create_ecr_repos() {
    print_header "Creating ECR Repositories"

    local repos=("$PROJECT_NAME-backend" "$PROJECT_NAME-frontend")

    for repo in "${repos[@]}"; do
        if aws ecr describe-repositories --repository-names "$repo" &> /dev/null 2>&1; then
            print_warning "ECR repository already exists: $repo"
        else
            print_info "Creating ECR repository: $repo"

            aws ecr create-repository \
                --repository-name "$repo" \
                --region "$AWS_REGION" \
                --image-scanning-configuration scanOnPush=true \
                --encryption-configuration encryptionType=AES256

            print_success "ECR repository created: $repo"
        fi

        # Get repository URI
        REPO_URI=$(aws ecr describe-repositories \
            --repository-names "$repo" \
            --query 'repositories[0].repositoryUri' \
            --output text)
        print_info "Repository URI: $REPO_URI"
    done
}

# Create IAM role for GitHub Actions (OIDC)
create_github_oidc_role() {
    print_header "Creating IAM Role for GitHub Actions (OIDC)"

    local ROLE_NAME="${PROJECT_NAME}-github-actions-role"
    local GITHUB_ORG="${GITHUB_ORG:-your-github-org}"
    local GITHUB_REPO="${GITHUB_REPO:-your-repo-name}"

    print_info "GitHub Organization: $GITHUB_ORG"
    print_info "GitHub Repository: $GITHUB_REPO"

    # Check if OIDC provider exists
    local OIDC_PROVIDER_ARN
    OIDC_PROVIDER_ARN=$(aws iam list-open-id-connect-providers \
        --query "OpenIDConnectProviderList[?contains(Arn, 'token.actions.githubusercontent.com')].Arn" \
        --output text)

    if [ -z "$OIDC_PROVIDER_ARN" ]; then
        print_info "Creating OIDC provider for GitHub Actions..."

        aws iam create-open-id-connect-provider \
            --url https://token.actions.githubusercontent.com \
            --client-id-list sts.amazonaws.com \
            --thumbprint-list 6938fd4d98bab03faadb97b34396831e3780aea1

        OIDC_PROVIDER_ARN=$(aws iam list-open-id-connect-providers \
            --query "OpenIDConnectProviderList[?contains(Arn, 'token.actions.githubusercontent.com')].Arn" \
            --output text)

        print_success "OIDC provider created"
    else
        print_warning "OIDC provider already exists"
    fi

    # Create trust policy
    cat > /tmp/github-trust-policy.json <<EOF
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Federated": "$OIDC_PROVIDER_ARN"
      },
      "Action": "sts:AssumeRoleWithWebIdentity",
      "Condition": {
        "StringEquals": {
          "token.actions.githubusercontent.com:aud": "sts.amazonaws.com"
        },
        "StringLike": {
          "token.actions.githubusercontent.com:sub": "repo:$GITHUB_ORG/$GITHUB_REPO:*"
        }
      }
    }
  ]
}
EOF

    # Create IAM role
    if aws iam get-role --role-name "$ROLE_NAME" &> /dev/null 2>&1; then
        print_warning "IAM role already exists: $ROLE_NAME"
    else
        print_info "Creating IAM role: $ROLE_NAME"

        aws iam create-role \
            --role-name "$ROLE_NAME" \
            --assume-role-policy-document file:///tmp/github-trust-policy.json

        print_success "IAM role created: $ROLE_NAME"
    fi

    # Attach policies
    print_info "Attaching policies to IAM role..."

    aws iam attach-role-policy \
        --role-name "$ROLE_NAME" \
        --policy-arn arn:aws:iam::aws:policy/AdministratorAccess || true

    print_warning "⚠️  Note: AdministratorAccess is attached for simplicity. In production, use least privilege policies."

    local ROLE_ARN=$(aws iam get-role --role-name "$ROLE_NAME" --query 'Role.Arn' --output text)
    print_success "IAM role ARN: $ROLE_ARN"

    rm /tmp/github-trust-policy.json
}

# Summary and next steps
print_summary() {
    print_header "Setup Complete!"

    echo ""
    echo "AWS Prerequisites have been created successfully:"
    echo ""
    echo -e "${GREEN}✓${NC} S3 bucket for Terraform state: ${BLUE}$STATE_BUCKET${NC}"
    echo -e "${GREEN}✓${NC} DynamoDB table for state locking: ${BLUE}$LOCK_TABLE${NC}"
    echo -e "${GREEN}✓${NC} ECR repositories created"
    echo -e "${GREEN}✓${NC} GitHub Actions OIDC role created"
    echo ""
    echo "Next steps:"
    echo ""
    echo "1. Update infrastructure/terraform/main.tf with backend configuration:"
    echo "   backend \"s3\" {"
    echo "     bucket         = \"$STATE_BUCKET\""
    echo "     key            = \"terraform.tfstate\""
    echo "     region         = \"$AWS_REGION\""
    echo "     dynamodb_table = \"$LOCK_TABLE\""
    echo "     encrypt        = true"
    echo "   }"
    echo ""
    echo "2. Set GitHub secrets in your repository:"
    echo "   - AWS_DEPLOY_ROLE_ARN: (see IAM role ARN above)"
    echo "   - TF_STATE_BUCKET: $STATE_BUCKET"
    echo "   - DB_PASSWORD: (generate secure password)"
    echo "   - INTERNAL_API_SECRET: (generate random secret)"
    echo "   - COGNITO_USER_POOL_ID: (from Cognito after terraform apply)"
    echo "   - COGNITO_CLIENT_ID: (from Cognito after terraform apply)"
    echo ""
    echo "3. Initialize Terraform:"
    echo "   cd infrastructure/terraform"
    echo "   terraform init"
    echo "   terraform plan"
    echo ""
    echo "4. Review and apply Terraform:"
    echo "   terraform apply"
    echo ""
}

##############################################################################
# Main Execution
##############################################################################

print_header "AWS Prerequisites Setup for $PROJECT_NAME"
echo ""

# Verify AWS CLI
check_aws_cli
echo ""

# Create resources
create_state_bucket
echo ""

create_lock_table
echo ""

create_ecr_repos
echo ""

create_github_oidc_role
echo ""

# Print summary
print_summary
