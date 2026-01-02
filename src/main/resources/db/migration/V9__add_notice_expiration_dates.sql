-- Add columns for pinned and important expiration dates to the notice table
ALTER TABLE notice
    ADD COLUMN `pinned_until` DATE NULL DEFAULT NULL COMMENT 'Pin expiration date',
    ADD COLUMN `important_until` DATE NULL DEFAULT NULL COMMENT 'Importance expiration date'; 