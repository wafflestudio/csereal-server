CREATE TABLE image_modal
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    created_at  datetime(6) default '1999-01-01 00:00:00.000000',
    modified_at datetime(6) default '1999-01-01 00:00:00.000000',

    title_ko VARCHAR(255),
    title_en VARCHAR(255),
    image_alt_ko    VARCHAR(255),
    image_alt_en    VARCHAR(255),
    display_until   datetime(6),
    external_link   VARCHAR(255),
    main_image_id BIGINT,

    CONSTRAINT FK_image_modal_main_image_main_image_id
        FOREIGN KEY (main_image_id) REFERENCES main_image (id)
);
