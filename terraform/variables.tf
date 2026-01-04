variable "aws_region" {
  description = "AWS Region"
  type        = string
  default     = "us-east-1"
}

variable "environment" {
  description = "Environment name"
  type        = string
  default     = "dev"
}

variable "project_name" {
  description = "Project name"
  type        = string
  default     = "aeo-analyzer"
}

# VPC Configuration
variable "vpc_cidr" {
  description = "VPC CIDR block"
  type        = string
  default     = "10.0.0.0/16"
}

variable "public_subnet_cidr" {
  description = "Public subnet CIDR"
  type        = string
  default     = "10.0.1.0/24"
}

variable "availability_zone" {
  description = "Availability Zone"
  type        = string
  default     = "us-east-1a"
}

# EC2 Configuration
variable "instance_type" {
  description = "EC2 instance type"
  type        = string
  default     = "t3.small"
}

variable "root_volume_size" {
  description = "Root volume size in GB"
  type        = number
  default     = 30
}

variable "root_volume_type" {
  description = "Root volume type (gp3, gp2, io1)"
  type        = string
  default     = "gp3"
}

variable "enable_prometheus_grafana" {
  description = "Enable Prometheus and Grafana monitoring"
  type        = bool
  default     = false
}

variable "ami_id" {
  description = "Ubuntu 24.04 LTS AMI ID"
  type        = string
  default     = "ami-0e2c8caa4b6378d8c" # Ubuntu 24.04 LTS us-east-1
}

variable "key_name" {
  description = "EC2 Key Pair name (create this in AWS console first)"
  type        = string
}

variable "allowed_ssh_ips" {
  description = "IPs allowed to SSH (use your IP)"
  type        = list(string)
  default     = ["0.0.0.0/0"] # Change to your IP for security
}

# Application Configuration
variable "mongodb_uri" {
  description = "MongoDB connection URI"
  type        = string
  sensitive   = true
}

variable "gemini_api_key" {
  description = "Google Gemini API key"
  type        = string
  sensitive   = true
}

variable "openrouter_api_key" {
  description = "OpenRouter API key"
  type        = string
  sensitive   = true
}

variable "app_username" {
  description = "Application admin username"
  type        = string
  default     = "admin"
  sensitive   = true
}

variable "app_password" {
  description = "Application admin password"
  type        = string
  sensitive   = true
}

# CloudWatch Configuration
variable "enable_detailed_monitoring" {
  description = "Enable detailed CloudWatch monitoring (costs extra)"
  type        = bool
  default     = false
}

# Budget Configuration
variable "monthly_budget_limit" {
  description = "Monthly budget limit in USD"
  type        = number
  default     = 10
}

variable "budget_alert_email" {
  description = "Email for budget alerts"
  type        = string
}

# S3 Configuration
variable "enable_s3_versioning" {
  description = "Enable S3 versioning"
  type        = bool
  default     = false
}

variable "s3_lifecycle_days" {
  description = "Days before moving to Glacier"
  type        = number
  default     = 30
}
