ALTER TABLE professor
    ADD COLUMN careers      TEXT    NULL;

UPDATE professor
SET careers = (
    SELECT JSON_ARRAYAGG(career.name)
    FROM career
    WHERE career.professor_id = professor.id
    ORDER BY career.id
)
WHERE EXISTS (
    SELECT * FROM career WHERE career.professor_id = professor.id
);
