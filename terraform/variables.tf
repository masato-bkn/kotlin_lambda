variable "aws_region" {
  description = "AWS region to deploy the Lambda function"
  type        = string
  default     = "ap-northeast-1"
}

variable "lambda_function_name" {
  description = "Name of the Lambda function"
  type        = string
  default     = "kotlin-lambda-hello-world"
}

variable "github_org" {
  description = "GitHub organization or username"
  type        = string
}

variable "github_repo" {
  description = "GitHub repository name"
  type        = string
}
