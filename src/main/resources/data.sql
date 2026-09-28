-- ===================================================================
-- GrievanceTrack — Seed Data SQL Script
-- ===================================================================

-- 1. Departments
INSERT INTO departments (id, name, officer_name, contact, created_at)
VALUES (1, 'Roads Department', 'Chief Engineer Rajesh Sharma', 'roads@civicgov.org / +91-9876543201', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO departments (id, name, officer_name, contact, created_at)
VALUES (2, 'Water Supply Department', 'Superintending Engineer Priya Sundaram', 'water@civicgov.org / +91-9876543202', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO departments (id, name, officer_name, contact, created_at)
VALUES (3, 'Sanitation Department', 'Health Officer Anil Kumar', 'sanitation@civicgov.org / +91-9876543203', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO departments (id, name, officer_name, contact, created_at)
VALUES (4, 'General Civic Department', 'Civic Welfare Director Sunita Rao', 'civic@civicgov.org / +91-9876543204', CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

-- 2. Categories
INSERT INTO categories (id, name, sla_days, department_id, created_at)
VALUES (1, 'Road', 3, 1, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (id, name, sla_days, department_id, created_at)
VALUES (2, 'Water', 2, 2, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (id, name, sla_days, department_id, created_at)
VALUES (3, 'Garbage', 1, 3, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;

INSERT INTO categories (id, name, sla_days, department_id, created_at)
VALUES (4, 'Other', 5, 4, CURRENT_TIMESTAMP)
ON DUPLICATE KEY UPDATE name=name;
