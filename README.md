# Kotlin Lambda for Slack Events

A Kotlin-based AWS Lambda function that handles Slack events, deployed automatically via GitHub Actions.

## Prerequisites

- AWS Account with appropriate permissions
- GitHub repository secrets configured:
  - `AWS_ROLE_ARN`: ARN of the IAM role for GitHub Actions OIDC

## Project Structure

```
.
├── src/
│   ├── main/kotlin/
│   │   ├── Handler.kt              # Lambda handler
│   │   └── model/SlackEvent.kt     # Data models
│   └── test/kotlin/
│       └── HandlerTest.kt          # Unit tests
├── terraform/
│   ├── main.tf                     # Main Terraform configuration
│   ├── backend.tf                  # S3 backend for state management
│   ├── github-oidc.tf              # GitHub Actions OIDC configuration
│   ├── variables.tf                # Input variables
│   └── outputs.tf                  # Output values
└── .github/workflows/
    └── deploy.yml                  # CI/CD workflow
```

## Local Development

### Build and Test

```bash
./gradlew test
./gradlew shadowJar
```

### Manual Deployment

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

## CI/CD

The project uses GitHub Actions for automated deployment:

1. **Build and Test**: Runs on every push and PR to `main`/`master` branches
   - Builds the Kotlin Lambda with Gradle
   - Runs unit tests
   - Creates JAR artifact

2. **Deploy**: Runs only on push to `main`/`master` branches
   - Authenticates to AWS using OIDC
   - Deploys infrastructure using Terraform
   - Updates Lambda function

## Architecture

- **Runtime**: Java 21 (AWS Corretto)
- **Framework**: Kotlin with kotlinx.serialization
- **AWS Services**:
  - Lambda Function with Function URL
  - IAM Roles for Lambda execution and GitHub Actions
  - S3 for Terraform state
  - DynamoDB for state locking
  - CloudWatch for logs

## Slack Event Handling

The Lambda function handles two types of Slack events:

1. **url_verification**: Returns the challenge parameter for Slack app verification
2. **event_callback**: Processes message events from Slack

## License

MIT
