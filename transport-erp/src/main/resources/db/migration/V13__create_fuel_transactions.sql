CREATE TABLE fuel_transactions (
    id UUID PRIMARY KEY,
    vehicle_id UUID NOT NULL,
    driver_id UUID,
    date TIMESTAMP WITH TIME ZONE NOT NULL,
    litres DECIMAL(10, 2) NOT NULL,
    price_per_litre DECIMAL(10, 2),
    total_amount DECIMAL(15, 2) NOT NULL,
    odometer_reading DECIMAL(12, 2),
    location VARCHAR(200),
    fuel_station VARCHAR(200),
    remarks TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_fuel_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id),
    CONSTRAINT fk_fuel_driver FOREIGN KEY (driver_id) REFERENCES drivers(id)
);

CREATE INDEX IF NOT EXISTS idx_fuel_transactions_vehicle_id ON fuel_transactions(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_fuel_transactions_driver_id ON fuel_transactions(driver_id);
CREATE INDEX IF NOT EXISTS idx_fuel_transactions_date ON fuel_transactions(date);
