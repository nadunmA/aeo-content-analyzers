output "instance_id" {
  description = "EC2 Instance ID"
  value       = aws_instance.app.id
}

output "instance_public_ip" {
  description = "EC2 Public IP (This will change on restart)"
  value       = aws_instance.app.public_ip
}

output "elastic_ip" {
  description = "Elastic IP (Static, use this for DNS)"
  value       = aws_eip.app.public_ip
}

output "frontend_url" {
  description = "Frontend Application URL"
  value       = "http://${aws_eip.app.public_ip}:2000"
}

output "backend_url" {
  description = "Backend API URL"
  value       = "http://${aws_eip.app.public_ip}:6060"
}

output "prometheus_url" {
  description = "Prometheus Metrics URL"
  value       = "http://${aws_eip.app.public_ip}:9090"
}

output "grafana_url" {
  description = "Grafana Dashboard URL"
  value       = "http://${aws_eip.app.public_ip}:3000"
}

output "grafana_credentials" {
  description = "Grafana login credentials"
  value       = "Username: admin, Password: (from terraform.tfvars app_password)"
  sensitive   = false
}

output "s3_bucket_name" {
  description = "S3 Bucket for application data"
  value       = aws_s3_bucket.app_data.id
}

output "s3_bucket_arn" {
  description = "S3 Bucket ARN"
  value       = aws_s3_bucket.app_data.arn
}

output "cloudwatch_log_group" {
  description = "CloudWatch Log Group"
  value       = aws_cloudwatch_log_group.app.name
}

output "cloudwatch_dashboard_url" {
  description = "CloudWatch Dashboard URL"
  value       = "https://console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}#dashboards:name=${aws_cloudwatch_dashboard.main.dashboard_name}"
}

output "ssh_command" {
  description = "SSH command to connect to instance"
  value       = "Use AWS Console or: aws ec2-instance-connect ssh --instance-id ${aws_instance.app.id} --region ${var.aws_region}"
}

output "deployment_instructions" {
  description = "Next steps for deployment"
  value       = <<-EOT

    ═══════════════════════════════════════════════════════════
    🎉 AEO Content Analyzer Infrastructure Created Successfully!
    ═══════════════════════════════════════════════════════════

    📋 Instance Details:
       • Instance ID: ${aws_instance.app.id}
       • Elastic IP:  ${aws_eip.app.public_ip}
       • Instance Type: ${var.instance_type}
       • Region: ${var.aws_region}

    🌐 Access URLs:
       • Frontend: http://${aws_eip.app.public_ip}:2000
       • Backend:  http://${aws_eip.app.public_ip}:6060

    📦 Resources Created:
       • VPC & Networking: ✓
       • Security Groups: ✓
       • S3 Bucket: ${aws_s3_bucket.app_data.id}
       • IAM Role: ✓
       • CloudWatch: ✓
       • Budget Alerts: ✓

    🚀 Next Steps:

    1. Connect to instance:
       ssh -i ${var.key_name}.pem ubuntu@${aws_eip.app.public_ip}

    2. Upload your project:
       scp -i ${var.key_name}.pem -r /path/to/project ubuntu@${aws_eip.app.public_ip}:/opt/aeo-analyzer/

    3. Deploy application:
       cd /opt/aeo-analyzer
       docker-compose up -d

    4. Check status:
       docker-compose ps
       docker-compose logs -f

    📊 Monitoring:
       CloudWatch: https://console.aws.amazon.com/cloudwatch/home?region=${var.aws_region}

    💰 Budget Alert: ${var.budget_alert_email}

    ═══════════════════════════════════════════════════════════
  EOT
}
