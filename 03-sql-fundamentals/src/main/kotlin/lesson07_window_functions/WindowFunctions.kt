/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 07 — Window functions
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson07_window_functions.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it on the
 *  bookstore database; the transcript (every query + its result) is the Output block.
 *  Exercises: exercises/ex07/Ex07.kt
 */
package lesson07_window_functions

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson07_window_functions.sql", withBookstore = true)

// Output:
// §1. OVER () — aggregate without collapsing
// sql> SELECT title,
// sql>        price_cents,
// sql>        sum(price_cents) OVER ()   AS all_books_total,
// sql>        round(avg(price_cents) OVER ()) AS avg_price
// sql> FROM books
// sql> WHERE author_id IN (1, 2)
// sql> ORDER BY id
// title               | price_cents | all_books_total | avg_price
// --------------------+-------------+-----------------+----------
// Dune                | 1299        | 4196            | 1049.0
// Pride and Prejudice | 799         | 4196            | 1049.0
// Emma                | 899         | 4196            | 1049.0
// Dune Messiah        | 1199        | 4196            | 1049.0
// (4 rows)
//
// sql> SELECT title,
// sql>        price_cents * stock AS stock_value,
// sql>        round(100.0 * price_cents * stock / sum(price_cents * stock) OVER (), 1) AS pct_of_total
// sql> FROM books
// sql> ORDER BY stock_value DESC
// sql> LIMIT 5
// title                                 | stock_value | pct_of_total
// --------------------------------------+-------------+-------------
// The Hobbit                            | 29980       | 18.4
// Pride and Prejudice                   | 23970       | 14.7
// The Lord of the Rings                 | 20993       | 12.9
// Designing Data-Intensive Applications | 18396       | 11.3
// Foundation                            | 17985       | 11.0
// (5 rows)
//
// §2. PARTITION BY — a separate window per group
// sql> SELECT category_id,
// sql>        title,
// sql>        price_cents,
// sql>        round(avg(price_cents) OVER (PARTITION BY category_id)) AS category_avg,
// sql>        count(*) OVER (PARTITION BY category_id) AS books_in_category
// sql> FROM books
// sql> WHERE category_id IN (2, 3)
// sql> ORDER BY category_id, price_cents DESC, id
// category_id | title                     | price_cents | category_avg | books_in_category
// ------------+---------------------------+-------------+--------------+------------------
// 2           | Dune                      | 1299        | 1139.0       | 5
// 2           | Foundation                | 1199        | 1139.0       | 5
// 2           | Dune Messiah              | 1199        | 1139.0       | 5
// 2           | The Left Hand of Darkness | 1099        | 1139.0       | 5
// 2           | I, Robot                  | 899         | 1139.0       | 5
// 3           | The Lord of the Rings     | 2999        | 1832.0       | 3
// 3           | The Hobbit                | 1499        | 1832.0       | 3
// 3           | A Wizard of Earthsea      | 999         | 1832.0       | 3
// (8 rows)
//
// §3. Ranking: ROW_NUMBER vs RANK vs DENSE_RANK
// sql> SELECT title,
// sql>        price_cents,
// sql>        row_number() OVER (ORDER BY price_cents DESC, id) AS row_num,
// sql>        rank()       OVER (ORDER BY price_cents DESC)     AS rnk,
// sql>        dense_rank() OVER (ORDER BY price_cents DESC)     AS dense
// sql> FROM books
// sql> WHERE category_id = 2
// sql> ORDER BY row_num
// title                     | price_cents | row_num | rnk | dense
// --------------------------+-------------+---------+-----+------
// Dune                      | 1299        | 1       | 1   | 1
// Foundation                | 1199        | 2       | 2   | 2
// Dune Messiah              | 1199        | 3       | 2   | 2
// The Left Hand of Darkness | 1099        | 4       | 4   | 3
// I, Robot                  | 899         | 5       | 5   | 4
// (5 rows)
//
// §4. Top-N per group (the most asked interview question in SQL 🙂)
// sql> WITH ranked AS (
// sql>     SELECT c.name AS category,
// sql>            b.title,
// sql>            b.price_cents,
// sql>            row_number() OVER (PARTITION BY b.category_id ORDER BY b.price_cents DESC, b.id) AS pos
// sql>     FROM books b
// sql>     JOIN categories c ON c.id = b.category_id
// sql> )
// sql> SELECT category, title, price_cents
// sql> FROM ranked
// sql> WHERE pos = 1
// sql> ORDER BY category
// category        | title                                 | price_cents
// ----------------+---------------------------------------+------------
// Classics        | Emma                                  | 899
// Databases       | Designing Data-Intensive Applications | 4599
// Fantasy         | The Lord of the Rings                 | 2999
// History         | SPQR                                  | 1899
// Programming     | The C Programming Language            | 3999
// Science Fiction | Dune                                  | 1299
// (6 rows)
//
// §5. Running totals — ORDER BY inside OVER makes it cumulative
// sql> WITH order_totals AS (
// sql>     SELECT o.id, o.created_at, sum(oi.quantity * oi.unit_price_cents) AS total
// sql>     FROM orders o
// sql>     JOIN order_items oi ON oi.order_id = o.id
// sql>     WHERE o.status <> 'cancelled'
// sql>     GROUP BY o.id
// sql> )
// sql> SELECT id,
// sql>        date(created_at) AS day,
// sql>        total,
// sql>        sum(total) OVER (ORDER BY created_at) AS running_revenue
// sql> FROM order_totals
// sql> ORDER BY created_at
// id | day        | total | running_revenue
// ---+------------+-------+----------------
// 1  | 2025-06-01 | 5898  | 5898
// 3  | 2025-06-03 | 3397  | 9295
// 2  | 2025-07-15 | 2998  | 12293
// 5  | 2025-08-01 | 3296  | 15589
// 6  | 2025-08-20 | 3999  | 19588
// 7  | 2025-09-05 | 8598  | 28186
// 8  | 2025-09-10 | 1899  | 30085
// 9  | 2025-09-12 | 3797  | 33882
// 10 | 2025-10-01 | 3597  | 37479
// (9 rows)
//
// §6. LAG / LEAD — peek at the previous / next row
// sql> SELECT customer_id,
// sql>        date(created_at) AS ordered_on,
// sql>        date(lag(created_at) OVER (PARTITION BY customer_id ORDER BY created_at)) AS previous,
// sql>        round(julianday(created_at)
// sql>              - julianday(lag(created_at) OVER (PARTITION BY customer_id ORDER BY created_at))) AS days_since_previous
// sql> FROM orders
// sql> WHERE customer_id IN (1, 2, 5)
// sql> ORDER BY customer_id, created_at
// customer_id | ordered_on | previous   | days_since_previous
// ------------+------------+------------+--------------------
// 1           | 2025-06-01 | NULL       | NULL
// 1           | 2025-07-15 | 2025-06-01 | 44.0
// 1           | 2025-09-10 | 2025-07-15 | 57.0
// 2           | 2025-06-03 | NULL       | NULL
// 2           | 2025-09-12 | 2025-06-03 | 101.0
// 5           | 2025-09-05 | NULL       | NULL
// 5           | 2025-10-01 | 2025-09-05 | 26.0
// (7 rows)
//
// §7. Naming a window and moving frames
// sql> WITH order_totals AS (
// sql>     SELECT o.id, o.created_at, sum(oi.quantity * oi.unit_price_cents) AS total
// sql>     FROM orders o
// sql>     JOIN order_items oi ON oi.order_id = o.id
// sql>     GROUP BY o.id
// sql> )
// sql> SELECT id,
// sql>        total,
// sql>        round(avg(total) OVER last_two) AS moving_avg_2,
// sql>        first_value(total) OVER w       AS first_order_total
// sql> FROM order_totals
// sql> WINDOW w AS (ORDER BY created_at),
// sql>        last_two AS (ORDER BY created_at ROWS BETWEEN 1 PRECEDING AND CURRENT ROW)
// sql> ORDER BY created_at
// sql> LIMIT 5
// id | total | moving_avg_2 | first_order_total
// ---+-------+--------------+------------------
// 1  | 5898  | 5898.0       | 5898
// 3  | 3397  | 4648.0       | 5898
// 4  | 2999  | 3198.0       | 5898
// 2  | 2998  | 2999.0       | 5898
// 5  | 3296  | 3147.0       | 5898
// (5 rows)
