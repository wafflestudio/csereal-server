ALTER TABLE reservation
    ADD COLUMN reservation_type VARCHAR(20) NULL;

ALTER TABLE reserve_term
    ADD COLUMN term_year INT NULL,
    ADD COLUMN term_type VARCHAR(32) NULL,
    ADD CONSTRAINT chk_reserve_term_metadata_pair CHECK (
        (term_year IS NULL AND term_type IS NULL)
        OR (term_year IS NOT NULL AND term_type IS NOT NULL)
    );

CREATE UNIQUE INDEX uk_reserve_term_year_type
    ON reserve_term (term_year, term_type);
