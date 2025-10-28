# Multi-Tenant SaaS Platform - Terraform Configuration
# Main infrastructure definition

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Uncomment for remote state management
  # backend "s3" {
  #   bucket         = "saas-platform-terraform-state"
  #   key            = "terraform.tfstate"
  #   region         = "us-east-1"
  #   dynamodb_table = "terraform-lock"
  #   encrypt        = true
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "Multi-Tenant SaaS Platform"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Provider alias for us-east-1 (required for CloudFront ACM certificates)
provider "aws" {
  alias  = "us-east-1"
  region = "us-east-1"

  default_tags {
    tags = {
      Project     = "Multi-Tenant SaaS Platform"
      Environment = var.environment
      ManagedBy   = "Terraform"
    }
  }
}

# Outputs from modules will be defined here
