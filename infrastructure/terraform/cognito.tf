# AWS Cognito User Pool for Multi-Tenant SaaS Platform

# Cognito User Pool
resource "aws_cognito_user_pool" "main" {
  name = "${var.project_name}-${var.environment}-user-pool"

  # Email configuration
  username_attributes      = ["email"]
  auto_verified_attributes = ["email"]

  # Password policy
  password_policy {
    minimum_length                   = 8
    require_lowercase                = true
    require_uppercase                = true
    require_numbers                  = true
    require_symbols                  = true
    temporary_password_validity_days = 7
  }

  # User attributes
  schema {
    name                = "email"
    attribute_data_type = "String"
    required            = true
    mutable             = false

    string_attribute_constraints {
      min_length = 1
      max_length = 256
    }
  }

  schema {
    name                = "name"
    attribute_data_type = "String"
    required            = true
    mutable             = true

    string_attribute_constraints {
      min_length = 1
      max_length = 256
    }
  }

  # MFA configuration
  mfa_configuration = "OPTIONAL"

  software_token_mfa_configuration {
    enabled = true
  }

  # Account recovery
  account_recovery_setting {
    recovery_mechanism {
      name     = "verified_email"
      priority = 1
    }
  }

  # Email settings
  email_configuration {
    email_sending_account = "COGNITO_DEFAULT"
  }

  # Lambda triggers
  lambda_config {
    post_confirmation = aws_lambda_function.cognito_post_confirmation.arn
  }

  # User pool tags
  tags = {
    Name = "${var.project_name}-${var.environment}-user-pool"
  }
}

# Cognito User Pool Client for Web Application
resource "aws_cognito_user_pool_client" "web_client" {
  name         = "${var.project_name}-${var.environment}-web-client"
  user_pool_id = aws_cognito_user_pool.main.id

  # OAuth flows
  allowed_oauth_flows_user_pool_client = true
  allowed_oauth_flows                  = ["code", "implicit"]
  allowed_oauth_scopes                 = ["openid", "email", "profile"]

  # Callback URLs
  callback_urls = [
    "http://localhost:3000/oauth/callback",
    "https://${var.domain_name}/oauth/callback"
  ]

  logout_urls = [
    "http://localhost:3000",
    "https://${var.domain_name}"
  ]

  # Token validity
  access_token_validity  = 1  # 1 hour
  id_token_validity      = 1  # 1 hour
  refresh_token_validity = 30 # 30 days

  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }

  # Read/write attributes
  read_attributes = [
    "email",
    "email_verified",
    "name",
    "sub"
  ]

  write_attributes = [
    "email",
    "name"
  ]

  # Prevent secret generation (for public clients)
  generate_secret = false

  # Authentication flows
  explicit_auth_flows = [
    "ALLOW_USER_SRP_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH",
    "ALLOW_USER_PASSWORD_AUTH"
  ]

  # Prevent user existence errors
  prevent_user_existence_errors = "ENABLED"
}

# Cognito User Pool Client for Backend/Internal API
resource "aws_cognito_user_pool_client" "backend_client" {
  name         = "${var.project_name}-${var.environment}-backend-client"
  user_pool_id = aws_cognito_user_pool.main.id

  # Token validity
  access_token_validity  = 1  # 1 hour
  id_token_validity      = 1  # 1 hour
  refresh_token_validity = 30 # 30 days

  token_validity_units {
    access_token  = "hours"
    id_token      = "hours"
    refresh_token = "days"
  }

  # Generate secret for backend
  generate_secret = true

  # Authentication flows
  explicit_auth_flows = [
    "ALLOW_ADMIN_USER_PASSWORD_AUTH",
    "ALLOW_REFRESH_TOKEN_AUTH"
  ]
}

# Cognito User Pool Domain
resource "aws_cognito_user_pool_domain" "main" {
  domain       = "${var.project_name}-${var.environment}"
  user_pool_id = aws_cognito_user_pool.main.id
}

# Google Identity Provider
resource "aws_cognito_identity_provider" "google" {
  user_pool_id  = aws_cognito_user_pool.main.id
  provider_name = "Google"
  provider_type = "Google"

  provider_details = {
    authorize_scopes = "openid email profile"
    client_id        = var.google_client_id
    client_secret    = var.google_client_secret
  }

  attribute_mapping = {
    email    = "email"
    name     = "name"
    username = "sub"
  }
}

# GitHub Identity Provider
resource "aws_cognito_identity_provider" "github" {
  user_pool_id  = aws_cognito_user_pool.main.id
  provider_name = "GitHub"
  provider_type = "OIDC"

  provider_details = {
    authorize_scopes              = "openid user:email"
    client_id                     = var.github_client_id
    client_secret                 = var.github_client_secret
    attributes_request_method     = "GET"
    oidc_issuer                   = "https://token.actions.githubusercontent.com"
    authorize_url                 = "https://github.com/login/oauth/authorize"
    token_url                     = "https://github.com/login/oauth/access_token"
    attributes_url                = "https://api.github.com/user"
    jwks_uri                      = "https://token.actions.githubusercontent.com/.well-known/jwks"
  }

  attribute_mapping = {
    email    = "email"
    name     = "name"
    username = "sub"
  }
}

# Lambda function for post-confirmation trigger
resource "aws_lambda_function" "cognito_post_confirmation" {
  function_name = "${var.project_name}-${var.environment}-cognito-post-confirmation"
  role          = aws_iam_role.lambda_cognito_execution.arn
  handler       = "post-confirmation.handler"
  runtime       = "nodejs18.x"
  timeout       = 30

  filename         = "${path.module}/../../lambda-functions/cognito-triggers/dist/post-confirmation.zip"
  source_code_hash = filebase64sha256("${path.module}/../../lambda-functions/cognito-triggers/dist/post-confirmation.zip")

  environment {
    variables = {
      BACKEND_API_URL     = "https://api.${var.domain_name}"
      INTERNAL_API_SECRET = var.internal_api_secret
    }
  }

  tags = {
    Name = "${var.project_name}-${var.environment}-cognito-post-confirmation"
  }
}

# IAM role for Lambda execution
resource "aws_iam_role" "lambda_cognito_execution" {
  name = "${var.project_name}-${var.environment}-lambda-cognito-role"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Action = "sts:AssumeRole"
        Effect = "Allow"
        Principal = {
          Service = "lambda.amazonaws.com"
        }
      }
    ]
  })

  tags = {
    Name = "${var.project_name}-${var.environment}-lambda-cognito-role"
  }
}

# Attach basic Lambda execution policy
resource "aws_iam_role_policy_attachment" "lambda_basic_execution" {
  role       = aws_iam_role.lambda_cognito_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
}

# Lambda permission for Cognito to invoke
resource "aws_lambda_permission" "cognito_invoke" {
  statement_id  = "AllowCognitoInvoke"
  action        = "lambda:InvokeFunction"
  function_name = aws_lambda_function.cognito_post_confirmation.function_name
  principal     = "cognito-idp.amazonaws.com"
  source_arn    = aws_cognito_user_pool.main.arn
}

# Outputs
output "cognito_user_pool_id" {
  description = "Cognito User Pool ID"
  value       = aws_cognito_user_pool.main.id
}

output "cognito_user_pool_arn" {
  description = "Cognito User Pool ARN"
  value       = aws_cognito_user_pool.main.arn
}

output "cognito_web_client_id" {
  description = "Cognito Web Client ID"
  value       = aws_cognito_user_pool_client.web_client.id
}

output "cognito_backend_client_id" {
  description = "Cognito Backend Client ID"
  value       = aws_cognito_user_pool_client.backend_client.id
  sensitive   = true
}

output "cognito_backend_client_secret" {
  description = "Cognito Backend Client Secret"
  value       = aws_cognito_user_pool_client.backend_client.client_secret
  sensitive   = true
}

output "cognito_user_pool_domain" {
  description = "Cognito User Pool Domain"
  value       = aws_cognito_user_pool_domain.main.domain
}

output "cognito_issuer_uri" {
  description = "Cognito OIDC Issuer URI"
  value       = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.main.id}"
}

output "cognito_jwks_uri" {
  description = "Cognito JWKS URI"
  value       = "https://cognito-idp.${var.aws_region}.amazonaws.com/${aws_cognito_user_pool.main.id}/.well-known/jwks.json"
}
