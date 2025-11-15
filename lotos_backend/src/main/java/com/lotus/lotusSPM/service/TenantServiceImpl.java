package com.lotus.lotusSPM.service;

import com.lotus.lotusSPM.dao.TenantDao;
import com.lotus.lotusSPM.multitenant.Tenant;
import com.lotus.lotusSPM.service.base.TenantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implementation for Tenant management
 * Handles multi-tenancy operations and provisioning
 */
@Service
@Transactional(rollbackFor = Exception.class)
@Slf4j
public class TenantServiceImpl implements TenantService {

    @Autowired
    private TenantDao tenantDao;

    @Override
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant createTenant(Tenant tenant) {
        log.info("Creating new tenant: {}", tenant.getName());

        // Validate subdomain
        if (tenantDao.existsBySubdomain(tenant.getSubdomain())) {
            throw new IllegalArgumentException("Subdomain already exists: " + tenant.getSubdomain());
        }

        // Set default values
        if (tenant.getStatus() == null) {
            tenant.setStatus("TRIAL");
        }
        if (tenant.getPlan() == null) {
            tenant.setplan("BASIC");
        }
        if (tenant.getMaxUsers() == null) {
            tenant.setMaxUsers(100);
        }
        if (tenant.getMaxStorageGb() == null) {
            tenant.setMaxStorageGb(10);
        }

        // Set trial period (30 days)
        if ("TRIAL".equals(tenant.getStatus())) {
            tenant.setTrialEndDate(LocalDateTime.now().plusDays(30));
        }

        Tenant savedTenant = tenantDao.save(tenant);
        log.info("Tenant created successfully: {} (ID: {})", savedTenant.getName(), savedTenant.getId());

        return savedTenant;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tenants", key = "#id"),
            @CacheEvict(value = "tenants", allEntries = true)
    })
    public Tenant updateTenant(Long id, Tenant tenant) {
        log.info("Updating tenant: {}", id);

        Tenant existingTenant = findById(id);

        // Update fields
        if (tenant.getName() != null) {
            existingTenant.setName(tenant.getName());
        }
        if (tenant.getStatus() != null) {
            existingTenant.setStatus(tenant.getStatus());
        }
        if (tenant.getPlan() != null) {
            existingTenant.setplan(tenant.getPlan());
        }
        if (tenant.getMaxUsers() != null) {
            existingTenant.setMaxUsers(tenant.getMaxUsers());
        }
        if (tenant.getMaxStorageGb() != null) {
            existingTenant.setMaxStorageGb(tenant.getMaxStorageGb());
        }
        if (tenant.getContactEmail() != null) {
            existingTenant.setContactEmail(tenant.getContactEmail());
        }
        if (tenant.getLogoUrl() != null) {
            existingTenant.setLogoUrl(tenant.getLogoUrl());
        }
        if (tenant.getPrimaryColor() != null) {
            existingTenant.setPrimaryColor(tenant.getPrimaryColor());
        }

        Tenant updatedTenant = tenantDao.save(existingTenant);
        log.info("Tenant updated successfully: {}", id);

        return updatedTenant;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(value = "tenants", key = "#id"),
            @CacheEvict(value = "tenants", allEntries = true)
    })
    public void deleteTenant(Long id) {
        log.info("Deleting tenant: {}", id);
        tenantDao.deleteById(id);
        log.info("Tenant deleted successfully: {}", id);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = "tenants", key = "#id")
    public Tenant findById(Long id) {
        log.debug("Finding tenant by ID: {}", id);
        return tenantDao.findById(id)
                .orElseThrow(() -> new RuntimeException("Tenant not found with id: " + id));
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    @Cacheable(value = "tenants", key = "'subdomain:' + #subdomain")
    public Tenant findBySubdomain(String subdomain) {
        log.debug("Finding tenant by subdomain: {}", subdomain);
        return tenantDao.findBySubdomain(subdomain)
                .orElseThrow(() -> new RuntimeException("Tenant not found with subdomain: " + subdomain));
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Page<Tenant> findAll(Pageable pageable) {
        log.debug("Finding all tenants with pagination");
        return tenantDao.findAll(pageable);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Tenant> findByStatus(String status) {
        log.debug("Finding tenants by status: {}", status);
        return tenantDao.findByStatus(status);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Tenant> findByPlan(String plan) {
        log.debug("Finding tenants by plan: {}", plan);
        return tenantDao.findByPlan(plan);
    }

    @Override
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant provisionTenant(Tenant tenant) {
        log.info("Provisioning new tenant: {}", tenant.getName());

        // Create the tenant
        Tenant createdTenant = createTenant(tenant);

        // In production, here you would:
        // 1. Create dedicated database/schema
        // 2. Run migrations for the new tenant
        // 3. Set up initial admin user
        // 4. Configure tenant-specific resources
        // 5. Send welcome email

        log.info("Tenant provisioned successfully: {} (ID: {})", createdTenant.getName(), createdTenant.getId());

        return createdTenant;
    }

    @Override
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant suspendTenant(Long id) {
        log.info("Suspending tenant: {}", id);

        Tenant tenant = findById(id);
        tenant.setStatus("SUSPENDED");

        Tenant updatedTenant = tenantDao.save(tenant);
        log.info("Tenant suspended: {}", id);

        return updatedTenant;
    }

    @Override
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant activateTenant(Long id) {
        log.info("Activating tenant: {}", id);

        Tenant tenant = findById(id);
        tenant.setStatus("ACTIVE");

        // Set subscription dates if not set
        if (tenant.getSubscriptionStartDate() == null) {
            tenant.setSubscriptionStartDate(LocalDateTime.now());
        }
        if (tenant.getSubscriptionEndDate() == null) {
            tenant.setSubscriptionEndDate(LocalDateTime.now().plusYears(1));
        }

        Tenant updatedTenant = tenantDao.save(tenant);
        log.info("Tenant activated: {}", id);

        return updatedTenant;
    }

    @Override
    @CacheEvict(value = "tenants", allEntries = true)
    public Tenant upgradePlan(Long id, String newPlan) {
        log.info("Upgrading tenant {} to plan: {}", id, newPlan);

        Tenant tenant = findById(id);
        String oldPlan = tenant.getPlan();
        tenant.setplan(newPlan);

        // Update resource limits based on plan
        switch (newPlan) {
            case "BASIC":
                tenant.setMaxUsers(100);
                tenant.setMaxStorageGb(10);
                break;
            case "PROFESSIONAL":
                tenant.setMaxUsers(500);
                tenant.setMaxStorageGb(50);
                break;
            case "ENTERPRISE":
                tenant.setMaxUsers(10000);
                tenant.setMaxStorageGb(500);
                break;
            default:
                throw new IllegalArgumentException("Invalid plan: " + newPlan);
        }

        Tenant updatedTenant = tenantDao.save(tenant);
        log.info("Tenant upgraded from {} to {}: {}", oldPlan, newPlan, id);

        return updatedTenant;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public boolean isSubdomainAvailable(String subdomain) {
        return !tenantDao.existsBySubdomain(subdomain);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public Map<String, Object> getTenantStatistics() {
        Map<String, Object> stats = new HashMap<>();

        long totalTenants = tenantDao.count();
        long activeTenants = tenantDao.countActiveTenants();
        long trialTenants = tenantDao.countTrialTenants();

        stats.put("totalTenants", totalTenants);
        stats.put("activeTenants", activeTenants);
        stats.put("trialTenants", trialTenants);
        stats.put("suspendedTenants", totalTenants - activeTenants - trialTenants);

        stats.put("byPlan", Map.of(
                "basic", tenantDao.findByPlan("BASIC").size(),
                "professional", tenantDao.findByPlan("PROFESSIONAL").size(),
                "enterprise", tenantDao.findByPlan("ENTERPRISE").size()
        ));

        return stats;
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Tenant> findExpiringTrials(int daysAhead) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(daysAhead);
        return tenantDao.findExpiringTrials(expiryDate);
    }

    @Override
    @Transactional(propagation = Propagation.SUPPORTS, readOnly = true)
    public List<Tenant> findExpiringSubscriptions(int daysAhead) {
        LocalDateTime expiryDate = LocalDateTime.now().plusDays(daysAhead);
        return tenantDao.findExpiringSubscriptions(expiryDate);
    }

    @Override
    @CacheEvict(value = "tenants", key = "#tenantId")
    public void updateLastAccessed(Long tenantId) {
        Tenant tenant = findById(tenantId);
        tenant.setLastAccessedAt(LocalDateTime.now());
        tenantDao.save(tenant);
    }
}
