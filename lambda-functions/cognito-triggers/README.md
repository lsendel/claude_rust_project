# Cognito Lambda Triggers

AWS Lambda functions triggered by Cognito authentication events.

## Functions

### post-confirmation.ts

Triggered after a user successfully confirms their email address. Creates or updates the user in the backend database.

**Environment Variables:**
- `API_BASE_URL`: Backend API base URL (e.g., `https://api.example.com`)
- `API_SECRET`: Secret for authenticating Lambda to backend (stored in AWS Secrets Manager)

**Trigger Type:** Post-confirmation

**IAM Permissions Required:**
- `secretsmanager:GetSecretValue` - To retrieve API secret
- `logs:CreateLogGroup` - To create CloudWatch log groups
- `logs:CreateLogStream` - To create CloudWatch log streams
- `logs:PutLogEvents` - To write logs

## Development

```bash
# Install dependencies
npm install

# Build TypeScript
npm run build

# Run tests
npm test

# Lint code
npm run lint
```

## Deployment

The Lambda functions are deployed via Terraform (see `/infrastructure/terraform/lambda.tf`).

To manually deploy:

```bash
# Build the function
npm run build

# Package for Lambda
cd dist
zip -r ../function.zip .
cd ..

# Upload to Lambda (via AWS CLI or Terraform)
aws lambda update-function-code \
  --function-name cognito-post-confirmation \
  --zip-file fileb://function.zip
```

## Error Handling

The post-confirmation handler is designed to be resilient:
- If backend API call fails, the confirmation still succeeds
- User will be created on first login via JWT token extraction
- Errors are logged to CloudWatch for monitoring

This ensures that Cognito authentication flow is never blocked by backend issues.
