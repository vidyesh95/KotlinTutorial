-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 01 — Tables and types
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : create tables, understand column types (and SQLite's relaxed take on
--         them), NULL, DEFAULT, and changing a table later.
--  Run  : the green ▶ in lesson01_tables_and_types/TablesAndTypes.kt
--         (or paste into any SQLite tool: `sqlite3` CLI, DB Browser, IntelliJ Database tab)
--  Next : lesson 02 — constraints and keys
--
-- 🧒 Like I'm 5:
--    A table is a spreadsheet with RULES. Each column has a name and says what
--    may go in it ("only numbers here", "this one can't be empty"). Each row is
--    one thing — one note, one book, one customer. The database is the folder
--    that holds all the spreadsheets and makes sure everybody follows the rules.

-- §1. Creating a table
-- CREATE TABLE name (column TYPE constraints, …). A row needs a way to be
-- identified: `id INTEGER PRIMARY KEY` (lesson 02 explains keys properly).
-- DEFAULT gives a value when an INSERT doesn't mention the column.
-- (Real apps often use DEFAULT CURRENT_TIMESTAMP; we use a fixed date so the
--  output never changes.)
CREATE TABLE notes (
    id         INTEGER PRIMARY KEY,
    title      TEXT    NOT NULL,
    body       TEXT,
    pinned     INTEGER NOT NULL DEFAULT 0,
    created_at TEXT    NOT NULL DEFAULT '2026-01-01T00:00:00Z'
);

INSERT INTO notes (title, body) VALUES ('Learn SQL', 'start with tables');
INSERT INTO notes (title, pinned) VALUES ('Buy milk', 1);
SELECT * FROM notes;

-- §2. Types — SQLite is relaxed, Postgres is strict
-- SQLite has only 5 *storage classes*: NULL, INTEGER, REAL, TEXT, BLOB.
-- A column's declared type is just a preference ("type affinity"): SQLite
-- converts when it safely can, and otherwise… stores whatever you gave it!
CREATE TABLE loose (n INTEGER, label TEXT);
INSERT INTO loose (n, label) VALUES
    (42, 'a number'),
    ('42', 'numeric text, converted'),
    ('hello', 'text kept as TEXT!'),
    (4.0, 'real 4.0 becomes 4'),
    (4.5, 'real stays real');
SELECT n, typeof(n) AS stored_as, label FROM loose;

-- ⚠️ Gotcha: 'hello' in an INTEGER column is accepted. Add STRICT to a table
-- (SQLite 3.37+) to get Postgres-like checking:
CREATE TABLE strict_numbers (n INTEGER NOT NULL) STRICT;
INSERT INTO strict_numbers (n) VALUES ('42');
-- expect error
INSERT INTO strict_numbers (n) VALUES ('hello');

-- There is no BOOLEAN storage class: TRUE is 1, FALSE is 0.
SELECT TRUE AS yes, FALSE AS no, typeof(TRUE) AS stored_as, 3 > 2 AS comparison;

-- 🐘 Postgres note — types you'll use there:
--   SQLite             Postgres
--   INTEGER            integer / bigint (use bigint for ids)
--   REAL               double precision (never for money)
--   (cents INTEGER)    numeric(12, 2) or bigint cents
--   TEXT               text (varchar(n) only if you need a limit)
--   INTEGER 0/1        boolean
--   TEXT (ISO-8601)    timestamptz   ← always "with time zone"
--   TEXT               uuid, jsonb (real types in Postgres)
--   BLOB               bytea
-- Postgres REJECTS 'hello' for an integer column: every table behaves like STRICT.

-- §3. NULL — "unknown / missing"
-- NULL is not zero and not ''. It means "no value". Any comparison with NULL
-- gives NULL (unknown) — so use IS NULL / IS NOT NULL, never = NULL.
SELECT title, body, body IS NULL AS no_body, coalesce(body, '(empty)') AS shown FROM notes;
SELECT NULL = NULL AS equals, NULL IS NULL AS is_null, NULL + 1 AS plus_one;

-- §4. Changing and inspecting tables
ALTER TABLE notes ADD COLUMN color TEXT DEFAULT 'yellow';
SELECT id, title, color FROM notes;

-- Every SQLite database describes itself in the `sqlite_schema` table:
SELECT name FROM sqlite_schema WHERE type = 'table' ORDER BY name;
SELECT name, type, "notnull", dflt_value, pk FROM pragma_table_info('notes');

DROP TABLE loose;
DROP TABLE IF EXISTS loose;   -- no error the second time thanks to IF EXISTS
SELECT count(*) AS tables_left FROM sqlite_schema WHERE type = 'table';

-- 🐘 Postgres note: inspect with `\d notes` in psql, or query
-- information_schema.columns. ALTER TABLE is much more powerful in Postgres
-- (ALTER COLUMN TYPE, DROP CONSTRAINT…); in SQLite, big changes mean
-- "create new table, copy rows, drop old, rename".

-- Recap
--  • CREATE TABLE with column types, NOT NULL, DEFAULT.
--  • SQLite: 5 storage classes + type affinity; add STRICT for real type checks.
--  • NULL = unknown. Test with IS NULL; replace with coalesce().
--  • ALTER TABLE … ADD COLUMN, DROP TABLE IF EXISTS, sqlite_schema / pragma_table_info.
