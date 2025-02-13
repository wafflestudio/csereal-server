ALTER TABLE council
    ADD COLUMN type          ENUM('INTRO', 'REPORT') NOT NULL,
    ADD COLUMN title         VARCHAR(255)            NOT NULL,
    ADD COLUMN description   MEDIUMTEXT              NOT NULL,
    ADD COLUMN main_image_id BIGINT                  NULL,
    ADD COLUMN sequence      INT                     NOT NULL,
    ADD COLUMN name          VARCHAR(255)            NOT NULL;

ALTER TABLE council
    ADD CONSTRAINT fk_council_main_image_main_image_id
        FOREIGN KEY (main_image_id) REFERENCES main_image (id);
