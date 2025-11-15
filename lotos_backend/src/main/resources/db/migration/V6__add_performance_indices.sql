-- Database Performance Optimization: Add Indices
-- Sprint 3: Infrastructure Improvements
-- Created: 2025-11-15

-- Student table indices
CREATE INDEX IF NOT EXISTS idx_student_username ON student(username);
CREATE INDEX IF NOT EXISTS idx_student_email ON student(email);
CREATE INDEX IF NOT EXISTS idx_student_department ON student(department);
CREATE INDEX IF NOT EXISTS idx_student_created_at ON student(created_at);

-- Instructor table indices
CREATE INDEX IF NOT EXISTS idx_instructor_username ON instructor(username);
CREATE INDEX IF NOT EXISTS idx_instructor_email ON instructor(email);
CREATE INDEX IF NOT EXISTS idx_instructor_department ON instructor(department);

-- Coordinator table indices
CREATE INDEX IF NOT EXISTS idx_coordinator_username ON coordinator(username);
CREATE INDEX IF NOT EXISTS idx_coordinator_email ON coordinator(email);

-- Admin table indices
CREATE INDEX IF NOT EXISTS idx_admin_username ON admin(username);

-- Career Center table indices
CREATE INDEX IF NOT EXISTS idx_career_center_username ON career_center(username);

-- Messages table indices
CREATE INDEX IF NOT EXISTS idx_messages_from ON messages(message_from);
CREATE INDEX IF NOT EXISTS idx_messages_to ON messages(message_to);
CREATE INDEX IF NOT EXISTS idx_messages_date ON messages(date);
CREATE INDEX IF NOT EXISTS idx_messages_from_to ON messages(message_from, message_to);

-- Notifications table indices
CREATE INDEX IF NOT EXISTS idx_notifications_to ON notifications(notification_to);
CREATE INDEX IF NOT EXISTS idx_notifications_from ON notifications(notification_from);
CREATE INDEX IF NOT EXISTS idx_notifications_date ON notifications(date);

-- Documents table indices
CREATE INDEX IF NOT EXISTS idx_documents_file_name ON documents(file_name);
CREATE INDEX IF NOT EXISTS idx_documents_upload_date ON documents(upload_date);

-- Official Letter table indices
CREATE INDEX IF NOT EXISTS idx_official_letter_student ON official_letter(student_name, student_surname);
CREATE INDEX IF NOT EXISTS idx_official_letter_company ON official_letter(company_name);
CREATE INDEX IF NOT EXISTS idx_official_letter_date ON official_letter(created_date);

-- Opportunities table indices
CREATE INDEX IF NOT EXISTS idx_opportunities_company ON opportunities(company_name);
CREATE INDEX IF NOT EXISTS idx_opportunities_deadline ON opportunities(deadline);
CREATE INDEX IF NOT EXISTS idx_opportunities_location ON opportunities(location);
CREATE INDEX IF NOT EXISTS idx_opportunities_title ON opportunities(title);

-- Composite indices for common queries
CREATE INDEX IF NOT EXISTS idx_messages_to_date ON messages(message_to, date DESC);
CREATE INDEX IF NOT EXISTS idx_notifications_to_date ON notifications(notification_to, date DESC);
CREATE INDEX IF NOT EXISTS idx_opportunities_deadline_company ON opportunities(deadline, company_name);

-- Optimize table statistics
ANALYZE TABLE student;
ANALYZE TABLE instructor;
ANALYZE TABLE coordinator;
ANALYZE TABLE admin;
ANALYZE TABLE career_center;
ANALYZE TABLE messages;
ANALYZE TABLE notifications;
ANALYZE TABLE documents;
ANALYZE TABLE official_letter;
ANALYZE TABLE opportunities;
