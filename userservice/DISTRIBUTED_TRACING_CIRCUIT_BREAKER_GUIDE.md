# Distributed Tracing & Circuit Breaker Implementation Guide

## Overview
This document describes the distributed tracing and circuit breaker pattern implementations added to the microservices architecture.

## Components Enhanced
- **User Service** ✅
- **Auth Server** ✅
- **Config Server** - N/A (no inter-service calls)
- **Common User Model** - N/A (library)
- **Exception Handler Lib** - N/A (library)

---

## 1. Distributed Tracing (Spring Cloud Sleuth + Brave)

### What is Distributed Tracing?
Distributed tracing allows you to track requests as they flow through multiple microservices, helping identify bottlenecks and troubleshoot issues.

### Implementation Details

#### Dependencies Added:
```xml
<!-- Distributed Tracing -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
</dependency>
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
```

#### Configuration (application.properties):
```properties
# Sampling Strategy
spring.sleuth.sampler.probability=1.0           # Sample 100% of requests (dev) / 0.1 (prod)
spring.sleuth.trace-id128=true                  # Use 128-bit trace IDs for better uniqueness

# Logging Pattern with Trace/Span IDs
logging.pattern.level=%5p [${spring.application.name:},%X{traceId:-},%X{spanId:-}]
```

#### Log Output Example:
```
 INFO [userservice,8c6f8c37f5bfb7b6,3bfeb58f8bfb7b6]
 DEBUG [userservice,8c6f8c37f5bfb7b6,c52adf2f10b3d9d9]
```

Each log line now includes:
- `traceId`: Unique ID for the entire request chain
- `spanId`: Unique ID for this service's portion of the request

### Benefits:
✅ End-to-end request tracking  
✅ Performance bottleneck identification  
✅ Error root cause analysis  
✅ Service dependency visualization  

---

## 2. Circuit Breaker Pattern (Resilience4j)

### What is the Circuit Breaker Pattern?
The Circuit Breaker pattern prevents cascading failures by stopping requests to failing services, similar to an electrical circuit breaker.

### Three States:

```
CLOSED (Normal) → OPEN (Failing) → HALF_OPEN (Testing) → CLOSED
     ↑                                    ↓
     └────────────────────────────────────┘
```

#### Dependencies Added:
```xml
<!-- Circuit Breaker Pattern -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-openfeign</artifactId>
</dependency>
```

### Configuration Details

#### Circuit Breaker Settings (application.properties):

**For User Service (calling Auth Service):**
```properties
resilience4j.circuitbreaker.instances.authServiceClient.registerHealthIndicator=true
resilience4j.circuitbreaker.instances.authServiceClient.slidingWindowSize=10
resilience4j.circuitbreaker.instances.authServiceClient.minimumNumberOfCalls=5
resilience4j.circuitbreaker.instances.authServiceClient.permittedNumberOfCallsInHalfOpenState=3
resilience4j.circuitbreaker.instances.authServiceClient.automaticTransitionFromOpenToHalfOpenEnabled=true
resilience4j.circuitbreaker.instances.authServiceClient.waitDurationInOpenState=15s
resilience4j.circuitbreaker.instances.authServiceClient.failureRateThreshold=50
resilience4j.circuitbreaker.instances.authServiceClient.recordExceptions=java.io.IOException,java.net.SocketTimeoutException,feign.FeignException
```

#### Retry Configuration:
```properties
resilience4j.retry.instances.authServiceClient.maxAttempts=3
resilience4j.retry.instances.authServiceClient.waitDuration=2000
resilience4j.retry.instances.authServiceClient.enableExponentialBackoff=true
```

#### Timeout Configuration:
```properties
resilience4j.timelimiter.instances.authServiceClient.timeoutDuration=10s
resilience4j.timelimiter.instances.authServiceClient.cancelRunningFuture=true
```

### Benefits:
✅ Prevents cascading failures  
✅ Fast failure detection  
✅ Automatic recovery attempts  
✅ Health check integration  
✅ Detailed metrics and monitoring  

---

## 3. Files Added/Modified

### New Files Created:

**User Service:**
- `src/main/java/com/cts/config/CircuitBreakerConfig.java` - Circuit breaker configuration
- `src/main/java/com/cts/config/TracingConfig.java` - Distributed tracing configuration
- `src/main/java/com/cts/client/CircuitBreakerClient.java` - Circuit breaker utility

**Auth Server:**
- `src/main/java/com/cts/config/CircuitBreakerConfig.java` - Circuit breaker configuration
- `src/main/java/com/cts/config/TracingConfig.java` - Distributed tracing configuration

### Modified Files:

**User Service:**
- `pom.xml` - Added dependencies and Spring Cloud BOM
- `src/main/resources/application.properties` - Tracing and circuit breaker configs

**Auth Server:**
- `pom.xml` - Upgraded Spring Boot 3.3.13 → 3.5.7, updated Spring Cloud 2023.0.5 → 2025.0.0
- `src/main/resources/application.properties` - Tracing and circuit breaker configs

---

## 4. Usage Example

### In Your Service Class:

```java
@Service
public class UserService {
    
    private final CircuitBreakerClient circuitBreakerClient;
    
    @Autowired
    public UserService(CircuitBreakerClient circuitBreakerClient) {
        this.circuitBreakerClient = circuitBreakerClient;
    }
    
    public User registerUser(UserDTO userDTO) {
        // Call auth service with circuit breaker protection
        return circuitBreakerClient.executeWithCircuitBreaker(
            "authServiceClient",
            unused -> authServiceClient.validateToken(userDTO.getToken()),
            exception -> handleAuthServiceFailure(userDTO)
        );
    }
    
    private User handleAuthServiceFailure(UserDTO userDTO) {
        logger.warn("Auth service unavailable, using fallback logic");
        return new User(userDTO); // Fallback response
    }
}
```

---

## 5. Health Checks & Monitoring

### Health Endpoint
```
GET /actuator/health
```

Includes circuit breaker status:
```json
{
  "status": "UP",
  "components": {
    "circuitBreakers": {
      "status": "UP",
      "details": {
        "authServiceClient": "CLOSED"
      }
    }
  }
}
```

### Metrics
```
GET /actuator/metrics/resilience4j.circuitbreaker.state
GET /actuator/metrics/resilience4j.circuitbreaker.calls
```

---

## 6. Production Recommendations

### Sampling (for Distributed Tracing)
```properties
# Development: Sample all requests
spring.sleuth.sampler.probability=1.0

# Production: Sample 10% to reduce overhead
spring.sleuth.sampler.probability=0.1
```

### Circuit Breaker Tuning
Adjust based on service requirements:
```properties
# Fail fast (aggressive)
resilience4j.circuitbreaker.instances.default.failureRateThreshold=30

# Tolerate more failures (conservative)
resilience4j.circuitbreaker.instances.default.failureRateThreshold=70
```

---

## 7. Integration with Monitoring Tools

Consider setting up:
- **Prometheus** - Metrics collection
- **Grafana** - Dashboard visualization
- **ELK Stack** - Centralized logging
- **Jaeger/Zipkin** - Trace visualization

---

## 8. Next Steps

1. Build and test both services
2. Verify distributed tracing in logs
3. Test circuit breaker with failure scenarios
4. Set up monitoring infrastructure
5. Tune circuit breaker parameters for your SLAs
