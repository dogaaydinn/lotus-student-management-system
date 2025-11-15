-- Multi-Tenancy Support for Lotus Student Management System
-- Version: 7.0.0
-- Description: Add tenants table for SaaS deployment with isolated data per institution

-- =====================================================
-- Tenant Management Table
-- =====================================================

CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL COMMENT 'Institution/Organization name',
    subdomain VARCHAR(100) NOT NULL UNIQUE COMMENT 'Unique subdomain identifier (e.g., university-xyz)',
    database_name VARCHAR(100) COMMENT 'Database name for database-per-tenant strategy',
    schema_name VARCHAR(100) COMMENT 'Schema name for schema-per-tenant strategy',
    connection_string VARCHAR(500) COMMENT 'Custom database connection string if needed',

    -- Status and Plan Information
    status VARCHAR(20) NOT NULL DEFAULT 'TRIAL' COMMENT 'Tenant status: ACTIVE, SUSPENDED, TRIAL, EXPIRED',
    plan VARCHAR(50) NOT NULL DEFAULT 'BASIC' COMMENT 'Subscription plan: BASIC, PROFESSIONAL, ENTERPRISE',

    -- Resource Limits
    max_users INT DEFAULT 100 COMMENT 'Maximum number of users allowed',
    max_storage_gb INT DEFAULT 10 COMMENT 'Maximum storage in GB',
    features TEXT COMMENT 'JSON string of enabled features',

    -- Branding Customization
    custom_domain VARCHAR(200) COMMENT 'Custom domain if tenant has one',
    logo_url VARCHAR(500) COMMENT 'URL to tenant logo',
    primary_color VARCHAR(7) DEFAULT '#1976d2' COMMENT 'Primary brand color (hex)',

    -- Contact Information
    contact_email VARCHAR(150) NOT NULL COMMENT 'Primary contact email',
    contact_phone VARCHAR(50) COMMENT 'Contact phone number',
    billing_email VARCHAR(150) COMMENT 'Billing contact email',

    -- Subscription Dates
    subscription_start_date TIMESTAMP NULL COMMENT 'Subscription start date',
    subscription_end_date TIMESTAMP NULL COMMENT 'Subscription end date',
    trial_end_date TIMESTAMP NULL COMMENT 'Trial period end date',

    -- Audit Fields
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP NULL COMMENT 'Last time tenant was accessed',

    -- Indexes for Performance
    INDEX idx_tenant_subdomain (subdomain),
    INDEX idx_tenant_status (status),
    INDEX idx_tenant_plan (plan),
    INDEX idx_tenant_created_at (created_at),

    -- Constraints
    CONSTRAINT chk_tenant_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'TRIAL', 'EXPIRED')),
    CONSTRAINT chk_tenant_plan CHECK (plan IN ('BASIC', 'PROFESSIONAL', 'ENTERPRISE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
COMMENT='Multi-tenant organizations/institutions table for SaaS deployment';

-- =====================================================
-- Tenant-Specific User Associations (Optional)
-- =====================================================
-- Add tenant_id to existing user tables for multi-tenant isolation

ALTER TABLE student ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL COMMENT 'Associated tenant ID for multi-tenancy';
ALTER TABLE instructor ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL COMMENT 'Associated tenant ID for multi-tenancy';
ALTER TABLE coordinator ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL COMMENT 'Associated tenant ID for multi-tenancy';
ALTER TABLE admin ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL COMMENT 'Associated tenant ID for multi-tenancy';
ALTER TABLE career_center ADD COLUMN IF NOT EXISTS tenant_id BIGINT NULL COMMENT 'Associated tenant ID for multi-tenancy';

-- Add indexes for tenant_id lookups
CREATE INDEX IF NOT EXISTS idx_student_tenant ON student(tenant_id);
CREATE INDEX IF NOT EXISTS idx_instructor_tenant ON instructor(tenant_id);
CREATE INDEX IF NOT EXISTS idx_coordinator_tenant ON coordinator(tenant_id);
CREATE INDEX IF NOT EXISTS idx_admin_tenant ON admin(tenant_id);
CREATE INDEX IF NOT EXISTS idx_career_center_tenant ON career_center(tenant_id);

-- Add foreign key constraints (optional - can be enabled later)
-- ALTER TABLE student ADD CONSTRAINT fk_student_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
-- ALTER TABLE instructor ADD CONSTRAINT fk_instructor_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
-- ALTER TABLE coordinator ADD CONSTRAINT fk_coordinator_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
-- ALTER TABLE admin ADD CONSTRAINT fk_admin_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;
-- ALTER TABLE career_center ADD CONSTRAINT fk_career_center_tenant FOREIGN KEY (tenant_id) REFERENCES tenants(id) ON DELETE CASCADE;

-- =====================================================
-- Sample Data for Testing (Development Only)
-- =====================================================
-- Insert a default tenant for existing data

INSERT INTO tenants (
    name,
    subdomain,
    status,
    plan,
    max_users,
    max_storage_gb,
    contact_email,
    subscription_start_date,
    features
) VALUES (
    'Default Institution',
    'default',
    'ACTIVE',
    'ENTERPRISE',
    1000,
    100,
    'admin@lotus-sms.com',
    CURRENT_TIMESTAMP,
    '{"analytics":true,"ai_features":true,"plugins":true,"advanced_reporting":true}'
) ON DUPLICATE KEY UPDATE name = name;

-- Set existing users to default tenant (for backward compatibility)
-- UPDATE student SET tenant_id = 1 WHERE tenant_id IS NULL;
-- UPDATE instructor SET tenant_id = 1 WHERE tenant_id IS NULL;
-- UPDATE coordinator SET tenant_id = 1 WHERE tenant_id IS NULL;
-- UPDATE admin SET tenant_id = 1 WHERE tenant_id IS NULL;
-- UPDATE career_center SET tenant_id = 1 WHERE tenant_id IS NULL;
