# Load Testing Guide for Lotus SPM

## Overview

This directory contains Apache JMeter test plans for load testing the Lotus Student Management System.

## Prerequisites

- Apache JMeter 5.5 or higher
- Running Lotus SPM backend (port 8085)
- Java 8 or higher

## Installation

1. Download Apache JMeter from https://jmeter.apache.org/download_jmeter.cgi
2. Extract to a directory (e.g., `/opt/jmeter`)
3. Add JMeter bin directory to PATH

## Running Load Tests

### GUI Mode (Development)

For test development and debugging:

```bash
jmeter -t jmeter-test-plan.jmx
```

### CLI Mode (Production)

For actual load testing:

```bash
jmeter -n -t jmeter-test-plan.jmx \
  -Jbase_url=localhost \
  -Jport=8085 \
  -Jusers=100 \
  -Jramp_up=60 \
  -Jduration=300 \
  -l results/test-$(date +%Y%m%d-%H%M%S).jtl \
  -e -o results/html-report-$(date +%Y%m%d-%H%M%S)
```

### Parameters

- `base_url`: Target server hostname (default: localhost)
- `port`: Target server port (default: 8085)
- `users`: Number of concurrent users (default: 100)
- `ramp_up`: Ramp-up period in seconds (default: 60)
- `duration`: Test duration in seconds (default: 300 = 5 minutes)

## Test Scenarios

### 1. Normal Load Test
- **Users**: 100 concurrent users
- **Duration**: 5 minutes
- **Ramp-up**: 60 seconds
- **Requests**: Health check, Get Students (paginated), Get Opportunities

```bash
jmeter -n -t jmeter-test-plan.jmx -l results/normal-load.jtl
```

### 2. Stress Test
- **Users**: 500 concurrent users
- **Duration**: 10 minutes
- **Ramp-up**: 120 seconds

```bash
jmeter -n -t jmeter-test-plan.jmx \
  -Jusers=500 \
  -Jduration=600 \
  -Jramp_up=120 \
  -l results/stress-test.jtl
```

### 3. Spike Test
- **Users**: 1000 concurrent users
- **Duration**: 2 minutes
- **Ramp-up**: 10 seconds (rapid spike)

```bash
jmeter -n -t jmeter-test-plan.jmx \
  -Jusers=1000 \
  -Jduration=120 \
  -Jramp_up=10 \
  -l results/spike-test.jtl
```

### 4. Endurance/Soak Test
- **Users**: 200 concurrent users
- **Duration**: 60 minutes
- **Ramp-up**: 300 seconds

```bash
jmeter -n -t jmeter-test-plan.jmx \
  -Jusers=200 \
  -Jduration=3600 \
  -Jramp_up=300 \
  -l results/endurance-test.jtl
```

## Performance Metrics

### Expected Results

| Metric | Target | Acceptable | Poor |
|--------|--------|------------|------|
| Response Time (avg) | < 200ms | < 500ms | > 1000ms |
| Response Time (95th) | < 500ms | < 1000ms | > 2000ms |
| Throughput | > 500 req/s | > 200 req/s | < 100 req/s |
| Error Rate | < 0.1% | < 1% | > 5% |
| CPU Usage | < 70% | < 85% | > 90% |
| Memory Usage | < 75% | < 85% | > 90% |

### Key Endpoints Tested

1. **Health Check** (`/actuator/health`)
   - Expected: < 50ms response time
   - No database queries

2. **Get Students Paginated** (`/api/v1/students?page=0&size=20`)
   - Expected: < 200ms response time
   - With Redis cache: < 50ms
   - With database indices: 10x faster than before

3. **Get Opportunities** (`/opportunities`)
   - Expected: < 300ms response time
   - Tests pagination and sorting

## Analyzing Results

### Generate HTML Report

```bash
jmeter -g results/test.jtl -o results/html-report
```

### Open Report

```bash
open results/html-report/index.html
```

### Key Metrics to Monitor

1. **Response Time Graph**: Shows response times over time
2. **Transactions Per Second**: Throughput graph
3. **Response Time Percentiles**: 50th, 90th, 95th, 99th percentiles
4. **Error Rate**: Percentage of failed requests
5. **Active Threads Over Time**: Concurrent user ramp-up

## System Monitoring

While running load tests, monitor:

### Backend Metrics (Prometheus/Actuator)

```bash
# Get Prometheus metrics
curl http://localhost:8085/actuator/prometheus

# Get health status
curl http://localhost:8085/actuator/health

# Get JVM metrics
curl http://localhost:8085/actuator/metrics/jvm.memory.used
```

### Database Metrics

```sql
-- MySQL: Check active connections
SHOW STATUS WHERE `variable_name` = 'Threads_connected';

-- Check slow queries
SHOW FULL PROCESSLIST;

-- Check query cache hit rate
SHOW STATUS LIKE 'Qcache%';
```

### Redis Metrics

```bash
# Redis CLI
redis-cli INFO stats
redis-cli INFO memory
redis-cli SLOWLOG GET 10
```

## Troubleshooting

### High Error Rate

1. Check backend logs for exceptions
2. Verify database connection pool size (HikariCP)
3. Check Redis connectivity
4. Monitor disk I/O

### Slow Response Times

1. Enable SQL logging (already enabled in application.yml)
2. Check for missing database indices
3. Verify Redis cache hit rate
4. Monitor JVM garbage collection

### Connection Timeouts

1. Increase HikariCP pool size (currently 20)
2. Increase server thread pool
3. Check network latency
4. Verify firewall settings

## Best Practices

1. **Baseline First**: Run tests on current version before changes
2. **Gradual Ramp-up**: Use appropriate ramp-up periods
3. **Monitor System**: Watch CPU, memory, disk, network
4. **Clean Environment**: Fresh database state between tests
5. **Consistent Data**: Use same dataset for comparable results
6. **Multiple Runs**: Run tests 3-5 times and average results
7. **Production-like**: Test on similar infrastructure to production

## CI/CD Integration

Add to GitHub Actions workflow:

```yaml
- name: Run Load Tests
  run: |
    docker-compose up -d
    sleep 30  # Wait for services to start
    jmeter -n -t load-testing/jmeter-test-plan.jmx \
      -Jusers=50 \
      -Jduration=120 \
      -l results/ci-load-test.jtl
    # Parse results and fail if thresholds exceeded
```

## Results Baseline (Pre-Sprint 3)

| Endpoint | Avg Response Time | 95th Percentile | Throughput |
|----------|------------------|-----------------|------------|
| Health Check | 25ms | 50ms | 1000 req/s |
| Get Students (no cache) | 450ms | 800ms | 150 req/s |
| Get Opportunities | 380ms | 650ms | 180 req/s |

## Expected Results (Post-Sprint 3)

| Endpoint | Avg Response Time | 95th Percentile | Throughput |
|----------|------------------|-----------------|------------|
| Health Check | 20ms | 40ms | 1200 req/s |
| Get Students (cached) | 45ms | 80ms | 800 req/s |
| Get Students (DB with indices) | 120ms | 250ms | 450 req/s |
| Get Opportunities (indexed) | 95ms | 180ms | 600 req/s |

**Improvement**: 3-4x faster response times, 3-4x higher throughput with Redis cache and database indices.
