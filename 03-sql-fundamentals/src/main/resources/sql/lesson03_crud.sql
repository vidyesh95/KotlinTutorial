-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 03 — CRUD: Create, Read, Update, Delete
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : the four everyday operations — INSERT, SELECT, UPDATE, DELETE —
--         with filtering, sorting, paging and CASE.
--  Run  : the green ▶ in lesson03_crud/Crud.kt (uses the bookstore database;
--         see resources/sql/bookstore.sql for the tables)
--  Next : lesson 04 — joins
--
-- 🧒 Like I'm 5:
--    The database is a toy box. CREATE = put a toy in. READ = look for toys
--    ("all the red cars"). UPDATE = paint a toy. DELETE = throw a toy away.
--    Almost every backend endpoint is one of these four.
--
--    POST /books → INSERT · GET /books → SELECT · PUT /books/1 → UPDATE · DELETE /books/1 → DELETE

-- §1. SELECT: pick columns, name them, compute things
SELECT title, price_cents FROM books LIMIT 3;

-- AS gives a column a new name (an "alias"). Expressions are allowed.
-- ⚠️ Gotcha: price_cents / 100 would be INTEGER division (1299 / 100 = 12).
-- Divide by 100.0 to get a decimal.
SELECT title,
       price_cents / 100 AS wrong_euros,
       price_cents / 100.0 AS euros,
       stock * price_cents AS stock_value_cents
FROM books
LIMIT 3;

-- §2. WHERE: filter rows
SELECT title, price_cents FROM books WHERE price_cents < 1000;

-- AND / OR / NOT. Use parentheses — AND binds tighter than OR.
SELECT title, published_year, stock
FROM books
WHERE (published_year < 1960 OR published_year > 2000) AND stock > 5;

-- IN (list), BETWEEN (inclusive on both ends), LIKE (% = any text, _ = one char)
SELECT title FROM books WHERE author_id IN (1, 3);
SELECT title, published_year FROM books WHERE published_year BETWEEN 1960 AND 1970;
SELECT title FROM books WHERE title LIKE 'The %';

-- NULL needs IS NULL — `= NULL` never matches anything (lesson 01).
SELECT title FROM books WHERE category_id = NULL;
SELECT title FROM books WHERE category_id IS NULL;

-- §3. ORDER BY, LIMIT, OFFSET, DISTINCT
-- Without ORDER BY, row order is NOT guaranteed. Always sort when order matters.
SELECT title, price_cents FROM books ORDER BY price_cents DESC LIMIT 3;

-- Several sort keys: first by author, then by year within each author.
SELECT author_id, published_year, title FROM books WHERE author_id <= 2 ORDER BY author_id, published_year DESC;

-- NULLs sort FIRST in ascending order in SQLite. Say what you want explicitly:
SELECT title, published_year FROM books ORDER BY published_year NULLS LAST LIMIT 3;
SELECT title, published_year FROM books ORDER BY published_year DESC NULLS LAST LIMIT 2;

-- Paging: page 2 with 4 books per page → skip 4, take 4.
-- (Lesson 06+ and the capstone show "keyset" paging, which stays fast on big tables.)
SELECT id, title FROM books ORDER BY id LIMIT 4 OFFSET 4;

SELECT DISTINCT country FROM authors ORDER BY country;

-- §4. CASE — if/else inside a query
SELECT title,
       stock,
       CASE
           WHEN stock = 0 THEN 'sold out'
           WHEN stock < 5 THEN 'only a few left'
           ELSE 'in stock'
       END AS availability
FROM books
ORDER BY stock
LIMIT 5;

-- §5. INSERT … RETURNING
-- Multi-row INSERT is one statement (faster, and all-or-nothing).
INSERT INTO authors (name, country) VALUES ('N. K. Jemisin', 'USA'), ('Terry Pratchett', 'UK');

-- RETURNING hands back the new rows — e.g. the generated id — without a second query.
INSERT INTO books (title, author_id, category_id, price_cents, published_year, stock)
VALUES ('The Fifth Season', 10, 3, 1399, 2015, 9)
RETURNING id, title;

-- §6. UPDATE … WHERE
UPDATE books SET stock = stock + 10 WHERE id = 3;
UPDATE books SET price_cents = price_cents * 90 / 100 WHERE category_id = 4 RETURNING title, price_cents;

-- ⚠️ Gotcha: UPDATE without WHERE changes EVERY row. Classic production
-- disaster. Habit: write the WHERE first, or run the same WHERE as a SELECT
-- before running the UPDATE. Many SQL clients warn you; the database won't.

-- §7. DELETE
-- Deleting a book that appears in orders is refused by the foreign key:
-- expect error
DELETE FROM books WHERE id = 1;

DELETE FROM books WHERE id = 15 RETURNING title;
DELETE FROM authors WHERE id IN (10, 11);
SELECT count(*) AS authors_left FROM authors;

-- 🏗️ In the real world: apps often prefer a "soft delete" — a `deleted_at`
-- column that is set instead of removing the row — so data can be restored
-- and history stays intact. Every query then needs `WHERE deleted_at IS NULL`.

-- 🐘 Postgres note:
--  • `ILIKE` = case-insensitive LIKE. (SQLite's LIKE is already
--    case-insensitive for ASCII letters; Postgres LIKE is case-SENSITIVE.)
--  • Postgres sorts NULLs LAST in ascending order (SQLite: FIRST). Writing
--    NULLS FIRST / NULLS LAST explicitly works in both.
--  • RETURNING works the same (Postgres had it first).
--  • Integer division is the same trap: 1299 / 100 = 12.

-- Recap
--  • SELECT cols FROM t WHERE … ORDER BY … LIMIT … OFFSET …
--  • IN, BETWEEN, LIKE, IS NULL, CASE, DISTINCT, aliases with AS.
--  • INSERT/UPDATE/DELETE … RETURNING saves a round trip.
--  • UPDATE/DELETE without WHERE hits every row.
