/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 05 — Aggregation: COUNT, SUM, AVG, GROUP BY, HAVING
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson05_aggregation.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it; the
 *  transcript (every query + its result) is shown in the Output block.
 *  Exercises: exercises/ex05/Ex05.kt
 */
package lesson05_aggregation

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson05_aggregation.sql", withBookstore = true)

// Output:
// §1. Aggregate functions over the whole table
// sql> SELECT count(*)         AS books,
// sql>        sum(stock)       AS items_in_stock,
// sql>        min(price_cents) AS cheapest,
// sql>        max(price_cents) AS dearest,
// sql>        avg(price_cents) AS average_price
// sql> FROM books
// books | items_in_stock | cheapest | dearest | average_price
// ------+----------------+----------+---------+-------------------
// 14    | 113            | 500      | 4599    | 1706.2142857142858
// (1 row)
//
// sql> SELECT count(*)                    AS reviews,
// sql>        count(body)                 AS reviews_with_text,
// sql>        count(DISTINCT customer_id) AS distinct_reviewers
// sql> FROM reviews
// reviews | reviews_with_text | distinct_reviewers
// --------+-------------------+-------------------
// 10      | 8                 | 5
// (1 row)
//
// sql> SELECT sum(rating) / count(rating)        AS integer_division,
// sql>        avg(rating)                        AS avg_rating,
// sql>        round(avg(rating), 2)              AS rounded,
// sql>        round(avg(price_cents) / 100.0, 2) AS avg_reviewed_price_euros
// sql> FROM reviews JOIN books ON books.id = reviews.book_id;   -- one row per review
// integer_division | avg_rating | rounded | avg_reviewed_price_euros
// -----------------+------------+---------+-------------------------
// 4                | 4.1        | 4.1     | 24.19
// (1 row)
//
// §2. GROUP BY — one result row per group
// sql> SELECT status, count(*) AS orders
// sql> FROM orders
// sql> GROUP BY status
// sql> ORDER BY orders DESC, status
// status    | orders
// ----------+-------
// shipped   | 4
// paid      | 3
// pending   | 2
// cancelled | 1
// (4 rows)
//
// sql> SELECT c.name,
// sql>        count(DISTINCT o.id)                    AS orders,
// sql>        sum(oi.quantity * oi.unit_price_cents)  AS revenue_cents
// sql> FROM customers c
// sql> JOIN orders o       ON o.customer_id = c.id
// sql> JOIN order_items oi ON oi.order_id = o.id
// sql> WHERE o.status <> 'cancelled'
// sql> GROUP BY c.id, c.name
// sql> ORDER BY revenue_cents DESC
// name              | orders | revenue_cents
// ------------------+--------+--------------
// Margaret Hamilton | 2      | 12195
// Ada Lovelace      | 3      | 10795
// Alan Turing       | 2      | 7194
// Linus Torvalds    | 1      | 3999
// Grace Hopper      | 1      | 3296
// (5 rows)
//
// §3. WHERE vs HAVING
// sql> SELECT a.name, count(b.id) AS books, sum(b.stock) AS stock
// sql> FROM authors a
// sql> JOIN books b ON b.author_id = a.id
// sql> WHERE b.stock > 0               -- row filter: ignore sold-out books
// sql> GROUP BY a.id, a.name
// sql> HAVING count(b.id) >= 2         -- group filter: authors with 2+ such books
// sql> ORDER BY a.name
// name              | books | stock
// ------------------+-------+------
// Isaac Asimov      | 3     | 19
// J.R.R. Tolkien    | 2     | 27
// Ursula K. Le Guin | 2     | 13
// (3 rows)
//
// §4. Grouping with LEFT JOIN — keep the zeros
// sql> SELECT c.name, count(o.id) AS orders
// sql> FROM customers c
// sql> LEFT JOIN orders o ON o.customer_id = c.id
// sql> GROUP BY c.id, c.name
// sql> ORDER BY orders DESC, c.name
// name              | orders
// ------------------+-------
// Ada Lovelace      | 3
// Alan Turing       | 2
// Grace Hopper      | 2
// Margaret Hamilton | 2
// Linus Torvalds    | 1
// Edsger Dijkstra   | 0
// (6 rows)
//
// §5. Building lists and conditional counts
// sql> SELECT a.name, group_concat(b.title, ', ' ORDER BY b.title) AS titles
// sql> FROM authors a
// sql> JOIN books b ON b.author_id = a.id
// sql> WHERE a.id IN (1, 2, 5)
// sql> GROUP BY a.id, a.name
// sql> ORDER BY a.name
// name           | titles
// ---------------+----------------------------------
// Frank Herbert  | Dune, Dune Messiah
// J.R.R. Tolkien | The Hobbit, The Lord of the Rings
// Jane Austen    | Emma, Pride and Prejudice
// (3 rows)
//
// sql> SELECT c.name,
// sql>        count(*) FILTER (WHERE o.status = 'shipped') AS shipped,
// sql>        count(*) FILTER (WHERE o.status = 'pending') AS pending
// sql> FROM customers c
// sql> JOIN orders o ON o.customer_id = c.id
// sql> GROUP BY c.id, c.name
// sql> ORDER BY c.id
// name              | shipped | pending
// ------------------+---------+--------
// Ada Lovelace      | 1       | 1
// Alan Turing       | 1       | 0
// Grace Hopper      | 1       | 0
// Linus Torvalds    | 0       | 1
// Margaret Hamilton | 1       | 0
// (5 rows)
//
// sql> SELECT sum(CASE WHEN stock = 0 THEN 1 ELSE 0 END) AS sold_out,
// sql>        sum(CASE WHEN stock > 0 THEN 1 ELSE 0 END) AS available
// sql> FROM books
// sold_out | available
// ---------+----------
// 2        | 12
// (1 row)
//
// §6. A small sales report: revenue per top-level category
// sql> SELECT coalesce(parent.name, c.name)            AS section,
// sql>        count(DISTINCT oi.order_id)              AS orders,
// sql>        sum(oi.quantity)                         AS copies_sold,
// sql>        sum(oi.quantity * oi.unit_price_cents)   AS revenue_cents
// sql> FROM order_items oi
// sql> JOIN orders o           ON o.id = oi.order_id AND o.status <> 'cancelled'
// sql> JOIN books b            ON b.id = oi.book_id
// sql> JOIN categories c       ON c.id = b.category_id
// sql> LEFT JOIN categories parent ON parent.id = c.parent_id
// sql> GROUP BY section
// sql> ORDER BY revenue_cents DESC
// section     | orders | copies_sold | revenue_cents
// ------------+--------+-------------+--------------
// Fiction     | 6      | 16          | 18384
// Non-fiction | 3      | 3           | 9897
// Programming | 2      | 2           | 9198
// (3 rows)
