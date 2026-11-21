/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 09 — Upserts, JSON and dates
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson09_upsert_json_and_dates.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it on the
 *  bookstore database; the transcript (every query + its result) is the Output block.
 *  Exercises: exercises/ex09/Ex09.kt
 */
package lesson09_upsert_json_and_dates

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson09_upsert_json_and_dates.sql", withBookstore = true)

// Output:
// §1. Upsert: INSERT … ON CONFLICT DO NOTHING
// sql> INSERT INTO newsletter (email, subscribed_at) VALUES ('ada@example.com', '2025-06-01T10:00:00Z')
// sql> ON CONFLICT (email) DO NOTHING
// → 1 row inserted
//
// sql> INSERT INTO newsletter (email, subscribed_at) VALUES ('ada@example.com', '2025-07-01T10:00:00Z')
// sql> ON CONFLICT (email) DO NOTHING
// → 0 rows inserted
//
// sql> SELECT * FROM newsletter
// email           | subscribed_at
// ----------------+---------------------
// ada@example.com | 2025-06-01T10:00:00Z
// (1 row)
//
// §2. Upsert: ON CONFLICT DO UPDATE with `excluded`
// sql> INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (1, 1, '2025-09-01T08:00:00Z')
// sql> ON CONFLICT (book_id) DO UPDATE SET views = views + 1, last_viewed_at = excluded.last_viewed_at
// → 1 row inserted
//
// sql> INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (1, 1, '2025-09-01T09:30:00Z')
// sql> ON CONFLICT (book_id) DO UPDATE SET views = views + 1, last_viewed_at = excluded.last_viewed_at
// → 1 row inserted
//
// sql> INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (8, 1, '2025-09-02T12:00:00Z')
// sql> ON CONFLICT (book_id) DO UPDATE SET views = views + 1, last_viewed_at = excluded.last_viewed_at
// → 1 row inserted
//
// sql> SELECT * FROM book_views ORDER BY book_id
// book_id | views | last_viewed_at
// --------+-------+---------------------
// 1       | 2     | 2025-09-01T09:30:00Z
// 8       | 1     | 2025-09-02T12:00:00Z
// (2 rows)
//
// sql> INSERT INTO book_views (book_id, views, last_viewed_at) VALUES (1, 1, '2025-08-01T00:00:00Z')
// sql> ON CONFLICT (book_id) DO UPDATE SET last_viewed_at = excluded.last_viewed_at
// sql> WHERE excluded.last_viewed_at > book_views.last_viewed_at
// → 0 rows inserted
//
// sql> SELECT book_id, last_viewed_at FROM book_views WHERE book_id = 1
// book_id | last_viewed_at
// --------+---------------------
// 1       | 2025-09-01T09:30:00Z
// (1 row)
//
// §3. ⚠️ Gotcha: INSERT OR REPLACE is NOT an upsert
// sql> INSERT INTO carts (id, owner, note) VALUES (1, 'ada', 'gift wrap please')
// → 1 row inserted
//
// sql> INSERT INTO cart_items (cart_id, book_id) VALUES (1, 1), (1, 8)
// → 2 rows inserted
//
// sql> INSERT OR REPLACE INTO carts (id, owner) VALUES (1, 'ada')
// → 1 row inserted
//
// sql> SELECT * FROM carts
// id | owner | note
// ---+-------+-----
// 1  | ada   | none
// (1 row)
//
// sql> SELECT count(*) AS items_left FROM cart_items
// items_left
// ----------
// 0
// (1 row)
//
// §4. JSON — building it
// sql> SELECT json_object('id', id, 'title', title, 'price', price_cents / 100.0) AS book_json
// sql> FROM books
// sql> WHERE id IN (1, 2)
// book_json
// ---------------------------------------------------
// {"id":1,"title":"Dune","price":12.99}
// {"id":2,"title":"Pride and Prejudice","price":7.99}
// (2 rows)
//
// sql> SELECT a.name,
// sql>        json_group_array(b.title) AS titles
// sql> FROM authors a
// sql> JOIN books b ON b.author_id = a.id
// sql> WHERE a.id IN (1, 5)
// sql> GROUP BY a.id
// sql> ORDER BY a.id
// name           | titles
// ---------------+---------------------------------------
// Frank Herbert  | ["Dune","Dune Messiah"]
// J.R.R. Tolkien | ["The Hobbit","The Lord of the Rings"]
// (2 rows)
//
// §5. JSON — reading it
// sql> INSERT INTO customer_settings (customer_id, prefs) VALUES
// sql>     (1, '{"theme": "dark", "newsletter": true, "favourite_genres": ["sci-fi", "classics"]}'),
// sql>     (2, '{"theme": "light", "newsletter": false, "favourite_genres": ["history"]}')
// → 2 rows inserted
//
// sql> INSERT INTO customer_settings (customer_id, prefs) VALUES (3, '{oops not json')
// ✗ error: [SQLITE_CONSTRAINT_CHECK] A CHECK constraint failed (CHECK constraint failed: json_valid(prefs))
//
// sql> SELECT customer_id,
// sql>        prefs ->> '$.theme'              AS theme,
// sql>        json_extract(prefs, '$.newsletter') AS newsletter,
// sql>        prefs -> '$.favourite_genres'    AS genres_json,
// sql>        prefs ->> '$.favourite_genres[0]' AS first_genre
// sql> FROM customer_settings
// sql> ORDER BY customer_id
// customer_id | theme | newsletter | genres_json           | first_genre
// ------------+-------+------------+-----------------------+------------
// 1           | dark  | 1          | ["sci-fi","classics"] | sci-fi
// 2           | light | 0          | ["history"]           | history
// (2 rows)
//
// sql> SELECT s.customer_id, g.value AS genre
// sql> FROM customer_settings s, json_each(s.prefs, '$.favourite_genres') g
// sql> ORDER BY s.customer_id, g.value
// customer_id | genre
// ------------+---------
// 1           | classics
// 1           | sci-fi
// 2           | history
// (3 rows)
//
// sql> UPDATE customer_settings SET prefs = json_set(prefs, '$.theme', 'solarized') WHERE customer_id = 2
// → 1 row updated
//
// sql> SELECT prefs ->> '$.theme' AS theme FROM customer_settings WHERE customer_id = 2
// theme
// ---------
// solarized
// (1 row)
//
// §6. Dates and times as ISO-8601 text
// sql> SELECT id, created_at FROM orders WHERE created_at >= '2025-09-01' ORDER BY created_at
// id | created_at
// ---+---------------------
// 7  | 2025-09-05T13:00:00Z
// 8  | 2025-09-10T08:45:00Z
// 9  | 2025-09-12T17:30:00Z
// 10 | 2025-10-01T09:00:00Z
// (4 rows)
//
// sql> SELECT date('2025-06-01T10:00:00Z')                  AS just_date,
// sql>        datetime('2025-06-01T10:00:00Z', '+7 days')   AS week_later,
// sql>        date('2025-01-31', '+1 month')                AS jan31_plus_month,
// sql>        date('2025-06-15', 'start of month')          AS month_start,
// sql>        strftime('%Y-%m', '2025-06-15')               AS year_month,
// sql>        strftime('%w', '2025-06-15')                  AS weekday_0_is_sunday
// just_date  | week_later          | jan31_plus_month | month_start | year_month | weekday_0_is_sunday
// -----------+---------------------+------------------+-------------+------------+--------------------
// 2025-06-01 | 2025-06-08 10:00:00 | 2025-03-03       | 2025-06-01  | 2025-06    | 0
// (1 row)
//
// sql> SELECT strftime('%Y-%m-%dT%H:%M:%SZ', '2025-06-01T10:00:00Z', '+7 days') AS week_later_iso
// week_later_iso
// --------------------
// 2025-06-08T10:00:00Z
// (1 row)
//
// sql> SELECT strftime('%Y-%m', created_at) AS month, count(*) AS orders
// sql> FROM orders
// sql> GROUP BY month
// sql> ORDER BY month
// month   | orders
// --------+-------
// 2025-06 | 3
// 2025-07 | 1
// 2025-08 | 2
// 2025-09 | 3
// 2025-10 | 1
// (5 rows)
//
// sql> SELECT round(julianday('2025-10-01T09:00:00Z') - julianday('2025-06-01T10:00:00Z'), 2) AS days_between,
// sql>        (julianday('2025-06-01T12:30:00Z') - julianday('2025-06-01T10:00:00Z')) * 24 * 60 AS minutes_raw,
// sql>        round((julianday('2025-06-01T12:30:00Z') - julianday('2025-06-01T10:00:00Z')) * 24 * 60) AS minutes
// days_between | minutes_raw        | minutes
// -------------+--------------------+--------
// 121.96       | 150.00000044703484 | 150.0
// (1 row)
//
// sql> SELECT (unixepoch('2025-06-01T12:30:00Z') - unixepoch('2025-06-01T10:00:00Z')) / 60 AS minutes_exact
// minutes_exact
// -------------
// 150
// (1 row)
//
// sql> SELECT unixepoch('2025-06-01T10:00:00Z')        AS epoch_seconds,
// sql>        datetime(1748772000, 'unixepoch')        AS back_to_text
// epoch_seconds | back_to_text
// --------------+--------------------
// 1748772000    | 2025-06-01 10:00:00
// (1 row)
