-- ============================================================
-- V2__seed_roles_and_admin.sql
-- Seed default roles, permissions, and admin user
-- ============================================================

-- ============================================================
-- 1. INSERT ROLES
-- ============================================================

INSERT INTO roles (id, name, description) VALUES
    (uuid_generate_v4(), 'SUPER_ADMIN', 'Super Administrator with full access'),
    (uuid_generate_v4(), 'OWNER', 'Business Owner'),
    (uuid_generate_v4(), 'FLEET_MANAGER', 'Fleet Operations Manager'),
    (uuid_generate_v4(), 'DISPATCHER', 'Trip Dispatcher'),
    (uuid_generate_v4(), 'ACCOUNTANT', 'Finance and Accounts'),
    (uuid_generate_v4(), 'HR', 'Human Resources'),
    (uuid_generate_v4(), 'MAINTENANCE_MANAGER', 'Vehicle Maintenance Manager'),
    (uuid_generate_v4(), 'DRIVER', 'Vehicle Driver'),
    (uuid_generate_v4(), 'VIEWER', 'Read-only access');

-- ============================================================
-- 2. INSERT PERMISSIONS FOR EACH ROLE
-- ============================================================

-- SUPER_ADMIN gets all permissions
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'), ('VEHICLE_EDIT'),
    ('DRIVER_VIEW'), ('DRIVER_EDIT'), ('DRIVER_MEDICAL_VIEW'),
    ('TRIP_VIEW'), ('TRIP_CREATE'), ('TRIP_ASSIGN'),
    ('EXPENSE_VIEW'), ('EXPENSE_APPROVE'),
    ('GPS_VIEW'), ('GPS_HISTORY_VIEW'),
    ('BRANCH_VIEW'), ('BRANCH_EDIT'),
    ('USER_VIEW'), ('USER_EDIT'),
    ('DOCUMENT_VIEW'), ('DOCUMENT_EDIT'),
    ('ASSIGNMENT_VIEW'), ('ASSIGNMENT_EDIT'),
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'SUPER_ADMIN';

-- OWNER gets all permissions
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'), ('VEHICLE_EDIT'),
    ('DRIVER_VIEW'), ('DRIVER_EDIT'), ('DRIVER_MEDICAL_VIEW'),
    ('TRIP_VIEW'), ('TRIP_CREATE'), ('TRIP_ASSIGN'),
    ('EXPENSE_VIEW'), ('EXPENSE_APPROVE'),
    ('GPS_VIEW'), ('GPS_HISTORY_VIEW'),
    ('BRANCH_VIEW'), ('BRANCH_EDIT'),
    ('USER_VIEW'), ('USER_EDIT'),
    ('DOCUMENT_VIEW'), ('DOCUMENT_EDIT'),
    ('ASSIGNMENT_VIEW'), ('ASSIGNMENT_EDIT'),
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'OWNER';

-- FLEET_MANAGER
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'), ('VEHICLE_EDIT'),
    ('DRIVER_VIEW'), ('DRIVER_EDIT'),
    ('TRIP_VIEW'), ('TRIP_CREATE'), ('TRIP_ASSIGN'),
    ('GPS_VIEW'), ('GPS_HISTORY_VIEW'),
    ('BRANCH_VIEW'),
    ('DOCUMENT_VIEW'), ('DOCUMENT_EDIT'),
    ('ASSIGNMENT_VIEW'), ('ASSIGNMENT_EDIT'),
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'FLEET_MANAGER';

-- DISPATCHER
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'),
    ('DRIVER_VIEW'),
    ('TRIP_VIEW'), ('TRIP_CREATE'), ('TRIP_ASSIGN'),
    ('GPS_VIEW'),
    ('BRANCH_VIEW'),
    ('ASSIGNMENT_VIEW'), ('ASSIGNMENT_EDIT'),
    ('DOCUMENT_VIEW')
) AS p(permission)
WHERE r.name = 'DISPATCHER';

-- ACCOUNTANT
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'),
    ('DRIVER_VIEW'),
    ('TRIP_VIEW'),
    ('EXPENSE_VIEW'), ('EXPENSE_APPROVE'),
    ('BRANCH_VIEW'),
    ('DOCUMENT_VIEW'),
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'ACCOUNTANT';

-- HR
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('DRIVER_VIEW'), ('DRIVER_EDIT'), ('DRIVER_MEDICAL_VIEW'),
    ('BRANCH_VIEW'),
    ('DOCUMENT_VIEW'), ('DOCUMENT_EDIT'),
    ('USER_VIEW'), ('USER_EDIT')
) AS p(permission)
WHERE r.name = 'HR';

-- MAINTENANCE_MANAGER
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'), ('VEHICLE_EDIT'),
    ('BRANCH_VIEW'),
    ('DOCUMENT_VIEW'), ('DOCUMENT_EDIT'),
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'MAINTENANCE_MANAGER';

-- DRIVER
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('TRIP_VIEW'),
    ('GPS_VIEW'),
    ('DOCUMENT_VIEW')
) AS p(permission)
WHERE r.name = 'DRIVER';

-- VIEWER
INSERT INTO role_permissions (role_id, permission)
SELECT r.id, p.permission FROM roles r
CROSS JOIN (VALUES
    ('VEHICLE_VIEW'),
    ('DRIVER_VIEW'),
    ('TRIP_VIEW'),
    ('GPS_VIEW'),
    ('BRANCH_VIEW'),
    ('DOCUMENT_VIEW'),
    ('ASSIGNMENT_VIEW'),
    ('REPORT_VIEW')
) AS p(permission)
WHERE r.name = 'VIEWER';

-- ============================================================
-- 3. CREATE DEFAULT ADMIN USER
--    Password: admin123 (BCrypt encoded)
-- ============================================================

INSERT INTO users (id, username, password, email, full_name, phone, active, created_by)
VALUES (
    uuid_generate_v4(),
    'admin',
    '$2a$10$EqKcp1WFKyS3L.RDnGQl/OQoBFzjBcqLvEn0./5gMzCLMYnSIS6qi',
    'admin@transport-erp.com',
    'System Administrator',
    '9999999999',
    true,
    'SYSTEM'
);

-- Assign SUPER_ADMIN role to admin user
INSERT INTO user_roles (user_id, role_id)
SELECT u.id, r.id FROM users u, roles r
WHERE u.username = 'admin' AND r.name = 'SUPER_ADMIN';
