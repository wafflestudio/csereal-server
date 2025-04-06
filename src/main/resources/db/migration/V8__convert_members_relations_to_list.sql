ALTER TABLE professor
    ADD COLUMN educations       TEXT    NULL,
    ADD COLUMN research_areas   TEXT    NULL;
ALTER TABLE staff
    ADD COLUMN tasks            TEXT    NULL;

UPDATE professor
SET educations = (
    SELECT JSON_ARRAYAGG(education.name)
    FROM education
    WHERE education.professor_id = professor.id
    ORDER BY education.id
)
WHERE EXISTS (
    SELECT * FROM education WHERE education.professor_id = professor.id
);

UPDATE professor
SET research_areas = (
    SELECT JSON_ARRAYAGG(research_area.name)
    FROM research_area
    WHERE research_area.professor_id = professor.id
    ORDER BY research_area.id
)
WHERE EXISTS (
    SELECT * FROM research_area WHERE research_area.professor_id = professor.id
);

UPDATE staff
SET tasks = (
    SELECT JSON_ARRAYAGG(task.name)
    FROM task
    WHERE task.staff_id = staff.id
    ORDER BY task.id
)
WHERE EXISTS (
    SELECT * FROM task WHERE task.staff_id = staff.id
);

DROP TABLE education;
DROP TABLE research_area;
DROP TABLE task;
