CREATE TABLE reserve_term
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000',
    modified_at datetime(6) default '1999-01-01 00:00:00.000000',

    apply_start_time datetime(6) NOT NULL,
    apply_end_time datetime(6) NOT NULL,
    term_start_time datetime(6) NOT NULL,
    term_end_time datetime(6) NOT NULL
);
