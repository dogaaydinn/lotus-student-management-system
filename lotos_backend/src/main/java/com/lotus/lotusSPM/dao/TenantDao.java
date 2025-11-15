package com.lotus.lotusSPM.dao;

import com.lotus.lotusSPM.multitenant.Tenant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * DAO for Tenant entity
 * Provides database operations for multi-tenant management
 */
@Repository
public interface TenantDao extends JpaRepository<Tenant, Long>, CrudRepository<Tenant, Long> {

    /**
     * Find tenant by subdomain
     * @param subdomain Unique subdomain identifier
     * @return Optional tenant
     */
    Optional<Tenant> findBySubdomain(String subdomain);

    /**
     * Find tenant by custom domain
     * @param customDomain Custom domain
     * @return Optional tenant
     */
    Optional<Tenant> findByCustomDomain(String customDomain);

    /**
     * Find all tenants by status
     * @param status Tenant status (ACTIVE, SUSPENDED, TRIAL, EXPIRED)
     * @return List of tenants
     */
    List<Tenant> findByStatus(String status);

    /**
     * Find all tenants by plan
     * @param plan Subscription plan (BASIC, PROFESSIONAL, ENTERPRISE)
     * @return List of tenants
     */
    List<Tenant> findByPlan(String plan);

    /**
     * Find tenants with expiring trials
     * @param expiryDate Date to check against
     * @return List of tenants with expiring trials
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'TRIAL' AND t.trialEndDate <= :expiryDate")
    List<Tenant> findExpiringTrials(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Find tenants with expiring subscriptions
     * @param expiryDate Date to check against
     * @return List of tenants with expiring subscriptions
     */
    @Query("SELECT t FROM Tenant t WHERE t.status = 'ACTIVE' AND t.subscriptionEndDate <= :expiryDate")
    List<Tenant> findExpiringSubscriptions(@Param("expiryDate") LocalDateTime expiryDate);

    /**
     * Check if subdomain exists
     * @param subdomain Subdomain to check
     * @return true if exists, false otherwise
     */
    boolean existsBySubdomain(String subdomain);

    /**
     * Count active tenants
     * @return Number of active tenants
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = 'ACTIVE'")
    long countActiveTenants();

    /**
     * Count trial tenants
     * @return Number of trial tenants
     */
    @Query("SELECT COUNT(t) FROM Tenant t WHERE t.status = 'TRIAL'")
    long countTrialTenants();
}
