package com.lotus.lotusSPM.scheduler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Quartz Scheduler Configuration for Background Jobs.
 *
 * Enterprise Pattern: Job Scheduling / Cron Jobs
 *
 * Use Cases:
 * - Periodic data cleanup
 * - Report generation
 * - Email digests
 * - Data synchronization
 * - Cache warming
 * - Metrics aggregation
 * - Backup operations
 * - Notification batching
 *
 * Features:
 * - Persistent job store (database)
 * - Clustered scheduling (no duplicate jobs)
 * - Misfire handling
 * - Job dependencies
 * - Priority scheduling
 * - Calendar-based scheduling
 */
@Configuration
@EnableScheduling
public class SchedulerConfig {

    /**
     * Configure Quartz Scheduler with database persistence.
     */
    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(DataSource dataSource) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setQuartzProperties(quartzProperties());
        schedulerFactory.setApplicationContextSchedulerContextKey("applicationContext");
        schedulerFactory.setWaitForJobsToCompleteOnShutdown(true);
        schedulerFactory.setOverwriteExistingJobs(true);

        return schedulerFactory;
    }

    /**
     * Quartz properties for clustering and persistence.
     */
    private Properties quartzProperties() {
        Properties properties = new Properties();

        // Scheduler properties
        properties.setProperty("org.quartz.scheduler.instanceName", "LotusScheduler");
        properties.setProperty("org.quartz.scheduler.instanceId", "AUTO");

        // Thread pool properties
        properties.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        properties.setProperty("org.quartz.threadPool.threadCount", "10");
        properties.setProperty("org.quartz.threadPool.threadPriority", "5");

        // Job store properties (database persistence)
        properties.setProperty("org.quartz.jobStore.class", "org.quartz.impl.jdbcjobstore.JobStoreTX");
        properties.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.StdJDBCDelegate");
        properties.setProperty("org.quartz.jobStore.useProperties", "false");
        properties.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        properties.setProperty("org.quartz.jobStore.tablePrefix", "QRTZ_");

        // Clustering properties
        properties.setProperty("org.quartz.jobStore.isClustered", "true");
        properties.setProperty("org.quartz.jobStore.clusterCheckinInterval", "20000");

        return properties;
    }
}
