ALTER TABLE reservation
    ADD COLUMN reservation_type VARCHAR(20) NULL;

UPDATE reservation
SET reservation_type = CASE
    WHEN recurring_weeks = 1 THEN 'AD_HOC'
    WHEN recurring_weeks > 1 THEN 'REGULAR'
    ELSE NULL
END
WHERE reservation_type IS NULL;

ALTER TABLE reserve_term
    ADD COLUMN term_year INT NULL,
    ADD COLUMN term_type VARCHAR(32) NULL;

CREATE UNIQUE INDEX uk_reserve_term_year_type
    ON reserve_term (term_year, term_type);
