-- V1: the first version of the schema, plus some starting data.
CREATE TABLE books (
    id    INTEGER PRIMARY KEY,
    title TEXT NOT NULL
);
INSERT INTO books (title) VALUES ('Dune'), ('Emma');
