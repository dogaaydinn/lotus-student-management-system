package com.lotus.lotusSPM.health;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * Custom Health Indicator for Disk Space
 * Monitors available disk space for file storage operations
 */
@Component("diskSpace")
public class DiskSpaceHealthIndicator implements HealthIndicator {

    private static final long THRESHOLD_BYTES = 1024 * 1024 * 1024; // 1GB
    private final String path;

    public DiskSpaceHealthIndicator() {
        this.path = System.getProperty("user.dir");
    }

    @Override
    public Health health() {
        try {
            File file = new File(path);
            long freeSpace = file.getFreeSpace();
            long totalSpace = file.getTotalSpace();
            long usableSpace = file.getUsableSpace();

            double usagePercent = ((double) (totalSpace - freeSpace) / totalSpace) * 100;

            Health.Builder builder;
            if (freeSpace < THRESHOLD_BYTES) {
                builder = Health.down()
                        .withDetail("status", "Low disk space");
            } else {
                builder = Health.up()
                        .withDetail("status", "Sufficient disk space");
            }

            return builder
                    .withDetail("path", path)
                    .withDetail("free", formatBytes(freeSpace))
                    .withDetail("total", formatBytes(totalSpace))
                    .withDetail("usable", formatBytes(usableSpace))
                    .withDetail("usage_percent", String.format("%.2f%%", usagePercent))
                    .withDetail("threshold", formatBytes(THRESHOLD_BYTES))
                    .build();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("status", "Unable to check disk space")
                    .withDetail("error", e.getMessage())
                    .build();
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else if (bytes < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", bytes / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
        }
    }
}
