-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 10 — Normalization and schema design
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : turn one messy "spreadsheet" table into a clean set of related
--         tables (1NF → 2NF → 3NF), know when to break the rules on purpose,
--         and read a real-world auth schema (better-auth style).
--  Run  : the green ▶ in lesson10_normalization_and_design/NormalizationAndDesign.kt
--         (starts from an EMPTY database, not the bookstore)
--  Next : lesson 11 — JDBC from Kotlin
--
-- 🧒 Like I'm 5:
--    Imagine writing your home address on EVERY page of your diary. When you
--    move house you must fix 300 pages — and you'll miss some, so now the
--    diary says you live in two places. Smarter: write the address ONCE on
--    the first page, and on other pages just write "see page 1".
--    Normalization = store each fact once, and point to it (with ids).

-- §1. The messy starting point
-- One wide table, the way a spreadsheet export often looks:
CREATE TABLE orders_flat (
    order_id       INTEGER,
    customer_email TEXT,
    customer_name  TEXT,
    customer_city  TEXT,
    book_title     TEXT,
    author_name    TEXT,
    price_cents    INTEGER,
    quantity       INTEGER,
    tags           TEXT          -- comma-separated list 😬
);
INSERT INTO orders_flat VALUES
    (1, 'ada@example.com', 'Ada Lovelace', 'London', 'Dune', 'Frank Herbert', 1299, 1, 'gift,express'),
    (1, 'ada@example.com', 'Ada Lovelace', 'London', 'Emma', 'Jane Austen', 899, 2, 'gift,express'),
    (2, 'ada@example.com', 'Ada Lovelace', 'London', 'Dune', 'Frank Herbert', 1299, 1, ''),
    (3, 'alan@example.com', 'Alan Turing', 'Manchester', 'Dune', 'Frank Herbert', 1299, 3, 'express');
SELECT order_id, customer_email, customer_city, book_title, tags FROM orders_flat;

-- §2. What goes wrong ("anomalies")
-- UPDATE anomaly: Ada moves to Paris, but we only fix ONE of her rows…
UPDATE orders_flat SET customer_city = 'Paris' WHERE order_id = 2;
SELECT DISTINCT customer_email, customer_city FROM orders_flat WHERE customer_email = 'ada@example.com';
-- …and now the data contradicts itself. Ada lives in London AND Paris.
--
-- INSERT anomaly: we can't add a new customer (or book) who hasn't ordered
-- yet without inventing a fake order row.
-- DELETE anomaly: delete Alan's only order and we forget Alan exists.
--
-- And `tags` holds a LIST in one cell: "find all express orders" needs
-- LIKE '%express%' (slow, and would also match 'not-express').
SELECT DISTINCT order_id FROM orders_flat WHERE tags LIKE '%express%';

-- §3. First normal form (1NF): one value per cell, no repeating lists
-- Move the list into its own table: one row per (order, tag).
CREATE TABLE order_tags (order_id INTEGER NOT NULL, tag TEXT NOT NULL, PRIMARY KEY (order_id, tag));
INSERT INTO order_tags (order_id, tag)
SELECT DISTINCT f.order_id, t.value
FROM orders_flat f, json_each('["' || replace(f.tags, ',', '","') || '"]') t
WHERE f.tags <> '';
SELECT order_id FROM order_tags WHERE tag = 'express' ORDER BY order_id;

-- §4. Second normal form (2NF): every column depends on the WHOLE key
-- In orders_flat the key is (order_id, book_title). But customer_email
-- depends only on order_id, and author_name only on book_title — they
-- depend on PART of the key. Split them out so each fact has its own table:
--   orders(id, customer_…)          ← depends on order_id
--   books(id, title, author_…, price) ← depends on the book
--   order_items(order_id, book_id, quantity) ← depends on BOTH
--
-- §5. Third normal form (3NF): no facts about OTHER facts
-- customer_city describes the customer, not the order → customers table.
-- author_name describes the author, not the book → authors table.
-- Rule of thumb (Bill Kent): every column describes "the key, the whole key,
-- and nothing but the key".
CREATE TABLE customers (
    id         INTEGER PRIMARY KEY,
    email      TEXT NOT NULL UNIQUE,
    name       TEXT NOT NULL,
    city       TEXT
);
CREATE TABLE authors (id INTEGER PRIMARY KEY, name TEXT NOT NULL UNIQUE);
CREATE TABLE books (
    id          INTEGER PRIMARY KEY,
    title       TEXT    NOT NULL,
    author_id   INTEGER NOT NULL REFERENCES authors (id),
    price_cents INTEGER NOT NULL
);
CREATE TABLE orders (
    id          INTEGER PRIMARY KEY,
    customer_id INTEGER NOT NULL REFERENCES customers (id)
);
CREATE TABLE order_items (
    order_id         INTEGER NOT NULL REFERENCES orders (id),
    book_id          INTEGER NOT NULL REFERENCES books (id),
    quantity         INTEGER NOT NULL CHECK (quantity > 0),
    unit_price_cents INTEGER NOT NULL,             -- see §6: copied ON PURPOSE
    PRIMARY KEY (order_id, book_id)
);

-- Fill them from the flat table (each fact exactly once).
-- Ada's rows now DISAGREE about her city (§2's anomaly). No query can know
-- which is right — a human decides. We pick 'London' (then move her properly below).
INSERT INTO customers (email, name, city)
SELECT customer_email, customer_name, 'London' FROM orders_flat WHERE customer_email = 'ada@example.com' LIMIT 1;
INSERT INTO customers (email, name, city)
SELECT DISTINCT customer_email, customer_name, customer_city FROM orders_flat WHERE customer_email <> 'ada@example.com';
INSERT INTO authors (name) SELECT DISTINCT author_name FROM orders_flat ORDER BY author_name;
INSERT INTO books (title, author_id, price_cents)
SELECT DISTINCT f.book_title, a.id, f.price_cents FROM orders_flat f JOIN authors a ON a.name = f.author_name ORDER BY f.book_title;
INSERT INTO orders (id, customer_id)
SELECT DISTINCT f.order_id, c.id FROM orders_flat f JOIN customers c ON c.email = f.customer_email ORDER BY f.order_id;
INSERT INTO order_items (order_id, book_id, quantity, unit_price_cents)
SELECT f.order_id, b.id, f.quantity, f.price_cents FROM orders_flat f JOIN books b ON b.title = f.book_title;

-- Now moving house is ONE update, and JOINs rebuild the old view on demand:
UPDATE customers SET city = 'Paris' WHERE email = 'ada@example.com';
SELECT o.id AS order_id, c.email, c.city, b.title, a.name AS author, oi.quantity
FROM order_items oi
JOIN orders o ON o.id = oi.order_id
JOIN customers c ON c.id = o.customer_id
JOIN books b ON b.id = oi.book_id
JOIN authors a ON a.id = b.author_id
ORDER BY o.id, b.title;

-- And a customer without orders is no problem any more:
INSERT INTO customers (email, name) VALUES ('grace@example.com', 'Grace Hopper');
SELECT count(*) AS customers FROM customers;

-- §6. Denormalizing on purpose
-- Rules are for understanding; real schemas break them deliberately:
--  • SNAPSHOTS: order_items.unit_price_cents copies the book's price at the
--    moment of purchase. If the book gets more expensive tomorrow, last
--    month's invoice must NOT change. It's a different fact ("price paid"),
--    not a duplicate of books.price_cents. (The bookstore DB does this too!)
--  • COUNTERS / CACHES: books.review_count to avoid counting on every page
--    view — but now your code must keep it in sync.
--  • REPORTING tables built from the normalized ones, for fast analytics.
-- ⚠️ Gotcha: every copy is a promise to keep it correct. Denormalize when a
-- measured performance problem (or a business rule like snapshots) demands it.
UPDATE books SET price_cents = 1599 WHERE title = 'Dune';
SELECT b.title, b.price_cents AS price_today, oi.unit_price_cents AS price_paid, oi.order_id
FROM order_items oi JOIN books b ON b.id = oi.book_id
WHERE b.title = 'Dune'
ORDER BY oi.order_id;

-- §7. Keys and naming conventions
--  • Tables: plural snake_case (orders, order_items). Columns: snake_case.
--  • Primary key `id`. Foreign keys `<thing>_id` (customer_id).
--  • Surrogate keys (generated ids) over natural keys (email can change!),
--    but put a UNIQUE constraint on the natural key too.
--  • Many-to-many → a join table with a composite primary key (order_tags).
--  • Audit columns: created_at, updated_at (UTC, ISO-8601 or timestamptz).
--  • Money as integer cents; enums as TEXT + CHECK (status IN (…)).
--  • Public-facing ids: random UUIDs/opaque tokens, so ids can't be guessed.

-- §8. A real-world schema: authentication (better-auth style)
-- better-auth (a popular TypeScript auth library) uses four tables. Module 06
-- implements this with Spring Security; here is the design and the WHY.
CREATE TABLE "user" (                     -- "user" is a reserved word → quoted (Postgres too)
    id             TEXT PRIMARY KEY,      -- random id, never an incrementing number
    name           TEXT NOT NULL,
    email          TEXT NOT NULL UNIQUE,
    email_verified INTEGER NOT NULL DEFAULT 0,
    image          TEXT,
    created_at     TEXT NOT NULL,
    updated_at     TEXT NOT NULL
);
-- WHO someone is. No password here! A user may log in several ways.

CREATE TABLE account (
    id                       TEXT PRIMARY KEY,
    user_id                  TEXT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    provider_id              TEXT NOT NULL,   -- 'credential' (email+password), 'github', 'google', …
    account_id               TEXT NOT NULL,   -- the id at that provider (for credential: the user id)
    password                 TEXT,            -- HASH (never the password) — only for 'credential'
    access_token             TEXT,            -- OAuth tokens — only for social providers
    refresh_token            TEXT,
    access_token_expires_at  TEXT,
    refresh_token_expires_at TEXT,
    scope                    TEXT,
    id_token                 TEXT,
    created_at               TEXT NOT NULL,
    updated_at               TEXT NOT NULL,
    UNIQUE (provider_id, account_id)
);
-- HOW they can log in. One user → many accounts (password AND GitHub). 1-to-many.

CREATE TABLE session (
    id          TEXT PRIMARY KEY,
    user_id     TEXT NOT NULL REFERENCES "user" (id) ON DELETE CASCADE,
    token       TEXT NOT NULL UNIQUE,     -- what's in the cookie (module 06 stores a HASH of it)
    expires_at  TEXT NOT NULL,
    ip_address  TEXT,
    user_agent  TEXT,
    created_at  TEXT NOT NULL,
    updated_at  TEXT NOT NULL
);
CREATE INDEX idx_session_user_id ON session (user_id);
-- WHERE they are logged in right now. One row per device/browser → "log out
-- everywhere" = DELETE FROM session WHERE user_id = ?.

CREATE TABLE verification (
    id         TEXT PRIMARY KEY,
    identifier TEXT NOT NULL,             -- e.g. 'email-verification:ada@example.com'
    value      TEXT NOT NULL,             -- the one-time code/token (store a HASH)
    expires_at TEXT NOT NULL,
    created_at TEXT NOT NULL,
    updated_at TEXT NOT NULL
);
-- Short-lived one-time secrets: email verification, password reset links.

INSERT INTO "user" VALUES ('u_1', 'Ada', 'ada@example.com', 1, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');
INSERT INTO account (id, user_id, provider_id, account_id, password, created_at, updated_at)
VALUES ('a_1', 'u_1', 'credential', 'u_1', '$argon2id$…hash…', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
       ('a_2', 'u_1', 'github', '583231', NULL, '2026-01-02T00:00:00Z', '2026-01-02T00:00:00Z');
INSERT INTO session (id, user_id, token, expires_at, created_at, updated_at)
VALUES ('s_1', 'u_1', 'tok_laptop', '2026-01-08T00:00:00Z', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
       ('s_2', 'u_1', 'tok_phone', '2026-01-08T00:00:00Z', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z');
SELECT u.email, a.provider_id, count(DISTINCT s.id) AS active_sessions
FROM "user" u
JOIN account a ON a.user_id = u.id
LEFT JOIN session s ON s.user_id = u.id
GROUP BY u.id, a.provider_id
ORDER BY a.provider_id;
-- expect error
INSERT INTO account (id, user_id, provider_id, account_id, created_at, updated_at)
VALUES ('a_3', 'u_1', 'github', '583231', '2026-01-03T00:00:00Z', '2026-01-03T00:00:00Z');

-- 🔐 Security: never store raw passwords or raw tokens. Passwords → slow hash
-- (argon2/bcrypt). Session & verification tokens → store a fast hash (SHA-256)
-- so a leaked database can't be used to log in. Module 06 does exactly this.

-- 🐘 Postgres note: same design. Use `uuid` (gen_random_uuid()) or text ids,
-- `boolean` for email_verified, `timestamptz` for the *_at columns, and
-- `citext` or a unique index on lower(email) for case-insensitive emails.
-- Big apps also add partial indexes like ON session (expires_at).

-- Recap
--  • Normalize: each fact once (1NF no lists in cells, 2NF whole key, 3NF nothing but the key).
--  • Anomalies (update/insert/delete) are the symptoms of duplicated facts.
--  • Denormalize deliberately: snapshots (price paid), counters, reports.
--  • Auth schema: user (who) · account (how they log in) · session (where logged in) · verification (one-time secrets).
