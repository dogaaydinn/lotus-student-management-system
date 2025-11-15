package com.lotus.lotusSPM.service.base;

import com.lotus.lotusSPM.multitenant.Tenant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Service interface for Tenant management
 */
public interface TenantService {

    /**
     * Create a new tenant
     * @param tenant Tenant to create
     * @return Created tenant
     */
    Tenant createTenant(Tenant tenant);

    /**
     * Update an existing tenant
     * @param id Tenant ID
     * @param tenant Updated tenant data
     * @return Updated tenant
     */
    Tenant updateTenant(Long id, Tenant tenant);

    /**
     * Delete a tenant
     * @param id Tenant ID
     */
    void deleteTenant(Long id);

    /**
     * Find tenant by ID
     * @param id Tenant ID
     * @return Tenant
     */
    Tenant findById(Long id);

    /**
     * Find tenant by subdomain
     * @param subdomain Subdomain
     * @return Tenant
     */
    Tenant findBySubdomain(String subdomain);

    /**
     * Find all tenants with pagination
     * @param pageable Pagination parameters
     * @return Page of tenants
     */
    Page<Tenant> findAll(Pageable pageable);

    /**
     * Find tenants by status
     * @param status Status
     * @return List of tenants
     */
    List<Tenant> findByStatus(String status);

    /**
     * Find tenants by plan
     * @param plan Plan
     * @return List of tenants
     */
    List<Tenant> findByPlan(String plan);

    /**
     * Provision a new tenant (create subdomain, database, etc.)
     * @param tenant Tenant to provision
     * @return Provisioned tenant
     */
    Tenant provisionTenant(Tenant tenant);

    /**
     * Suspend a tenant
     * @param id Tenant ID
     * @return Suspended tenant
     */
    Tenant suspendTenant(Long id);

    /**
     * Activate a tenant
     * @param id Tenant ID
     * @return Activated tenant
     */
    Tenant activateTenant(Long id);

    /**
     * Upgrade tenant plan
     * @param id Tenant ID
     * @param newPlan New plan
     * @return Updated tenant
     */
    Tenant upgradePlan(Long id, String newPlan);

    /**
     * Check if subdomain is available
     * @param subdomain Subdomain to check
     * @return true if available, false otherwise
     */
    boolean isSubdomainAvailable(String subdomain);

    /**
     * Get tenant statistics
     * @return Statistics map
     */
    Map<String, Object> getTenantStatistics();

    /**
     * Find expiring trials
     * @param daysAhead Days to look ahead
     * @return List of tenants with expiring trials
     */
    List<Tenant> findExpiringTrials(int daysAhead);

    /**
     * Find expiring subscriptions
     * @param daysAhead Days to look ahead
     * @return List of tenants with expiring subscriptions
     */
    List<Tenant> findExpiringSubscriptions(int daysAhead);

    /**
     * Update last accessed timestamp
     * @param tenantId Tenant ID
     */
    void updateLastAccessed(Long tenantId);
}
