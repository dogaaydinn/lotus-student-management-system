package com.lotus.lotusSPM.plugin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Plugin Manager for Phase 5: Ecosystem Expansion
 * Enables third-party plugin development and integration
 */
@Service
public class PluginManager {

    private static final Logger logger = LoggerFactory.getLogger(PluginManager.class);

    private final Map<String, Plugin> plugins = new ConcurrentHashMap<>();
    private final Map<String, PluginMetadata> metadata = new ConcurrentHashMap<>();

    @PostConstruct
    public void initialize() {
        logger.info("Initializing Plugin Manager");
        loadPlugins();
    }

    /**
     * Register a new plugin
     */
    public void registerPlugin(Plugin plugin) {
        PluginMetadata meta = plugin.getMetadata();
        String pluginId = meta.getId();

        if (plugins.containsKey(pluginId)) {
            logger.warn("Plugin {} already registered, updating", pluginId);
        }

        // Validate plugin
        if (validatePlugin(plugin)) {
            plugins.put(pluginId, plugin);
            metadata.put(pluginId, meta);
            logger.info("Plugin {} v{} registered successfully", meta.getName(), meta.getVersion());
        } else {
            logger.error("Plugin {} failed validation", pluginId);
        }
    }

    /**
     * Unregister a plugin
     */
    public void unregisterPlugin(String pluginId) {
        Plugin plugin = plugins.get(pluginId);
        if (plugin != null) {
            try {
                plugin.onDisable();
                plugins.remove(pluginId);
                metadata.remove(pluginId);
                logger.info("Plugin {} unregistered", pluginId);
            } catch (Exception e) {
                logger.error("Error unregistering plugin {}", pluginId, e);
            }
        }
    }

    /**
     * Enable a plugin
     */
    public void enablePlugin(String pluginId) {
        Plugin plugin = plugins.get(pluginId);
        if (plugin != null) {
            try {
                plugin.onEnable();
                logger.info("Plugin {} enabled", pluginId);
            } catch (Exception e) {
                logger.error("Error enabling plugin {}", pluginId, e);
            }
        }
    }

    /**
     * Disable a plugin
     */
    public void disablePlugin(String pluginId) {
        Plugin plugin = plugins.get(pluginId);
        if (plugin != null) {
            try {
                plugin.onDisable();
                logger.info("Plugin {} disabled", pluginId);
            } catch (Exception e) {
                logger.error("Error disabling plugin {}", pluginId, e);
            }
        }
    }

    /**
     * Get all registered plugins
     */
    public List<PluginMetadata> getAllPlugins() {
        return new ArrayList<>(metadata.values());
    }

    /**
     * Get plugin by ID
     */
    public Plugin getPlugin(String pluginId) {
        return plugins.get(pluginId);
    }

    /**
     * Execute plugin hook
     */
    public void executeHook(String hookName, Map<String, Object> context) {
        for (Plugin plugin : plugins.values()) {
            try {
                plugin.executeHook(hookName, context);
            } catch (Exception e) {
                logger.error("Error executing hook {} in plugin {}", hookName, plugin.getMetadata().getId(), e);
            }
        }
    }

    private void loadPlugins() {
        // In production, scan plugins directory and load JAR files
        logger.info("Plugin loading completed. {} plugins loaded", plugins.size());
    }

    private boolean validatePlugin(Plugin plugin) {
        PluginMetadata meta = plugin.getMetadata();

        // Check required fields
        if (meta.getId() == null || meta.getName() == null || meta.getVersion() == null) {
            logger.error("Plugin missing required metadata");
            return false;
        }

        // Check API compatibility
        String apiVersion = meta.getApiVersion();
        if (!isCompatibleApiVersion(apiVersion)) {
            logger.error("Plugin {} requires API version {} but current version is 2.0.0",
                meta.getName(), apiVersion);
            return false;
        }

        return true;
    }

    private boolean isCompatibleApiVersion(String requiredVersion) {
        // Simple version check - in production, use semantic versioning
        return requiredVersion != null && requiredVersion.startsWith("2.");
    }
}
