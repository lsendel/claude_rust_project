# AWS Secrets Manager Configuration
# Multi-Tenant SaaS Platform - Secure Secrets Storage

##############################################################################
# Database Password Secret
##############################################################################

resource "aws_secretsmanager_secret" "db_password" {
  name                    = "${var.project_name}/${var.environment}/db-password"
  description             = "RDS PostgreSQL master password"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name        = "${var.project_name}-db-password"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "db_password" {
  secret_id     = aws_secretsmanager_secret.db_password.id
  secret_string = var.db_password
}

##############################################################################
# Internal API Secret
# Used for authentication between Lambda functions and Backend API
##############################################################################

resource "aws_secretsmanager_secret" "internal_api" {
  name                    = "${var.project_name}/${var.environment}/internal-api-secret"
  description             = "Secret for internal API authentication (Lambda to Backend)"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name        = "${var.project_name}-internal-api-secret"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "internal_api" {
  secret_id     = aws_secretsmanager_secret.internal_api.id
  secret_string = var.internal_api_secret
}

##############################################################################
# OAuth Secrets (Optional)
##############################################################################

# Google OAuth
resource "aws_secretsmanager_secret" "google_oauth" {
  count                   = var.google_client_id != "" ? 1 : 0
  name                    = "${var.project_name}/${var.environment}/oauth/google"
  description             = "Google OAuth credentials"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name        = "${var.project_name}-google-oauth"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "google_oauth" {
  count     = var.google_client_id != "" ? 1 : 0
  secret_id = aws_secretsmanager_secret.google_oauth[0].id

  secret_string = jsonencode({
    client_id     = var.google_client_id
    client_secret = var.google_client_secret
  })
}

# GitHub OAuth
resource "aws_secretsmanager_secret" "github_oauth" {
  count                   = var.github_client_id != "" ? 1 : 0
  name                    = "${var.project_name}/${var.environment}/oauth/github"
  description             = "GitHub OAuth credentials"
  recovery_window_in_days = var.environment == "prod" ? 30 : 0

  tags = {
    Name        = "${var.project_name}-github-oauth"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

resource "aws_secretsmanager_secret_version" "github_oauth" {
  count     = var.github_client_id != "" ? 1 : 0
  secret_id = aws_secretsmanager_secret.github_oauth[0].id

  secret_string = jsonencode({
    client_id     = var.github_client_id
    client_secret = var.github_client_secret
  })
}

##############################################################################
# Rotation Configuration (for production)
##############################################################################

# Enable automatic rotation for database password in production
resource "aws_secretsmanager_secret_rotation" "db_password" {
  count               = var.environment == "prod" ? 1 : 0
  secret_id           = aws_secretsmanager_secret.db_password.id
  rotation_lambda_arn = aws_lambda_function.secret_rotation[0].arn

  rotation_rules {
    automatically_after_days = 90
  }
}

# Lambda function for secret rotation (production only)
resource "aws_lambda_function" "secret_rotation" {
  count         = var.environment == "prod" ? 1 : 0
  filename      = "lambda/secret-rotation.zip"
  function_name = "${var.project_name}-secret-rotation-${var.environment}"
  role          = aws_iam_role.lambda_execution_role.arn
  handler       = "index.handler"
  runtime       = "nodejs18.x"

  environment {
    variables = {
      RDS_ENDPOINT = aws_db_instance.postgres.endpoint
      SECRET_ARN   = aws_secretsmanager_secret.db_password.arn
    }
  }

  tags = {
    Name        = "${var.project_name}-secret-rotation"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }

  # Note: This is a placeholder. Actual rotation function needs to be implemented
  # and packaged as lambda/secret-rotation.zip
  lifecycle {
    ignore_changes = [filename]
  }
}

##############################################################################
# Outputs
##############################################################################

output "db_password_secret_arn" {
  description = "ARN of database password secret"
  value       = aws_secretsmanager_secret.db_password.arn
  sensitive   = true
}

output "internal_api_secret_arn" {
  description = "ARN of internal API secret"
  value       = aws_secretsmanager_secret.internal_api.arn
  sensitive   = true
}
