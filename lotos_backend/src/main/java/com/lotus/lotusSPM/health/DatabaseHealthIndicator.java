package com.lotus.lotusSPM.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Custom Health Indicator for Database
 * Provides detailed health status including connection pool metrics
 */
@Component("database")
public class DatabaseHealthIndicator implements HealthIndicator {

    private final DataSource dataSource;

    public DatabaseHealthIndicator(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {

            // Test database connection
            if (connection.isValid(2)) {
                Health.Builder builder = Health.up();

                // Get database version
                try (Statement statement = connection.createStatement();
                     ResultSet resultSet = statement.executeQuery("SELECT VERSION()")) {
                    if (resultSet.next()) {
                        String version = resultSet.getString(1);
                        builder.withDetail("database", "MySQL");
                        builder.withDetail("version", version);
                    }
                }

                // Get connection pool stats if using HikariCP
                if (dataSource.getClass().getName().contains("HikariDataSource")) {
                    try {
                        com.zaxxer.hikari.HikariDataSource hikariDS = (com.zaxxer.hikari.HikariDataSource) dataSource;
                        com.zaxxer.hikari.HikariPoolMXBean poolMXBean = hikariDS.getHikariPoolMXBean();

                        builder.withDetail("pool_name", hikariDS.getPoolName());
                        builder.withDetail("active_connections", poolMXBean.getActiveConnections());
                        builder.withDetail("idle_connections", poolMXBean.getIdleConnections());
                        builder.withDetail("total_connections", poolMXBean.getTotalConnections());
                        builder.withDetail("threads_awaiting_connection", poolMXBean.getThreadsAwaitingConnection());
                        builder.withDetail("max_pool_size", hikariDS.getMaximumPoolSize());
                    } catch (Exception e) {
                        // HikariCP details not available
                        builder.withDetail("pool_info", "unavailable");
                    }
                }

                builder.withDetail("status", "Database is reachable");
                return builder.build();
            } else {
                return Health.down()
                        .withDetail("status", "Database connection validation failed")
                        .build();
            }
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Database connection failed")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }
}
