# AEO Content Analyzer

Evaluate and improve content visibility for ChatGPT & Gemini. A comprehensive full-stack application built with modern technologies to analyze and optimize content for AI search engines.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Java](https://img.shields.io/badge/Java-20. 1%25-orange)
![JavaScript](https://img.shields.io/badge/JavaScript-64.9%25-yellow)
![HCL](https://img.shields.io/badge/HCL-9.8%25-purple)

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Configuration](#configuration)
- [Usage](#usage)
- [API Documentation](#api-documentation)
- [Monitoring & Observability](#monitoring--observability)
- [Deployment](#deployment)
- [Contributing](#contributing)
- [Visual Proofs & Dashboards](#-visual-proofs--dashboards)
- [License](#license)

## 🎯 Overview

AEO Content Analyzer is a sophisticated tool designed to help content creators and marketers optimize their content for AI-powered search engines like ChatGPT and Google Gemini. The application analyzes content quality, relevance, and structure to provide actionable insights for improving visibility in AI-generated responses.

## ✨ Features

- **Content Analysis**: Deep analysis of content structure, readability, and AI optimization
- **Multi-Platform Support**:  Optimize for ChatGPT, Gemini, and other AI search engines
- **Real-time Feedback**: Instant suggestions and recommendations
- **Performance Metrics**: Track content performance over time
- **REST API**: Comprehensive API for integration with other tools
- **Responsive UI**: Modern React-based user interface
- **Monitoring Dashboard**: Real-time application monitoring with Grafana
- **Code Quality**:  Integrated SonarQube analysis
- **Containerized Deployment**: Docker support for easy deployment
- **Cloud-Ready**: AWS infrastructure with Terraform

## 🛠 Tech Stack

### Backend
- **Spring Boot** - Java framework for building the REST API
- **MongoDB** - NoSQL database for storing content and analysis data
- **Java 17+** - Core programming language

### Frontend
- **React** - UI framework for building the web interface
- **JavaScript/ES6+** - Frontend programming
- **HTML/CSS** - Markup and styling

### DevOps & Infrastructure
- **Docker** - Containerization
- **Terraform** - Infrastructure as Code (IaC)
- **AWS** - Cloud hosting platform
- **Nginx** - Web server and reverse proxy
- **Cloudflare** - CDN and DNS management

### Monitoring & Quality
- **Grafana** - Metrics visualization
- **Prometheus** - Metrics collection and monitoring
- **SonarQube** - Code quality and security analysis

### Scripting
- **Shell Scripts** - Automation and deployment scripts

## 🏗 Architecture

```
┌─────────────────┐
│   Cloudflare    │ (CDN, DNS, Security)
└────────┬────────┘
         │
┌────────▼────────┐
│      Nginx      │ (Reverse Proxy)
└────────┬────────┘
         │
    ┌────┴────┐
    │         │
┌───▼──┐  ┌──▼───┐
│React │  │Spring│
│  UI  │  │ Boot │
└──────┘  └──┬───┘
             │
        ┌────▼────┐
        │ MongoDB │
        └─────────┘

Monitoring Stack: 
┌───────────┐    ┌────────────┐    ┌──────────┐
│Prometheus │───▶│  Grafana   │    │SonarQube │
└───────────┘    └────────────┘    └──────────┘
```

## 📦 Prerequisites

Before you begin, ensure you have the following installed:

- **Java 17** or higher
- **Node.js 16+** and npm
- **Docker** and Docker Compose
- **MongoDB 5.0+** (or use Docker)
- **Maven 3.8+** (for building Java)
- **AWS CLI** (for deployment)
- **Terraform 1.0+** (for infrastructure provisioning)

## 🚀 Installation

### 1. Clone the Repository

```bash
git clone https://github.com/nadunmA/aeo-content-analyzers.git
cd aeo-content-analyzers
```

### 2. Backend Setup

```bash
# Navigate to backend directory
cd backend

# Install dependencies and build
mvn clean install

# Run the application
mvn spring-boot:run
```

The backend API will be available at `http://localhost:8080`

### 3. Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start development server
npm start
```

The frontend will be available at `http://localhost:3000`

### 4. Docker Setup (Recommended)

```bash
# Build and start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down
```

## ⚙️ Configuration

### Backend Configuration

Create an `application.properties` or `application.yml` file:

```yaml
# MongoDB Configuration
spring.data.mongodb.uri=mongodb://localhost:27017/aeo-analyzer
spring.data.mongodb.database=aeo-analyzer

# Server Configuration
server.port=8080
server.servlet.context-path=/api

# Logging
logging.level.root=INFO
logging.level.com.yourpackage=DEBUG

# OpenAI/Gemini API Keys (if applicable)
openai.api.key=${OPENAI_API_KEY}
gemini.api.key=${GEMINI_API_KEY}
```

### Frontend Configuration

Create a `.env` file in the frontend directory:

```env
REACT_APP_API_BASE_URL=http://localhost:8080/api
REACT_APP_ENVIRONMENT=development
```

### Environment Variables

```bash
export OPENAI_API_KEY=your_openai_key
export GEMINI_API_KEY=your_gemini_key
export MONGODB_URI=mongodb://localhost:27017/aeo-analyzer
export AWS_ACCESS_KEY_ID=your_aws_key
export AWS_SECRET_ACCESS_KEY=your_aws_secret
```

## 📖 Usage

### Analyzing Content

1. Navigate to the web interface at `http://localhost:3000`
2. Enter or paste your content in the text area
3. Select target platforms (ChatGPT, Gemini, etc.)
4. Click "Analyze" to get insights
5. Review recommendations and metrics
6. Export reports or track changes over time

### API Usage

```bash
# Analyze content via API
curl -X POST http://localhost:8080/api/analyze \
  -H "Content-Type: application/json" \
  -d '{
    "content": "Your content here.. .",
    "platforms": ["chatgpt", "gemini"]
  }'
```

## 📚 API Documentation

### Endpoints

#### POST `/api/analyze`
Analyze content for AEO optimization

**Request Body:**
```json
{
  "content": "string",
  "platforms": ["chatgpt", "gemini"],
  "options": {
    "detailed": true,
    "includeRecommendations": true
  }
}
```

**Response:**
```json
{
  "score": 85,
  "recommendations": [],
  "metrics": {
    "readability": 90,
    "structure": 80,
    "relevance": 85
  }
}
```

#### GET `/api/history`
Retrieve analysis history

#### GET `/api/metrics`
Get application metrics for monitoring

## 📊 Monitoring & Observability

### Grafana Dashboard

Access Grafana at `http://localhost:3000/grafana`

Default credentials:
- Username: `admin`
- Password: `admin`

### Prometheus Metrics

Access Prometheus at `http://localhost:9090`

Key metrics:
- Application health
- API response times
- Database connections
- Content analysis throughput

### SonarQube

Access SonarQube at `http://localhost:9000`

For code quality reports and security analysis. 

## 🚢 Deployment

### AWS Deployment with Terraform

```bash
# Navigate to infrastructure directory
cd terraform

# Initialize Terraform
terraform init

# Plan deployment
terraform plan

# Apply infrastructure
terraform apply

# Destroy infrastructure (when needed)
terraform destroy
```

### Docker Production Build

```bash
# Build production images
docker-compose -f docker-compose.prod.yml build

# Deploy to production
docker-compose -f docker-compose.prod.yml up -d
```

### Manual Deployment

1. Build the backend: `mvn clean package`
2. Build the frontend: `npm run build`
3. Deploy JAR file to your server
4. Serve frontend static files with Nginx
5. Configure reverse proxy
6. Set up SSL with Cloudflare

## 🤝 Contributing

Contributions are welcome! Please follow these steps:

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/your-feature-name`
3. Commit your changes: `git commit -m 'Add some feature'`
4. Push to the branch: `git push origin feature/your-feature-name`
5. Open a Pull Request

### Code Standards

- Follow Java code conventions for backend
- Use ESLint and Prettier for frontend
- Write unit tests for new features
- Update documentation as needed
- Ensure SonarQube quality gates pass

## 🖼 Visual Proofs & Dashboards
To demonstrate the system's operational and security standards, here are the core dashboards:

### 📊 Real-time Observability (Grafana)
Comprehensive view of backend/frontend status, CPU utilization, and real-time application logs.
<img width="1920" height="1080" alt="Screenshot 2026-01-08 at 6 49 09 PM" src="https://github.com/user-attachments/assets/2c53797f-0a71-4c7e-8fa5-49150001ecee" />

### 🛡️ Security & Quality Ratings (SonarQube)
Achieved 'A' ratings for Security, Reliability, and Maintainability across the codebase.
![Screenshot_4-1-2026_224320_sonarcloud io](https://github.com/user-attachments/assets/4feb0977-d08d-4c2a-807e-2c6c8573e475)

### ☁️ Infrastructure Monitoring (AWS CloudWatch)
Detailed monitoring of EC2 performance metrics and network traffic patterns.
<img width="1920" height="1080" alt="Screenshot 2026-01-08 at 7 29 55 PM" src="https://github.com/user-attachments/assets/07522c9b-a28b-4b5a-b957-c6b50fa7fb8b" />
<img width="1915" height="1080" alt="Screenshot 2026-01-08 at 7 31 04 PM" src="https://github.com/user-attachments/assets/61e5f641-4d0f-4daa-96eb-befdf050edbf" />

### 📉 Cost Management (AWS Billing)
Monitoring monthly costs and detecting anomalies to ensure budget adherence.
<img width="1920" height="1080" alt="Screenshot 2026-01-08 at 7 35 02 PM" src="https://github.com/user-attachments/assets/320580f1-0c41-46ba-b2e8-5431837aea08" />

### 🛡️ Dependency Security & Vulnerability Scanning (Snyk)
Continuous monitoring and scanning of application dependencies and Docker images to identify and mitigate security vulnerabilities.
<img width="1920" height="1080" alt="Screenshot 2026-01-08 at 8 05 30 PM" src="https://github.com/user-attachments/assets/88e10a36-df45-4337-b56b-765f4445ecb1" />

### 🍃 Database Performance & Health (MongoDB Atlas)
Real-time tracking of database operations (Opcounters), active connections, and hardware performance metrics to ensure data reliability.
<img width="1920" height="1080" alt="Screenshot 2026-01-08 at 7 55 07 PM" src="https://github.com/user-attachments/assets/72ee9be0-0605-4b9a-8b2e-3f95b3c557e0" />

### 🌐 DNS Traffic & Global Analytics (Cloudflare)
Insights into global traffic patterns and DNS query volumes, providing an additional layer of performance optimization and security.
<img width="1913" height="1080" alt="Screenshot 2026-01-08 at 7 43 15 PM" src="https://github.com/user-attachments/assets/902fb2e3-a506-4748-ae33-c51a6a5a51b7" />


## 📝 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👥 Authors

- **nadunmA** - *Initial work* - [@nadunmA](https://github.com/nadunmA)

---

Made with ❤️ by nadunmA
