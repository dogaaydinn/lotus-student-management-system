package com.lotus.lotusSPM.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Base class for integration tests with TestContainers support.
 * Provides MySQL and Redis containers for isolated testing.
 *
 * Usage: Extend this class in your integration tests
 *
 * @author Lotus SMS Team
 * @version 1.0
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {

    @Container
    protected static MySQLContainer<?> mysqlContainer = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("lotus_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @Container
    protected static GenericContainer<?> redisContainer = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379)
            .withReuse(true);

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // MySQL configuration
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "com.mysql.cj.jdbc.Driver");

        // Redis configuration
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));

        // Flyway configuration
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.clean-disabled", () -> "false");

        // JPA configuration
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add("spring.jpa.show-sql", () -> "false");
    }

    @BeforeEach
    public void setUp() {
        cleanDatabase();
    }

    /**
     * Clean database before each test to ensure isolation
     */
    protected void cleanDatabase() {
        // Disable foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");

        // Get all tables
        String query = "SELECT table_name FROM information_schema.tables WHERE table_schema = 'lotus_test'";
        jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("table_name"))
                .forEach(tableName -> {
                    if (!tableName.equals("flyway_schema_history")) {
                        jdbcTemplate.execute("TRUNCATE TABLE " + tableName);
                    }
                });

        // Re-enable foreign key checks
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    /**
     * Helper method to get JWT token for testing authenticated endpoints
     *
     * @param username Username
     * @param role User role
     * @return JWT token
     */
    protected String getAuthToken(String username, String role) {
        // Implementation would call auth service to generate token
        // For testing purposes, you can use a test token or mock the auth
        return "Bearer test-jwt-token";
    }

    /**
     * Helper to convert object to JSON string
     */
    protected String toJson(Object object) throws Exception {
        return objectMapper.writeValueAsString(object);
    }

    /**
     * Helper to convert JSON string to object
     */
    protected <T> T fromJson(String json, Class<T> clazz) throws Exception {
        return objectMapper.readValue(json, clazz);
    }
}
