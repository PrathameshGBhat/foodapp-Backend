# Quick Reference Guide

## 📋 What Was Added?

### Services Enhanced
- ✅ **User Service** - Distributed Tracing + Circuit Breaker
- ✅ **Auth Server** - Distributed Tracing + Circuit Breaker + Version Upgrade

### New Dependencies (Both Services)
```
spring-cloud-starter-sleuth              (Distributed Tracing)
micrometer-tracing-bridge-brave          (Tracing Bridge)
spring-cloud-starter-circuitbreaker-resilience4j  (Circuit Breaker)
spring-cloud-starter-openfeign           (Service Calls)
```

### New Configuration Files
```
src/main/java/com/cts/config/CircuitBreakerConfig.java
src/main/java/com/cts/config/TracingConfig.java
src/main/java/com/cts/client/CircuitBreakerClient.java (User Service only)
```

---

## 🚀 Quick Start

### 1. Build Services
```bash
cd userservice/userservice && mvn clean package
cd authserver/authserver && mvn clean package
```

### 2. Run Services
```bash
# Terminal 1 - Config Server
cd config-server/config-server
mvn spring-boot:run

# Terminal 2 - Auth Server (Port 9001)
cd authserver/authserver
mvn spring-boot:run

# Terminal 3 - User Service (Port 8080)
cd userservice/userservice
mvn spring-boot:run
```

### 3. Test Tracing
```bash
# Make request
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"test@example.com","name":"Test"}'

# View logs - look for trace IDs
# Format: [serviceName,traceId,spanId]
```

### 4. View Circuit Breaker Status
```bash
# Health check
curl http://localhost:8080/actuator/health

# Detailed metrics
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state
```

---

## 🔍 Finding Trace IDs in Logs

### Pattern
```
LEVEL [application-name,traceId,spanId] - message
```

### Example
```
INFO  [userservice,8c6f8c37f5bfb7b6,3bfeb58f8bfb7b6] - Starting user registration
DEBUG [userservice,8c6f8c37f5bfb7b6,c52adf2f10b3d9d9] - Calling auth service
INFO  [authserver,8c6f8c37f5bfb7b6,f8c9e0a1b2c3d4e5] - Validating token
INFO  [userservice,8c6f8c37f5bfb7b6,3bfeb58f8bfb7b6] - User created successfully
```

All logs with same `traceId` = single request flow

---

## 🛡️ Circuit Breaker States

| State | What's Happening | Action |
|-------|-----------------|--------|
| **CLOSED** ✅ | Normal operation | Requests pass through |
| **OPEN** ❌ | Too many failures | Requests rejected immediately |
| **HALF_OPEN** 🔄 | Testing recovery | Allow test requests |

### How to Trigger Circuit Breaker
```bash
# 1. Make multiple requests to Auth Service
for i in {1..20}; do
  curl http://localhost:8080/api/users/register
done

# 2. Stop Auth Service
# Ctrl+C in auth server terminal

# 3. Make more requests
# Will see circuit breaker OPEN after failure threshold

# 4. Restart Auth Service
# Circuit will recover after 15 seconds (HALF_OPEN → CLOSED)
```

---

## ⚙️ Configuration Quick Reference

### Tracing Settings
```properties
# In application.properties

# Sample all requests (dev)
spring.sleuth.sampler.probability=1.0

# 128-bit trace IDs (more unique)
spring.sleuth.trace-id128=true

# Include trace/span in logs
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

### Circuit Breaker Settings
```properties
# Failure Rate Threshold
resilience4j.circuitbreaker.instances.authServiceClient.failureRateThreshold=50

# Sliding Window Size
resilience4j.circuitbreaker.instances.authServiceClient.slidingWindowSize=10

# Wait time before recovery attempt
resilience4j.circuitbreaker.instances.authServiceClient.waitDurationInOpenState=15s

# Test calls in HALF_OPEN state
resilience4j.circuitbreaker.instances.authServiceClient.permittedNumberOfCallsInHalfOpenState=3

# Retry Configuration
resilience4j.retry.instances.authServiceClient.maxAttempts=3
resilience4j.retry.instances.authServiceClient.waitDuration=2000
resilience4j.retry.instances.authServiceClient.enableExponentialBackoff=true

# Timeout
resilience4j.timelimiter.instances.authServiceClient.timeoutDuration=10s
```

---

## 📊 Production Tuning

### For Low Latency Services
```properties
resilience4j.circuitbreaker.instances.myService.failureRateThreshold=30
resilience4j.circuitbreaker.instances.myService.waitDurationInOpenState=5s
resilience4j.retry.instances.myService.maxAttempts=2
```

### For High-Availability Services
```properties
resilience4j.circuitbreaker.instances.myService.failureRateThreshold=70
resilience4j.circuitbreaker.instances.myService.waitDurationInOpenState=30s
resilience4j.retry.instances.myService.maxAttempts=5
```

### For Production Tracing
```properties
# Reduce overhead - sample 10% of requests
spring.sleuth.sampler.probability=0.1
```

---

## 🐛 Troubleshooting

### Problem: No trace IDs in logs
**Solution**: 
- Check `spring-cloud-starter-sleuth` is in pom.xml ✓
- Check `logging.pattern.level` includes `%X{traceId:-}` ✓
- Restart application after config change

### Problem: Circuit breaker not opening
**Solution**:
- Check failure rate calculation: (failures/total) × 100 ≥ threshold
- Minimum 5 calls needed before decision
- Check exceptions are being recorded correctly

### Problem: Timeout errors after adding circuit breaker
**Solution**:
- Increase `timelimiter.timeoutDuration` (default 10s)
- Check external service response times
- Verify network connectivity

### Problem: Too many circuit breaker state changes
**Solution**:
- Increase `waitDurationInOpenState` (default 15s)
- Increase `failureRateThreshold` (default 50%)
- Improve service stability

---

## 📚 Documentation Files

### In userservice/

1. **IMPLEMENTATION_CHANGES.md**
   - What was modified
   - Files created/changed
   - Configuration details

2. **DISTRIBUTED_TRACING_CIRCUIT_BREAKER_GUIDE.md**
   - In-depth explanation
   - Code examples
   - Testing scenarios

3. **VISUAL_ARCHITECTURE_SUMMARY.md**
   - Diagrams
   - Flow examples
   - State machines

4. **QUICK_REFERENCE.md** (this file)
   - Quick commands
   - Common patterns
   - Troubleshooting

---

## 🎯 Common Commands

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run

# Run tests
mvn test

# Check dependencies
mvn dependency:tree

# View metrics
curl http://localhost:8080/actuator/metrics

# View health
curl http://localhost:8080/actuator/health

# View circuit breaker details
curl http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state

# Check specific metric
curl 'http://localhost:8080/actuator/metrics/resilience4j.circuitbreaker.state?tag=instance:authServiceClient'

# Stream logs with grep
grep -i "circuitbreaker\|trace" application.log

# Get trace ID from logs
grep "8c6f8c37f5bfb7b6" userservice.log authserver.log
```

---

## ✅ Pre-Deployment Checklist

- [ ] All services build without errors
- [ ] Trace IDs appear in logs
- [ ] Circuit breaker state shown in health endpoint
- [ ] Tested with service failure scenario
- [ ] Recovery after service restart verified
- [ ] Logging pattern includes trace IDs
- [ ] Spring Cloud version aligned (2025.0.0)
- [ ] Spring Boot version aligned (3.5.7)
- [ ] Configuration documented for ops team
- [ ] Monitoring configured (if applicable)

---

## 📞 Version Information

### User Service
- Spring Boot: 3.5.7
- Spring Cloud: 2025.0.0
- Java: 21

### Auth Server
- Spring Boot: 3.5.7 (upgraded from 3.3.13)
- Spring Cloud: 2025.0.0 (upgraded from 2023.0.5)
- Java: 21

---

## 🔗 Related Resources

- [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth)
- [Resilience4j CircuitBreaker](https://resilience4j.readme.io/docs/circuitbreaker)
- [Brave Tracer](https://github.com/openzipkin/brave)
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)

---

**Last Updated**: November 18, 2025  
**Status**: Ready for Use  
**Implementation**: Complete ✅
