package com.lotus.lotusSPM.health;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for DatabaseHealthIndicator
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DatabaseHealthIndicator Tests")
class DatabaseHealthIndicatorTest {

    @Mock
    private HikariDataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    @Mock
    private HikariPoolMXBean poolMXBean;

    private DatabaseHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new DatabaseHealthIndicator(dataSource);
    }

    @Test
    @DisplayName("Should report UP when database is healthy with HikariCP")
    void testHealthyDatabaseWithHikari() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT VERSION()")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("8.0.33-MySQL");

        // HikariCP configuration
        when(dataSource.getClass().getName()).thenReturn("com.zaxxer.hikari.HikariDataSource");
        when(dataSource.getHikariPoolMXBean()).thenReturn(poolMXBean);
        when(dataSource.getPoolName()).thenReturn("HikariPool-1");
        when(dataSource.getMaximumPoolSize()).thenReturn(20);
        when(poolMXBean.getActiveConnections()).thenReturn(5);
        when(poolMXBean.getIdleConnections()).thenReturn(10);
        when(poolMXBean.getTotalConnections()).thenReturn(15);
        when(poolMXBean.getThreadsAwaitingConnection()).thenReturn(0);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("status", "Database is reachable");
        assertThat(health.getDetails()).containsEntry("database", "MySQL");
        assertThat(health.getDetails()).containsEntry("version", "8.0.33-MySQL");
        assertThat(health.getDetails()).containsEntry("pool_name", "HikariPool-1");
        assertThat(health.getDetails()).containsEntry("active_connections", 5);
        assertThat(health.getDetails()).containsEntry("idle_connections", 10);
        assertThat(health.getDetails()).containsEntry("total_connections", 15);
        assertThat(health.getDetails()).containsEntry("max_pool_size", 20);

        verify(connection, times(1)).close();
    }

    @Test
    @DisplayName("Should report UP when database is healthy without HikariCP details")
    void testHealthyDatabaseWithoutHikari() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery("SELECT VERSION()")).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("8.0.33-MySQL");
        when(dataSource.getClass().getName()).thenReturn("org.apache.tomcat.jdbc.pool.DataSource");

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("database", "MySQL");
        assertThat(health.getDetails()).containsKey("pool_info");
    }

    @Test
    @DisplayName("Should report DOWN when connection validation fails")
    void testConnectionValidationFails() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(false);

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "Database connection validation failed");
    }

    @Test
    @DisplayName("Should report DOWN when connection fails")
    void testConnectionFails() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails()).containsEntry("status", "Database connection failed");
        assertThat(health.getDetails()).containsKey("error");
    }

    @Test
    @DisplayName("Should handle pool metrics unavailable")
    void testPoolMetricsUnavailable() throws SQLException {
        // Given
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(2)).thenReturn(true);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getString(1)).thenReturn("8.0.33-MySQL");
        when(dataSource.getClass().getName()).thenReturn("com.zaxxer.hikari.HikariDataSource");
        when(dataSource.getHikariPoolMXBean()).thenThrow(new RuntimeException("Pool not initialized"));

        // When
        Health health = healthIndicator.health();

        // Then
        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("pool_info", "unavailable");
    }
}
