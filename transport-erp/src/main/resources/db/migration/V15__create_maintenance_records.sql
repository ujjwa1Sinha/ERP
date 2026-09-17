CREATE TABLE maintenance_records (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    maintenance_type VARCHAR(50) NOT NULL,
    service_date TIMESTAMP WITH TIME ZONE NOT NULL,
    odometer_reading DECIMAL(12, 2) NOT NULL,
    vendor VARCHAR(200),
    cost DECIMAL(15, 2) NOT NULL,
    description TEXT,
    next_service_date TIMESTAMP WITH TIME ZONE,
    next_service_odometer DECIMAL(12, 2),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_maintenance_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id)
);

CREATE INDEX IF NOT EXISTS idx_maintenance_records_vehicle_id ON maintenance_records(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_records_service_date ON maintenance_records(service_date);
CREATE INDEX IF NOT EXISTS idx_maintenance_records_type ON maintenance_records(maintenance_type);
