# S3 Bucket for Application Data
resource "aws_s3_bucket" "app_data" {
  bucket = "${var.project_name}-app-data-${data.aws_caller_identity.current.account_id}"

  tags = {
    Name    = "${var.project_name}-app-data"
    Purpose = "Application Data Storage"
  }
}

# Block Public Access
resource "aws_s3_bucket_public_access_block" "app_data" {
  bucket = aws_s3_bucket.app_data.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

# Server-Side Encryption
resource "aws_s3_bucket_server_side_encryption_configuration" "app_data" {
  bucket = aws_s3_bucket.app_data.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm = "AES256"
    }
  }
}

# Versioning
resource "aws_s3_bucket_versioning" "app_data" {
  bucket = aws_s3_bucket.app_data.id

  versioning_configuration {
    status = var.enable_s3_versioning ? "Enabled" : "Suspended"
  }
}

# Lifecycle Policy (Cost Optimization)
resource "aws_s3_bucket_lifecycle_configuration" "app_data" {
  bucket = aws_s3_bucket.app_data.id

  rule {
    id     = "archive-old-data"
    status = "Enabled"

    filter {}

    transition {
      days          = var.s3_lifecycle_days
      storage_class = "GLACIER_IR"
    }

    expiration {
      days = 365
    }
  }

  rule {
    id     = "delete-incomplete-uploads"
    status = "Enabled"

    filter {}

    abort_incomplete_multipart_upload {
      days_after_initiation = 7
    }
  }
}


# Data source for current AWS account
data "aws_caller_identity" "current" {}
