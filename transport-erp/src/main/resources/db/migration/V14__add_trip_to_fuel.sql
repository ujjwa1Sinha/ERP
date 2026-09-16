ALTER TABLE fuel_transactions
ADD COLUMN trip_id UUID REFERENCES trips(id) ON DELETE SET NULL;

CREATE INDEX IF NOT EXISTS idx_fuel_transactions_trip_id ON fuel_transactions(trip_id);
