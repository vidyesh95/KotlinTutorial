-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 02 — Constraints and keys
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : make the database REFUSE bad data: primary keys, NOT NULL, UNIQUE,
--         CHECK and foreign keys (with ON DELETE behaviour).
--  Run  : the green ▶ in lesson02_constraints_and_keys/ConstraintsAndKeys.kt
--  Next : lesson 03 — CRUD: insert, select, update, delete
--
-- 🧒 Like I'm 5:
--    Constraints are the rules taped to the spreadsheet. "Everyone needs a
--    name tag" (NOT NULL). "No two kids may have the same library card number"
--    (UNIQUE / PRIMARY KEY). "Age must be at least 0" (CHECK). The database is
--    a strict teacher: break a rule and your row is sent back.
--
-- Why in the DATABASE and not only in Kotlin? Because many programs (and
-- humans with SQL consoles) write to the same tables. The database is the
-- last line of defence — it sees EVERY write.

-- §1. PRIMARY KEY — every row's unique name tag
-- In SQLite, `INTEGER PRIMARY KEY` is special: it becomes the row's built-in
-- id (the "rowid"). Leave it out of the INSERT and SQLite picks the next number.
CREATE TABLE members (
    id    INTEGER PRIMARY KEY,
    name  TEXT NOT NULL,
    email TEXT NOT NULL UNIQUE
);

INSERT INTO members (name, email) VALUES ('Ada', 'ada@example.com'), ('Alan', 'alan@example.com');
INSERT INTO members (id, name, email) VALUES (10, 'Grace', 'grace@example.com');
INSERT INTO members (name, email) VALUES ('Linus', 'linus@example.com');
SELECT id, name FROM members;

-- expect error
INSERT INTO members (id, name, email) VALUES (1, 'Copycat', 'copy@example.com');

-- ⚠️ Gotcha: without AUTOINCREMENT, SQLite may REUSE the id of a deleted
-- LAST row. AUTOINCREMENT forbids reuse but costs a little speed. You rarely
-- need it — real apps don't expose or recycle ids in ways where it matters.
DELETE FROM members WHERE id = 11;
INSERT INTO members (name, email) VALUES ('Margaret', 'margaret@example.com');
SELECT id, name FROM members WHERE name = 'Margaret';

-- Natural key vs surrogate key:
--  • natural   = a real-world value that is unique (email, ISBN). Can CHANGE!
--  • surrogate = a meaningless id (1, 2, 3… or a UUID). Never changes.
-- Use a surrogate primary key, and protect natural keys with UNIQUE.
-- UUID ids are popular for public APIs (not guessable). SQLite stores them as
-- TEXT, e.g. id TEXT PRIMARY KEY with '0f8fad5b-d9cb-469f-a165-70867728950e'.

-- §2. NOT NULL and UNIQUE
-- expect error
INSERT INTO members (name, email) VALUES (NULL, 'nobody@example.com');
-- expect error
INSERT INTO members (name, email) VALUES ('Ada Again', 'ada@example.com');

-- UNIQUE can span several columns: "one vote per member per poll".
-- A PRIMARY KEY can too ("composite key") — order_items in the bookstore uses (order_id, book_id).
CREATE TABLE votes (
    poll_id   INTEGER NOT NULL,
    member_id INTEGER NOT NULL,
    choice    TEXT    NOT NULL,
    PRIMARY KEY (poll_id, member_id)
);
INSERT INTO votes VALUES (1, 1, 'yes'), (1, 2, 'no'), (2, 1, 'no');
-- expect error
INSERT INTO votes VALUES (1, 1, 'changed my mind');

-- ⚠️ Gotcha: UNIQUE allows many NULLs (NULL is "unknown", and two unknowns
-- aren't considered equal). Postgres behaves the same by default.

-- §3. CHECK — any rule you can write as an expression
CREATE TABLE products (
    id          INTEGER PRIMARY KEY,
    name        TEXT    NOT NULL CHECK (length(name) > 0),
    price_cents INTEGER NOT NULL CHECK (price_cents >= 0),
    status      TEXT    NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'active', 'archived'))
);
INSERT INTO products (name, price_cents) VALUES ('Pen', 150);
-- expect error
INSERT INTO products (name, price_cents) VALUES ('Broken', -5);
-- expect error
INSERT INTO products (name, price_cents, status) VALUES ('Mug', 900, 'deleted');
SELECT * FROM products;

-- §4. FOREIGN KEY — "this label must point to something that exists"
-- 🧒 Like I'm 5:
--    In a library every book has a shelf label like "Shelf 7". A foreign key
--    is the rule "Shelf 7 must really exist". You can't label a book with a
--    shelf that isn't there — and you must decide what happens to the books
--    when someone removes a shelf.
--
-- ⚠️ SQLite only enforces foreign keys after `PRAGMA foreign_keys = ON`
-- (per connection). Our runner switches it on for you. Postgres always enforces.
CREATE TABLE shelves (
    id   INTEGER PRIMARY KEY,
    name TEXT NOT NULL
);
CREATE TABLE library_books (
    id       INTEGER PRIMARY KEY,
    title    TEXT NOT NULL,
    shelf_id INTEGER REFERENCES shelves (id) ON DELETE SET NULL
);
CREATE TABLE loans (
    id      INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL REFERENCES library_books (id) ON DELETE RESTRICT,
    member  TEXT    NOT NULL
);
CREATE TABLE book_notes (
    id      INTEGER PRIMARY KEY,
    book_id INTEGER NOT NULL REFERENCES library_books (id) ON DELETE CASCADE,
    note    TEXT    NOT NULL
);

INSERT INTO shelves VALUES (1, 'Sci-Fi'), (2, 'Classics');
INSERT INTO library_books VALUES (1, 'Dune', 1), (2, 'Emma', 2), (3, 'Foundation', 1);
-- expect error
INSERT INTO library_books VALUES (4, 'Ghost Book', 99);

INSERT INTO loans VALUES (1, 1, 'ada');
INSERT INTO book_notes VALUES (1, 2, 'first edition'), (2, 2, 'slightly torn');

-- ON DELETE RESTRICT: can't delete a book that is still on loan.
-- (SQLite reports RESTRICT through an internal trigger, hence the odd
--  SQLITE_CONSTRAINT_TRIGGER code — the message still says FOREIGN KEY.)
-- expect error
DELETE FROM library_books WHERE id = 1;

-- ON DELETE CASCADE: deleting Emma deletes her notes too.
DELETE FROM library_books WHERE id = 2;
SELECT count(*) AS notes_left FROM book_notes;

-- ON DELETE SET NULL: removing the Sci-Fi shelf keeps the books, shelf becomes NULL.
DELETE FROM shelves WHERE id = 1;
SELECT id, title, shelf_id FROM library_books;

-- Which to choose?
--  CASCADE  — child can't live without parent (order → order_items, user → sessions)
--  RESTRICT — deleting the parent would lose something important (book on loan)
--  SET NULL — child survives on its own (book without a shelf)

-- 🐘 Postgres note:
--  • ids: `id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY` (modern) or
--    `bigserial` (older). Postgres never reuses identity values.
--  • UUIDs: `id uuid PRIMARY KEY DEFAULT gen_random_uuid()` — a real 16-byte type.
--  • Constraints can be DEFERRABLE (checked at COMMIT instead of per statement),
--    handy when inserting rows that reference each other.
--  • Constraint errors carry names, e.g. `members_email_key`: name your
--    constraints (`CONSTRAINT uq_members_email UNIQUE (email)`) so app code
--    can map them to nice API errors ("email already taken").

-- Recap
--  • PRIMARY KEY = unique row id (INTEGER PRIMARY KEY = SQLite rowid).
--  • NOT NULL, UNIQUE (also multi-column), CHECK (any expression).
--  • FOREIGN KEY + ON DELETE CASCADE / RESTRICT / SET NULL.
--  • Let the database refuse bad data — it sees every write.
