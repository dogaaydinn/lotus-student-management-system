# External Services Configuration Guide

This file contains configuration templates for all external services used in Lotus SMS.

## 📋 Table of Contents
1. [OAuth2 Configuration (Google, Microsoft, LinkedIn)](#oauth2-configuration)
2. [OpenAI GPT-4 API Configuration](#openai-gpt-4-api)
3. [SMTP Email Server Configuration](#smtp-email-server)
4. [Stripe/PayPal Billing Configuration](#stripe-paypal-billing)
5. [AWS S3 Storage Configuration](#aws-s3-storage)
6. [Redis Configuration](#redis-configuration)
7. [MySQL Configuration](#mysql-configuration)

---

## 1. OAuth2 Configuration

### Google OAuth2

1. **Get Credentials**:
   - Go to [Google Cloud Console](https://console.cloud.google.com/)
   - Create a new project or select existing
   - Enable "Google+ API"
   - Go to "Credentials" → "Create Credentials" → "OAuth 2.0 Client ID"
   - Application type: Web application
   - Authorized redirect URIs: `http://localhost:8080/login/oauth2/code/google`

2. **Configuration** (`application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope:
              - email
              - profile
              - openid
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/v2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub
```

### Microsoft OAuth2

1. **Get Credentials**:
   - Go to [Azure Portal](https://portal.azure.com/)
   - Navigate to "Azure Active Directory" → "App registrations"
   - Click "New registration"
   - Name: Lotus SMS
   - Redirect URI: `http://localhost:8080/login/oauth2/code/microsoft`
   - Copy Application (client) ID and create a client secret

2. **Configuration** (`application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          microsoft:
            client-id: YOUR_MICROSOFT_CLIENT_ID
            client-secret: YOUR_MICROSOFT_CLIENT_SECRET
            scope:
              - openid
              - profile
              - email
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
        provider:
          microsoft:
            authorization-uri: https://login.microsoftonline.com/common/oauth2/v2.0/authorize
            token-uri: https://login.microsoftonline.com/common/oauth2/v2.0/token
            user-info-uri: https://graph.microsoft.com/oidc/userinfo
            jwk-set-uri: https://login.microsoftonline.com/common/discovery/v2.0/keys
            user-name-attribute: sub
```

### LinkedIn OAuth2

1. **Get Credentials**:
   - Go to [LinkedIn Developers](https://www.linkedin.com/developers/)
   - Create a new app
   - Add redirect URL: `http://localhost:8080/login/oauth2/code/linkedin`
   - Get Client ID and Client Secret

2. **Configuration** (`application.yml`):
```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          linkedin:
            client-id: YOUR_LINKEDIN_CLIENT_ID
            client-secret: YOUR_LINKEDIN_CLIENT_SECRET
            scope:
              - r_liteprofile
              - r_emailaddress
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
            authorization-grant-type: authorization_code
        provider:
          linkedin:
            authorization-uri: https://www.linkedin.com/oauth/v2/authorization
            token-uri: https://www.linkedin.com/oauth/v2/accessToken
            user-info-uri: https://api.linkedin.com/v2/me
            user-name-attribute: id
```

---

## 2. OpenAI GPT-4 API

### Get API Key

1. Go to [OpenAI Platform](https://platform.openai.com/)
2. Sign up or log in
3. Navigate to "API Keys"
4. Create new secret key
5. Copy and save the key (you won't see it again!)

### Configuration (`application.yml`):
```yaml
openai:
  api-key: YOUR_OPENAI_API_KEY
  api-url: https://api.openai.com/v1
  model: gpt-4-turbo-preview
  max-tokens: 2000
  temperature: 0.7
  timeout: 30000 # 30 seconds

# Alternative for Azure OpenAI
azure-openai:
  enabled: false
  api-key: YOUR_AZURE_OPENAI_KEY
  endpoint: https://YOUR_RESOURCE_NAME.openai.azure.com/
  deployment-name: gpt-4
  api-version: 2024-02-15-preview
```

### Java Configuration:
```java
@Configuration
public class OpenAIConfig {

    @Value("${openai.api-key}")
    private String apiKey;

    @Value("${openai.api-url}")
    private String apiUrl;

    @Bean
    public RestTemplate openAIRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().set("Authorization", "Bearer " + apiKey);
            request.getHeaders().set("Content-Type", "application/json");
            return execution.execute(request, body);
        });
        return restTemplate;
    }
}
```

---

## 3. SMTP Email Server

### Gmail SMTP

1. **Enable App Passwords**:
   - Go to Google Account settings
   - Security → 2-Step Verification → App passwords
   - Generate app password for "Mail"

2. **Configuration** (`application.yml`):
```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: your-email@gmail.com
    password: YOUR_APP_PASSWORD
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
            required: true
          connectiontimeout: 5000
          timeout: 5000
          writetimeout: 5000
    default-encoding: UTF-8

# Email settings
email:
  from: noreply@lotus-sms.com
  from-name: Lotus SMS
  support: support@lotus-sms.com
  templates-path: classpath:/templates/email/
```

### SendGrid (Recommended for Production)

1. **Get API Key**:
   - Sign up at [SendGrid](https://sendgrid.com/)
   - Create API Key with "Mail Send" permissions

2. **Configuration** (`application.yml`):
```yaml
sendgrid:
  api-key: YOUR_SENDGRID_API_KEY
  from-email: noreply@lotus-sms.com
  from-name: Lotus SMS

spring:
  mail:
    host: smtp.sendgrid.net
    port: 587
    username: apikey
    password: YOUR_SENDGRID_API_KEY
```

### AWS SES

```yaml
aws:
  ses:
    region: us-east-1
    access-key: YOUR_AWS_ACCESS_KEY
    secret-key: YOUR_AWS_SECRET_KEY
    from-email: noreply@lotus-sms.com
```

---

## 4. Stripe/PayPal Billing

### Stripe

1. **Get API Keys**:
   - Sign up at [Stripe Dashboard](https://dashboard.stripe.com/)
   - Get Publishable key and Secret key
   - Set up webhook endpoint

2. **Configuration** (`application.yml`):
```yaml
stripe:
  api-key: sk_test_YOUR_SECRET_KEY # Use sk_live_ for production
  publishable-key: pk_test_YOUR_PUBLISHABLE_KEY
  webhook-secret: whsec_YOUR_WEBHOOK_SECRET
  currency: USD

  # Product IDs for each plan
  plans:
    basic:
      price-id: price_BASIC_MONTHLY
      amount: 2900 # $29.00
    professional:
      price-id: price_PROFESSIONAL_MONTHLY
      amount: 9900 # $99.00
    enterprise:
      price-id: price_ENTERPRISE_MONTHLY
      amount: 29900 # $299.00

  # Webhook events to listen for
  webhook-events:
    - invoice.payment_succeeded
    - invoice.payment_failed
    - customer.subscription.created
    - customer.subscription.updated
    - customer.subscription.deleted
```

### PayPal

1. **Get Credentials**:
   - Sign up at [PayPal Developer](https://developer.paypal.com/)
   - Create app to get Client ID and Secret

2. **Configuration** (`application.yml`):
```yaml
paypal:
  mode: sandbox # Use 'live' for production
  client-id: YOUR_PAYPAL_CLIENT_ID
  client-secret: YOUR_PAYPAL_CLIENT_SECRET

  # Sandbox endpoints
  sandbox:
    api-base-url: https://api-m.sandbox.paypal.com
    web-url: https://www.sandbox.paypal.com

  # Production endpoints
  live:
    api-base-url: https://api-m.paypal.com
    web-url: https://www.paypal.com

  # Plan IDs
  plans:
    basic: P-BASIC_PLAN_ID
    professional: P-PROFESSIONAL_PLAN_ID
    enterprise: P-ENTERPRISE_PLAN_ID
```

---

## 5. AWS S3 Storage

### Get Credentials

1. **Create IAM User**:
   - Go to [AWS IAM Console](https://console.aws.amazon.com/iam/)
   - Create user with "Programmatic access"
   - Attach policy: `AmazonS3FullAccess`
   - Save Access Key ID and Secret Access Key

2. **Create S3 Bucket**:
   - Go to [S3 Console](https://s3.console.aws.amazon.com/)
   - Create bucket (e.g., `lotus-sms-documents`)
   - Set appropriate permissions

### Configuration (`application.yml`):
```yaml
aws:
  s3:
    enabled: true
    region: us-east-1
    bucket-name: lotus-sms-documents
    access-key: YOUR_AWS_ACCESS_KEY_ID
    secret-key: YOUR_AWS_SECRET_ACCESS_KEY

  # CloudFront CDN (optional)
  cloudfront:
    enabled: false
    distribution-domain: d1234567890.cloudfront.net

# File upload settings
file:
  upload:
    max-size: 10485760 # 10MB in bytes
    allowed-extensions: pdf,doc,docx,jpg,jpeg,png
    storage-type: s3 # Options: local, s3, azure
    local-path: /var/lotus-sms/uploads

# Alternative: Azure Blob Storage
azure:
  storage:
    enabled: false
    account-name: YOUR_AZURE_STORAGE_ACCOUNT
    account-key: YOUR_AZURE_STORAGE_KEY
    container-name: lotus-sms-documents
    connection-string: DefaultEndpointsProtocol=https;AccountName=...
```

---

## 6. Redis Configuration

### Redis Standalone

```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: YOUR_REDIS_PASSWORD # Leave empty if no password
    database: 0
    timeout: 60000

    # Connection pool settings
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
        max-wait: -1ms
      shutdown-timeout: 100ms
```

### Redis Sentinel (High Availability)

```yaml
spring:
  redis:
    sentinel:
      master: mymaster
      nodes:
        - localhost:26379
        - localhost:26380
        - localhost:26381
    password: YOUR_REDIS_PASSWORD
```

### Redis Cluster

```yaml
spring:
  redis:
    cluster:
      nodes:
        - localhost:7000
        - localhost:7001
        - localhost:7002
        - localhost:7003
        - localhost:7004
        - localhost:7005
      max-redirects: 3
    password: YOUR_REDIS_PASSWORD
```

---

## 7. MySQL Configuration

### Single Database

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/lotus_sms?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
    username: lotus_user
    password: YOUR_MYSQL_PASSWORD
    driver-class-name: com.mysql.cj.jdbc.Driver

    # HikariCP settings
    hikari:
      maximum-pool-size: 20
      minimum-idle: 5
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000
      pool-name: LotusHikariPool
```

### Read Replicas (Master-Slave)

```yaml
spring:
  datasource:
    # Master (Write operations)
    master:
      url: jdbc:mysql://master-db.example.com:3306/lotus_sms
      username: lotus_user
      password: YOUR_MASTER_PASSWORD

    # Slave 1 (Read operations)
    slave1:
      url: jdbc:mysql://slave1-db.example.com:3306/lotus_sms
      username: lotus_reader
      password: YOUR_SLAVE_PASSWORD

    # Slave 2 (Read operations)
    slave2:
      url: jdbc:mysql://slave2-db.example.com:3306/lotus_sms
      username: lotus_reader
      password: YOUR_SLAVE_PASSWORD

# Custom routing configuration (see ReadWriteDataSourceConfig.java)
datasource:
  routing:
    enabled: true
    read-write-separation: true
```

---

## 🔒 Security Best Practices

### Environment Variables

Never commit credentials to git. Use environment variables:

```bash
# .env file (add to .gitignore)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
OPENAI_API_KEY=your_openai_api_key
STRIPE_SECRET_KEY=your_stripe_secret_key
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
MYSQL_PASSWORD=your_mysql_password
REDIS_PASSWORD=your_redis_password
```

### Spring Boot Profile

```yaml
# application-prod.yml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}

openai:
  api-key: ${OPENAI_API_KEY}

stripe:
  api-key: ${STRIPE_SECRET_KEY}

aws:
  s3:
    access-key: ${AWS_ACCESS_KEY_ID}
    secret-key: ${AWS_SECRET_ACCESS_KEY}
```

### Docker Secrets

```yaml
# docker-compose.yml
version: '3.8'
services:
  lotus-backend:
    image: lotus-sms:latest
    environment:
      - GOOGLE_CLIENT_ID_FILE=/run/secrets/google_client_id
      - OPENAI_API_KEY_FILE=/run/secrets/openai_api_key
    secrets:
      - google_client_id
      - openai_api_key

secrets:
  google_client_id:
    external: true
  openai_api_key:
    external: true
```

---

## 📝 Configuration Checklist

### Development
- [ ] Set up OAuth2 (Google at minimum)
- [ ] Get OpenAI API key (or use mocks)
- [ ] Configure SMTP (Gmail for testing)
- [ ] Use Stripe test mode
- [ ] Use local file storage
- [ ] Use local Redis
- [ ] Use local MySQL

### Staging
- [ ] Configure all OAuth2 providers
- [ ] Use OpenAI API key
- [ ] Use SendGrid for emails
- [ ] Use Stripe test mode
- [ ] Use S3 for file storage
- [ ] Use Redis Sentinel
- [ ] Use MySQL with read replica

### Production
- [ ] All OAuth2 providers configured
- [ ] OpenAI production API key
- [ ] SendGrid with dedicated IP
- [ ] Stripe live mode with webhook
- [ ] S3 with CloudFront CDN
- [ ] Redis Cluster
- [ ] MySQL with multiple read replicas
- [ ] All credentials in secrets manager
- [ ] SSL/TLS enabled everywhere
- [ ] Monitoring and alerts configured

---

## 🚀 Quick Start Commands

### Export Environment Variables
```bash
export GOOGLE_CLIENT_ID=your_client_id
export GOOGLE_CLIENT_SECRET=your_client_secret
export OPENAI_API_KEY=your_api_key
# ... add all others

# Or load from .env file
set -a
source .env
set +a
```

### Run with Profile
```bash
# Development
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Production
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

---

**Last Updated**: 2025-11-15
**Version**: v3.0.0
**Status**: Ready for configuration ✅
