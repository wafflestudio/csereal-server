CREATE TABLE council_file
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000',
    modified_at datetime(6) default '1999-01-01 00:00:00.000000',
    type        enum ('RULE', 'MEETING_MINUTE') not null,
    `key`         varchar(255) not null
);

CREATE UNIQUE INDEX UK_council_file_type_key ON council_file (type, `key`);

ALTER TABLE attachment
    ADD COLUMN council_file_id BIGINT default NULL,
    ADD CONSTRAINT FK_attachment_council_file_id FOREIGN KEY (council_file_id) REFERENCES council_file (id)
;
