# Lotus SMS Plugin SDK Documentation

**Version:** 2.0.0
**Last Updated:** 2025-11-15
**Status:** Production-Ready

---

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Plugin Architecture](#plugin-architecture)
4. [Creating Your First Plugin](#creating-your-first-plugin)
5. [Plugin Lifecycle](#plugin-lifecycle)
6. [Hooks and Events](#hooks-and-events)
7. [Configuration Management](#configuration-management)
8. [Best Practices](#best-practices)
9. [Sample Plugins](#sample-plugins)
10. [API Reference](#api-reference)
11. [Publishing Your Plugin](#publishing-your-plugin)

---

## Introduction

The Lotus Student Management System (SMS) Plugin SDK allows developers to extend the platform's functionality through a robust plugin system. Plugins can add new features, integrate with external services, customize workflows, and enhance the user experience.

### What Can Plugins Do?

- **Integrate External Services**: Connect with Slack, Discord, email providers, CRM systems, etc.
- **Add Custom Features**: Implement institution-specific functionality
- **Automate Workflows**: Create automated responses to system events
- **Enhance Notifications**: Add custom notification channels and templates
- **Data Export/Import**: Integrate with external data sources
- **Analytics Extensions**: Add custom analytics and reporting

---

## Getting Started

### Prerequisites

- **Java 8+** installed
- **Maven** or **Gradle** for dependency management
- **Lotus SMS API Version**: 2.0.0+
- Basic knowledge of Spring Boot and Java

### Setup Development Environment

1. **Add Lotus SMS Plugin SDK Dependency**

```xml
<!-- Maven -->
<dependency>
    <groupId>com.lotus</groupId>
    <artifactId>lotus-sms-plugin-sdk</artifactId>
    <version>2.0.0</version>
</dependency>
```

```gradle
// Gradle
implementation 'com.lotus:lotus-sms-plugin-sdk:2.0.0'
```

2. **Import Required Classes**

```java
import com.lotus.lotusSPM.plugin.Plugin;
import com.lotus.lotusSPM.plugin.PluginMetadata;
```

---

## Plugin Architecture

### Core Components

1. **Plugin Interface** (`Plugin.java`)
   - Defines the contract all plugins must implement
   - Methods: `onEnable()`, `onDisable()`, `executeHook()`, `getMetadata()`, `getConfiguration()`, `setConfiguration()`

2. **PluginMetadata** (`PluginMetadata.java`)
   - Contains plugin information: ID, name, version, author, dependencies, permissions
   - Used for plugin discovery and validation

3. **PluginManager** (`PluginManager.java`)
   - Manages plugin lifecycle (load, enable, disable, unload)
   - Executes hooks across all registered plugins
   - Validates plugin compatibility

---

## Creating Your First Plugin

### Step 1: Implement the Plugin Interface

```java
package com.example.myplugin;

import com.lotus.lotusSPM.plugin.Plugin;
import com.lotus.lotusSPM.plugin.PluginMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class MyFirstPlugin implements Plugin {

    private static final Logger logger = LoggerFactory.getLogger(MyFirstPlugin.class);

    private PluginMetadata metadata;
    private Map<String, Object> configuration;
    private boolean enabled = false;

    public MyFirstPlugin() {
        // Initialize metadata
        metadata = new PluginMetadata();
        metadata.setId("my-first-plugin");
        metadata.setName("My First Plugin");
        metadata.setVersion("1.0.0");
        metadata.setDescription("A simple example plugin");
        metadata.setAuthor("Your Name");
        metadata.setApiVersion("2.0.0");
        metadata.setDependencies(Arrays.asList());
        metadata.setPermissions(Arrays.asList("read_student_data"));

        // Default configuration
        configuration = new HashMap<>();
        configuration.put("enabled", true);
    }

    @Override
    public PluginMetadata getMetadata() {
        return metadata;
    }

    @Override
    public void onEnable() {
        logger.info("Enabling My First Plugin");
        enabled = true;
        logger.info("My First Plugin enabled successfully!");
    }

    @Override
    public void onDisable() {
        logger.info("Disabling My First Plugin");
        enabled = false;
        logger.info("My First Plugin disabled");
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
            default:
                logger.debug("Hook not handled: {}", hookName);
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
        }
    }

    private void handleStudentRegistration(Map<String, Object> context) {
        String studentName = (String) context.get("studentName");
        logger.info("Welcome, {}!", studentName);
    }
}
```

### Step 2: Build Your Plugin JAR

```bash
mvn clean package
```

This creates a JAR file: `target/my-first-plugin-1.0.0.jar`

### Step 3: Deploy Your Plugin

1. Copy the JAR to: `/opt/lotus-sms/plugins/`
2. Restart Lotus SMS or reload plugins via Admin UI
3. Your plugin will be automatically discovered and loaded

---

## Plugin Lifecycle

### 1. Discovery Phase

PluginManager scans the plugins directory for JAR files

### 2. Validation Phase

- Checks plugin metadata (ID, version, API compatibility)
- Validates required permissions
- Verifies dependencies are met

### 3. Registration Phase

```java
pluginManager.registerPlugin(myPlugin);
```

### 4. Activation Phase

```java
pluginManager.enablePlugin("my-first-plugin");
// Calls: myPlugin.onEnable()
```

### 5. Running Phase

- Plugin receives hooks via `executeHook(hookName, context)`
- Plugin can be configured via `setConfiguration(config)`

### 6. Deactivation Phase

```java
pluginManager.disablePlugin("my-first-plugin");
// Calls: myPlugin.onDisable()
```

### 7. Cleanup Phase

```java
pluginManager.unregisterPlugin("my-first-plugin");
```

---

## Hooks and Events

### Available Hooks

| Hook Name | Triggered When | Context Parameters |
|-----------|----------------|-------------------|
| `student_registered` | New student signs up | `studentId`, `studentName`, `studentEmail`, `faculty`, `department` |
| `application_submitted` | Student submits internship application | `studentId`, `studentName`, `opportunityId`, `companyName`, `position` |
| `application_status_changed` | Application status updates | `applicationId`, `studentId`, `status`, `companyName` |
| `application_accepted` | Application is accepted | `applicationId`, `studentId`, `studentName`, `companyName` |
| `application_rejected` | Application is rejected | `applicationId`, `studentId`, `companyName` |
| `document_uploaded` | Student uploads document | `studentId`, `documentType`, `fileName` |
| `message_sent` | User sends message | `senderId`, `receiverId`, `messageContent` |
| `deadline_approaching` | Deadline is within threshold | `deadlineType`, `deadlineDate`, `daysRemaining` |
| `opportunity_created` | New job opportunity posted | `opportunityId`, `companyName`, `position`, `deadline` |
| `system_error` | System encounters error | `component`, `errorMessage`, `severity` |

### Executing Hooks in Your Plugin

```java
@Override
public void executeHook(String hookName, Map<String, Object> context) {
    if (!enabled) {
        return; // Don't process if plugin is disabled
    }

    switch (hookName) {
        case "student_registered":
            String studentEmail = (String) context.get("studentEmail");
            sendWelcomeEmail(studentEmail);
            break;

        case "application_accepted":
            String companyName = (String) context.get("companyName");
            notifyAdmins("Placement at " + companyName);
            break;

        default:
            // Ignore unknown hooks
            break;
    }
}
```

---

## Configuration Management

### Default Configuration

Set default configuration in your plugin constructor:

```java
configuration = new HashMap<>();
configuration.put("enabled", true);
configuration.put("apiKey", "");
configuration.put("webhookUrl", "");
configuration.put("retryAttempts", 3);
```

### Dynamic Configuration Updates

Admins can update plugin configuration via UI or API:

```java
@Override
public void setConfiguration(Map<String, Object> config) {
    if (config != null) {
        this.configuration.putAll(config);
        logger.info("Configuration updated");

        // Re-initialize with new config
        reinitialize();
    }
}
```

### Configuration Validation

```java
private void validateConfiguration() throws IllegalArgumentException {
    String apiKey = (String) configuration.get("apiKey");
    if (apiKey == null || apiKey.isEmpty()) {
        throw new IllegalArgumentException("API key is required");
    }
}
```

---

## Best Practices

### 1. Error Handling

Always wrap plugin logic in try-catch blocks:

```java
@Override
public void executeHook(String hookName, Map<String, Object> context) {
    try {
        // Your plugin logic
    } catch (Exception ex) {
        logger.error("Error in plugin hook: " + hookName, ex);
        // Don't throw exceptions - let the system continue
    }
}
```

### 2. Logging

Use SLF4J for logging:

```java
private static final Logger logger = LoggerFactory.getLogger(MyPlugin.class);

logger.info("Plugin enabled");
logger.debug("Processing hook: {}", hookName);
logger.error("Failed to process", exception);
```

### 3. Resource Management

Clean up resources in `onDisable()`:

```java
@Override
public void onDisable() {
    // Close database connections
    if (connection != null) {
        connection.close();
    }

    // Stop background threads
    if (executor != null) {
        executor.shutdown();
    }

    enabled = false;
}
```

### 4. Permissions

Request only necessary permissions:

```java
metadata.setPermissions(Arrays.asList(
    "read_student_data",
    "send_email",
    "external_api"
));
```

### 5. Versioning

Follow semantic versioning (MAJOR.MINOR.PATCH):

```java
metadata.setVersion("1.2.3");
// 1 = Major (breaking changes)
// 2 = Minor (new features, backward compatible)
// 3 = Patch (bug fixes)
```

---

## Sample Plugins

### 1. Email Notification Plugin

See: `lotos_backend/src/main/java/com/lotus/lotusSPM/plugin/samples/EmailNotificationPlugin.java`

**Features:**
- Sends welcome emails to new students
- Notifies students of application status changes
- Sends deadline reminders

### 2. Slack Integration Plugin

See: `lotos_backend/src/main/java/com/lotus/lotusSPM/plugin/samples/SlackIntegrationPlugin.java`

**Features:**
- Sends real-time notifications to Slack channels
- Mentions admins for urgent events
- Customizable channel mapping

---

## API Reference

### Plugin Interface

```java
public interface Plugin {
    PluginMetadata getMetadata();
    void onEnable();
    void onDisable();
    void executeHook(String hookName, Map<String, Object> context);
    Map<String, Object> getConfiguration();
    void setConfiguration(Map<String, Object> config);
}
```

### PluginMetadata

```java
public class PluginMetadata {
    private String id;               // Unique plugin ID (required)
    private String name;             // Display name (required)
    private String version;          // Semantic version (required)
    private String description;      // Plugin description
    private String author;           // Author name
    private String apiVersion;       // Required Lotus API version
    private List<String> dependencies; // Other plugins this depends on
    private List<String> permissions;  // Required permissions
}
```

### PluginManager

```java
// Register plugin
pluginManager.registerPlugin(myPlugin);

// Enable plugin
pluginManager.enablePlugin("my-plugin-id");

// Disable plugin
pluginManager.disablePlugin("my-plugin-id");

// Unregister plugin
pluginManager.unregisterPlugin("my-plugin-id");

// Get all plugins
List<PluginMetadata> plugins = pluginManager.getAllPlugins();

// Execute hook across all plugins
pluginManager.executeHook("student_registered", contextData);
```

---

## Publishing Your Plugin

### 1. Package Your Plugin

```bash
mvn clean package
```

### 2. Test Your Plugin

```bash
# Copy to plugins directory
cp target/my-plugin-1.0.0.jar /opt/lotus-sms/plugins/

# Restart Lotus SMS
systemctl restart lotus-sms

# Check logs
tail -f /var/log/lotus-sms/application.log
```

### 3. Document Your Plugin

Create a README with:
- Plugin description
- Installation instructions
- Configuration options
- Supported hooks
- Troubleshooting guide

### 4. Submit to Plugin Marketplace

1. Create account on [plugins.lotus-sms.com](https://plugins.lotus-sms.com)
2. Upload JAR and documentation
3. Submit for review
4. Publish to marketplace

---

## Security Considerations

### 1. Permission Model

Plugins must declare permissions:

```java
metadata.setPermissions(Arrays.asList(
    "read_student_data",     // Read student information
    "write_student_data",    // Modify student information
    "send_email",            // Send emails
    "external_api",          // Make external API calls
    "database_access",       // Direct database access
    "admin_functions"        // Admin-level operations
));
```

### 2. Sandboxing

Plugins run in a controlled environment with limited access.

### 3. Code Review

All marketplace plugins undergo security review before publication.

---

## Support and Resources

- **Documentation**: https://docs.lotus-sms.com/plugins
- **Community Forum**: https://community.lotus-sms.com
- **GitHub**: https://github.com/lotus-sms/plugin-sdk
- **Email Support**: plugins@lotus-sms.com

---

## Changelog

### Version 2.0.0 (2025-11-15)
- Initial plugin SDK release
- Support for 10+ system hooks
- Plugin marketplace integration
- Sample plugins included

---

**Happy Plugin Development! 🚀**
