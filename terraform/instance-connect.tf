# EC2 Instance Connect Endpoint

# Security Group for Instance Connect Endpoint
resource "aws_security_group" "instance_connect" {
  name        = "${var.project_name}-instance-connect-sg"
  description = "Security group for EC2 Instance Connect Endpoint"
  vpc_id      = aws_vpc.main.id

  # Allow outbound SSH to EC2 instances in VPC
  egress {
    description = "SSH to EC2 instances"
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = [var.vpc_cidr]
  }

  tags = {
    Name = "${var.project_name}-instance-connect-sg"
  }
}

# EC2 Instance Connect Endpoint
resource "aws_ec2_instance_connect_endpoint" "main" {
  subnet_id          = aws_subnet.public.id
  security_group_ids = [aws_security_group.instance_connect.id]

  tags = {
    Name = "${var.project_name}-instance-connect-endpoint"
  }
}

# Output for easy reference
output "instance_connect_endpoint_id" {
  description = "EC2 Instance Connect Endpoint ID"
  value       = aws_ec2_instance_connect_endpoint.main.id
}

output "instance_connect_endpoint_dns" {
  description = "EC2 Instance Connect Endpoint DNS"
  value       = aws_ec2_instance_connect_endpoint.main.dns_name
}
