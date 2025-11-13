# 🌸 Lotus Student Management System

<div align="center">

![Version](https://img.shields.io/badge/version-2.0.0-blue.svg)
![License](https://img.shields.io/badge/license-MIT-green.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.2-brightgreen.svg)
![Vue.js](https://img.shields.io/badge/Vue.js-3.3.9-4FC08D.svg)
![Java](https://img.shields.io/badge/Java-8-orange.svg)

**Enterprise-Grade Student Management System**
*Internship Tracking • Career Services • Document Management • Real-time Communication*

[Features](#-features) •
[Architecture](#%EF%B8%8F-architecture) •
[Quick Start](#-quick-start) •
[Documentation](#-documentation) •
[Roadmap](./ROADMAP.md)

</div>

---

## 📋 Table of Contents

- [Overview](#-overview)
- [Features](#-features)
- [Architecture](#%EF%B8%8F-architecture)
- [Quick Start](#-quick-start)
- [Documentation](#-documentation)
- [Testing](#-testing)
- [Deployment](#-deployment)
- [Monitoring](#-monitoring)
- [Contributing](#-contributing)
- [License](#-license)

---

## 🌟 Overview

**Lotus Student Management System** is a production-ready, enterprise-grade platform built with Silicon Valley engineering standards. It streamlines student lifecycle management, internship coordination, career development, and administrative operations for educational institutions.

### 🎯 Built for Portfolio Excellence

This project demonstrates:
- **🏢 Enterprise Architecture**: Microservices-ready, cloud-native design
- **🔒 Production Security**: JWT, BCrypt, RBAC, comprehensive security measures
- **⚡ Performance**: Redis caching, optimized queries, HikariCP pooling
- **🧪 Quality**: 80%+ test coverage, SonarQube integration, CI/CD pipelines
- **📊 Observability**: Prometheus, Grafana, structured logging, audit trails
- **🚀 DevOps Excellence**: Docker, Kubernetes, automated deployments
- **📚 Documentation**: OpenAPI, comprehensive guides, architecture diagrams

---

## ✨ Features

### 👥 Core Functionality

| Feature | Description |
|---------|-------------|
| **Multi-Role Management** | Students, Coordinators, Administrators, Instructors, Career Center Staff |
| **Internship Tracking** | End-to-end internship lifecycle, applications, approvals, tracking |
| **Career Services** | Job postings, company profiles, application management |
| **Document Management** | Secure uploads, versioning, access control, metadata |
| **Communication** | Internal messaging, notifications, file attachments |
| **Reporting** | Analytics, dashboards, export functionality |

### 🔐 Enterprise Security

- **JWT Authentication** with token refresh
- **BCrypt Password Hashing** (12 rounds)
- **Role-Based Access Control** (RBAC)
- **CORS** configuration
- **SQL Injection** prevention
- **XSS Protection**
- **CSRF Protection**
- **Rate Limiting**
- **Account Lockout** policies
- **Audit Logging**

### 📊 Performance & Scalability

- **Redis Distributed Cache** for session management
- **HikariCP Connection Pooling** (20-50 connections)
- **Database Query Optimization** with indexes
- **Async Processing** for heavy operations
- **HTTP/2 Support**
- **GZIP Compression**
- **Lazy Loading** strategies
- **Horizontal Scaling** with Kubernetes

### 🧪 Quality Assurance

- **Unit Tests**: JUnit 5, Mockito
- **Integration Tests**: Spring Test, TestContainers
- **E2E Tests**: Cypress
- **Code Coverage**: JaCoCo (target: 80%+)
- **Static Analysis**: SonarQube
- **Security Scanning**: OWASP, Snyk, Trivy
- **Performance Testing**: JMeter, Gatling

### 🔄 CI/CD & DevOps

- **GitHub Actions** workflows
- **Docker** multi-stage builds
- **Kubernetes** with autoscaling
- **Prometheus** + **Grafana**
- **Flyway** database migrations
- **Blue-Green** deployments
- **Automated** security scans

---

## 🏗️ Architecture

### Tech Stack

#### Backend
- **Framework**: Spring Boot 2.7.2
- **Language**: Java 8
- **Security**: Spring Security + JWT (io.jsonwebtoken 0.11.5)
- **Database**: MySQL 8.0 + Flyway
- **Cache**: Redis 7
- **Documentation**: SpringDoc OpenAPI 3
- **Monitoring**: Micrometer + Prometheus
- **Testing**: JUnit 5 + TestContainers + REST Assured

#### Frontend
- **Framework**: Vue.js 3.3.9 (Composition API)
- **Build**: Vite 5
- **State**: Pinia 2.1.7 + Persistence
- **Router**: Vue Router 4.2.5
- **HTTP**: Axios 1.6.2
- **UI**: Bootstrap 5.3.2
- **Validation**: Vee-Validate + Yup
- **PWA**: Vite Plugin PWA
- **Testing**: Vitest + Cypress

#### Infrastructure
- **Containers**: Docker
- **Orchestration**: Kubernetes
- **Proxy**: Nginx
- **Metrics**: Prometheus + Grafana
- **CI/CD**: GitHub Actions
- **Registry**: GitHub Container Registry (GHCR)

### System Architecture

```
                       ┌─────────────────────┐
                       │  Load Balancer      │
                       │  (K8s Ingress)      │
                       └──────────┬──────────┘
                                  │
                    ┌─────────────┴──────────────┐
                    │                            │
           ┌────────▼────────┐          ┌────────▼────────┐
           │  Frontend       │          │  Backend API    │
           │  Vue.js + Nginx │          │  Spring Boot    │
           │  Replicas: 2    │          │  Replicas: 3+   │
           └─────────────────┘          └────────┬────────┘
                                                  │
                                    ┌─────────────┴─────────────┐
                                    │                           │
                           ┌────────▼────────┐        ┌────────▼────────┐
                           │   MySQL 8.0     │        │   Redis 7       │
                           │   Primary +     │        │   Sentinel      │
                           │   Read Replicas │        │                 │
                           └─────────────────┘        └─────────────────┘
```

---

## 🚀 Quick Start

### Prerequisites

```bash
Java 8+          # Backend runtime
Node.js 18+      # Frontend development
MySQL 8.0+       # Database
Redis 7+         # Cache (optional)
Docker           # Containerization
Maven 3.6+       # Build tool
```

### Using Docker Compose (Recommended)

```bash
# Clone repository
git clone https://github.com/dogaaydinn/lotus-student-management-system.git
cd lotus-student-management-system

# Setup environment
cp .env.docker.example .env
# Edit .env with your configuration

# Start services
docker-compose up -d

# Verify deployment
docker-compose ps
docker-compose logs -f backend

# Access services
# Frontend:  http://localhost
# Backend:   http://localhost:8085
# Swagger:   http://localhost:8085/swagger-ui.html
# Grafana:   http://localhost:3000 (admin/admin123)
```

### Local Development

#### Backend

```bash
cd lotos_backend

# Configure
cp .env.example .env

# Build & Test
mvn clean install
mvn test

# Run
mvn spring-boot:run
# API available at: http://localhost:8085
```

#### Frontend

```bash
cd lotus_frontend

# Install
npm install

# Develop
npm run dev
# App available at: http://localhost:5173

# Build
npm run build
npm run preview
```

---

## 📖 Documentation

### Guides

| Document | Description |
|----------|-------------|
| [**ROADMAP.md**](./ROADMAP.md) | Development timeline and future features |
| [**ARCHITECTURE.md**](./docs/ARCHITECTURE.md) | System design patterns |
| [**DEPLOYMENT.md**](./docs/DEPLOYMENT.md) | Production deployment |
| [**SECURITY.md**](./docs/SECURITY.md) | Security implementation |
| [**DATABASE.md**](./docs/DATABASE.md) | Schema and migrations |
| [**TESTING.md**](./docs/TESTING.md) | Testing strategies |
| [**CONTRIBUTING.md**](./CONTRIBUTING.md) | Contribution guidelines |

### API Documentation

**Interactive API Documentation** (when running):
- Swagger UI: http://localhost:8085/swagger-ui.html
- OpenAPI JSON: http://localhost:8085/v3/api-docs

**Key Endpoints**:
- `POST /api/auth/login` - User authentication
- `GET /api/students` - List students
- `POST /api/opportunities` - Create job posting
- `GET /api/messages` - Fetch messages
- `GET /actuator/health` - Health check

---

## 🧪 Testing

```bash
# Backend
cd lotos_backend

mvn test                           # Unit tests
mvn verify -P integration-tests    # Integration tests
mvn jacoco:report                  # Coverage report

# Frontend
cd lotus_frontend

npm run test:unit                  # Unit tests
npm run test:coverage              # With coverage
npm run test:e2e                   # E2E tests
npm run test:e2e:open              # E2E interactive
```

**Coverage Reports**:
- Backend: `lotos_backend/target/site/jacoco/index.html`
- Frontend: `lotus_frontend/coverage/index.html`

---

## 🐳 Deployment

### Kubernetes

```bash
# Deploy to K8s
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/mysql-deployment.yaml
kubectl apply -f k8s/redis-deployment.yaml
kubectl apply -f k8s/backend-deployment.yaml
kubectl apply -f k8s/frontend-deployment.yaml

# Verify
kubectl get pods -n lotus-spm
kubectl get svc -n lotus-spm

# Monitor
kubectl logs -f deployment/lotus-backend -n lotus-spm
```

### Production Checklist

- [ ] Update secrets (JWT, DB passwords)
- [ ] Configure SSL/TLS
- [ ] Set up backups
- [ ] Configure monitoring alerts
- [ ] Enable rate limiting
- [ ] Security audit
- [ ] Load testing
- [ ] Disaster recovery plan

---

## 📊 Monitoring

### Health Checks

```bash
# Application health
curl http://localhost:8085/actuator/health

# Detailed health
curl http://localhost:8085/actuator/health | jq

# Metrics
curl http://localhost:8085/actuator/prometheus
```

### Dashboards

**Grafana** (http://localhost:3000):
- Application Performance
- JVM Metrics
- Database Performance
- Cache Hit Rates
- Error Rates

**Prometheus** (http://localhost:9090):
- Query metrics
- Alert rules
- Targets monitoring

---

## 🤝 Contributing

Contributions welcome! See [CONTRIBUTING.md](./CONTRIBUTING.md)

### Workflow

1. Fork repository
2. Create feature branch: `git checkout -b feature/amazing`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push: `git push origin feature/amazing`
5. Open Pull Request

### Standards

- Follow Java/Vue.js conventions
- Write unit tests
- Update documentation
- Pass CI checks

---

## 📄 License

MIT License - see [LICENSE](LICENSE) file

---

## 🙏 Acknowledgments

- Spring Boot team
- Vue.js community
- Open-source contributors

---

## 📞 Support

- **Issues**: [GitHub Issues](https://github.com/dogaaydinn/lotus-student-management-system/issues)
- **Discussions**: [GitHub Discussions](https://github.com/dogaaydinn/lotus-student-management-system/discussions)
- **Email**: support@lotus-spm.com

---

<div align="center">

**Built with ❤️ using enterprise-grade technologies**

[![GitHub stars](https://img.shields.io/github/stars/dogaaydinn/lotus-student-management-system?style=social)](https://github.com/dogaaydinn/lotus-student-management-system)
[![GitHub forks](https://img.shields.io/github/forks/dogaaydinn/lotus-student-management-system?style=social)](https://github.com/dogaaydinn/lotus-student-management-system/fork)

[⬆ Back to top](#-lotus-student-management-system)

</div>
