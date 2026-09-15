-- Add standard indexes for Users
CREATE INDEX IF NOT EXISTS idx_users_branch_id ON users(branch_id);
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

-- Add standard indexes for Drivers
CREATE INDEX IF NOT EXISTS idx_drivers_branch_id ON drivers(branch_id);
CREATE INDEX IF NOT EXISTS idx_drivers_status ON drivers(status);
CREATE INDEX IF NOT EXISTS idx_drivers_name ON drivers(name);

-- Add standard indexes for Vehicles
CREATE INDEX IF NOT EXISTS idx_vehicles_branch_id ON vehicles(branch_id);
CREATE INDEX IF NOT EXISTS idx_vehicles_status ON vehicles(status);
CREATE INDEX IF NOT EXISTS idx_vehicles_vehicle_type_id ON vehicles(vehicle_type_id);

-- Add standard indexes for Trips
CREATE INDEX IF NOT EXISTS idx_trips_status ON trips(status);
CREATE INDEX IF NOT EXISTS idx_trips_vehicle_id ON trips(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_trips_primary_driver_id ON trips(primary_driver_id);
CREATE INDEX IF NOT EXISTS idx_trips_source_branch_id ON trips(source_branch_id);
CREATE INDEX IF NOT EXISTS idx_trips_dest_branch_id ON trips(destination_branch_id);
CREATE INDEX IF NOT EXISTS idx_trips_planned_departure ON trips(planned_departure);

-- Add standard indexes for Driver Assignments
CREATE INDEX IF NOT EXISTS idx_driver_assignments_vehicle_id ON driver_assignments(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_driver_assignments_driver_id ON driver_assignments(driver_id);
CREATE INDEX IF NOT EXISTS idx_driver_assignments_trip_id ON driver_assignments(trip_id);
CREATE INDEX IF NOT EXISTS idx_driver_assignments_released_at ON driver_assignments(released_at);
CREATE INDEX IF NOT EXISTS idx_driver_assignments_assigned_at ON driver_assignments(assigned_at);
