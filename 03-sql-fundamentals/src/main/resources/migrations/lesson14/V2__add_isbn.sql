-- V2: "expand" step — add a new, NULLABLE column. Old code keeps working.
ALTER TABLE books ADD COLUMN isbn TEXT;
