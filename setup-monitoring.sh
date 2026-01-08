#!/bin/bash
# Prometheus & Node Exporter Setup for AEO Content Analyzer
# Run this on your EC2 instance

set -e

echo "🚀 Setting up Prometheus monitoring..."

# Create directories
echo "📁 Creating directories..."
mkdir -p /opt/aeo-analyzer/prometheus
mkdir -p /opt/aeo-analyzer/grafana/provisioning/datasources

# Create Prometheus configuration
echo "⚙️ Creating Prometheus config..."
cat > /opt/aeo-analyzer/prometheus/prometheus.yml <<'EOF'
global:
  scrape_interval: 15s
  evaluation_interval: 15s
  external_labels:
    monitor: 'aeo-analyzer'

scrape_configs:
  # Node Exporter (System Metrics)
  - job_name: 'node-exporter'
    static_configs:
      - targets: ['node-exporter:9100']
        labels:
          instance: 'aeo-ec2'
          environment: 'production'

  # Spring Boot Backend (if configured)
  - job_name: 'spring-boot'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:6060']
        labels:
          application: 'aeo-backend'

  # Frontend (if configured)
  - job_name: 'frontend'
    static_configs:
      - targets: ['frontend:2000']
        labels:
          application: 'aeo-frontend'
EOF

# Create Grafana datasource
echo "📊 Creating Grafana datasource..."
cat > /opt/aeo-analyzer/grafana/provisioning/datasources/prometheus.yml <<'EOF'
apiVersion: 1

datasources:
  - name: Prometheus
    type: prometheus
    access: proxy
    url: http://prometheus:9090
    isDefault: true
    editable: true
    jsonData:
      timeInterval: "15s"
EOF

# Backup existing docker-compose
echo "💾 Backing up docker-compose.yml..."
cp /opt/aeo-analyzer/docker-compose.yml /opt/aeo-analyzer/docker-compose.yml.backup.$(date +%Y%m%d_%H%M%S)

# Create new docker-compose with monitoring
echo "🐳 Updating docker-compose.yml..."
cat > /opt/aeo-analyzer/docker-compose.yml <<'EOF'
version: '3.8'

networks:
  app-network:
    driver: bridge

volumes:
  grafana-storage:
  prometheus-storage:

services:
  # Node Exporter - System Metrics
  node-exporter:
    image: prom/node-exporter:latest
    container_name: node-exporter
    command:
      - '--path.procfs=/host/proc'
      - '--path.sysfs=/host/sys'
      - '--path.rootfs=/rootfs'
      - '--collector.filesystem.mount-points-exclude=^/(sys|proc|dev|host|etc)($$|/)'
    volumes:
      - /proc:/host/proc:ro
      - /sys:/host/sys:ro
      - /:/rootfs:ro
    ports:
      - "9100:9100"
    networks:
      - app-network
    restart: unless-stopped

  # Prometheus - Metrics Collection
  prometheus:
    image: prom/prometheus:latest
    container_name: prometheus
    command:
      - '--config.file=/etc/prometheus/prometheus.yml'
      - '--storage.tsdb.path=/prometheus'
      - '--web.console.libraries=/usr/share/prometheus/console_libraries'
      - '--web.console.templates=/usr/share/prometheus/consoles'
      - '--web.enable-lifecycle'
    volumes:
      - ./prometheus/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-storage:/prometheus
    ports:
      - "9090:9090"
    networks:
      - app-network
    restart: unless-stopped
    depends_on:
      - node-exporter

  # Grafana - Visualization
  grafana:
    image: grafana/grafana:latest
    container_name: grafana
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=${GRAFANA_PASSWORD:-admin}
      - GF_USERS_ALLOW_SIGN_UP=false
      - GF_SERVER_ROOT_URL=http://localhost:3000
      - GF_INSTALL_PLUGINS=grafana-clock-panel
    volumes:
      - grafana-storage:/var/lib/grafana
      - ./grafana/provisioning:/etc/grafana/provisioning
    ports:
      - "3000:3000"
    networks:
      - app-network
    restart: unless-stopped
    depends_on:
      - prometheus

  # Your Backend Service (Example - adjust ports/config as needed)
  # backend:
  #   image: your-backend-image
  #   container_name: backend
  #   ports:
  #     - "6060:6060"
  #   networks:
  #     - app-network
  #   restart: unless-stopped

  # Your Frontend Service (Example - adjust ports/config as needed)
  # frontend:
  #   image: your-frontend-image
  #   container_name: frontend
  #   ports:
  #     - "2000:2000"
  #   networks:
  #     - app-network
  #   restart: unless-stopped
EOF

echo "✅ Configuration complete!"
echo ""
echo "🔄 Restarting services..."
cd /opt/aeo-analyzer
docker-compose down
docker-compose up -d

echo ""
echo "⏳ Waiting for services to start..."
sleep 15

echo ""
echo "✅ Setup complete!"
echo ""
echo "📊 Access URLs:"
echo "  • Prometheus: http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):9090"
echo "  • Grafana:    http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):3000"
echo "  • Metrics:    http://$(curl -s http://169.254.169.254/latest/meta-data/public-ipv4):9100/metrics"
echo ""
echo "🔐 Grafana Login:"
echo "  Username: admin"
echo "  Password: admin (or check your .env file)"
echo ""
echo "✅ Dashboard should now show data!"
echo ""
echo "🔍 Check status:"
echo "  docker-compose ps"
echo "  docker-compose logs -f prometheus"
echo "  docker-compose logs -f node-exporter"
