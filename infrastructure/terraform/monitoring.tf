# T088, T089: CloudWatch Log Groups and X-Ray Tracing Configuration
# Multi-Tenant SaaS Platform - Monitoring Infrastructure

##############################################################################
# CloudWatch Log Groups (T088)
##############################################################################

# Backend API Log Group
resource "aws_cloudwatch_log_group" "backend_api" {
  name              = "/aws/ecs/${var.environment}/backend-api"
  retention_in_days = var.log_retention_days

  tags = {
    Name        = "${var.project_name}-backend-logs"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# Frontend CloudFront Log Group
resource "aws_cloudwatch_log_group" "frontend_cloudfront" {
  name              = "/aws/cloudfront/${var.environment}/frontend"
  retention_in_days = var.log_retention_days

  tags = {
    Name        = "${var.project_name}-frontend-logs"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# Lambda Automation Engine Log Group
resource "aws_cloudwatch_log_group" "lambda_automation" {
  name              = "/aws/lambda/${var.environment}/automation-engine"
  retention_in_days = var.log_retention_days

  tags = {
    Name        = "${var.project_name}-automation-logs"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# Lambda Cognito Triggers Log Group
resource "aws_cloudwatch_log_group" "lambda_cognito_triggers" {
  name              = "/aws/lambda/${var.environment}/cognito-triggers"
  retention_in_days = var.log_retention_days

  tags = {
    Name        = "${var.project_name}-cognito-triggers-logs"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# Application Security Audit Log Group
resource "aws_cloudwatch_log_group" "security_audit" {
  name              = "/aws/security/${var.environment}/audit"
  retention_in_days = 90 # Minimum 90 days per constitution

  tags = {
    Name        = "${var.project_name}-security-audit"
    Environment = var.environment
    ManagedBy   = "Terraform"
    Compliance  = "SecurityAudit"
  }
}

##############################################################################
# CloudWatch Log Metric Filters - Performance & Security Monitoring
##############################################################################

# API Response Time Metric
resource "aws_cloudwatch_log_metric_filter" "api_response_time" {
  name           = "ApiResponseTime"
  log_group_name = aws_cloudwatch_log_group.backend_api.name
  pattern        = "[timestamp, request_id, method, path, status, duration_ms]"

  metric_transformation {
    name      = "ApiResponseTimeMs"
    namespace = "${var.project_name}/Performance"
    value     = "$duration_ms"
    unit      = "Milliseconds"
  }
}

# Authentication Failures Metric (Constitution requirement: FR-007)
resource "aws_cloudwatch_log_metric_filter" "auth_failures" {
  name           = "AuthenticationFailures"
  log_group_name = aws_cloudwatch_log_group.security_audit.name
  pattern        = "[timestamp, level=ERROR, event=AUTH_FAILURE, ...]"

  metric_transformation {
    name      = "AuthenticationFailureCount"
    namespace = "${var.project_name}/Security"
    value     = "1"
    unit      = "Count"
  }
}

# Authorization Failures Metric (Constitution requirement: FR-007)
resource "aws_cloudwatch_log_metric_filter" "authz_failures" {
  name           = "AuthorizationFailures"
  log_group_name = aws_cloudwatch_log_group.security_audit.name
  pattern        = "[timestamp, level=ERROR, event=AUTHZ_FAILURE, ...]"

  metric_transformation {
    name      = "AuthorizationFailureCount"
    namespace = "${var.project_name}/Security"
    value     = "1"
    unit      = "Count"
  }
}

# Quota Exceeded Events Metric (FR-016 monitoring)
resource "aws_cloudwatch_log_metric_filter" "quota_exceeded" {
  name           = "QuotaExceeded"
  log_group_name = aws_cloudwatch_log_group.backend_api.name
  pattern        = "[timestamp, level=WARN, event=QUOTA_EXCEEDED, ...]"

  metric_transformation {
    name      = "QuotaExceededCount"
    namespace = "${var.project_name}/Business"
    value     = "1"
    unit      = "Count"
  }
}

##############################################################################
# CloudWatch Alarms - SLA & Security Monitoring
##############################################################################

# API Response Time Alarm (SC-003: <2s for 95% of requests)
resource "aws_cloudwatch_metric_alarm" "api_response_time_high" {
  alarm_name          = "${var.environment}-api-response-time-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "ApiResponseTimeMs"
  namespace           = "${var.project_name}/Performance"
  period              = 300
  statistic           = "p95"
  threshold           = 2000
  alarm_description   = "API p95 response time exceeded 2 seconds"
  treat_missing_data  = "notBreaching"

  alarm_actions = var.alarm_sns_topic_arn != "" ? [var.alarm_sns_topic_arn] : []

  tags = {
    Name        = "${var.project_name}-api-response-alarm"
    Environment = var.environment
    Severity    = "High"
  }
}

# Authentication Failures Alarm (Security monitoring)
resource "aws_cloudwatch_metric_alarm" "auth_failures_high" {
  alarm_name          = "${var.environment}-auth-failures-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 1
  metric_name         = "AuthenticationFailureCount"
  namespace           = "${var.project_name}/Security"
  period              = 300
  statistic           = "Sum"
  threshold           = 50
  alarm_description   = "High number of authentication failures detected"
  treat_missing_data  = "notBreaching"

  alarm_actions = var.alarm_sns_topic_arn != "" ? [var.alarm_sns_topic_arn] : []

  tags = {
    Name        = "${var.project_name}-auth-failure-alarm"
    Environment = var.environment
    Severity    = "Critical"
  }
}

##############################################################################
# AWS X-Ray Tracing Configuration (T089)
##############################################################################

# X-Ray Sampling Rule for Backend API
resource "aws_xray_sampling_rule" "backend_api" {
  rule_name      = "${var.environment}-backend-api-sampling"
  priority       = 1000
  version        = 1
  reservoir_size = 1
  fixed_rate     = var.xray_sampling_rate
  url_path       = "/api/*"
  host           = "*"
  http_method    = "*"
  service_type   = "*"
  service_name   = "${var.environment}-backend-api"
  resource_arn   = "*"

  attributes = {
    Environment = var.environment
  }
}

# X-Ray Sampling Rule for Lambda Functions
resource "aws_xray_sampling_rule" "lambda_functions" {
  rule_name      = "${var.environment}-lambda-sampling"
  priority       = 1001
  version        = 1
  reservoir_size = 1
  fixed_rate     = var.xray_sampling_rate
  url_path       = "*"
  host           = "*"
  http_method    = "*"
  service_type   = "AWS::Lambda::Function"
  service_name   = "*"
  resource_arn   = "*"

  attributes = {
    Environment = var.environment
  }
}

# X-Ray Group for Backend API Traces
resource "aws_xray_group" "backend_api" {
  group_name        = "${var.environment}-backend-api"
  filter_expression = "service(\"${var.environment}-backend-api\") { fault = true OR error = true OR responsetime > 2 }"

  insights_configuration {
    insights_enabled      = true
    notifications_enabled = false
  }

  tags = {
    Name        = "${var.project_name}-api-xray-group"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

# X-Ray Group for Performance Analysis
resource "aws_xray_group" "performance_analysis" {
  group_name        = "${var.environment}-performance"
  filter_expression = "responsetime > 1"

  insights_configuration {
    insights_enabled      = true
    notifications_enabled = false
  }

  tags = {
    Name        = "${var.project_name}-performance-xray-group"
    Environment = var.environment
    ManagedBy   = "Terraform"
  }
}

##############################################################################
# CloudWatch Dashboard - Observability Overview
##############################################################################

resource "aws_cloudwatch_dashboard" "main" {
  dashboard_name = "${var.environment}-${var.project_name}-overview"

  dashboard_body = jsonencode({
    widgets = [
      {
        type = "metric"
        properties = {
          metrics = [
            ["${var.project_name}/Performance", "ApiResponseTimeMs", { stat = "p95" }],
            ["...", { stat = "p99" }],
            ["...", { stat = "Average" }]
          ]
          period = 300
          stat   = "Average"
          region = var.aws_region
          title  = "API Response Time (ms)"
          yAxis = {
            left = {
              min = 0
              max = 5000
            }
          }
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["${var.project_name}/Security", "AuthenticationFailureCount", { stat = "Sum" }],
            [".", "AuthorizationFailureCount", { stat = "Sum" }]
          ]
          period = 300
          stat   = "Sum"
          region = var.aws_region
          title  = "Security Events"
        }
      },
      {
        type = "metric"
        properties = {
          metrics = [
            ["${var.project_name}/Business", "QuotaExceededCount", { stat = "Sum" }]
          ]
          period = 300
          stat   = "Sum"
          region = var.aws_region
          title  = "Quota Exceeded Events"
        }
      }
    ]
  })
}

##############################################################################
# Outputs
##############################################################################

output "cloudwatch_log_groups" {
  description = "CloudWatch log group names"
  value = {
    backend_api         = aws_cloudwatch_log_group.backend_api.name
    frontend_cloudfront = aws_cloudwatch_log_group.frontend_cloudfront.name
    lambda_automation   = aws_cloudwatch_log_group.lambda_automation.name
    lambda_cognito      = aws_cloudwatch_log_group.lambda_cognito_triggers.name
    security_audit      = aws_cloudwatch_log_group.security_audit.name
  }
}

output "xray_sampling_rules" {
  description = "X-Ray sampling rule ARNs"
  value = {
    backend_api = aws_xray_sampling_rule.backend_api.arn
    lambda      = aws_xray_sampling_rule.lambda_functions.arn
  }
}

output "cloudwatch_dashboard_url" {
  description = "CloudWatch dashboard URL"
  value       = "https://console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards:name=${aws_cloudwatch_dashboard.main.dashboard_name}"
}
