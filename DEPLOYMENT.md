# Lotus Student Management System - Deployment Guide

## Table of Contents

1. [System Requirements](#system-requirements)
2. [Pre-Deployment Checklist](#pre-deployment-checklist)
3. [Environment Configuration](#environment-configuration)
4. [Database Setup](#database-setup)
5. [Backend Deployment](#backend-deployment)
6. [Frontend Deployment](#frontend-deployment)
7. [Infrastructure Services](#infrastructure-services)
8. [Monitoring & Observability](#monitoring--observability)
9. [Security Hardening](#security-hardening)
10. [Scaling & Performance](#scaling--performance)
11. [Disaster Recovery](#disaster-recovery)
12. [Troubleshooting](#troubleshooting)

---

## System Requirements

### Minimum Requirements (Development)
- **CPU**: 2 cores
- **RAM**: 4 GB
- **Disk**: 20 GB SSD
- **OS**: Ubuntu 20.04+ / Amazon Linux 2 / macOS

### Recommended Requirements (Production)
- **CPU**: 4-8 cores
- **RAM**: 8-16 GB
- **Disk**: 50 GB SSD with IOPS provisioning
- **OS**: Ubuntu 22.04 LTS / Amazon Linux 2023

### Software Dependencies

**Backend:**
- Java 8 or higher (OpenJDK recommended)
- Maven 3.6+
- MySQL 8.0+
- Redis 6.2+

**Frontend:**
- Node.js 18+ LTS
- npm 9+ or yarn 1.22+

---

## Pre-Deployment Checklist

### 1. Code Readiness
- ✅ All tests passing (target: 90% coverage achieved)
- ✅ Security scan completed (CodeQL)
- ✅ Load testing completed
- ✅ Code reviewed and merged to main branch
- ✅ Version tagged (e.g., `v1.0.0`)

### 2. Infrastructure Readiness
- ☐ Database provisioned and accessible
- ☐ Redis cluster provisioned
- ☐ S3 bucket created (if using S3 for file storage)
- ☐ SSL/TLS certificates obtained
- ☐ Domain names configured
- ☐ Load balancer configured (if applicable)

### 3. Configuration Files
- ☐ Environment variables set
- ☐ Secrets managed securely (AWS Secrets Manager / Vault)
- ☐ Database connection strings configured
- ☐ API keys and JWT secrets set

### 4. Monitoring Setup
- ☐ Prometheus scraping configured
- ☐ Grafana dashboards imported
- ☐ Log aggregation configured (ELK / CloudWatch)
- ☐ Alerting rules configured
- ☐ PagerDuty / Slack integrations set up

---

## Environment Configuration

### Backend Environment Variables

Create `.env` file or set environment variables:

```bash
# Application
SPRING_PROFILES_ACTIVE=production
SERVER_PORT=8085

# Database
DB_URL=jdbc:mysql://prod-mysql.example.com:3306/lotus?sessionVariables=sql_mode='NO_ENGINE_SUBSTITUTION'&jdbcCompliantTruncation=false
DB_USERNAME=lotus_prod
DB_PASSWORD=<secure-password-from-secrets-manager>

# Redis
REDIS_HOST=prod-redis.example.com
REDIS_PORT=6379
REDIS_PASSWORD=<redis-password>

# JWT
JWT_SECRET=<256-bit-secret-key>
JWT_EXPIRATION_MS=86400000  # 24 hours

# CORS
CORS_ALLOWED_ORIGINS=https://lotus.example.com,https://www.lotus.example.com

# AWS S3 (Optional)
AWS_S3_ENABLED=true
AWS_ACCESS_KEY=<aws-access-key>
AWS_SECRET_KEY=<aws-secret-key>
AWS_REGION=eu-north-1
AWS_S3_BUCKET=lotus-spm-prod-files
```

### Frontend Environment Variables

Create `.env.production`:

```bash
# API Configuration
VITE_API_BASE_URL=https://api.lotus.example.com
VITE_API_TIMEOUT=30000
VITE_ENABLE_API_LOGGING=false

# Feature Flags
VITE_FEATURE_S3_UPLOAD=true
VITE_FEATURE_ANALYTICS=true
```

---

## Database Setup

### 1. Create Database

```sql
CREATE DATABASE lotus CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE USER 'lotus_prod'@'%' IDENTIFIED BY '<secure-password>';
GRANT ALL PRIVILEGES ON lotus.* TO 'lotus_prod'@'%';
FLUSH PRIVILEGES;
```

### 2. Run Flyway Migrations

```bash
cd lotos_backend

# Enable Flyway in application.yml
# spring.flyway.enabled: true

mvn clean install
mvn flyway:migrate
```

This will:
- Create database schema
- Create 25+ performance indices (Sprint 3)
- Optimize query performance

### 3. Verify Database Indices

```sql
USE lotus;

-- Check indices on student table
SHOW INDEXES FROM student;

-- Verify index usage
EXPLAIN SELECT * FROM student WHERE username = 'test';
```

Expected: `type: ref` (using index)

---

## Backend Deployment

### Option 1: JAR Deployment

```bash
cd lotos_backend

# Build production JAR
mvn clean package -DskipTests

# Run with production profile
java -jar \
  -Xms2g -Xmx4g \
  -Dspring.profiles.active=production \
  -Dserver.port=8085 \
  target/lotusSPM-0.0.1-SNAPSHOT.jar
```

### Option 2: Docker Deployment

Create `Dockerfile`:

```dockerfile
FROM openjdk:8-jre-alpine

# Create app directory
WORKDIR /app

# Copy JAR
COPY target/lotusSPM-0.0.1-SNAPSHOT.jar app.jar

# Expose port
EXPOSE 8085

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8085/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-Xms2g", \
  "-Xmx4g", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", \
  "app.jar"]
```

Build and run:

```bash
docker build -t lotus-backend:v1.0.0 .
docker run -d \
  -p 8085:8085 \
  --name lotus-backend \
  --env-file .env.production \
  --restart unless-stopped \
  lotus-backend:v1.0.0
```

### Option 3: Kubernetes Deployment

Create `k8s/deployment.yaml`:

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lotus-backend
spec:
  replicas: 3
  selector:
    matchLabels:
      app: lotus-backend
  template:
    metadata:
      labels:
        app: lotus-backend
    spec:
      containers:
      - name: lotus-backend
        image: lotus-backend:v1.0.0
        ports:
        - containerPort: 8085
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "production"
        envFrom:
        - secretRef:
            name: lotus-backend-secrets
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8085
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8085
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: lotus-backend-service
spec:
  selector:
    app: lotus-backend
  ports:
  - protocol: TCP
    port: 80
    targetPort: 8085
  type: LoadBalancer
```

Deploy:

```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/secrets.yaml
```

---

## Frontend Deployment

### Build for Production

```bash
cd lotus_frontend

# Install dependencies
npm install

# Build
npm run build

# Output in dist/ directory
```

### Option 1: Nginx Deployment

Create `nginx.conf`:

```nginx
server {
    listen 80;
    server_name lotus.example.com;

    # Redirect to HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name lotus.example.com;

    ssl_certificate /etc/nginx/ssl/lotus.crt;
    ssl_certificate_key /etc/nginx/ssl/lotus.key;

    # Security headers
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "no-referrer-when-downgrade" always;
    add_header Content-Security-Policy "default-src 'self' https:; script-src 'self' 'unsafe-inline' 'unsafe-eval'; style-src 'self' 'unsafe-inline';" always;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml;

    root /var/www/lotus/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # API proxy
    location /api {
        proxy_pass http://lotus-backend:8085;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg|woff|woff2|ttf|eot)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

Deploy:

```bash
# Copy built files
sudo cp -r dist/* /var/www/lotus/dist/

# Copy nginx config
sudo cp nginx.conf /etc/nginx/sites-available/lotus
sudo ln -s /etc/nginx/sites-available/lotus /etc/nginx/sites-enabled/

# Test and reload nginx
sudo nginx -t
sudo systemctl reload nginx
```

### Option 2: S3 + CloudFront Deployment

```bash
# Upload to S3
aws s3 sync dist/ s3://lotus-frontend-prod --delete

# Invalidate CloudFront cache
aws cloudfront create-invalidation \
  --distribution-id E1234567890ABC \
  --paths "/*"
```

---

## Infrastructure Services

### MySQL (RDS)

**Recommended Configuration:**
- Instance: db.t3.medium (2 vCPU, 4 GB RAM)
- Storage: 100 GB SSD with 3000 IOPS
- Multi-AZ: Enabled for high availability
- Automated backups: 7-day retention
- Backup window: 02:00-03:00 UTC
- Maintenance window: Sun 03:00-04:00 UTC

**Performance Tuning:**

```sql
-- Check query performance
SELECT * FROM performance_schema.events_statements_summary_by_digest
ORDER BY SUM_TIMER_WAIT DESC LIMIT 10;

-- Check index usage
SELECT * FROM sys.schema_unused_indexes;
```

### Redis (ElastiCache)

**Recommended Configuration:**
- Node type: cache.t3.medium
- Number of replicas: 2
- Engine version: 6.2
- Automatic failover: Enabled

**Monitoring:**

```bash
# Check hit rate
redis-cli INFO stats | grep keyspace_hits

# Monitor memory
redis-cli INFO memory

# Check slow queries
redis-cli SLOWLOG GET 10
```

---

## Monitoring & Observability

### Prometheus Metrics

**Scrape Configuration** (`prometheus.yml`):

```yaml
scrape_configs:
  - job_name: 'lotus-backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['lotus-backend:8085']
```

**Key Metrics to Monitor:**

1. **Application Metrics:**
   - `lotus_student_login_count_total`
   - `lotus_document_upload_count_total`
   - `lotus_api_response_time_seconds`
   - `lotus_cache_hit_total` / `lotus_cache_miss_total`

2. **JVM Metrics:**
   - `jvm_memory_used_bytes`
   - `jvm_gc_pause_seconds`
   - `jvm_threads_live`

3. **HTTP Metrics:**
   - `http_server_requests_seconds`
   - `http_server_requests_active`

4. **Database Metrics:**
   - `hikaricp_connections_active`
   - `hikaricp_connections_pending`

### Grafana Dashboards

Import dashboards:
1. JVM Micrometer Dashboard (ID: 4701)
2. Spring Boot Dashboard (ID: 12900)
3. Custom Lotus SPM Dashboard

### Log Aggregation

**ELK Stack Configuration:**

```yaml
# Logstash input
input {
  file {
    path => "/var/log/lotus/*.json"
    codec => "json"
  }
}

# Filter
filter {
  if [correlationId] {
    mutate {
      add_tag => ["correlated"]
    }
  }
}

# Output to Elasticsearch
output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "lotus-logs-%{+YYYY.MM.dd}"
  }
}
```

**Key Queries:**

```
# Find all errors for a correlation ID
correlationId:"abc-123-def" AND level:ERROR

# Find slow requests (>1s)
requestUri:* AND duration:>1000

# Find failed logins
message:"login failure" AND level:WARN
```

---

## Security Hardening

### 1. HTTPS/TLS

```bash
# Generate Let's Encrypt certificate
sudo certbot --nginx -d lotus.example.com -d www.lotus.example.com
```

### 2. Firewall Rules

```bash
# Allow only necessary ports
sudo ufw allow 22/tcp   # SSH
sudo ufw allow 80/tcp   # HTTP
sudo ufw allow 443/tcp  # HTTPS
sudo ufw enable
```

### 3. Database Security

```sql
-- Disable remote root login
DELETE FROM mysql.user WHERE User='root' AND Host NOT IN ('localhost', '127.0.0.1', '::1');

-- Require SSL
GRANT USAGE ON *.* TO 'lotus_prod'@'%' REQUIRE SSL;

FLUSH PRIVILEGES;
```

### 4. Application Security

**Enable in `application.yml`:**

```yaml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${KEYSTORE_PASSWORD}
    key-store-type: PKCS12

security:
  require-ssl: true
```

### 5. Rate Limiting

Already configured:
- 100 requests/minute per IP
- 1000 requests/hour per IP

### 6. Secrets Management

**AWS Secrets Manager:**

```bash
# Store JWT secret
aws secretsmanager create-secret \
  --name lotus/prod/jwt-secret \
  --secret-string ${JWT_SECRET}

# Retrieve in application
export JWT_SECRET=$(aws secretsmanager get-secret-value \
  --secret-id lotus/prod/jwt-secret \
  --query SecretString \
  --output text)
```

---

## Scaling & Performance

### Horizontal Scaling

**Auto-scaling Configuration (AWS):**

```json
{
  "AutoScalingGroupName": "lotus-backend-asg",
  "MinSize": 2,
  "MaxSize": 10,
  "DesiredCapacity": 3,
  "TargetTrackingConfiguration": {
    "PredefinedMetricType": "ASGAverageCPUUtilization",
    "TargetValue": 70.0
  }
}
```

### Database Read Replicas

```yaml
# application.yml
spring:
  datasource:
    primary:
      url: jdbc:mysql://primary.rds.amazonaws.com:3306/lotus
    replica:
      url: jdbc:mysql://read-replica.rds.amazonaws.com:3306/lotus
```

### CDN Configuration

Use CloudFront for static assets:
- Cache TTL: 1 year for images/fonts
- Cache TTL: 1 hour for JS/CSS
- Gzip compression: Enabled

---

## Disaster Recovery

### Backup Strategy

**Database Backups:**
- Automated daily backups (RDS)
- Manual snapshots before deployments
- Cross-region replication for critical data

**Application Backups:**
- S3 file storage (versioning enabled)
- Git repository backups
- Configuration backups

### Recovery Procedures

**Database Recovery:**

```bash
# Restore from RDS snapshot
aws rds restore-db-instance-from-db-snapshot \
  --db-instance-identifier lotus-restored \
  --db-snapshot-identifier lotus-backup-2025-11-15

# Point-in-time recovery
aws rds restore-db-instance-to-point-in-time \
  --source-db-instance-identifier lotus-prod \
  --target-db-instance-identifier lotus-recovered \
  --restore-time 2025-11-15T12:00:00Z
```

### High Availability

- Multi-AZ RDS deployment
- Redis cluster with automatic failover
- Load balancer health checks
- Auto-scaling groups

---

## Troubleshooting

### Common Issues

#### 1. Application Won't Start

```bash
# Check logs
tail -f logs/lotus-spm.log

# Common causes:
# - Database connection refused
# - Redis unreachable
# - Port already in use
```

#### 2. High Memory Usage

```bash
# Check JVM heap
jmap -heap <pid>

# Generate heap dump
jmap -dump:live,format=b,file=heap.bin <pid>

# Analyze with VisualVM or Eclipse MAT
```

#### 3. Slow Database Queries

```sql
-- Enable slow query log
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 1;

-- Check slow queries
SELECT * FROM mysql.slow_log;
```

#### 4. Cache Not Working

```bash
# Check Redis connectivity
redis-cli -h redis-host -p 6379 ping

# Check cache stats
curl http://localhost:8085/actuator/metrics/cache.gets
```

### Support Contacts

- **DevOps**: devops@example.com
- **Database**: dba@example.com
- **Security**: security@example.com
- **On-call**: PagerDuty escalation policy

---

## Deployment Checklist

### Pre-Deployment
- [ ] Run full test suite locally
- [ ] Build artifacts successfully
- [ ] Database migrations tested
- [ ] Environment variables configured
- [ ] SSL certificates valid
- [ ] Backups completed

### Deployment
- [ ] Notify team of deployment window
- [ ] Put application in maintenance mode (if needed)
- [ ] Deploy database migrations
- [ ] Deploy backend application
- [ ] Deploy frontend application
- [ ] Run smoke tests

### Post-Deployment
- [ ] Verify health endpoints
- [ ] Check application logs
- [ ] Monitor error rates
- [ ] Test critical user journeys
- [ ] Update documentation
- [ ] Notify team of successful deployment

---

## Version History

- **v1.0.0** (2025-11-15): Initial production release
  - Sprint 1: Testing foundation (40% coverage)
  - Sprint 2: Frontend hardening
  - Sprint 3: Infrastructure improvements
  - Sprint 4: Production readiness (90% coverage)

---

**For questions or issues, please contact the development team or create an issue in the GitHub repository.**
