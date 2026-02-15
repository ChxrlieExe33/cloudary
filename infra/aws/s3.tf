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