-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 08 — Indexes and query plans
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : see HOW the database runs a query (EXPLAIN QUERY PLAN), make slow
--         lookups fast with indexes, and know when an index can't help.
--  Run  : the green ▶ in lesson08_indexes_and_query_plans/IndexesAndQueryPlans.kt
--  Next : lesson 09 — upserts, JSON and dates
--
-- 🧒 Like I'm 5:
--    You want every page of a big book that mentions "dragons".
--    Without help you read the WHOLE book, page by page (a "full scan").
--    With the INDEX at the back of the book you look up "dragons" — it's
--    sorted A–Z, so you find it instantly — and it tells you "pages 12, 87".
--    A database index is exactly that: a sorted list of values pointing to rows.
--    The price: every time you add a page, someone must update the index too.
--
-- ⚠️ Plans below come from the SQLite version bundled with our sqlite-jdbc.
-- Other versions may word them slightly differently; the ideas are the same.

-- §1. A bigger table to play with
-- 10 000 generated customers (recursive CTE from lesson 06). Plans only get
-- interesting when tables have more than a handful of rows.
CREATE TABLE big_customers (
    id         INTEGER PRIMARY KEY,
    email      TEXT    NOT NULL,
    city       TEXT    NOT NULL,
    vip        INTEGER NOT NULL,
    created_at TEXT    NOT NULL
);

INSERT INTO big_customers (id, email, city, vip, created_at)
WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 10000)
SELECT i,
       'user' || i || '@example.com',
       CASE i % 4 WHEN 0 THEN 'London' WHEN 1 THEN 'Paris' WHEN 2 THEN 'Berlin' ELSE 'Madrid' END,
       CASE WHEN i % 100 = 0 THEN 1 ELSE 0 END,
       date('2025-01-01', '+' || (i % 365) || ' days')
FROM n;

-- §2. Reading a plan: SCAN vs SEARCH
-- EXPLAIN QUERY PLAN shows the strategy without running the query.
--   SCAN   = read every row (fine for tiny tables, slow for big ones)
--   SEARCH = jump straight to matching rows using an index
-- Read the `detail` column (the others are internal ids).
-- Looking up by the primary key is always a SEARCH (the table IS sorted by it):
EXPLAIN QUERY PLAN SELECT * FROM big_customers WHERE id = 42;

-- Looking up by email: no index → SCAN all 10 000 rows.
EXPLAIN QUERY PLAN SELECT * FROM big_customers WHERE email = 'user42@example.com';

-- §3. CREATE INDEX — and the plan changes
CREATE INDEX idx_big_customers_email ON big_customers (email);
EXPLAIN QUERY PLAN SELECT * FROM big_customers WHERE email = 'user42@example.com';
SELECT id, email, city FROM big_customers WHERE email = 'user42@example.com';

-- Emails must be unique anyway — a UNIQUE index enforces that AND speeds lookups.
DROP INDEX idx_big_customers_email;
CREATE UNIQUE INDEX ux_big_customers_email ON big_customers (email);
-- expect error
INSERT INTO big_customers (email, city, vip, created_at) VALUES ('user42@example.com', 'Rome', 0, '2025-01-01');

-- §4. Index your foreign keys
-- Joins and "all books of author X" filter on the foreign key column.
-- SQLite (and Postgres!) do NOT index foreign keys automatically.
EXPLAIN QUERY PLAN SELECT title FROM books WHERE author_id = 2;
CREATE INDEX idx_books_author_id ON books (author_id);
EXPLAIN QUERY PLAN SELECT title FROM books WHERE author_id = 2;

-- §5. Composite indexes and the "leftmost prefix" rule
-- An index on (city, created_at) is sorted by city FIRST, then by date inside
-- each city — like a phone book sorted by last name, then first name.
CREATE INDEX idx_big_customers_city_created ON big_customers (city, created_at);

-- Uses both columns: find the city, then the date range inside it.
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE city = 'Paris' AND created_at >= '2025-12-01';
-- Uses the leftmost column alone: still fine.
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE city = 'Paris';
-- Only the SECOND column: the phone book can't find "everyone named John" quickly.
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE created_at = '2025-06-01';
-- ⚠️ Read carefully: "SCAN … USING COVERING INDEX" is still a SCAN — it reads
-- EVERY entry, just from the (smaller) index instead of the table. Only
-- "SEARCH" means it jumped straight to the matching rows.

-- §6. Covering indexes — answer from the index alone
-- If every column the query needs is IN the index, SQLite never touches the
-- table: "COVERING INDEX". (The index above contains city, created_at and,
-- implicitly, the rowid/id.)
EXPLAIN QUERY PLAN SELECT id, created_at FROM big_customers WHERE city = 'Berlin';
-- Ask for a column NOT in the index → it must visit the table for each row.
EXPLAIN QUERY PLAN SELECT id, email FROM big_customers WHERE city = 'Berlin';

-- §7. Partial and expression indexes
-- Partial: index only the rows you query often (1% of customers are VIP).
CREATE INDEX idx_big_customers_vip ON big_customers (created_at) WHERE vip = 1;
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE vip = 1 AND created_at > '2025-06-01';

-- Expression: people type emails in random case. Index lower(email) and
-- query with exactly the same expression.
CREATE INDEX idx_big_customers_email_lower ON big_customers (lower(email));
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE lower(email) = 'user7@example.com';

-- §8. When an index can't help
-- (a) A function wrapped around the column hides it from a normal index:
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE substr(email, 1, 6) = 'user42';
-- (b) A leading wildcard: the index is sorted by the START of the text.
EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE email LIKE '%42@example.com';
-- Both are SCANs again (of the email index — smaller than the table, but every entry).
-- (c) Low selectivity: "vip = 0" matches 99% of rows. Even with an index, the
--     planner may rightly prefer a scan — jumping around costs more than reading.
SELECT vip, count(*) AS customers FROM big_customers GROUP BY vip;

-- §9. The cost: writes and space
-- Every INSERT/UPDATE/DELETE must also update EVERY index on the table.
-- List what we created (one table, many indexes = slower writes):
SELECT name FROM sqlite_schema WHERE type = 'index' AND tbl_name = 'big_customers' ORDER BY name;
-- Rule of thumb: index columns you filter, join or sort by OFTEN; primary
-- keys and UNIQUE constraints already have one; measure before adding more.

-- 🏗️ In the real world: when an endpoint is slow, the first move is
-- EXPLAIN on its query. A missing index on a foreign key or on the column
-- in WHERE is the #1 cause of slow backends.

-- 🐘 Postgres note:
--   • EXPLAIN shows the plan; EXPLAIN (ANALYZE, BUFFERS) RUNS the query and
--     shows real timings and row counts — your best friend for tuning.
--   • "Seq Scan" ≈ SCAN, "Index Scan"/"Index Only Scan" ≈ SEARCH/COVERING.
--   • Index types: B-tree (default, like SQLite), GIN (jsonb, arrays,
--     full-text), GiST (geo), BRIN (huge append-only tables, e.g. by date).
--   • CREATE INDEX CONCURRENTLY builds an index without locking writes —
--     use it on production tables. INCLUDE (col) makes covering indexes.
--   • Run ANALYZE so the planner has fresh statistics (SQLite has ANALYZE too).

-- Recap
--  • EXPLAIN QUERY PLAN: SCAN = read everything, SEARCH … USING INDEX = jump.
--  • Index foreign keys and frequent WHERE / JOIN / ORDER BY columns.
--  • Composite (a, b): works for a, or a + b — not b alone. COVERING = no table visit.
--  • Partial and expression indexes for special cases; functions & '%x' defeat indexes.
--  • Indexes cost write speed and space — add them for real queries, not "just in case".
