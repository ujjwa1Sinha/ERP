-- V4: Migrate from roles/user_roles join tables to single role column on users

-- Step 1: Add role column to users (default VIEWER while we migrate)
ALTER TABLE users ADD COLUMN IF NOT EXISTS role VARCHAR(30) NOT NULL DEFAULT 'VIEWER';

-- Step 2: Populate role from existing user_roles join table (take the first role per user)
UPDATE users u
SET role = (
    SELECT r.name
    FROM user_roles ur
    JOIN roles r ON r.id = ur.role_id
    WHERE ur.user_id = u.id
    ORDER BY r.name
    LIMIT 1
)
WHERE EXISTS (
    SELECT 1 FROM user_roles ur WHERE ur.user_id = u.id
);

-- Step 3: Drop join tables and roles table (no longer needed)
DROP TABLE IF EXISTS user_roles;
DROP TABLE IF EXISTS role_permissions;
DROP TABLE IF EXISTS roles;
