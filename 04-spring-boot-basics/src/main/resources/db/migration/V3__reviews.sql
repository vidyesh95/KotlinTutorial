-- Flyway migration V3 — used by exercise 05 (reviews with JdbcClient).
-- A NEW file instead of editing V1: V1 and V2 may already have run on someone's database.
CREATE TABLE reviews (
    id         INTEGER PRIMARY KEY,
    book_id    INTEGER NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text       TEXT,
    created_at TEXT    NOT NULL
);

CREATE INDEX reviews_book_id_idx ON reviews (book_id);
