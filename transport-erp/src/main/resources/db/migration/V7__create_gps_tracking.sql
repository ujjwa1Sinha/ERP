CREATE TABLE vehicle_locations (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    trip_id UUID,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    speed DECIMAL(5, 2),
    heading DECIMAL(5, 2),
    accuracy DECIMAL(8, 2),
    recorded_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(50),
    
    CONSTRAINT fk_vehicle_location_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_vehicle_location_trip FOREIGN KEY (trip_id) REFERENCES trips(id)
);

CREATE INDEX idx_vehicle_locations_vehicle_id ON vehicle_locations(vehicle_id);
CREATE INDEX idx_vehicle_locations_trip_id ON vehicle_locations(trip_id);
CREATE INDEX idx_vehicle_locations_recorded_at ON vehicle_locations(recorded_at);
