ALTER TABLE reservation
    ADD COLUMN reservation_type VARCHAR(20) NULL;

ALTER TABLE reserve_term
    ADD COLUMN term_year INT NULL,
    ADD COLUMN term_type VARCHAR(32) NULL;

CREATE UNIQUE INDEX uk_reserve_term_year_type
    ON reserve_term (term_year, term_type);
