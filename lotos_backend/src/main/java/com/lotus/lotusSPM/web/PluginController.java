package com.lotus.lotusSPM.web;

import com.lotus.lotusSPM.plugin.PluginManager;
import com.lotus.lotusSPM.plugin.PluginMetadata;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plugins")
@Tag(name = "Plugin Management", description = "Plugin system for extending Lotus SMS functionality")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class PluginController {

    @Autowired
    private PluginManager pluginManager;

    @GetMapping
    @Operation(summary = "List all plugins", description = "Get list of all installed plugins")
    public ResponseEntity<List<PluginMetadata>> listPlugins() {
        try {
            List<PluginMetadata> plugins = pluginManager.getAllPlugins();
            return ResponseEntity.ok(plugins);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/{pluginId}/enable")
    @Operation(summary = "Enable plugin", description = "Enable a specific plugin")
    public ResponseEntity<?> enablePlugin(@PathVariable String pluginId) {
        try {
            pluginManager.enablePlugin(pluginId);
            return ResponseEntity.ok("Plugin enabled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error enabling plugin: " + e.getMessage());
        }
    }

    @PostMapping("/{pluginId}/disable")
    @Operation(summary = "Disable plugin", description = "Disable a specific plugin")
    public ResponseEntity<?> disablePlugin(@PathVariable String pluginId) {
        try {
            pluginManager.disablePlugin(pluginId);
            return ResponseEntity.ok("Plugin disabled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error disabling plugin: " + e.getMessage());
        }
    }

    @DeleteMapping("/{pluginId}")
    @Operation(summary = "Uninstall plugin", description = "Completely remove a plugin")
    public ResponseEntity<?> uninstallPlugin(@PathVariable String pluginId) {
        try {
            pluginManager.unregisterPlugin(pluginId);
            return ResponseEntity.ok("Plugin uninstalled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error uninstalling plugin: " + e.getMessage());
        }
    }
}
