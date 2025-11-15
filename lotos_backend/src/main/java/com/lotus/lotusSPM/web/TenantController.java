package com.lotus.lotusSPM.web;

import com.lotus.lotusSPM.multitenant.Tenant;
import com.lotus.lotusSPM.service.base.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for Multi-Tenant Management
 * Provides endpoints for tenant administration and provisioning
 */
@RestController
@RequestMapping("/api/v1/tenants")
@Slf4j
public class TenantController {

    @Autowired
    private TenantService tenantService;

    /**
     * Get all tenants with pagination
     * @param page Page number (0-indexed, default: 0)
     * @param size Page size (default: 20, max: 100)
     * @param sortBy Field to sort by (default: id)
     * @param sortDir Sort direction (asc/desc, default: asc)
     * @return Paginated list of tenants
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<Tenant>> getAllTenants(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        try {
            // Limit max page size to prevent abuse
            size = Math.min(size, 100);

            Sort sort = sortDir.equalsIgnoreCase("desc")
                    ? Sort.by(sortBy).descending()
                    : Sort.by(sortBy).ascending();

            Pageable pageable = PageRequest.of(page, size, sort);

            log.info("Fetching tenants - page: {}, size: {}, sortBy: {}, sortDir: {}",
                    page, size, sortBy, sortDir);

            Page<Tenant> tenants = tenantService.findAll(pageable);

            return ResponseEntity.ok(tenants);
        } catch (Exception ex) {
            log.error("Error fetching tenants", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get tenant by ID
     * @param id Tenant ID
     * @return Tenant
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> getTenantById(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.findById(id);
            return ResponseEntity.ok(tenant);
        } catch (Exception ex) {
            log.error("Tenant not found with id: {}", id, ex);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get tenant by subdomain
     * @param subdomain Subdomain
     * @return Tenant
     */
    @GetMapping("/subdomain/{subdomain}")
    public ResponseEntity<Object> getTenantBySubdomain(@PathVariable String subdomain) {
        try {
            Tenant tenant = tenantService.findBySubdomain(subdomain);
            return ResponseEntity.ok(tenant);
        } catch (Exception ex) {
            log.error("Tenant not found with subdomain: {}", subdomain, ex);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Get tenants by status
     * @param status Status filter
     * @return List of tenants
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getTenantsByStatus(@PathVariable String status) {
        try {
            List<Tenant> tenants = tenantService.findByStatus(status);
            return ResponseEntity.ok(tenants);
        } catch (Exception ex) {
            log.error("Error fetching tenants by status: {}", status, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get tenants by plan
     * @param plan Plan filter
     * @return List of tenants
     */
    @GetMapping("/plan/{plan}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getTenantsByPlan(@PathVariable String plan) {
        try {
            List<Tenant> tenants = tenantService.findByPlan(plan);
            return ResponseEntity.ok(tenants);
        } catch (Exception ex) {
            log.error("Error fetching tenants by plan: {}", plan, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new tenant
     * @param tenant Tenant to create
     * @return Created tenant
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> createTenant(@Valid @RequestBody Tenant tenant) {
        try {
            Tenant createdTenant = tenantService.createTenant(tenant);
            log.info("Tenant created: {}", createdTenant.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(createdTenant);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid tenant data: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error creating tenant", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to create tenant"));
        }
    }

    /**
     * Provision a new tenant (full setup)
     * @param tenant Tenant to provision
     * @return Provisioned tenant
     */
    @PostMapping("/provision")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> provisionTenant(@Valid @RequestBody Tenant tenant) {
        try {
            Tenant provisionedTenant = tenantService.provisionTenant(tenant);
            log.info("Tenant provisioned: {}", provisionedTenant.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(provisionedTenant);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid tenant data: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error provisioning tenant", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to provision tenant"));
        }
    }

    /**
     * Update a tenant
     * @param id Tenant ID
     * @param tenant Updated tenant data
     * @return Updated tenant
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> updateTenant(@PathVariable Long id, @RequestBody Tenant tenant) {
        try {
            Tenant updatedTenant = tenantService.updateTenant(id, tenant);
            return ResponseEntity.ok(updatedTenant);
        } catch (Exception ex) {
            log.error("Error updating tenant: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to update tenant"));
        }
    }

    /**
     * Delete a tenant
     * @param id Tenant ID
     * @return No content
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteTenant(@PathVariable Long id) {
        try {
            tenantService.deleteTenant(id);
            log.info("Tenant deleted: {}", id);
            return ResponseEntity.noContent().build();
        } catch (Exception ex) {
            log.error("Error deleting tenant: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Suspend a tenant
     * @param id Tenant ID
     * @return Suspended tenant
     */
    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> suspendTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.suspendTenant(id);
            return ResponseEntity.ok(tenant);
        } catch (Exception ex) {
            log.error("Error suspending tenant: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to suspend tenant"));
        }
    }

    /**
     * Activate a tenant
     * @param id Tenant ID
     * @return Activated tenant
     */
    @PutMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> activateTenant(@PathVariable Long id) {
        try {
            Tenant tenant = tenantService.activateTenant(id);
            return ResponseEntity.ok(tenant);
        } catch (Exception ex) {
            log.error("Error activating tenant: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to activate tenant"));
        }
    }

    /**
     * Upgrade tenant plan
     * @param id Tenant ID
     * @param planRequest Request with new plan
     * @return Updated tenant
     */
    @PutMapping("/{id}/upgrade")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> upgradePlan(@PathVariable Long id, @RequestBody Map<String, String> planRequest) {
        try {
            String newPlan = planRequest.get("plan");
            if (newPlan == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Plan is required"));
            }

            Tenant tenant = tenantService.upgradePlan(id, newPlan);
            return ResponseEntity.ok(tenant);
        } catch (IllegalArgumentException ex) {
            log.error("Invalid plan: {}", ex.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            log.error("Error upgrading tenant plan: {}", id, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upgrade plan"));
        }
    }

    /**
     * Check if subdomain is available
     * @param subdomain Subdomain to check
     * @return Availability status
     */
    @GetMapping("/check-subdomain")
    public ResponseEntity<Map<String, Boolean>> checkSubdomainAvailability(@RequestParam String subdomain) {
        try {
            boolean available = tenantService.isSubdomainAvailable(subdomain);
            return ResponseEntity.ok(Map.of("available", available));
        } catch (Exception ex) {
            log.error("Error checking subdomain availability: {}", subdomain, ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get tenant statistics
     * @return Statistics
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getTenantStatistics() {
        try {
            Map<String, Object> stats = tenantService.getTenantStatistics();
            return ResponseEntity.ok(stats);
        } catch (Exception ex) {
            log.error("Error fetching tenant statistics", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expiring trials
     * @param daysAhead Days to look ahead (default: 7)
     * @return List of tenants with expiring trials
     */
    @GetMapping("/expiring-trials")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getExpiringTrials(
            @RequestParam(defaultValue = "7") int daysAhead) {
        try {
            List<Tenant> tenants = tenantService.findExpiringTrials(daysAhead);
            return ResponseEntity.ok(tenants);
        } catch (Exception ex) {
            log.error("Error fetching expiring trials", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get expiring subscriptions
     * @param daysAhead Days to look ahead (default: 30)
     * @return List of tenants with expiring subscriptions
     */
    @GetMapping("/expiring-subscriptions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<Tenant>> getExpiringSubscriptions(
            @RequestParam(defaultValue = "30") int daysAhead) {
        try {
            List<Tenant> tenants = tenantService.findExpiringSubscriptions(daysAhead);
            return ResponseEntity.ok(tenants);
        } catch (Exception ex) {
            log.error("Error fetching expiring subscriptions", ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
