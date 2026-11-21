-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 09 — Upserts, JSON and dates
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : "insert or update" in ONE safe statement, store and query JSON, and
--         work with dates and times stored as ISO-8601 text.
--  Run  : the green ▶ in lesson09_upsert_json_and_dates/UpsertJsonAndDates.kt
--  Next : lesson 10 — normalization and schema design

-- §1. Upsert: INSERT … ON CONFLICT DO NOTHING
-- 🧒 Like I'm 5:
--    The sign-up sheet for the school trip. If your name is already on it,
--    you don't write it again (DO NOTHING) — or you fix your phone number
--    next to it (DO UPDATE). One move, no "check first, then write".
--
-- Why not "SELECT, then INSERT if missing"? Two requests at the same moment
-- can both see "missing" and both insert → duplicate or crash. An upsert is
-- ONE atomic statement; the UNIQUE/PRIMARY KEY constraint decides.
CREATE TABLE newsletter (
    email         TEXT PRIMARY KEY,
    subscribed_at TEXT NOT NULL
);
INSERT INTO newsletter (email, subscribed_at) VALUES ('ada@example.com', '2025-06-01T10:00:00Z')
ON CONFLICT (email) DO NOTHING;
INSERT INTO newsletter (email, subscribed_at) VALUES ('ada@example.com', '2025-07-01T10:00:00Z')
ON CONFLICT (email) DO NOTHING;
SELECT * FROM newsletter;

-- §2. Upsert: ON CONFLICT DO UPDATE with `excluded`
-- `excluded` = the row you TRIED to insert. Perfect for counters and "last seen".
CREATE TABLE book_views (
    book_id        INTEGER PRIMARY KEY REFERENCES books (id),
    views          INTEGER NOT NULL,
    last_viewed_at TEXT    NOT NULL
);
INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (1, 1, '2025-09-01T08:00:00Z')
ON CONFLICT (book_id) DO UPDATE SET views = views + 1, last_viewed_at = excluded.last_viewed_at;
INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (1, 1, '2025-09-01T09:30:00Z')
ON CONFLICT (book_id) DO UPDATE SET views = views + 1, last_viewed_at = excluded.last_viewed_at;
INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (8, 1, '2025-09-02T12:00:00Z')
ON CONFLICT (book_id) DO UPDATE SET views = views + 1, last_viewed_at = excluded.last_viewed_at;
SELECT * FROM book_views ORDER BY book_id;
-- (The transcript says "1 row inserted" each time: the driver reports 1 row
--  CHANGED, whether the upsert inserted or updated it. DO NOTHING reports 0.)

-- A WHERE on DO UPDATE makes it conditional: only move "last seen" forwards.
INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (1, 1, '2025-08-01T00:00:00Z')
ON CONFLICT (book_id) DO UPDATE SET last_viewed_at = excluded.last_viewed_at
WHERE excluded.last_viewed_at > book_views.last_viewed_at;
SELECT book_id, last_viewed_at FROM book_views WHERE book_id = 1;

-- §3. ⚠️ Gotcha: INSERT OR REPLACE is NOT an upsert
-- REPLACE = on conflict, DELETE the old row, then INSERT the new one.
-- Columns you didn't mention fall back to their defaults, and the delete can
-- CASCADE to child rows!
CREATE TABLE carts (id INTEGER PRIMARY KEY, owner TEXT NOT NULL, note TEXT DEFAULT 'none');
CREATE TABLE cart_items (
    cart_id INTEGER NOT NULL REFERENCES carts (id) ON DELETE CASCADE,
    book_id INTEGER NOT NULL
);
INSERT INTO carts (id, owner, note) VALUES (1, 'ada', 'gift wrap please');
INSERT INTO cart_items (cart_id, book_id) VALUES (1, 1), (1, 8);
INSERT OR REPLACE INTO carts (id, owner) VALUES (1, 'ada');
SELECT * FROM carts;
SELECT count(*) AS items_left FROM cart_items;
-- The note is gone AND both cart items were deleted. Use ON CONFLICT … DO UPDATE.

-- §4. JSON — building it
-- SQLite has a full set of JSON functions (stored as TEXT). Build JSON from
-- rows — e.g. the exact shape an API endpoint returns:
SELECT json_object('id', id, 'title', title, 'price', price_cents / 100.0) AS book_json
FROM books
WHERE id IN (1, 2);

-- json_group_array = aggregate rows INTO a JSON array (one row per author):
SELECT a.name,
       json_group_array(b.title) AS titles
FROM authors a
JOIN books b ON b.author_id = a.id
WHERE a.id IN (1, 5)
GROUP BY a.id
ORDER BY a.id;

-- §5. JSON — reading it
-- A JSON column for flexible settings (things you don't want 20 columns for):
CREATE TABLE customer_settings (
    customer_id INTEGER PRIMARY KEY REFERENCES customers (id),
    prefs       TEXT NOT NULL CHECK (json_valid(prefs))
);
INSERT INTO customer_settings (customer_id, prefs) VALUES
    (1, '{"theme": "dark", "newsletter": true, "favourite_genres": ["sci-fi", "classics"]}'),
    (2, '{"theme": "light", "newsletter": false, "favourite_genres": ["history"]}');
-- expect error
INSERT INTO customer_settings (customer_id, prefs) VALUES (3, '{oops not json');

-- json_extract / ->> give an SQL value; -> gives JSON text.
SELECT customer_id,
       prefs ->> '$.theme'              AS theme,
       json_extract(prefs, '$.newsletter') AS newsletter,
       prefs -> '$.favourite_genres'    AS genres_json,
       prefs ->> '$.favourite_genres[0]' AS first_genre
FROM customer_settings
ORDER BY customer_id;

-- json_each turns an array into ROWS — now you can filter/join on its items:
SELECT s.customer_id, g.value AS genre
FROM customer_settings s, json_each(s.prefs, '$.favourite_genres') g
ORDER BY s.customer_id, g.value;

-- json_set changes one field inside the document:
UPDATE customer_settings SET prefs = json_set(prefs, '$.theme', 'solarized') WHERE customer_id = 2;
SELECT prefs ->> '$.theme' AS theme FROM customer_settings WHERE customer_id = 2;

-- ⚠️ Gotcha: JSON is great for optional, rarely-queried extras. If you filter,
-- join or need constraints on a field, make it a real column.

-- §6. Dates and times as ISO-8601 text
-- SQLite has no DATE type. Store UTC timestamps as 'YYYY-MM-DDTHH:MM:SSZ'
-- text. Because the format is "biggest unit first", plain TEXT comparison
-- and ORDER BY sort them correctly!
SELECT id, created_at FROM orders WHERE created_at >= '2025-09-01' ORDER BY created_at;

-- date() / time() / datetime() / strftime() parse and format.
-- (We always pass fixed dates — 'now' would make the output change every day.)
SELECT date('2025-06-01T10:00:00Z')                  AS just_date,
       datetime('2025-06-01T10:00:00Z', '+7 days')   AS week_later,
       date('2025-01-31', '+1 month')                AS jan31_plus_month,
       date('2025-06-15', 'start of month')          AS month_start,
       strftime('%Y-%m', '2025-06-15')               AS year_month,
       strftime('%w', '2025-06-15')                  AS weekday_0_is_sunday;
-- ⚠️ Gotchas in that result:
--   • Jan 31 + 1 month = March 3 ("Feb 31" overflows). Month math is tricky everywhere.
--   • datetime() returns '2025-06-08 10:00:00' — a SPACE and no 'Z'. Compared as
--     text with '2025-06-08T…Z' values it sorts wrongly! Keep one format:
SELECT strftime('%Y-%m-%dT%H:%M:%SZ', '2025-06-01T10:00:00Z', '+7 days') AS week_later_iso;

-- Orders per month — group by a formatted date:
SELECT strftime('%Y-%m', created_at) AS month, count(*) AS orders
FROM orders
GROUP BY month
ORDER BY month;

-- Differences: julianday() = days as a decimal number.
SELECT round(julianday('2025-10-01T09:00:00Z') - julianday('2025-06-01T10:00:00Z'), 2) AS days_between,
       (julianday('2025-06-01T12:30:00Z') - julianday('2025-06-01T10:00:00Z')) * 24 * 60 AS minutes_raw,
       round((julianday('2025-06-01T12:30:00Z') - julianday('2025-06-01T10:00:00Z')) * 24 * 60) AS minutes;
-- julianday is a floating-point number, so tiny errors appear (150.0000004…). Round it,
-- or use unixepoch() (whole seconds) for exact differences:
SELECT (unixepoch('2025-06-01T12:30:00Z') - unixepoch('2025-06-01T10:00:00Z')) / 60 AS minutes_exact;

-- Alternative storage: Unix epoch seconds in an INTEGER (compact, fast math).
SELECT unixepoch('2025-06-01T10:00:00Z')        AS epoch_seconds,
       datetime(1748772000, 'unixepoch')        AS back_to_text;

-- 🏗️ In the real world: store UTC, always. Convert to the user's time zone
-- only when DISPLAYING (in Kotlin: kotlin.time.Instant → java.time.ZonedDateTime).

-- 🐘 Postgres note:
--   • ON CONFLICT … DO NOTHING / DO UPDATE SET x = EXCLUDED.x — identical. (No INSERT OR REPLACE.)
--     Postgres 15+ also has MERGE.
--   • jsonb (binary JSON) with operators -> ->> @> ? and GIN indexes for fast
--     "contains" queries; jsonb_agg / jsonb_build_object instead of json_group_array / json_object.
--   • Real types: date, timestamptz (ALWAYS "with time zone"), interval:
--       now() + interval '7 days', date_trunc('month', created_at), extract(dow FROM d),
--       age(a, b). Comparing timestamps is native, no text tricks needed.

-- Recap
--  • Upsert = INSERT … ON CONFLICT (key) DO NOTHING | DO UPDATE SET … excluded.col.
--  • INSERT OR REPLACE deletes + inserts: lost columns and cascades. Avoid.
--  • json_object / json_group_array build JSON; ->> / json_extract / json_each read it.
--  • Dates: ISO-8601 UTC text sorts correctly; date/datetime/strftime/julianday do the math.
