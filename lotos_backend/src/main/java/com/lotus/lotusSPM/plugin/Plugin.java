package com.lotus.lotusSPM.plugin;

import java.util.Map;

/**
 * Base interface for all Lotus SMS plugins
 */
public interface Plugin {

    /**
     * Get plugin metadata
     */
    PluginMetadata getMetadata();

    /**
     * Called when plugin is enabled
     */
    void onEnable();

    /**
     * Called when plugin is disabled
     */
    void onDisable();

    /**
     * Execute plugin hook
     */
    void executeHook(String hookName, Map<String, Object> context);

    /**
     * Get plugin configuration
     */
    Map<String, Object> getConfiguration();

    /**
     * Set plugin configuration
     */
    void setConfiguration(Map<String, Object> config);
}
