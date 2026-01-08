# Elastic IP
resource "aws_eip" "app" {
  domain   = "vpc"
  instance = aws_instance.app.id

  tags = {
    Name = "${var.project_name}-eip"
  }

  depends_on = [aws_internet_gateway.main]
}

# EC2 Instance
resource "aws_instance" "app" {
  ami           = var.ami_id
  instance_type = var.instance_type
  key_name      = var.key_name

  subnet_id                   = aws_subnet.public.id
  vpc_security_group_ids      = [aws_security_group.ec2.id]
  iam_instance_profile        = aws_iam_instance_profile.ec2_profile.name
  associate_public_ip_address = true

  # Root volume configuration
  root_block_device {
    volume_type           = var.root_volume_type
    volume_size           = var.root_volume_size
    delete_on_termination = true
    encrypted             = true

    # gp3 specific settings
    iops       = var.root_volume_type == "gp3" ? 3000 : null
    throughput = var.root_volume_type == "gp3" ? 125 : null

    tags = {
      Name = "${var.project_name}-root-volume"
    }
  }

  monitoring = var.enable_detailed_monitoring

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    project_name       = var.project_name
    mongodb_uri        = var.mongodb_uri
    gemini_api_key     = var.gemini_api_key
    openrouter_api_key = var.openrouter_api_key
    app_username       = var.app_username
    app_password       = var.app_password
    s3_bucket          = aws_s3_bucket.app_data.id
    aws_region         = var.aws_region
  }))

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  tags = {
    Name = "${var.project_name}-app-server"
  }

  lifecycle {
    ignore_changes = [
      ami,
      user_data
    ]
  }
}

# CloudWatch Alarms for EC2
resource "aws_cloudwatch_metric_alarm" "cpu_high" {
  alarm_name          = "${var.project_name}-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = 300
  statistic           = "Average"
  threshold           = 80
  alarm_description   = "Alert when CPU exceeds 80%"
  alarm_actions       = []

  dimensions = {
    InstanceId = aws_instance.app.id
  }
}

resource "aws_cloudwatch_metric_alarm" "status_check_failed" {
  alarm_name          = "${var.project_name}-status-check-failed"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = 2
  metric_name         = "StatusCheckFailed"
  namespace           = "AWS/EC2"
  period              = 60
  statistic           = "Maximum"
  threshold           = 0
  alarm_description   = "Alert when instance fails status checks"
  alarm_actions       = []

  dimensions = {
    InstanceId = aws_instance.app.id
  }
}
