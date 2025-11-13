-- V5: Add password column to INSTRUCTOR table
-- This migration adds the missing PASSWORD column to support instructor authentication

-- Add PASSWORD column to INSTRUCTOR table
ALTER TABLE INSTRUCTOR
ADD COLUMN PASSWORD VARCHAR(255) DEFAULT NULL
COMMENT 'BCrypt hashed password for instructor authentication';

-- Create index on username for faster authentication lookups
CREATE INDEX idx_instructor_username ON INSTRUCTOR(USERNAME);

-- Note: Existing instructor records will have NULL passwords
-- Administrators should reset passwords for existing instructors after this migration
