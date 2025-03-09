DROP TABLE IF EXISTS council;

CREATE TABLE council
(
    id          BIGINT AUTO_INCREMENT            NOT NULL,
    created_at  datetime(6) DEFAULT '1999-01-01' NULL,
    modified_at datetime(6) DEFAULT '1999-01-01' NULL,
    CONSTRAINT pk_council PRIMARY KEY (id)
);
