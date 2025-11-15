package com.lotus.lotusSPM.plugin.samples;

import com.lotus.lotusSPM.plugin.Plugin;
import com.lotus.lotusSPM.plugin.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Sample Plugin: Slack Integration
 * Demonstrates external API integration through plugin system
 *
 * This plugin sends notifications to Slack channels for important events
 */
public class SlackIntegrationPlugin implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(SlackIntegrationPlugin.class);

    private PluginMetadata metadata;
    private Map<String, Object> configuration;
    private boolean enabled = false;

    public SlackIntegrationPlugin() {
        // Initialize metadata
        metadata = new PluginMetadata();
        metadata.setId("slack-integration-plugin");
        metadata.setName("Slack Integration");
        metadata.setVersion("1.0.0");
        metadata.setDescription("Send real-time notifications to Slack channels for team collaboration");
        metadata.setAuthor("Lotus SMS Community");
        metadata.setApiVersion("2.0.0");
        metadata.setDependencies(Arrays.asList("slack-api-client"));
        metadata.setPermissions(Arrays.asList("external_api", "read_student_data", "read_application_data"));

        // Default configuration
        configuration = new HashMap<>();
        configuration.put("webhookUrl", "");
        configuration.put("channelMapping", new HashMap<String, String>() {{
            put("student_events", "#student-events");
            put("applications", "#internship-applications");
            put("system_alerts", "#lotus-alerts");
        }});
        configuration.put("mentionAdmins", true);
        configuration.put("enabledEvents", Arrays.asList(
                "application_submitted",
                "application_accepted",
                "student_registered",
                "deadline_approaching"
        ));
    }

    @Override
    public PluginMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void onEnable() {
        logger.info("Enabling Slack Integration Plugin");

        // Validate webhook URL
        String webhookUrl = (String) configuration.get("webhookUrl");
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.warn("Slack webhook URL not configured. Plugin will run in dry-run mode.");
        }

        enabled = true;
        sendSlackMessage("#lotus-alerts", "✅ Lotus SMS Slack Integration is now active!");

        logger.info("Slack Integration Plugin enabled successfully");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling Slack Integration Plugin");

        sendSlackMessage("#lotus-alerts", "⏸️ Lotus SMS Slack Integration is now disabled");

        enabled = false;
        logger.info("Slack Integration Plugin disabled");
    }

    @Override
    public void executeHook(String hookName, Map<String, Object> context) {
        if (!enabled) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<String> enabledEvents = (List<String>) configuration.get("enabledEvents");
        if (enabledEvents != null && !enabledEvents.contains(hookName)) {
            return;
        }

        switch (hookName) {
            case "student_registered":
                handleStudentRegistration(context);
                break;
            case "application_submitted":
                handleApplicationSubmitted(context);
                break;
            case "application_accepted":
                handleApplicationAccepted(context);
                break;
            case "application_rejected":
                handleApplicationRejected(context);
                break;
            case "deadline_approaching":
                handleDeadlineReminder(context);
                break;
            case "system_error":
                handleSystemError(context);
                break;
            default:
                logger.debug("Hook not handled by Slack Integration Plugin: {}", hookName);
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
            logger.info("Slack Integration Plugin configuration updated");
        }
    }

    private void handleStudentRegistration(Map<String, Object> context) {
        String studentName = (String) context.get("studentName");
        String studentEmail = (String) context.get("studentEmail");
        String faculty = (String) context.get("faculty");

        String message = String.format(
                "🎓 *New Student Registered*\n" +
                        "Name: %s\n" +
                        "Email: %s\n" +
                        "Faculty: %s",
                studentName, studentEmail, faculty
        );

        sendSlackMessage("#student-events", message);
    }

    private void handleApplicationSubmitted(Map<String, Object> context) {
        String studentName = (String) context.get("studentName");
        String companyName = (String) context.get("companyName");
        String position = (String) context.get("position");

        String message = String.format(
                "📝 *New Application Submitted*\n" +
                        "Student: %s\n" +
                        "Company: %s\n" +
                        "Position: %s",
                studentName, companyName, position
        );

        sendSlackMessage("#internship-applications", message);
    }

    private void handleApplicationAccepted(Map<String, Object> context) {
        String studentName = (String) context.get("studentName");
        String companyName = (String) context.get("companyName");

        String message = String.format(
                "🎉 *Application Accepted!*\n" +
                        "Congratulations to %s on getting accepted at %s!",
                studentName, companyName
        );

        sendSlackMessage("#internship-applications", message);
    }

    private void handleApplicationRejected(Map<String, Object> context) {
        String studentName = (String) context.get("studentName");
        String companyName = (String) context.get("companyName");

        String message = String.format(
                "❌ *Application Update*\n" +
                        "Student: %s\n" +
                        "Company: %s\n" +
                        "Status: Not selected",
                studentName, companyName
        );

        sendSlackMessage("#internship-applications", message);
    }

    private void handleDeadlineReminder(Map<String, Object> context) {
        String deadlineType = (String) context.get("deadlineType");
        String deadlineDate = (String) context.get("deadlineDate");
        Integer daysRemaining = (Integer) context.get("daysRemaining");

        String message = String.format(
                "⏰ *Deadline Approaching*\n" +
                        "Type: %s\n" +
                        "Date: %s\n" +
                        "Days Remaining: %d",
                deadlineType, deadlineDate, daysRemaining
        );

        if (daysRemaining != null && daysRemaining <= 3) {
            message += "\n<!channel> Urgent attention needed!";
        }

        sendSlackMessage("#lotus-alerts", message);
    }

    private void handleSystemError(Map<String, Object> context) {
        String errorMessage = (String) context.get("errorMessage");
        String component = (String) context.get("component");

        String message = String.format(
                "🚨 *System Error Detected*\n" +
                        "Component: %s\n" +
                        "Error: %s\n" +
                        "<!here> Admin attention required",
                component, errorMessage
        );

        sendSlackMessage("#lotus-alerts", message);
    }

    private void sendSlackMessage(String channel, String message) {
        String webhookUrl = (String) configuration.get("webhookUrl");

        if (webhookUrl == null || webhookUrl.isEmpty()) {
            logger.debug("[DRY-RUN] Slack message to {}: {}", channel, message);
            return;
        }

        try {
            // In production, use Slack Web API or Webhook
            // Example with webhook:
            // String payload = String.format("{\"channel\":\"%s\",\"text\":\"%s\"}", channel, message);
            // HttpClient.post(webhookUrl, payload);

            logger.info("Slack message sent to {}: {}", channel, message);
        } catch (Exception ex) {
            logger.error("Failed to send Slack message", ex);
        }
    }
}
