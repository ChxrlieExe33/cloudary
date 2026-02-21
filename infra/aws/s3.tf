resource "aws_s3_bucket" "cloudary_uploads_bucket" {
  bucket = "cloudary-uploads"

  tags = {
    Name = "cloudary-uploads"
  }
}

# Enable versioning on the bucket
resource "aws_s3_bucket_versioning" "cloudary_uploads_versioning" {

  bucket = aws_s3_bucket.cloudary_uploads_bucket.id

  versioning_configuration {
    status = "Enabled"
  }
}

# The policy allowing ecs to access the bucket.
resource "aws_iam_policy" "ecs_bucket_policy" {
  name = "cloudary-ecs-s3-access"

  policy = jsonencode({
    Version = "2012-10-17"
    Statement = [
      {
        Effect = "Allow"
        Action = [
          "s3:GetObject",
          "s3:PutObject",
          "s3:DeleteObject"
        ]
        Resource = aws_s3_bucket.cloudary_uploads_bucket.arn
      },
      {
        Effect = "Allow"
        Action = ["s3:ListBucket"]
        Resource = aws_s3_bucket.cloudary_uploads_bucket.arn
      }
    ]
  })
}

# The attachment to join them.
resource "aws_iam_role_policy_attachment" "ecs_bucket_policy_attachment" {

  policy_arn = aws_iam_policy.ecs_bucket_policy.arn
  role       = aws_iam_role.ecs_task_role.name
}