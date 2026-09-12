ALTER TABLE trips ADD COLUMN source_lat numeric(10, 6);
ALTER TABLE trips ADD COLUMN source_lng numeric(10, 6);
ALTER TABLE trips ADD COLUMN dest_lat numeric(10, 6);
ALTER TABLE trips ADD COLUMN dest_lng numeric(10, 6);
ALTER TABLE trips ADD COLUMN wandered_alert_sent boolean DEFAULT false;
