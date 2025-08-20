ALTER TABLE attachment
ADD COLUMN directory VARCHAR(255) NULL,
DROP CONSTRAINT UQ_attachment_filename,
ADD CONSTRAINT UQ_attachment_directory_filename UNIQUE (directory, filename);

ALTER TABLE main_image
ADD COLUMN directory VARCHAR(255) NULL,
DROP CONSTRAINT UQ_main_image_filename,
ADD CONSTRAINT UQ_main_image_directory_filename UNIQUE (directory, filename);
