package com.lotus.lotusSPM.plugin.samples;

import com.lotus.lotusSPM.plugin.Plugin;
import com.lotus.lotusSPM.plugin.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Sample Plugin: Email Notification Enhancement
 * Demonstrates plugin system capabilities
 *
 * This plugin adds enhanced email notifications for various student events
 */
public class EmailNotificationPlugin implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationPlugin.class);

    private PluginMetadata metadata;
    private Map<String, Object> configuration;
    private boolean enabled = false;

    public EmailNotificationPlugin() {
        // Initialize metadata
        metadata = new PluginMetadata();
        metadata.setId("email-notification-plugin");
        metadata.setName("Enhanced Email Notifications");
        metadata.setVersion("1.0.0");
        metadata.setDescription("Adds advanced email notification capabilities with templates and scheduling");
        metadata.setAuthor("Lotus SMS Team");
        metadata.setApiVersion("2.0.0");
        metadata.setDependencies(Arrays.asList());
        metadata.setPermissions(Arrays.asList("send_email", "read_student_data"));

        // Default configuration
        configuration = new HashMap<>();
        configuration.put("smtpHost", "smtp.gmail.com");
        configuration.put("smtpPort", 587);
        configuration.put("enableTLS", true);
        configuration.put("fromEmail", "noreply@lotus-sms.com");
        configuration.put("enableScheduling", true);
    }

    @Override
    public PluginMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void onEnable() {
        logger.info("Enabling Email Notification Plugin");
        enabled = true;

        // Initialize email templates
        initializeTemplates();

        logger.info("Email Notification Plugin enabled successfully");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling Email Notification Plugin");
        enabled = false;
        logger.info("Email Notification Plugin disabled");
    }

    @Override
    public void executeHook(String hookName, Map<String, Object> context) {
        if (!enabled) {
            return;
        }

        switch (hookName) {
            case "student_registered":
                handleStudentRegistration(context);
                break;
            case "application_submitted":
                handleApplicationSubmitted(context);
                break;
            case "application_status_changed":
                handleApplicationStatusChanged(context);
                break;
            case "deadline_approaching":
                handleDeadlineReminder(context);
                break;
            default:
                logger.debug("Hook not handled by Email Notification Plugin: {}", hookName);
        }
    }

    @Override
    public Map<String, Object> getConfiguration() {
        return new HashMap<>(configuration);
    }

    @Override
    public void setConfiguration(Map<String, Object> config) {
        if (config != null) {
            this.configuration.putAll(config);
            logger.info("Email Notification Plugin configuration updated");
        }
    }

    private void initializeTemplates() {
        logger.info("Initializing email templates");
        // In production, load templates from files or database
    }

    private void handleStudentRegistration(Map<String, Object> context) {
        String studentEmail = (String) context.get("studentEmail");
        String studentName = (String) context.get("studentName");

        logger.info("Sending welcome email to: {} <{}>", studentName, studentEmail);

        // In production: Send actual email using JavaMail or SendGrid
        String emailContent = buildWelcomeEmail(studentName);

        logger.debug("Email content: {}", emailContent);
    }

    private void handleApplicationSubmitted(Map<String, Object> context) {
        String studentEmail = (String) context.get("studentEmail");
        String companyName = (String) context.get("companyName");

        logger.info("Sending application confirmation to: {} for company: {}", studentEmail, companyName);

        // In production: Send actual email
    }

    private void handleApplicationStatusChanged(Map<String, Object> context) {
        String studentEmail = (String) context.get("studentEmail");
        String newStatus = (String) context.get("status");
        String companyName = (String) context.get("companyName");

        logger.info("Sending status update email to: {} - Status: {} - Company: {}",
                studentEmail, newStatus, companyName);

        // In production: Send actual email based on status
    }

    private void handleDeadlineReminder(Map<String, Object> context) {
        String studentEmail = (String) context.get("studentEmail");
        String deadlineType = (String) context.get("deadlineType");
        String deadlineDate = (String) context.get("deadlineDate");

        logger.info("Sending deadline reminder to: {} - Type: {} - Date: {}",
                studentEmail, deadlineType, deadlineDate);

        // In production: Send actual reminder email
    }

    private String buildWelcomeEmail(String studentName) {
        return String.format(
                "Dear %s,\n\n" +
                        "Welcome to Lotus Student Management System!\n\n" +
                        "We're excited to have you on board. You can now:\n" +
                        "- Browse internship opportunities\n" +
                        "- Apply for positions\n" +
                        "- Track your applications\n" +
                        "- Upload documents\n\n" +
                        "Best regards,\n" +
                        "Lotus SMS Team",
                studentName
        );
    }
}
