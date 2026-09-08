-- ============================================================
-- V6__create_trips_module.sql
-- Phase 2: Trip / Dispatch Management
-- ============================================================

-- ============================================================
-- 1. TRIPS
-- ============================================================

CREATE TABLE trips (
    id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_number             VARCHAR(30) NOT NULL UNIQUE,
    vehicle_id              UUID REFERENCES vehicles(id),
    primary_driver_id       UUID REFERENCES drivers(id),
    secondary_driver_id     UUID REFERENCES drivers(id),
    source_branch_id        UUID REFERENCES branches(id),
    destination_branch_id   UUID REFERENCES branches(id),
    source                  VARCHAR(200) NOT NULL,
    destination             VARCHAR(200) NOT NULL,
    planned_departure       TIMESTAMPTZ,
    planned_arrival         TIMESTAMPTZ,
    actual_departure        TIMESTAMPTZ,
    actual_arrival          TIMESTAMPTZ,
    status                  VARCHAR(25) NOT NULL DEFAULT 'PLANNED',
    trip_type               VARCHAR(30),
    distance_planned        NUMERIC(10,2),
    distance_actual         NUMERIC(10,2),
    remarks                 TEXT,
    created_at              TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMPTZ,
    created_by              VARCHAR(100),
    updated_by              VARCHAR(100)
);

CREATE INDEX idx_trips_number ON trips(trip_number);
CREATE INDEX idx_trips_status ON trips(status);
CREATE INDEX idx_trips_vehicle ON trips(vehicle_id);
CREATE INDEX idx_trips_primary_driver ON trips(primary_driver_id);
CREATE INDEX idx_trips_source_branch ON trips(source_branch_id);
CREATE INDEX idx_trips_dest_branch ON trips(destination_branch_id);
CREATE INDEX idx_trips_planned_departure ON trips(planned_departure);

-- ============================================================
-- 2. TRIP EVENTS (Timeline)
-- ============================================================

CREATE TABLE trip_events (
    id                  UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    trip_id             UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    event_type          VARCHAR(30) NOT NULL,
    event_timestamp     TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    latitude            NUMERIC(10,7),
    longitude           NUMERIC(10,7),
    remarks             VARCHAR(500),
    created_by          VARCHAR(100),
    created_at          TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trip_events_trip ON trip_events(trip_id);
CREATE INDEX idx_trip_events_type ON trip_events(event_type);
CREATE INDEX idx_trip_events_timestamp ON trip_events(event_timestamp);

-- ============================================================
-- 3. Add FK from driver_assignments.trip_id → trips.id
-- ============================================================

ALTER TABLE driver_assignments
    ADD CONSTRAINT fk_assignments_trip
    FOREIGN KEY (trip_id) REFERENCES trips(id);
