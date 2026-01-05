#!/bin/bash
set -e

# Logging
exec > >(tee /var/log/user-data.log)
exec 2>&1

echo "=== Starting AEO Content Analyzer Setup ==="
echo "Timestamp: $(date)"

# Update system
echo "Updating system packages..."
apt-get update
apt-get upgrade -y

# Install required packages
echo "Installing required packages..."
apt-get install -y \
    apt-transport-https \
    ca-certificates \
    curl \
    gnupg \
    lsb-release \
    unzip \
    git \
    htop

# Configure Swap
echo "Configuring 4GB swap space..."
fallocate -l 4G /swapfile
chmod 600 /swapfile
mkswap /swapfile
swapon /swapfile
echo '/swapfile none swap sw 0 0' >> /etc/fstab

# Optimize swap usage
echo 'vm.swappiness=10' >> /etc/sysctl.conf
echo 'vm.vfs_cache_pressure=50' >> /etc/sysctl.conf
sysctl -p

# Install Docker
echo "Installing Docker..."
curl -fsSL https://get.docker.com -o get-docker.sh
sh get-docker.sh
usermod -aG docker ubuntu

# Install Docker Compose
echo "Installing Docker Compose..."
DOCKER_COMPOSE_VERSION="v2.24.5"
curl -L "https://github.com/docker/compose/releases/download/$DOCKER_COMPOSE_VERSION/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose
ln -sf /usr/local/bin/docker-compose /usr/bin/docker-compose

# Install AWS CLI
echo "Installing AWS CLI..."
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
unzip awscliv2.zip
./aws/install
rm -rf aws awscliv2.zip

# Install CloudWatch Agent (optional, commented out to save costs)
# echo "Installing CloudWatch Agent..."
# wget https://s3.amazonaws.com/amazoncloudwatch-agent/ubuntu/amd64/latest/amazon-cloudwatch-agent.deb
# dpkg -i -E ./amazon-cloudwatch-agent.deb
# rm amazon-cloudwatch-agent.deb

# Create application directory
echo "Creating application directory..."
mkdir -p /opt/aeo-analyzer
cd /opt/aeo-analyzer

# Create Docker volumes directory for Prometheus & Grafana
echo "Creating monitoring volumes..."
mkdir -p /opt/aeo-analyzer/prometheus/data
mkdir -p /opt/aeo-analyzer/grafana/data
chown -R 65534:65534 /opt/aeo-analyzer/prometheus/data
chown -R 472:472 /opt/aeo-analyzer/grafana/data

# Create .env file
echo "Creating environment configuration..."
cat > .env << 'EOF'
# MongoDB
MONGODB_URI=${mongodb_uri}

# API Keys
GEMINI_API_KEY=${gemini_api_key}
OPENROUTER_API_KEY=${openrouter_api_key}

# App Credentials
APP_USERNAME=${app_username}
APP_PASSWORD=${app_password}

# AWS
AWS_DEFAULT_REGION=${aws_region}
S3_BUCKET=${s3_bucket}

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=6060
JAVA_OPTS=-Xms512m -Xmx2048m

# Monitoring (Prometheus/Grafana enabled)
ENABLE_MONITORING=true
EOF

# Set proper permissions
chown -R ubuntu:ubuntu /opt/aeo-analyzer
chmod 600 /opt/aeo-analyzer/.env

# Create systemd service for Docker Compose
echo "Creating systemd service..."
cat > /etc/systemd/system/aeo-analyzer.service << 'EOF'
[Unit]
Description=AEO Content Analyzer
Requires=docker.service
After=docker.service

[Service]
Type=oneshot
RemainAfterExit=yes
WorkingDirectory=/opt/aeo-analyzer
ExecStart=/usr/local/bin/docker-compose up -d
ExecStop=/usr/local/bin/docker-compose down
User=ubuntu
Group=ubuntu

[Install]
WantedBy=multi-user.target
EOF

# Enable service (will start after docker-compose.yml is added)
systemctl daemon-reload
# systemctl enable aeo-analyzer.service

# Configure log rotation
echo "Configuring log rotation..."
cat > /etc/logrotate.d/aeo-analyzer << 'EOF'
/var/log/aeo-analyzer/*.log {
    daily
    rotate 7
    compress
    delaycompress
    missingok
    notifempty
    create 0644 ubuntu ubuntu
}
EOF

mkdir -p /var/log/aeo-analyzer
chown ubuntu:ubuntu /var/log/aeo-analyzer

# Setup monitoring script (simple resource monitoring)
echo "Setting up monitoring..."
cat > /usr/local/bin/aeo-monitor.sh << 'EOF'
#!/bin/bash
# Simple monitoring script
LOG_FILE="/var/log/aeo-analyzer/monitor.log"

echo "=== $(date) ===" >> $LOG_FILE
echo "CPU: $(top -bn1 | grep "Cpu(s)" | awk '{print $2}')" >> $LOG_FILE
echo "Memory: $(free -h | grep Mem | awk '{print $3 "/" $2}')" >> $LOG_FILE
echo "Disk: $(df -h / | tail -1 | awk '{print $3 "/" $2 " (" $5 ")"}')" >> $LOG_FILE
echo "Docker Containers: $(docker ps --format 'table {{.Names}}\t{{.Status}}' 2>/dev/null || echo 'Docker not running')" >> $LOG_FILE
echo "" >> $LOG_FILE
EOF

chmod +x /usr/local/bin/aeo-monitor.sh

# Add monitoring cron job (every 5 minutes)
(crontab -l 2>/dev/null; echo "*/5 * * * * /usr/local/bin/aeo-monitor.sh") | crontab -

# Create deployment instructions
cat > /home/ubuntu/DEPLOYMENT_INSTRUCTIONS.md << 'EOF'
# AEO Content Analyzer Deployment

## Your application is ready! Follow these steps:

### 1. Upload your application code:
```bash
# From your local machine:
scp -r /path/to/your/project ubuntu@YOUR_EC2_IP:/opt/aeo-analyzer/
```

### 2. SSH into the instance:
```bash
ssh -i your-key.pem ubuntu@YOUR_EC2_IP
```

### 3. Navigate and deploy:
```bash
cd /opt/aeo-analyzer
docker-compose up -d
```

### 4. Check status:
```bash
docker-compose ps
docker-compose logs -f
```

### 5. Access your application:
- Frontend: http://YOUR_EC2_IP:2000
- Backend: http://YOUR_EC2_IP:6060

## Useful Commands:

### Docker Management:
```bash
# View logs
docker-compose logs -f [service_name]

# Restart services
docker-compose restart

# Stop services
docker-compose down

# Rebuild and restart
docker-compose up -d --build
```

### System Monitoring:
```bash
# Check resources
htop

# View monitoring logs
tail -f /var/log/aeo-analyzer/monitor.log

# Check swap usage
free -h
```

### Service Management:
```bash
# Start service
sudo systemctl start aeo-analyzer

# Check status
sudo systemctl status aeo-analyzer

# View logs
sudo journalctl -u aeo-analyzer -f
```

## Environment Variables:
Configuration is in: /opt/aeo-analyzer/.env

## Backup S3 Bucket:
Your data bucket: ${s3_bucket}
EOF

chown ubuntu:ubuntu /home/ubuntu/DEPLOYMENT_INSTRUCTIONS.md

echo "=== Setup Complete! ==="
echo "Instance is ready for deployment."
echo "Check /home/ubuntu/DEPLOYMENT_INSTRUCTIONS.md for next steps."
