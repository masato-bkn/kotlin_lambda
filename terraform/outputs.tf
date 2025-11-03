output "lambda_function_arn" {
  description = "ARN of the Lambda function"
  value       = aws_lambda_function.kotlin_lambda.arn
}

output "lambda_function_name" {
  description = "Name of the Lambda function"
  value       = aws_lambda_function.kotlin_lambda.function_name
}

output "lambda_invoke_arn" {
  description = "Invoke ARN of the Lambda function"
  value       = aws_lambda_function.kotlin_lambda.invoke_arn
}

output "function_url" {
  description = "Lambda Function URL"
  value       = aws_lambda_function_url.url.function_url
}

output "github_actions_role_arn" {
  description = "ARN of the IAM role for GitHub Actions"
  value       = aws_iam_role.github_actions.arn
}
