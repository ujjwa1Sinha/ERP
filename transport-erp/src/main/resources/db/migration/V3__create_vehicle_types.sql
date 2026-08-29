-- V3: Create vehicle_types table and migrate FK (idempotent)

CREATE TABLE IF NOT EXISTS vehicle_types (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(50) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ,
    created_by VARCHAR(50),
    updated_by VARCHAR(50)
);

-- If table was created by Hibernate without the DEFAULT, add it explicitly
ALTER TABLE vehicle_types ALTER COLUMN id SET DEFAULT uuid_generate_v4();
ALTER TABLE vehicle_types ALTER COLUMN created_at SET DEFAULT NOW();

INSERT INTO vehicle_types (id, name, description) VALUES
(uuid_generate_v4(), 'BUS',    'Standard Bus'),
(uuid_generate_v4(), 'TRUCK',  'Cargo Truck'),
(uuid_generate_v4(), 'PICKUP', 'Pickup Truck'),
(uuid_generate_v4(), 'CAR',    'Light Motor Vehicle'),
(uuid_generate_v4(), 'CAB',    'Commercial Cab'),
(uuid_generate_v4(), 'OTHER',  'Other Vehicle Type')
ON CONFLICT (name) DO NOTHING;

-- Migrate vehicle_type column -> vehicle_type_id FK (only if not already done)
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name = 'vehicles' AND column_name = 'vehicle_type_id'
    ) THEN
        ALTER TABLE vehicles ADD COLUMN vehicle_type_id UUID;
        UPDATE vehicles v SET vehicle_type_id = vt.id FROM vehicle_types vt WHERE v.vehicle_type = vt.name;
        ALTER TABLE vehicles ALTER COLUMN vehicle_type_id SET NOT NULL;
        ALTER TABLE vehicles DROP COLUMN IF EXISTS vehicle_type;
        ALTER TABLE vehicles ADD CONSTRAINT fk_vehicles_type FOREIGN KEY (vehicle_type_id) REFERENCES vehicle_types(id);
        CREATE INDEX IF NOT EXISTS idx_vehicles_type_id ON vehicles(vehicle_type_id);
    END IF;
END $$;
