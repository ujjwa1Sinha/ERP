-- V5: Flatten driver_licenses + driver_emergency_contacts into drivers table
--     and drop the documents table entirely

-- ── 1. Add flattened license columns ───────────────────────────────────────
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_number          VARCHAR(30);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_type            VARCHAR(20);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_issuing_authority VARCHAR(100);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_issue_date      DATE;
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS license_expiry_date     DATE;

-- ── 2. Migrate existing primary license row → drivers ──────────────────────
UPDATE drivers d
SET
    license_number            = dl.license_number,
    license_type              = dl.license_type,
    license_issuing_authority = dl.issuing_authority,
    license_issue_date        = dl.issue_date,
    license_expiry_date       = dl.expiry_date
FROM driver_licenses dl
WHERE dl.driver_id = d.id
  AND dl.is_primary = TRUE;

-- ── 3. Add flattened emergency contact columns ──────────────────────────────
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS ec_name             VARCHAR(100);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS ec_relationship     VARCHAR(50);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS ec_phone            VARCHAR(15);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS ec_alternate_phone  VARCHAR(15);
ALTER TABLE drivers ADD COLUMN IF NOT EXISTS ec_address          VARCHAR(500);

-- ── 4. Migrate existing primary emergency contact row → drivers ─────────────
UPDATE drivers d
SET
    ec_name            = dec.name,
    ec_relationship    = dec.relationship,
    ec_phone           = dec.phone,
    ec_alternate_phone = dec.alternate_phone,
    ec_address         = dec.address
FROM driver_emergency_contacts dec
WHERE dec.driver_id = d.id
  AND dec.is_primary = TRUE;

-- ── 5. Drop old child tables ────────────────────────────────────────────────
DROP TABLE IF EXISTS driver_licenses;
DROP TABLE IF EXISTS driver_emergency_contacts;

-- ── 6. Drop documents table (insurance data already lives in vehicles) ──────
DROP TABLE IF EXISTS documents;
