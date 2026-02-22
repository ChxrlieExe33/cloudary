resource "aws_ssm_parameter" "rds_master_pass" {
  name = "/cloudary/prod/db/password"
  type = "SecureString"
  value = random_password.db_master_pass.result
}

resource "aws_ssm_parameter" "cloudary_email" {
  name = "/cloudary/prod/email/username"
  type = "SecureString"
  value = var.cloudary_email
}

resource "aws_ssm_parameter" "cloudary_email_password" {
  name = "/cloudary/prod/email/password"
  type = "SecureString"
  value = var.cloudary_email_password
}

resource "aws_iam_policy" "ecs_db_pass_policy" {
  name = "ecs-ssm-access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "ssm:GetParameter",
          "ssm:GetParameters"
        ]
        Resource = [
          aws_ssm_parameter.rds_master_pass.arn,
          aws_ssm_parameter.cloudary_email.arn,
          aws_ssm_parameter.cloudary_email_password.arn
        ]
      },
      {
        Effect = "Allow"
        Action = [
          "kms:Decrypt"
        ]
        Resource = "*"
      }
    ]
  })
}

resource "aws_iam_role_policy_attachment" "attach_ssm" {
  role       = aws_iam_role.ecs_execution_role.name # Execution role because its needed before the app starts.
  policy_arn = aws_iam_policy.ecs_db_pass_policy.arn
}