-- Phase 1-5: Advanced Features Implementation
-- Add MFA, Tenants, Analytics, and Plugin tables

-- ==================================================
-- Phase 1: Multi-Factor Authentication
-- ==================================================

ALTER TABLE student ADD COLUMN mfa_secret VARCHAR(255) DEFAULT NULL;
ALTER TABLE student ADD COLUMN mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE student ADD COLUMN mfa_backup_codes TEXT DEFAULT NULL;

ALTER TABLE coordinator ADD COLUMN mfa_secret VARCHAR(255) DEFAULT NULL;
ALTER TABLE coordinator ADD COLUMN mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE coordinator ADD COLUMN mfa_backup_codes TEXT DEFAULT NULL;

ALTER TABLE admin ADD COLUMN mfa_secret VARCHAR(255) DEFAULT NULL;
ALTER TABLE admin ADD COLUMN mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE admin ADD COLUMN mfa_backup_codes TEXT DEFAULT NULL;

ALTER TABLE career_center ADD COLUMN mfa_secret VARCHAR(255) DEFAULT NULL;
ALTER TABLE career_center ADD COLUMN mfa_enabled BOOLEAN DEFAULT FALSE;
ALTER TABLE career_center ADD COLUMN mfa_backup_codes TEXT DEFAULT NULL;

-- ==================================================
-- Phase 4: Multi-Tenancy
-- ==================================================

CREATE TABLE IF NOT EXISTS tenants (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(200) NOT NULL,
    subdomain VARCHAR(100) NOT NULL UNIQUE,
    database_name VARCHAR(100),
    schema_name VARCHAR(100),
    connection_string VARCHAR(500),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    plan VARCHAR(50) DEFAULT 'BASIC',
    max_users INT DEFAULT 100,
    max_storage_gb INT DEFAULT 10,
    features TEXT,
    custom_domain VARCHAR(200),
    logo_url VARCHAR(500),
    primary_color VARCHAR(7) DEFAULT '#007bff',
    contact_email VARCHAR(150),
    contact_phone VARCHAR(50),
    billing_email VARCHAR(150),
    subscription_start_date TIMESTAMP NULL,
    subscription_end_date TIMESTAMP NULL,
    trial_end_date TIMESTAMP NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_accessed_at TIMESTAMP NULL,
    INDEX idx_tenant_subdomain (subdomain),
    INDEX idx_tenant_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add tenant_id to existing tables for multi-tenancy support
ALTER TABLE student ADD COLUMN tenant_id BIGINT DEFAULT NULL;
ALTER TABLE coordinator ADD COLUMN tenant_id BIGINT DEFAULT NULL;
ALTER TABLE admin ADD COLUMN tenant_id BIGINT DEFAULT NULL;
ALTER TABLE instructor ADD COLUMN tenant_id BIGINT DEFAULT NULL;
ALTER TABLE career_center ADD COLUMN tenant_id BIGINT DEFAULT NULL;
ALTER TABLE opportunities ADD COLUMN tenant_id BIGINT DEFAULT NULL;
ALTER TABLE messages ADD COLUMN tenant_id BIGINT DEFAULT NULL;

-- Add indexes for tenant_id
CREATE INDEX idx_student_tenant ON student(tenant_id);
CREATE INDEX idx_coordinator_tenant ON coordinator(tenant_id);
CREATE INDEX idx_opportunities_tenant ON opportunities(tenant_id);

-- ==================================================
-- Phase 2: Analytics Tables
-- ==================================================

CREATE TABLE IF NOT EXISTS analytics_snapshots (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    snapshot_date DATE NOT NULL,
    tenant_id BIGINT,
    metric_name VARCHAR(100) NOT NULL,
    metric_value DECIMAL(15,2),
    metadata TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_snapshot_date (snapshot_date),
    INDEX idx_metric_name (metric_name),
    INDEX idx_analytics_tenant (tenant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS student_predictions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    prediction_type VARCHAR(50) NOT NULL,
    success_probability DECIMAL(5,4),
    risk_level VARCHAR(20),
    recommendations TEXT,
    model_version VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_prediction_student (student_id),
    INDEX idx_prediction_type (prediction_type),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- Phase 3: AI Features
-- ==================================================

CREATE TABLE IF NOT EXISTS chatbot_conversations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    user_type VARCHAR(50),
    session_id VARCHAR(100),
    message TEXT NOT NULL,
    response TEXT,
    intent VARCHAR(100),
    confidence DECIMAL(5,4),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_chat_user (user_id, user_type),
    INDEX idx_chat_session (session_id),
    INDEX idx_chat_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS resume_analyses (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    filename VARCHAR(255),
    score INT,
    strengths TEXT,
    improvements TEXT,
    suggestions TEXT,
    analyzed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_resume_student (student_id),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS job_matches (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    student_id BIGINT NOT NULL,
    opportunity_id BIGINT NOT NULL,
    match_score DECIMAL(5,2),
    reasons TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_match_student (student_id),
    INDEX idx_match_opportunity (opportunity_id),
    INDEX idx_match_score (match_score DESC),
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (opportunity_id) REFERENCES opportunities(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- Phase 5: Plugin System
-- ==================================================

CREATE TABLE IF NOT EXISTS plugins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plugin_id VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    version VARCHAR(50) NOT NULL,
    description TEXT,
    author VARCHAR(200),
    api_version VARCHAR(20),
    status VARCHAR(20) DEFAULT 'DISABLED',
    enabled BOOLEAN DEFAULT FALSE,
    configuration TEXT,
    permissions TEXT,
    dependencies TEXT,
    installed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plugin_id (plugin_id),
    INDEX idx_plugin_status (status),
    INDEX idx_plugin_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS plugin_hooks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    plugin_id VARCHAR(100) NOT NULL,
    hook_name VARCHAR(100) NOT NULL,
    hook_type VARCHAR(50),
    priority INT DEFAULT 10,
    enabled BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_hook_plugin (plugin_id),
    INDEX idx_hook_name (hook_name),
    INDEX idx_hook_enabled (enabled)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ==================================================
-- Additional Indexes for Performance
-- ==================================================

-- Improve multi-tenant queries
CREATE INDEX idx_student_tenant_active ON student(tenant_id, active);
CREATE INDEX idx_opportunities_tenant_active ON opportunities(tenant_id, is_active);

-- Improve analytics queries
CREATE INDEX idx_student_created ON student(created_at);
CREATE INDEX idx_opportunities_created ON opportunities(created_at);

-- Improve search performance
ALTER TABLE student ADD FULLTEXT INDEX idx_ft_student_search (name, surname, email);
ALTER TABLE opportunities ADD FULLTEXT INDEX idx_ft_opportunity_search (title, company_name, text);
