terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }

  # Optional: Store state in S3 (uncomment after first apply)
  # backend "s3" {
  #   bucket = "aeo-terraform-state"
  #   key    = "terraform.tfstate"
  #   region = "us-east-1"
  # }
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = {
      Project     = "AEO-Content-Analyzer"
      Environment = var.environment
      ManagedBy   = "Terraform"
      Owner       = "AEO-Developer"
    }
  }
}
