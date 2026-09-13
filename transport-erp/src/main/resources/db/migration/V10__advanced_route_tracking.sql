ALTER TABLE trips
ADD COLUMN route_polyline TEXT,
ADD COLUMN deviation_alert_sent BOOLEAN DEFAULT FALSE,
ADD COLUMN last_halt_start_time TIMESTAMP,
ADD COLUMN frequent_halts_count INT DEFAULT 0,
ADD COLUMN last_halt_alert_time TIMESTAMP;
