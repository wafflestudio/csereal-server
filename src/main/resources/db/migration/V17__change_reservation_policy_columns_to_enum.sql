ALTER TABLE reservation
    DROP COLUMN reservation_type,
    ADD COLUMN reservation_type ENUM(
        'ONE_TIME',
        'REGULAR',
        'UNRESTRICTED'
    ) NULL;

ALTER TABLE reserve_term
    DROP CHECK chk_reserve_term_metadata_pair,
    DROP INDEX uk_reserve_term_year_type,
    DROP COLUMN term_type,
    ADD COLUMN term_type ENUM(
        'WINTER',
        'FIRST_SEMESTER',
        'SUMMER',
        'SECOND_SEMESTER'
    ) NULL,
    ADD CONSTRAINT chk_reserve_term_metadata_pair CHECK (
        (term_year IS NULL AND term_type IS NULL)
        OR (term_year IS NOT NULL AND term_type IS NOT NULL)
    ),
    ADD UNIQUE INDEX uk_reserve_term_year_type (term_year, term_type);
