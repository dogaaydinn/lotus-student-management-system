# 🚀 Silicon Valley/NVIDIA-Grade Implementation Roadmap
## Lotus Student Management System - Production Excellence Plan

**Author**: Senior Software Engineer
**Target**: Production-Ready, Enterprise-Grade System
**Timeline**: 12 Weeks (Sprint-Based)
**Methodology**: Agile with TDD, CI/CD, Performance-First

---

## 📊 Executive Summary

Transform Lotus SMS from a solid MVP into a **production-grade, Silicon Valley-caliber system** that can:
- Handle **100,000+ concurrent users**
- Achieve **99.99% uptime SLA**
- Maintain **<100ms p95 API response time**
- Scale horizontally with **zero downtime deployments**
- Pass **OWASP, SOC 2, and enterprise security audits**

### Current State Assessment

```
✅ Strengths:
- Solid Spring Boot 2.7.2 foundation with clean architecture
- Redis + MySQL infrastructure ready
- Docker/K8s deployment configured
- Prometheus/Grafana monitoring setup
- JWT + MFA security implemented
- 99 Java files with enterprise patterns

⚠️ Critical Gaps:
- Test coverage: 9% (Target: 95%)
- No pagination (will crash at scale)
- Redis configured but NEVER used (0 cache hits)
- System.out.println() instead of structured logging
- No rate limiting (DDoS vulnerable)
- No query optimization
- Frontend hardcoded to localhost:8085
```

---

## 🎯 Strategic Implementation Phases

## SPRINT 1-2: FOUNDATION STABILIZATION (Weeks 1-2)
**Goal**: Achieve production-ready infrastructure and testing

### Week 1: Testing Infrastructure & Core Coverage

#### 1.1 Testing Framework Enhancement
**Files to Create/Modify:**

```
lotos_backend/src/test/java/com/lotus/lotusSPM/
├── base/
│   ├── BaseIntegrationTest.java           [CREATE]
│   ├── BaseServiceTest.java                [CREATE]
│   ├── BaseControllerTest.java             [CREATE]
│   └── TestDataFactory.java                [CREATE]
├── service/
│   ├── StudentServiceImplTest.java         [ENHANCE]
│   ├── CoordinatorServiceImplTest.java     [CREATE]
│   ├── OpportunitiesServiceImplTest.java   [CREATE]
│   ├── MessagesServiceImplTest.java        [CREATE]
│   ├── DocumentsServiceImplTest.java       [CREATE]
│   └── NotificationsServiceImplTest.java   [CREATE]
├── web/
│   ├── AuthControllerTest.java             [CREATE]
│   ├── StudentControllerTest.java          [CREATE]
│   ├── OpportunitiesControllerTest.java    [CREATE]
│   └── AdminControllerTest.java            [CREATE]
└── integration/
    ├── StudentWorkflowIntegrationTest.java [CREATE]
    ├── InternshipApplicationFlowTest.java  [CREATE]
    └── MessagingSystemIntegrationTest.java [CREATE]
```

**Implementation Details:**

```java
// BaseIntegrationTest.java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("lotus_test")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @Autowired protected MockMvc mockMvc;
    @Autowired protected ObjectMapper objectMapper;
    @Autowired protected TestRestTemplate restTemplate;

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.redis.host", redis::getHost);
        registry.add("spring.redis.port", redis::getFirstMappedPort);
    }

    @BeforeEach
    void setUp() {
        // Clean database before each test
        cleanDatabase();
    }

    protected void cleanDatabase() {
        // Implement cleanup logic
    }

    protected String getAuthToken(String username, String role) {
        // Helper to get JWT token for testing
    }
}
```

**Coverage Target**: 95% by end of Sprint 2

#### 1.2 Structured Logging Implementation

**Dependencies to Add** (pom.xml):
```xml
<!-- SLF4J API -->
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>

<!-- Logback Classic (already included via spring-boot-starter-logging) -->
<!-- Logstash Encoder (already present) -->
```

**Create Logging Configuration**:

```
lotos_backend/src/main/resources/
├── logback-spring.xml                      [CREATE]
└── application-prod.yml                    [CREATE]
```

**logback-spring.xml** structure:
- JSON formatted logs for production
- Correlation ID tracking
- Performance metrics logging
- Separate files for errors/access/audit
- Log rotation (10GB max, 30 days retention)

**Files to Modify** (Replace System.out.println):
```
web/DocumentsController.java               [MODIFY]
web/MessagesController.java                 [MODIFY]
web/NotificationsController.java            [MODIFY]
web/ApplicationFormController.java          [MODIFY]
service/impl/OfficialLetterServiceImpl.java [MODIFY]
```

**Pattern**:
```java
// BEFORE:
System.out.println("Processing document: " + fileName);

// AFTER:
log.info("Processing document upload",
    kv("fileName", fileName),
    kv("fileSize", fileSize),
    kv("userId", userId),
    kv("correlationId", MDC.get("correlationId"))
);
```

### Week 2: Performance Optimization - Pagination & Caching

#### 2.1 Pagination Implementation

**Strategy**: Implement cursor-based pagination for infinite scroll + offset-based for page numbers

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
├── dto/pagination/
│   ├── PageRequest.java                    [CREATE]
│   ├── PageResponse.java                   [CREATE]
│   └── CursorPageResponse.java             [CREATE]
└── util/
    └── PaginationUtil.java                 [CREATE]
```

**Implementation**:

```java
// PageRequest.java
@Data
@Builder
public class PageRequest {
    @Min(0)
    private int page = 0;

    @Min(1) @Max(100)
    private int size = 20;

    private String sortBy = "id";
    private String sortDirection = "ASC";

    public Pageable toPageable() {
        Sort.Direction direction = Sort.Direction.fromString(sortDirection);
        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }
}

// PageResponse.java
@Data
@Builder
public class PageResponse<T> {
    private List<T> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
    private boolean empty;

    public static <T> PageResponse<T> of(Page<T> page) {
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }
}
```

**Controllers to Update**:
```
web/StudentController.java                 [MODIFY - Add pagination]
web/OpportunitiesController.java           [MODIFY - Add pagination]
web/MessagesController.java                 [MODIFY - Add pagination]
web/DocumentsController.java                [MODIFY - Add pagination]
web/ApplicationFormController.java          [MODIFY - Add pagination]
```

**Example Endpoint Modification**:
```java
// BEFORE:
@GetMapping("/students")
public ResponseEntity<List<Student>> getAllStudents() {
    return ResponseEntity.ok(studentService.findStudents());
}

// AFTER:
@GetMapping("/students")
public ResponseEntity<PageResponse<StudentDTO>> getAllStudents(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "id") String sortBy,
        @RequestParam(defaultValue = "ASC") String sortDirection) {

    PageRequest pageRequest = PageRequest.builder()
            .page(page)
            .size(size)
            .sortBy(sortBy)
            .sortDirection(sortDirection)
            .build();

    Page<Student> studentPage = studentService.findStudents(pageRequest.toPageable());
    PageResponse<StudentDTO> response = PageResponse.of(
            studentPage.map(studentMapper::toDTO)
    );

    return ResponseEntity.ok(response);
}
```

**Repository Updates**:
```java
// StudentDao.java
public interface StudentDao extends JpaRepository<Student, Long> {
    Page<Student> findAll(Pageable pageable);
    Page<Student> findByFaculty(String faculty, Pageable pageable);
    Page<Student> findByDepartment(String department, Pageable pageable);
}
```

#### 2.2 Redis Caching Strategy

**Caching Layers**:
1. **L1 Cache**: Frequently accessed read-only data (TTL: 5 min)
2. **L2 Cache**: User sessions, permissions (TTL: 30 min)
3. **L3 Cache**: Analytics, reports (TTL: 1 hour)

**Files to Modify**:
```
config/RedisConfig.java                     [ENHANCE]
service/impl/StudentServiceImpl.java        [ADD CACHING]
service/impl/OpportunitiesServiceImpl.java  [ADD CACHING]
service/impl/AnalyticsService.java          [ADD CACHING]
```

**RedisConfig Enhancement**:
```java
@Configuration
@EnableCaching
public class RedisConfig {

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(5))
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new GenericJackson2JsonRedisSerializer()))
                .disableCachingNullValues();

        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Short-lived cache (5 minutes)
        cacheConfigurations.put("students", config.entryTtl(Duration.ofMinutes(5)));
        cacheConfigurations.put("opportunities", config.entryTtl(Duration.ofMinutes(5)));

        // Medium-lived cache (30 minutes)
        cacheConfigurations.put("userSessions", config.entryTtl(Duration.ofMinutes(30)));
        cacheConfigurations.put("permissions", config.entryTtl(Duration.ofMinutes(30)));

        // Long-lived cache (1 hour)
        cacheConfigurations.put("analytics", config.entryTtl(Duration.ofHours(1)));
        cacheConfigurations.put("reports", config.entryTtl(Duration.ofHours(1)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        return template;
    }
}
```

**Service Layer Caching**:
```java
@Service
@Slf4j
public class StudentServiceImpl implements StudentService {

    @Cacheable(value = "students", key = "#id")
    public Student findStdById(Long id) {
        log.info("Cache miss - fetching student from database", kv("studentId", id));
        return studentDao.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + id));
    }

    @CacheEvict(value = "students", key = "#student.id")
    public Student updateStudent(Student student) {
        log.info("Updating student - evicting cache", kv("studentId", student.getId()));
        return studentDao.save(student);
    }

    @CacheEvict(value = "students", allEntries = true)
    public void deleteStudent(Long id) {
        log.info("Deleting student - clearing cache", kv("studentId", id));
        studentDao.deleteById(id);
    }

    @Cacheable(value = "students", key = "'all:' + #page + ':' + #size")
    public Page<Student> findStudents(Pageable pageable) {
        log.info("Fetching students page",
            kv("page", pageable.getPageNumber()),
            kv("size", pageable.getPageSize())
        );
        return studentDao.findAll(pageable);
    }
}
```

**Cache Monitoring**:
```java
@Component
@Slf4j
public class CacheMetrics {

    @Autowired
    private CacheManager cacheManager;

    @Scheduled(fixedRate = 60000) // Every minute
    public void logCacheStats() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Cache cache = cacheManager.getCache(cacheName);
            if (cache instanceof RedisCache) {
                // Log cache hit/miss ratios
                log.info("Cache statistics",
                    kv("cacheName", cacheName),
                    kv("size", getCacheSize(cache))
                );
            }
        });
    }
}
```

---

## SPRINT 3-4: SECURITY & API HARDENING (Weeks 3-4)

### Week 3: Security Hardening

#### 3.1 Rate Limiting Implementation

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
├── ratelimit/
│   ├── RateLimitService.java               [CREATE]
│   ├── RateLimitFilter.java                [CREATE]
│   ├── RateLimitConfig.java                [CREATE]
│   └── RateLimitExceededException.java     [CREATE]
└── annotation/
    └── RateLimit.java                      [CREATE]
```

**Implementation Strategy**:
- Token bucket algorithm (via Bucket4j + Redis)
- Per-user limits: 100 req/min
- Per-IP limits: 1000 req/min
- Admin endpoints: 50 req/min
- Login endpoint: 5 req/min (prevent brute force)

**Dependencies to Add**:
```xml
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-core</artifactId>
    <version>8.7.0</version>
</dependency>
<dependency>
    <groupId>com.bucket4j</groupId>
    <artifactId>bucket4j-redis</artifactId>
    <version>8.7.0</version>
</dependency>
```

**RateLimitService.java**:
```java
@Service
@Slf4j
public class RateLimitService {

    private final RedisTemplate<String, String> redisTemplate;
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    public boolean allowRequest(String key, int capacity, int refillTokens, Duration refillDuration) {
        Bucket bucket = bucketCache.computeIfAbsent(key, k -> createBucket(capacity, refillTokens, refillDuration));

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            log.warn("Rate limit exceeded",
                kv("key", key),
                kv("capacity", capacity)
            );
            return false;
        }
    }

    private Bucket createBucket(int capacity, int refillTokens, Duration refillDuration) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(refillTokens, refillDuration)
                .build();
        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
```

**@RateLimit Annotation**:
```java
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    int capacity() default 100;
    int refillTokens() default 100;
    long refillDuration() default 60; // seconds
    RateLimitType type() default RateLimitType.USER;

    enum RateLimitType {
        USER,    // Per authenticated user
        IP,      // Per IP address
        GLOBAL   // Global limit
    }
}
```

**Usage in Controllers**:
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @PostMapping("/login")
    @RateLimit(capacity = 5, refillTokens = 5, refillDuration = 60)
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request) {
        // Login logic
    }

    @GetMapping("/students")
    @RateLimit(capacity = 100, type = RateLimitType.USER)
    public ResponseEntity<PageResponse<StudentDTO>> getStudents() {
        // Get students
    }
}
```

#### 3.2 File Upload Security

**Files to Modify**:
```
web/DocumentsController.java                [ENHANCE]
```

**Create Validation Service**:
```
service/FileValidationService.java          [CREATE]
config/FileUploadConfig.java                [CREATE]
```

**FileValidationService.java**:
```java
@Service
@Slf4j
public class FileValidationService {

    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "image/jpeg",
        "image/png"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    private static final Set<String> DANGEROUS_EXTENSIONS = Set.of(
        "exe", "bat", "sh", "jar", "war", "dll", "so"
    );

    public void validateFile(MultipartFile file) {
        // 1. Check file is not empty
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }

        // 2. Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds limit: " + MAX_FILE_SIZE);
        }

        // 3. Check MIME type
        String contentType = file.getContentType();
        if (!ALLOWED_MIME_TYPES.contains(contentType)) {
            log.warn("Rejected file upload - invalid MIME type",
                kv("fileName", file.getOriginalFilename()),
                kv("contentType", contentType)
            );
            throw new BadRequestException("File type not allowed: " + contentType);
        }

        // 4. Check file extension
        String fileName = file.getOriginalFilename();
        String extension = getFileExtension(fileName).toLowerCase();
        if (DANGEROUS_EXTENSIONS.contains(extension)) {
            log.warn("Rejected file upload - dangerous extension",
                kv("fileName", fileName),
                kv("extension", extension)
            );
            throw new BadRequestException("File extension not allowed: " + extension);
        }

        // 5. Validate actual file content matches MIME type
        try {
            String detectedType = Files.probeContentType(
                Path.of(file.getOriginalFilename())
            );
            if (detectedType != null && !detectedType.equals(contentType)) {
                throw new BadRequestException("File content does not match MIME type");
            }
        } catch (IOException e) {
            log.error("Error validating file content", e);
            throw new BadRequestException("Error validating file");
        }
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1) : "";
    }
}
```

**Enhanced DocumentsController**:
```java
@RestController
@RequestMapping("/api/documents")
@Slf4j
public class DocumentsController {

    @Autowired
    private FileValidationService fileValidationService;

    @PostMapping("/upload")
    @RateLimit(capacity = 10, refillDuration = 60)
    public ResponseEntity<DocumentDTO> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description,
            @AuthenticationPrincipal UserDetails userDetails) {

        // Validate file
        fileValidationService.validateFile(file);

        // Sanitize filename (prevent path traversal)
        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());

        log.info("Uploading document",
            kv("fileName", sanitizedFileName),
            kv("fileSize", file.getSize()),
            kv("user", userDetails.getUsername())
        );

        // Process upload
        Document document = documentService.uploadDocument(file, description, userDetails.getUsername());

        return ResponseEntity.ok(documentMapper.toDTO(document));
    }

    private String sanitizeFileName(String fileName) {
        // Remove path separators and dangerous characters
        return fileName.replaceAll("[^a-zA-Z0-9.-]", "_");
    }
}
```

#### 3.3 Security Headers & CORS

**Files to Create**:
```
config/SecurityHeadersConfig.java           [CREATE]
```

**SecurityHeadersConfig.java**:
```java
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new SecurityHeadersFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }

    private static class SecurityHeadersFilter implements Filter {
        @Override
        public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
                throws IOException, ServletException {

            HttpServletResponse httpResponse = (HttpServletResponse) response;

            // Content Security Policy
            httpResponse.setHeader("Content-Security-Policy",
                "default-src 'self'; " +
                "script-src 'self' 'unsafe-inline'; " +
                "style-src 'self' 'unsafe-inline'; " +
                "img-src 'self' data: https:; " +
                "font-src 'self'; " +
                "connect-src 'self'; " +
                "frame-ancestors 'none';"
            );

            // HTTP Strict Transport Security
            httpResponse.setHeader("Strict-Transport-Security",
                "max-age=31536000; includeSubDomains");

            // X-Content-Type-Options
            httpResponse.setHeader("X-Content-Type-Options", "nosniff");

            // X-Frame-Options
            httpResponse.setHeader("X-Frame-Options", "DENY");

            // X-XSS-Protection
            httpResponse.setHeader("X-XSS-Protection", "1; mode=block");

            // Referrer-Policy
            httpResponse.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

            // Permissions-Policy
            httpResponse.setHeader("Permissions-Policy",
                "geolocation=(), microphone=(), camera=()");

            chain.doFilter(request, response);
        }
    }
}
```

**Enhanced CORS Configuration**:
```java
@Configuration
public class CorsConfig {

    @Value("${app.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Use environment-specific allowed origins (NO WILDCARDS in prod)
        configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));

        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("X-Total-Count", "X-Page-Number"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
```

**application.yml**:
```yaml
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:5173
```

**application-prod.yml**:
```yaml
app:
  cors:
    allowed-origins:
      - https://lotus.yourdomain.com
      - https://app.lotus.yourdomain.com
```

### Week 4: API Quality & Documentation

#### 4.1 Request/Response Logging with Correlation IDs

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
├── logging/
│   ├── CorrelationIdFilter.java            [CREATE]
│   ├── RequestResponseLoggingFilter.java   [CREATE]
│   └── LoggingAspect.java                  [CREATE]
└── util/
    └── MDCUtil.java                        [CREATE]
```

**CorrelationIdFilter.java**:
```java
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }

        MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(CORRELATION_ID_MDC_KEY);
        }
    }
}
```

**LoggingAspect.java** (Method-level performance logging):
```java
@Aspect
@Component
@Slf4j
public class LoggingAspect {

    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public Object logControllerMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;

            log.info("Controller method executed",
                kv("method", methodName),
                kv("executionTimeMs", executionTime),
                kv("success", true)
            );

            return result;
        } catch (Exception e) {
            long executionTime = System.currentTimeMillis() - startTime;

            log.error("Controller method failed",
                kv("method", methodName),
                kv("executionTimeMs", executionTime),
                kv("error", e.getMessage()),
                kv("success", false),
                e
            );

            throw e;
        }
    }

    @Around("@annotation(org.springframework.cache.annotation.Cacheable)")
    public Object logCacheableMethod(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();

        log.debug("Cache lookup",
            kv("method", methodName),
            kv("args", Arrays.toString(joinPoint.getArgs()))
        );

        return joinPoint.proceed();
    }
}
```

#### 4.2 Enhanced OpenAPI Documentation

**Files to Modify**:
```
config/OpenApiConfig.java                   [ENHANCE]
```

**Enhanced OpenApiConfig.java**:
```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${app.version}") String appVersion) {
        return new OpenAPI()
                .info(new Info()
                        .title("Lotus Student Management System API")
                        .version(appVersion)
                        .description("Production-grade REST API for student management with enterprise features")
                        .contact(new Contact()
                                .name("Lotus SMS Team")
                                .email("api@lotus-sms.com")
                                .url("https://github.com/dogaaydinn/lotus-student-management-system"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .externalDocs(new ExternalDocumentation()
                        .description("Full Documentation")
                        .url("https://docs.lotus-sms.com"))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8085").description("Local Development"),
                        new Server().url("https://api.lotus-sms.com").description("Production")
                ))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .name("Authorization"))
                        .addSchemas("Error", new Schema<>()
                                .type("object")
                                .addProperty("timestamp", new Schema<>().type("string").format("date-time"))
                                .addProperty("status", new Schema<>().type("integer"))
                                .addProperty("error", new Schema<>().type("string"))
                                .addProperty("message", new Schema<>().type("string"))
                                .addProperty("path", new Schema<>().type("string"))
                                .addProperty("correlationId", new Schema<>().type("string"))))
                .addSecurityItem(new SecurityRequirement().addList("bearer-jwt"));
    }

    @Bean
    public GroupedOpenApi studentApi() {
        return GroupedOpenApi.builder()
                .group("students")
                .pathsToMatch("/api/students/**")
                .build();
    }

    @Bean
    public GroupedOpenApi authApi() {
        return GroupedOpenApi.builder()
                .group("authentication")
                .pathsToMatch("/api/auth/**")
                .build();
    }

    @Bean
    public GroupedOpenApi adminApi() {
        return GroupedOpenApi.builder()
                .group("admin")
                .pathsToMatch("/api/admin/**")
                .build();
    }
}
```

**Add OpenAPI Annotations to Controllers**:
```java
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student management endpoints")
public class StudentController {

    @GetMapping
    @Operation(
        summary = "Get all students",
        description = "Retrieve paginated list of students with optional filtering and sorting"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved students",
            content = @Content(schema = @Schema(implementation = PageResponse.class))),
        @ApiResponse(responseCode = "401", description = "Unauthorized",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "429", description = "Rate limit exceeded",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<PageResponse<StudentDTO>> getAllStudents(
            @Parameter(description = "Page number (0-indexed)", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(description = "Page size", example = "20")
            @RequestParam(defaultValue = "20") int size) {
        // Implementation
    }
}
```

---

## SPRINT 5-6: DATABASE OPTIMIZATION (Weeks 5-6)

### Week 5: Query Optimization & Indexing

#### 5.1 Database Index Strategy

**Create Migration File**:
```
lotos_backend/src/main/resources/db/migration/
└── V6__add_performance_indexes.sql         [CREATE]
```

**V6__add_performance_indexes.sql**:
```sql
-- Composite indexes for common query patterns
CREATE INDEX idx_student_faculty_dept ON students(faculty, department);
CREATE INDEX idx_student_email_active ON students(email, active);
CREATE INDEX idx_student_username ON students(username);

-- Opportunities indexes
CREATE INDEX idx_opportunities_status ON opportunities(status);
CREATE INDEX idx_opportunities_deadline ON opportunities(deadline);
CREATE INDEX idx_opportunities_company_status ON opportunities(company_name, status);

-- Application form indexes
CREATE INDEX idx_application_student_id ON application_forms(student_id);
CREATE INDEX idx_application_opportunity_id ON application_forms(opportunity_id);
CREATE INDEX idx_application_status ON application_forms(status);
CREATE INDEX idx_application_student_status ON application_forms(student_id, status);

-- Messages indexes
CREATE INDEX idx_messages_sender_id ON messages(sender_id);
CREATE INDEX idx_messages_receiver_id ON messages(receiver_id);
CREATE INDEX idx_messages_timestamp ON messages(timestamp);
CREATE INDEX idx_messages_receiver_unread ON messages(receiver_id, is_read);

-- Documents indexes
CREATE INDEX idx_documents_student_id ON documents(student_id);
CREATE INDEX idx_documents_upload_date ON documents(upload_date);

-- Full-text search indexes
CREATE FULLTEXT INDEX idx_opportunities_fulltext ON opportunities(title, description);
CREATE FULLTEXT INDEX idx_student_fulltext ON students(username, email, faculty, department);

-- Audit log partitioning preparation
ALTER TABLE audit_logs PARTITION BY RANGE (YEAR(timestamp)) (
    PARTITION p2024 VALUES LESS THAN (2025),
    PARTITION p2025 VALUES LESS THAN (2026),
    PARTITION p2026 VALUES LESS THAN (2027),
    PARTITION p_future VALUES LESS THAN MAXVALUE
);
```

#### 5.2 Query Optimization

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
└── dao/custom/
    ├── CustomStudentRepository.java         [CREATE]
    ├── CustomStudentRepositoryImpl.java     [CREATE]
    ├── CustomOpportunitiesRepository.java   [CREATE]
    └── CustomOpportunitiesRepositoryImpl.java [CREATE]
```

**CustomStudentRepository.java**:
```java
public interface CustomStudentRepository {

    @Query("SELECT s FROM Student s " +
           "LEFT JOIN FETCH s.applicationForms " +
           "WHERE s.id = :id")
    Optional<Student> findByIdWithApplications(@Param("id") Long id);

    @Query("SELECT s FROM Student s " +
           "WHERE s.faculty = :faculty AND s.department = :department " +
           "AND s.active = true")
    Page<Student> findActiveByFacultyAndDepartment(
            @Param("faculty") String faculty,
            @Param("department") String department,
            Pageable pageable);

    @Query(value = "SELECT s.* FROM students s " +
                   "WHERE MATCH(s.username, s.email, s.faculty, s.department) " +
                   "AGAINST (:searchTerm IN NATURAL LANGUAGE MODE)",
           nativeQuery = true)
    List<Student> fullTextSearch(@Param("searchTerm") String searchTerm);

    // DTO projection for better performance
    @Query("SELECT new com.lotus.lotusSPM.dto.StudentSummaryDTO(" +
           "s.id, s.username, s.email, s.faculty, s.department) " +
           "FROM Student s " +
           "WHERE s.active = true")
    Page<StudentSummaryDTO> findAllSummaries(Pageable pageable);
}
```

**Implement N+1 Query Prevention**:
```java
@Repository
public interface OpportunitiesDao extends JpaRepository<Opportunities, Long> {

    // BAD: Causes N+1 queries
    // List<Opportunities> findAll();

    // GOOD: Fetch associations in single query
    @Query("SELECT DISTINCT o FROM Opportunities o " +
           "LEFT JOIN FETCH o.applications " +
           "LEFT JOIN FETCH o.careerCenter")
    List<Opportunities> findAllWithAssociations();

    @Query("SELECT o FROM Opportunities o " +
           "WHERE o.status = :status " +
           "AND o.deadline > :currentDate " +
           "ORDER BY o.deadline ASC")
    Page<Opportunities> findActiveOpportunities(
            @Param("status") String status,
            @Param("currentDate") LocalDate currentDate,
            Pageable pageable);
}
```

### Week 6: Performance Monitoring & Optimization

#### 6.1 Query Performance Logging

**Create Hibernate Statistics Configuration**:

**application.yml**:
```yaml
spring:
  jpa:
    properties:
      hibernate:
        generate_statistics: true
        session:
          events:
            log:
              LOG_QUERIES_SLOWER_THAN_MS: 200
```

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
├── monitoring/
│   ├── QueryPerformanceLogger.java         [CREATE]
│   └── DatabaseMetrics.java                [CREATE]
└── config/
    └── HibernateConfig.java                [CREATE]
```

**QueryPerformanceLogger.java**:
```java
@Component
@Slf4j
public class QueryPerformanceLogger {

    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(fixedRate = 300000) // Every 5 minutes
    public void logHibernateStatistics() {
        Statistics stats = entityManager.getEntityManagerFactory()
                .unwrap(SessionFactory.class)
                .getStatistics();

        log.info("Hibernate statistics",
            kv("queryCacheHitCount", stats.getQueryCacheHitCount()),
            kv("queryCacheMissCount", stats.getQueryCacheMissCount()),
            kv("queryCachePutCount", stats.getQueryCachePutCount()),
            kv("secondLevelCacheHitCount", stats.getSecondLevelCacheHitCount()),
            kv("secondLevelCacheMissCount", stats.getSecondLevelCacheMissCount()),
            kv("queryExecutionCount", stats.getQueryExecutionCount()),
            kv("queryExecutionMaxTime", stats.getQueryExecutionMaxTime()),
            kv("sessionOpenCount", stats.getSessionOpenCount()),
            kv("sessionCloseCount", stats.getSessionCloseCount()),
            kv("transactionCount", stats.getTransactionCount())
        );

        // Log slow queries
        String[] queries = stats.getQueries();
        for (String query : queries) {
            QueryStatistics queryStats = stats.getQueryStatistics(query);
            if (queryStats.getExecutionMaxTime() > 200) {
                log.warn("Slow query detected",
                    kv("query", query),
                    kv("executionMaxTime", queryStats.getExecutionMaxTime()),
                    kv("executionCount", queryStats.getExecutionCount())
                );
            }
        }
    }
}
```

#### 6.2 Connection Pool Tuning

**Enhanced HikariCP Configuration**:

**application-prod.yml**:
```yaml
spring:
  datasource:
    hikari:
      # Connection pool sizing (formula: ((core_count * 2) + effective_spindle_count))
      maximum-pool-size: 20
      minimum-idle: 10

      # Connection timeout
      connection-timeout: 30000
      idle-timeout: 600000
      max-lifetime: 1800000

      # Performance tuning
      auto-commit: false
      connection-test-query: SELECT 1
      pool-name: LotusHikariPool

      # Leak detection (production)
      leak-detection-threshold: 60000

      # Metrics
      register-mbeans: true
```

---

## SPRINT 7-8: FRONTEND OPTIMIZATION (Weeks 7-8)

### Week 7: Frontend Performance & PWA

#### 7.1 Environment Configuration

**Files to Create**:
```
lotus_frontend/
├── .env.development                        [CREATE]
├── .env.production                         [CREATE]
└── src/config/
    └── api.config.js                       [CREATE]
```

**.env.development**:
```
VITE_API_BASE_URL=http://localhost:8085/api
VITE_APP_ENVIRONMENT=development
VITE_ENABLE_LOGGING=true
```

**.env.production**:
```
VITE_API_BASE_URL=https://api.lotus-sms.com/api
VITE_APP_ENVIRONMENT=production
VITE_ENABLE_LOGGING=false
```

**api.config.js**:
```javascript
export const API_CONFIG = {
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json',
  },
}

export const ENDPOINTS = {
  AUTH: {
    LOGIN: '/auth/login',
    LOGOUT: '/auth/logout',
    REFRESH: '/auth/refresh',
  },
  STUDENTS: {
    LIST: '/students',
    DETAIL: (id) => `/students/${id}`,
    CREATE: '/students',
    UPDATE: (id) => `/students/${id}`,
    DELETE: (id) => `/students/${id}`,
  },
  // ... other endpoints
}
```

#### 7.2 Axios Interceptors & Error Handling

**Files to Create/Modify**:
```
lotus_frontend/src/
├── api/
│   ├── axios.config.js                     [CREATE]
│   ├── interceptors.js                     [CREATE]
│   └── errorHandler.js                     [CREATE]
└── utils/
    └── logger.js                           [CREATE]
```

**axios.config.js**:
```javascript
import axios from 'axios'
import { API_CONFIG } from '@/config/api.config'
import { setupInterceptors } from './interceptors'

const apiClient = axios.create(API_CONFIG)

// Setup request/response interceptors
setupInterceptors(apiClient)

export default apiClient
```

**interceptors.js**:
```javascript
import { useAuthStore } from '@/stores/auth.store'
import { logger } from '@/utils/logger'

let requestInterceptorId = null
let responseInterceptorId = null

export function setupInterceptors(axiosInstance) {
  // Request interceptor
  requestInterceptorId = axiosInstance.interceptors.request.use(
    (config) => {
      // Add correlation ID
      config.headers['X-Correlation-ID'] = crypto.randomUUID()

      // Add auth token
      const authStore = useAuthStore()
      const token = authStore.token
      if (token) {
        config.headers.Authorization = `Bearer ${token}`
      }

      // Log request
      logger.debug('API Request', {
        method: config.method,
        url: config.url,
        correlationId: config.headers['X-Correlation-ID'],
      })

      // Start request timer
      config.metadata = { startTime: new Date() }

      return config
    },
    (error) => {
      logger.error('Request Error', error)
      return Promise.reject(error)
    }
  )

  // Response interceptor
  responseInterceptorId = axiosInstance.interceptors.response.use(
    (response) => {
      // Calculate request duration
      const duration = new Date() - response.config.metadata.startTime

      logger.debug('API Response', {
        status: response.status,
        url: response.config.url,
        duration: `${duration}ms`,
        correlationId: response.headers['x-correlation-id'],
      })

      return response
    },
    async (error) => {
      const originalRequest = error.config

      // Handle 401 Unauthorized - try to refresh token
      if (error.response?.status === 401 && !originalRequest._retry) {
        originalRequest._retry = true

        try {
          const authStore = useAuthStore()
          await authStore.refreshToken()

          // Retry original request with new token
          originalRequest.headers.Authorization = `Bearer ${authStore.token}`
          return axiosInstance(originalRequest)
        } catch (refreshError) {
          // Refresh failed - logout user
          const authStore = useAuthStore()
          authStore.logout()
          window.location.href = '/login'
          return Promise.reject(refreshError)
        }
      }

      // Handle 429 Rate Limit - retry with exponential backoff
      if (error.response?.status === 429 && !originalRequest._retryCount) {
        originalRequest._retryCount = 0
      }

      if (error.response?.status === 429 && originalRequest._retryCount < 3) {
        originalRequest._retryCount++
        const delay = Math.pow(2, originalRequest._retryCount) * 1000

        logger.warn('Rate limited - retrying', {
          retryCount: originalRequest._retryCount,
          delayMs: delay,
        })

        await new Promise(resolve => setTimeout(resolve, delay))
        return axiosInstance(originalRequest)
      }

      // Log error
      logger.error('API Error', {
        status: error.response?.status,
        message: error.response?.data?.message || error.message,
        url: error.config?.url,
        correlationId: error.response?.headers?.['x-correlation-id'],
      })

      return Promise.reject(error)
    }
  )
}
```

#### 7.3 PWA Implementation

**Files to Modify**:
```
lotus_frontend/
├── vite.config.js                          [MODIFY]
└── index.html                              [MODIFY]
```

**Files to Create**:
```
lotus_frontend/
├── public/
│   ├── manifest.json                       [CREATE]
│   ├── sw.js                               [CREATE]
│   └── icons/                              [CREATE FOLDER]
└── src/
    └── registerServiceWorker.js            [CREATE]
```

**vite.config.js** (Add PWA plugin):
```javascript
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { VitePWA } from 'vite-plugin-pwa'

export default defineConfig({
  plugins: [
    vue(),
    VitePWA({
      registerType: 'autoUpdate',
      includeAssets: ['favicon.ico', 'robots.txt', 'apple-touch-icon.png'],
      manifest: {
        name: 'Lotus Student Management System',
        short_name: 'Lotus SMS',
        description: 'Enterprise student management platform',
        theme_color: '#ffffff',
        background_color: '#ffffff',
        display: 'standalone',
        orientation: 'portrait',
        scope: '/',
        start_url: '/',
        icons: [
          {
            src: '/icons/icon-72x72.png',
            sizes: '72x72',
            type: 'image/png',
          },
          {
            src: '/icons/icon-96x96.png',
            sizes: '96x96',
            type: 'image/png',
          },
          {
            src: '/icons/icon-128x128.png',
            sizes: '128x128',
            type: 'image/png',
          },
          {
            src: '/icons/icon-144x144.png',
            sizes: '144x144',
            type: 'image/png',
          },
          {
            src: '/icons/icon-152x152.png',
            sizes: '152x152',
            type: 'image/png',
          },
          {
            src: '/icons/icon-192x192.png',
            sizes: '192x192',
            type: 'image/png',
          },
          {
            src: '/icons/icon-384x384.png',
            sizes: '384x384',
            type: 'image/png',
          },
          {
            src: '/icons/icon-512x512.png',
            sizes: '512x512',
            type: 'image/png',
          },
        ],
      },
      workbox: {
        runtimeCaching: [
          {
            urlPattern: /^https:\/\/api\.lotus-sms\.com\/api\/.*/i,
            handler: 'NetworkFirst',
            options: {
              cacheName: 'api-cache',
              expiration: {
                maxEntries: 100,
                maxAgeSeconds: 60 * 5, // 5 minutes
              },
              cacheableResponse: {
                statuses: [0, 200],
              },
            },
          },
          {
            urlPattern: /\.(?:png|jpg|jpeg|svg|gif)$/,
            handler: 'CacheFirst',
            options: {
              cacheName: 'images-cache',
              expiration: {
                maxEntries: 50,
                maxAgeSeconds: 60 * 60 * 24 * 30, // 30 days
              },
            },
          },
        ],
      },
    }),
  ],
  build: {
    // Code splitting
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['vue', 'vue-router', 'pinia'],
          'ui': ['bootstrap'],
          'utils': ['axios', 'yup'],
        },
      },
    },
    // Chunk size warnings
    chunkSizeWarningLimit: 500,
  },
})
```

### Week 8: UI/UX Improvements

#### 8.1 Dark Mode Implementation

**Files to Create**:
```
lotus_frontend/src/
├── composables/
│   └── useDarkMode.js                      [CREATE]
├── styles/
│   ├── themes/
│   │   ├── light.css                       [CREATE]
│   │   └── dark.css                        [CREATE]
│   └── variables.css                       [CREATE]
└── stores/
    └── theme.store.js                      [CREATE]
```

**theme.store.js**:
```javascript
import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useThemeStore = defineStore('theme', () => {
  const isDark = ref(false)

  // Initialize from localStorage or system preference
  const initTheme = () => {
    const stored = localStorage.getItem('theme')
    if (stored) {
      isDark.value = stored === 'dark'
    } else {
      // Use system preference
      isDark.value = window.matchMedia('(prefers-color-scheme: dark)').matches
    }
    applyTheme()
  }

  const toggleTheme = () => {
    isDark.value = !isDark.value
    applyTheme()
  }

  const applyTheme = () => {
    const theme = isDark.value ? 'dark' : 'light'
    document.documentElement.setAttribute('data-theme', theme)
    localStorage.setItem('theme', theme)
  }

  // Watch for system theme changes
  window.matchMedia('(prefers-color-scheme: dark)')
    .addEventListener('change', (e) => {
      if (!localStorage.getItem('theme')) {
        isDark.value = e.matches
        applyTheme()
      }
    })

  return {
    isDark,
    toggleTheme,
    initTheme,
  }
})
```

**variables.css**:
```css
:root[data-theme='light'] {
  --bg-primary: #ffffff;
  --bg-secondary: #f8f9fa;
  --text-primary: #212529;
  --text-secondary: #6c757d;
  --border-color: #dee2e6;
  --primary-color: #0d6efd;
  --success-color: #198754;
  --warning-color: #ffc107;
  --danger-color: #dc3545;
}

:root[data-theme='dark'] {
  --bg-primary: #1a1a1a;
  --bg-secondary: #2d2d2d;
  --text-primary: #e9ecef;
  --text-secondary: #adb5bd;
  --border-color: #495057;
  --primary-color: #0d6efd;
  --success-color: #198754;
  --warning-color: #ffc107;
  --danger-color: #dc3545;
}

body {
  background-color: var(--bg-primary);
  color: var(--text-primary);
  transition: background-color 0.3s ease, color 0.3s ease;
}
```

---

## SPRINT 9-10: MONITORING & OBSERVABILITY (Weeks 9-10)

### Week 9: Distributed Tracing & Metrics

#### 9.1 Distributed Tracing with Spring Cloud Sleuth

**Dependencies to Add**:
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-sleuth</artifactId>
    <version>3.1.9</version>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-sleuth-zipkin</artifactId>
    <version>3.1.9</version>
</dependency>
```

**application.yml**:
```yaml
spring:
  sleuth:
    sampler:
      probability: 1.0  # Sample 100% in dev, 0.1 (10%) in prod
  zipkin:
    base-url: http://localhost:9411
    enabled: true
```

#### 9.2 Custom Metrics

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
└── monitoring/
    ├── BusinessMetrics.java                [CREATE]
    └── PerformanceMetrics.java             [CREATE]
```

**BusinessMetrics.java**:
```java
@Component
public class BusinessMetrics {

    private final Counter studentRegistrations;
    private final Counter applicationSubmissions;
    private final Gauge activeStudents;
    private final Timer loginDuration;

    public BusinessMetrics(MeterRegistry registry) {
        this.studentRegistrations = Counter.builder("students.registered")
                .description("Total number of student registrations")
                .tag("type", "business")
                .register(registry);

        this.applicationSubmissions = Counter.builder("applications.submitted")
                .description("Total number of internship applications")
                .tag("type", "business")
                .register(registry);

        this.activeStudents = Gauge.builder("students.active", this::getActiveStudentCount)
                .description("Number of active students")
                .tag("type", "business")
                .register(registry);

        this.loginDuration = Timer.builder("login.duration")
                .description("Time taken for login")
                .tag("type", "performance")
                .register(registry);
    }

    public void recordStudentRegistration() {
        studentRegistrations.increment();
    }

    public void recordApplicationSubmission() {
        applicationSubmissions.increment();
    }

    public void recordLogin(Runnable loginLogic) {
        loginDuration.record(loginLogic);
    }

    private double getActiveStudentCount() {
        // Implement logic to get active student count
        return 0;
    }
}
```

### Week 10: ELK Stack Integration

#### 10.1 Logstash Configuration

**Files to Create**:
```
monitoring/
└── logstash/
    ├── pipeline/
    │   └── lotus-logs.conf                 [CREATE]
    └── docker-compose.elk.yml              [CREATE]
```

**lotus-logs.conf**:
```
input {
  tcp {
    port => 5000
    codec => json
  }
  file {
    path => "/var/log/lotus/*.log"
    start_position => "beginning"
    codec => json
  }
}

filter {
  if [logger_name] =~ "com.lotus.lotusSPM" {
    mutate {
      add_field => { "application" => "lotus-sms" }
    }
  }

  # Extract correlation ID
  if [correlationId] {
    mutate {
      add_field => { "[@metadata][correlation_id]" => "%{correlationId}" }
    }
  }

  # Parse duration
  if [executionTimeMs] {
    mutate {
      convert => { "executionTimeMs" => "integer" }
    }
  }
}

output {
  elasticsearch {
    hosts => ["elasticsearch:9200"]
    index => "lotus-logs-%{+YYYY.MM.dd}"
  }

  # Also output to console for debugging
  stdout {
    codec => rubydebug
  }
}
```

**docker-compose.elk.yml**:
```yaml
version: '3.8'

services:
  elasticsearch:
    image: docker.elastic.co/elasticsearch/elasticsearch:8.11.0
    container_name: elasticsearch
    environment:
      - discovery.type=single-node
      - "ES_JAVA_OPTS=-Xms512m -Xmx512m"
      - xpack.security.enabled=false
    ports:
      - "9200:9200"
    volumes:
      - elasticsearch-data:/usr/share/elasticsearch/data
    networks:
      - elk

  logstash:
    image: docker.elastic.co/logstash/logstash:8.11.0
    container_name: logstash
    volumes:
      - ./logstash/pipeline:/usr/share/logstash/pipeline
    ports:
      - "5000:5000"
    environment:
      - "LS_JAVA_OPTS=-Xmx256m -Xms256m"
    depends_on:
      - elasticsearch
    networks:
      - elk

  kibana:
    image: docker.elastic.co/kibana/kibana:8.11.0
    container_name: kibana
    ports:
      - "5601:5601"
    environment:
      - ELASTICSEARCH_URL=http://elasticsearch:9200
    depends_on:
      - elasticsearch
    networks:
      - elk

volumes:
  elasticsearch-data:

networks:
  elk:
    driver: bridge
```

---

## SPRINT 11-12: PRODUCTION DEPLOYMENT (Weeks 11-12)

### Week 11: CI/CD Enhancement

#### 11.1 Enhanced GitHub Actions Workflow

**Files to Modify**:
```
.github/workflows/
├── backend-ci.yml                          [ENHANCE]
├── frontend-ci.yml                         [ENHANCE]
└── deploy-production.yml                   [CREATE]
```

**backend-ci.yml** (Enhanced):
```yaml
name: Backend CI/CD

on:
  push:
    branches: [main, develop]
    paths:
      - 'lotos_backend/**'
  pull_request:
    branches: [main]
    paths:
      - 'lotos_backend/**'

jobs:
  build-and-test:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_DATABASE: lotus_test
          MYSQL_ROOT_PASSWORD: test_password
        ports:
          - 3306:3306
        options: >-
          --health-cmd="mysqladmin ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

      redis:
        image: redis:7-alpine
        ports:
          - 6379:6379
        options: >-
          --health-cmd="redis-cli ping"
          --health-interval=10s
          --health-timeout=5s
          --health-retries=3

    steps:
      - name: Checkout code
        uses: actions/checkout@v4

      - name: Set up JDK 8
        uses: actions/setup-java@v4
        with:
          java-version: '8'
          distribution: 'temurin'
          cache: maven

      - name: Cache Maven packages
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
          restore-keys: ${{ runner.os }}-m2

      - name: Run tests with coverage
        run: |
          cd lotos_backend
          mvn clean test jacoco:report
        env:
          SPRING_DATASOURCE_URL: jdbc:mysql://localhost:3306/lotus_test
          SPRING_DATASOURCE_USERNAME: root
          SPRING_DATASOURCE_PASSWORD: test_password
          SPRING_REDIS_HOST: localhost
          SPRING_REDIS_PORT: 6379

      - name: Check test coverage
        run: |
          cd lotos_backend
          mvn jacoco:check -Dja coco.unit-tests.limit.minimum=0.90

      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./lotos_backend/target/site/jacoco/jacoco.xml
          flags: backend
          name: backend-coverage

      - name: Build Docker image
        if: github.ref == 'refs/heads/main'
        run: |
          cd lotos_backend
          docker build -t lotus-backend:${{ github.sha }} .

      - name: Security scan with Trivy
        uses: aquasecurity/trivy-action@master
        with:
          image-ref: lotus-backend:${{ github.sha }}
          format: 'sarif'
          output: 'trivy-results.sarif'

      - name: Upload Trivy results to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

### Week 12: Production Readiness Checklist

#### 12.1 Health Checks & Readiness Probes

**Files to Create**:
```
lotos_backend/src/main/java/com/lotus/lotusSPM/
└── health/
    ├── DatabaseHealthIndicator.java        [CREATE]
    ├── RedisHealthIndicator.java           [CREATE]
    └── CustomHealthCheck.java              [CREATE]
```

**DatabaseHealthIndicator.java**:
```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {

    @Autowired
    private DataSource dataSource;

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1)) {
                return Health.up()
                        .withDetail("database", "MySQL")
                        .withDetail("validationQuery", "SELECT 1")
                        .build();
            }
        } catch (SQLException e) {
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .build();
        }
        return Health.down().build();
    }
}
```

**application.yml** (Health endpoints):
```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /actuator
  endpoint:
    health:
      show-details: when-authorized
      probes:
        enabled: true
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
```

**Kubernetes Deployment** (Enhanced):
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: lotus-backend
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  template:
    spec:
      containers:
      - name: backend
        image: lotus-backend:latest
        ports:
        - containerPort: 8085

        # Resource limits
        resources:
          requests:
            memory: "512Mi"
            cpu: "500m"
          limits:
            memory: "1Gi"
            cpu: "1000m"

        # Liveness probe
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8085
          initialDelaySeconds: 60
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3

        # Readiness probe
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8085
          initialDelaySeconds: 30
          periodSeconds: 5
          timeoutSeconds: 3
          failureThreshold: 3

        # Environment variables from ConfigMap/Secret
        envFrom:
        - configMapRef:
            name: lotus-config
        - secretRef:
            name: lotus-secrets
```

---

## 📊 SUCCESS METRICS & VALIDATION

### Performance Targets
```
API Response Time (p95):     < 200ms ✅
API Response Time (p99):     < 500ms ✅
Database Query Time (p95):   < 100ms ✅
Cache Hit Ratio:             > 80%  ✅
Test Coverage:               > 95%  ✅
Bundle Size (Frontend):      < 200KB ✅
```

### Scalability Targets
```
Concurrent Users:            100,000+ ✅
Requests per Second:         10,000+  ✅
Database Connections:        50 max   ✅
Memory Usage:                < 1GB    ✅
CPU Usage:                   < 70%    ✅
```

### Security Targets
```
OWASP Top 10:               0 vulnerabilities ✅
Rate Limiting:              Implemented ✅
HTTPS:                      Enforced ✅
Security Headers:           All configured ✅
Dependency Vulnerabilities: 0 critical ✅
```

---

## 🛠️ IMPLEMENTATION SEQUENCE

### Priority Order (What to Build First)

**Week 1-2: CRITICAL PATH**
1. ✅ Testing framework (BaseIntegrationTest, BaseServiceTest)
2. ✅ Structured logging (Replace System.out.println)
3. ✅ Pagination (PageRequest, PageResponse)
4. ✅ Caching (@Cacheable annotations)

**Week 3-4: SECURITY**
5. ✅ Rate limiting (Bucket4j + Redis)
6. ✅ File upload validation
7. ✅ Security headers
8. ✅ CORS configuration

**Week 5-6: DATABASE**
9. ✅ Database indexes (V6 migration)
10. ✅ Query optimization
11. ✅ Connection pooltuning
12. ✅ Query performance logging

**Week 7-8: FRONTEND**
13. ✅ Environment config
14. ✅ Axios interceptors
15. ✅ PWA implementation
16. ✅ Dark mode

**Week 9-10: MONITORING**
17. ✅ Distributed tracing
18. ✅ Custom metrics
19. ✅ ELK stack

**Week 11-12: DEPLOYMENT**
20. ✅ Enhanced CI/CD
21. ✅ Health checks
22. ✅ Production deployment

---

## 📚 TECHNOLOGY STACK EVOLUTION

### Current → Target

```
Java:           8 → 17 LTS (Future sprint)
Spring Boot:    2.7.2 → 3.2.x (Future sprint)
Vue.js:         3.3.9 → 3.4.x (Current OK)
MySQL:          8.0 → 8.0 optimized ✅
Redis:          7.x → 7.x + Stack ✅
Kubernetes:     1.28+ → 1.30+ (Ongoing)
```

---

## 🎯 FINAL DELIVERABLES

By end of 12 weeks:

1. **✅ 95% Test Coverage** - Comprehensive unit, integration, E2E tests
2. **✅ Sub-200ms API Response** - Optimized queries, caching, indexing
3. **✅ Production-Ready Security** - Rate limiting, file validation, headers
4. **✅ Scalable Architecture** - Handles 100k+ users
5. **✅ Full Observability** - Logs, metrics, tracing, dashboards
6. **✅ Zero-Downtime Deployment** - Rolling updates, health checks
7. **✅ Enterprise Documentation** - API docs, runbooks, architecture

---

**This roadmap represents a SILICON VALLEY/NVIDIA-grade implementation approach:**
- Performance-first mindset
- Test-driven development
- Security by design
- Observable systems
- Scalable architecture
- Production excellence

**Next Step**: Begin Sprint 1 implementation with testing framework and pagination.

Ready to start coding! 🚀
