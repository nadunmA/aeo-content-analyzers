terraform {
  required_version = ">= 1.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
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
