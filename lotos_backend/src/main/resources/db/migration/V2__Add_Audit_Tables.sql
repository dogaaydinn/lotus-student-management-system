-- Add audit and tracking tables
-- Version: 2.0.0
-- Description: Add audit logging and system tracking

-- =====================================================
-- Audit Log Table
-- =====================================================

CREATE TABLE IF NOT EXISTS audit_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    user_type VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    old_value TEXT,
    new_value TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_user_id (user_id),
    INDEX idx_audit_username (username),
    INDEX idx_audit_action (action),
    INDEX idx_audit_entity_type (entity_type),
    INDEX idx_audit_timestamp (timestamp)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Login History Table
-- =====================================================

CREATE TABLE IF NOT EXISTS login_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    user_type VARCHAR(50) NOT NULL,
    username VARCHAR(100) NOT NULL,
    login_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    logout_time TIMESTAMP NULL,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    success BOOLEAN DEFAULT TRUE,
    failure_reason VARCHAR(255),
    INDEX idx_login_user_id (user_id),
    INDEX idx_login_username (username),
    INDEX idx_login_time (login_time),
    INDEX idx_login_success (success)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- System Settings Table
-- =====================================================

CREATE TABLE IF NOT EXISTS system_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    setting_key VARCHAR(100) NOT NULL UNIQUE,
    setting_value TEXT,
    description VARCHAR(500),
    category VARCHAR(50),
    is_editable BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(100),
    INDEX idx_settings_key (setting_key),
    INDEX idx_settings_category (category)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- File Upload Metadata Table
-- =====================================================

CREATE TABLE IF NOT EXISTS file_metadata (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    file_id VARCHAR(36) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename VARCHAR(255) NOT NULL,
    file_type VARCHAR(100),
    file_size BIGINT,
    mime_type VARCHAR(100),
    uploaded_by VARCHAR(100),
    uploaded_by_id BIGINT,
    upload_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checksum VARCHAR(64),
    storage_path VARCHAR(500),
    is_deleted BOOLEAN DEFAULT FALSE,
    deleted_at TIMESTAMP NULL,
    INDEX idx_file_meta_id (file_id),
    INDEX idx_file_meta_uploaded_by (uploaded_by),
    INDEX idx_file_meta_upload_time (upload_time),
    INDEX idx_file_meta_is_deleted (is_deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =====================================================
-- Insert Default System Settings
-- =====================================================

INSERT INTO system_settings (setting_key, setting_value, description, category, is_editable) VALUES
('max_file_upload_size', '10485760', 'Maximum file upload size in bytes (10MB)', 'FILE_MANAGEMENT', TRUE),
('session_timeout_minutes', '30', 'User session timeout in minutes', 'SECURITY', TRUE),
('password_min_length', '8', 'Minimum password length', 'SECURITY', TRUE),
('enable_email_notifications', 'true', 'Enable email notifications', 'NOTIFICATIONS', TRUE),
('application_name', 'Lotus Student Management System', 'Application display name', 'GENERAL', FALSE),
('maintenance_mode', 'false', 'Enable maintenance mode', 'SYSTEM', TRUE),
('max_login_attempts', '5', 'Maximum login attempts before account lockout', 'SECURITY', TRUE),
('account_lockout_duration_minutes', '15', 'Account lockout duration in minutes', 'SECURITY', TRUE);
