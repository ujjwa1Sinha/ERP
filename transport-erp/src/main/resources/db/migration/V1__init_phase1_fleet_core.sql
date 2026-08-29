-- ============================================================
-- V1__init_phase1_fleet_core.sql
-- Phase 1: Fleet Core - Auth, Branches, Vehicles, Drivers,
--          Documents, Assignments
-- ============================================================

-- Enable UUID extension
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ============================================================
-- 1. ROLES & PERMISSIONS
-- ============================================================

CREATE TABLE roles (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(30) NOT NULL UNIQUE,
    description     VARCHAR(255),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE role_permissions (
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    permission      VARCHAR(50) NOT NULL,
    PRIMARY KEY (role_id, permission)
);

-- ============================================================
-- 2. BRANCHES
-- ============================================================

CREATE TABLE branches (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name            VARCHAR(100) NOT NULL,
    code            VARCHAR(50) UNIQUE,
    address         VARCHAR(500),
    city            VARCHAR(100),
    state           VARCHAR(100),
    pin_code        VARCHAR(10),
    phone           VARCHAR(15),
    email           VARCHAR(100),
    contact_person  VARCHAR(100),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE INDEX idx_branches_code ON branches(code);
CREATE INDEX idx_branches_active ON branches(active);

-- ============================================================
-- 3. USERS
-- ============================================================

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username        VARCHAR(50) NOT NULL UNIQUE,
    password        VARCHAR(255) NOT NULL,
    email           VARCHAR(100) NOT NULL UNIQUE,
    full_name       VARCHAR(100) NOT NULL,
    phone           VARCHAR(15),
    active          BOOLEAN NOT NULL DEFAULT TRUE,
    branch_id       UUID REFERENCES branches(id),
    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ,
    created_by      VARCHAR(100),
    updated_by      VARCHAR(100)
);

CREATE TABLE user_roles (
    user_id         UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role_id         UUID NOT NULL REFERENCES roles(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE INDEX idx_users_username ON users(username);
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_branch ON users(branch_id);

-- ============================================================
-- 4. VEHICLES
-- ============================================================

CREATE TABLE vehicles (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    registration_number     VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type            VARCHAR(20) NOT NULL,
    make                    VARCHAR(50),
    model                   VARCHAR(50),
    manufacture_year        INTEGER,
    fuel_type               VARCHAR(20),
    capacity                INTEGER,
    status                  VARCHAR(25) NOT NULL DEFAULT 'ACTIVE',
    current_odometer        NUMERIC(12,2),
    chassis_number          VARCHAR(25),
    engine_number           VARCHAR(25),
    gps_device_id           VARCHAR(50),
    insurance_expiry        DATE,
    fitness_expiry          DATE,
    permit_expiry           DATE,
    pollution_expiry        DATE,
    tax_expiry              DATE,
    branch_id               UUID REFERENCES branches(id),
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100)
);

CREATE INDEX idx_vehicles_registration ON vehicles(registration_number);
CREATE INDEX idx_vehicles_status ON vehicles(status);
CREATE INDEX idx_vehicles_type ON vehicles(vehicle_type);
CREATE INDEX idx_vehicles_branch ON vehicles(branch_id);
CREATE INDEX idx_vehicles_insurance_expiry ON vehicles(insurance_expiry);
CREATE INDEX idx_vehicles_fitness_expiry ON vehicles(fitness_expiry);
CREATE INDEX idx_vehicles_permit_expiry ON vehicles(permit_expiry);
CREATE INDEX idx_vehicles_pollution_expiry ON vehicles(pollution_expiry);

-- ============================================================
-- 5. DRIVERS
-- ============================================================

CREATE TABLE drivers (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    employee_code       VARCHAR(20) UNIQUE,
    name                VARCHAR(100) NOT NULL,
    phone               VARCHAR(15) NOT NULL,
    alternate_phone     VARCHAR(15),
    date_of_birth       DATE,
    joining_date        DATE,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    address             VARCHAR(500),
    city                VARCHAR(100),
    state               VARCHAR(100),
    pin_code            VARCHAR(10),
    aadhar_number       VARCHAR(12),
    pan_number          VARCHAR(10),
    blood_group         VARCHAR(5),
    branch_id           UUID REFERENCES branches(id),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_drivers_employee_code ON drivers(employee_code);
CREATE INDEX idx_drivers_phone ON drivers(phone);
CREATE INDEX idx_drivers_status ON drivers(status);
CREATE INDEX idx_drivers_branch ON drivers(branch_id);
CREATE INDEX idx_drivers_name ON drivers(name);

-- ============================================================
-- 6. DRIVER LICENSES
-- ============================================================

CREATE TABLE driver_licenses (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id           UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    license_number      VARCHAR(30) NOT NULL,
    license_type        VARCHAR(20),
    issuing_authority   VARCHAR(100),
    issue_date          DATE,
    expiry_date         DATE,
    is_primary          BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_driver_licenses_driver ON driver_licenses(driver_id);
CREATE INDEX idx_driver_licenses_expiry ON driver_licenses(expiry_date);

-- ============================================================
-- 7. DRIVER EMERGENCY CONTACTS
-- ============================================================

CREATE TABLE driver_emergency_contacts (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id           UUID NOT NULL REFERENCES drivers(id) ON DELETE CASCADE,
    name                VARCHAR(100) NOT NULL,
    relationship        VARCHAR(50) NOT NULL,
    phone               VARCHAR(15) NOT NULL,
    alternate_phone     VARCHAR(15),
    address             VARCHAR(500),
    is_primary          BOOLEAN DEFAULT TRUE,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_emergency_contacts_driver ON driver_emergency_contacts(driver_id);

-- ============================================================
-- 8. DOCUMENTS (Generic document system)
-- ============================================================

CREATE TABLE documents (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type         VARCHAR(20) NOT NULL,
    entity_id           UUID NOT NULL,
    document_type       VARCHAR(30) NOT NULL,
    document_number     VARCHAR(50),
    issue_date          DATE,
    expiry_date         DATE,
    file_url            VARCHAR(500),
    remarks             VARCHAR(500),
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    alert_days_before   INTEGER DEFAULT 30,
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_documents_entity ON documents(entity_type, entity_id);
CREATE INDEX idx_documents_type ON documents(document_type);
CREATE INDEX idx_documents_expiry ON documents(expiry_date);
CREATE INDEX idx_documents_status ON documents(status);

-- ============================================================
-- 9. DRIVER-VEHICLE ASSIGNMENTS
-- ============================================================

CREATE TABLE driver_assignments (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    driver_id           UUID NOT NULL REFERENCES drivers(id),
    vehicle_id          UUID NOT NULL REFERENCES vehicles(id),
    trip_id             UUID,
    assigned_at         TIMESTAMPTZ NOT NULL,
    released_at         TIMESTAMPTZ,
    role                VARCHAR(20) NOT NULL DEFAULT 'PRIMARY_DRIVER',
    remarks             VARCHAR(500),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMPTZ,
    created_by          VARCHAR(100),
    updated_by          VARCHAR(100)
);

CREATE INDEX idx_assignments_driver ON driver_assignments(driver_id);
CREATE INDEX idx_assignments_vehicle ON driver_assignments(vehicle_id);
CREATE INDEX idx_assignments_trip ON driver_assignments(trip_id);
CREATE INDEX idx_assignments_active ON driver_assignments(released_at) WHERE released_at IS NULL;

-- ============================================================
-- 10. AUDIT LOG
-- ============================================================

CREATE TABLE audit_logs (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    entity_type         VARCHAR(50) NOT NULL,
    entity_id           UUID NOT NULL,
    action              VARCHAR(50) NOT NULL,
    description         VARCHAR(500),
    old_value           TEXT,
    new_value           TEXT,
    performed_by        VARCHAR(100),
    performed_at        TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_logs(action);
CREATE INDEX idx_audit_performer ON audit_logs(performed_by);
CREATE INDEX idx_audit_time ON audit_logs(performed_at);
