-- Add additional indexes and constraints for performance
-- Version: 3.0.0
-- Description: Optimize database performance with additional indexes

-- =====================================================
-- Additional Indexes for Performance
-- =====================================================

-- Composite indexes for common queries
CREATE INDEX idx_student_faculty_dept ON student(faculty, department);
CREATE INDEX idx_student_status_active ON student(internship_status, active);

CREATE INDEX idx_messages_to_read ON messages(to_user, is_read);
CREATE INDEX idx_messages_date_time ON messages(date, time);

CREATE INDEX idx_opportunities_active_deadline ON opportunities(is_active, deadline);
CREATE INDEX idx_opportunities_company_active ON opportunities(company_name, is_active);

CREATE INDEX idx_notifications_user_read ON notifications(user_type, user_id, is_read);

-- =====================================================
-- Full-text search indexes
-- =====================================================

ALTER TABLE opportunities ADD FULLTEXT INDEX idx_ft_opportunities_search (title, company_name, text);
ALTER TABLE messages ADD FULLTEXT INDEX idx_ft_messages_search (subject, title, text);

-- =====================================================
-- Add missing constraints
-- =====================================================

-- Ensure email uniqueness across all user tables (handled by unique constraints in V1)
-- Add check constraints for data integrity

ALTER TABLE student ADD CONSTRAINT chk_student_email CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');
ALTER TABLE instructor ADD CONSTRAINT chk_instructor_email CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');
ALTER TABLE coordinator ADD CONSTRAINT chk_coordinator_email CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');
ALTER TABLE admin ADD CONSTRAINT chk_admin_email CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');
ALTER TABLE career_center ADD CONSTRAINT chk_career_center_email CHECK (email REGEXP '^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$');
