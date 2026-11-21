/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 06 — Subqueries and CTEs (WITH, including recursive)
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson06_subqueries_and_ctes.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it on the
 *  bookstore database; the transcript (every query + its result) is the Output block.
 *  Exercises: exercises/ex06/Ex06.kt
 */
package lesson06_subqueries_and_ctes

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson06_subqueries_and_ctes.sql", withBookstore = true)

// Output:
// §1. Scalar subquery — a query that returns ONE value
// sql> SELECT title, price_cents
// sql> FROM books
// sql> WHERE price_cents > (SELECT avg(price_cents) FROM books)
// sql> ORDER BY price_cents DESC
// title                                 | price_cents
// --------------------------------------+------------
// Designing Data-Intensive Applications | 4599
// The C Programming Language            | 3999
// The Lord of the Rings                 | 2999
// SPQR                                  | 1899
// (4 rows)
//
// sql> SELECT title,
// sql>        price_cents,
// sql>        (SELECT round(avg(price_cents)) FROM books) AS shop_avg
// sql> FROM books
// sql> WHERE author_id = 1
// sql> ORDER BY id
// title        | price_cents | shop_avg
// -------------+-------------+---------
// Dune         | 1299        | 1706.0
// Dune Messiah | 1199        | 1706.0
// (2 rows)
//
// §2. IN (subquery) — a query that returns a LIST
// sql> SELECT title
// sql> FROM books
// sql> WHERE author_id IN (SELECT id FROM authors WHERE country = 'UK')
// sql> ORDER BY title
// title
// -------------------------------------
// Designing Data-Intensive Applications
// Emma
// Pride and Prejudice
// SPQR
// The Hobbit
// The Lord of the Rings
// (6 rows)
//
// sql> SELECT count(*) AS books_not_in_list_with_null
// sql> FROM books
// sql> WHERE category_id NOT IN (SELECT category_id FROM books WHERE id = 14)
// books_not_in_list_with_null
// ---------------------------
// 0
// (1 row)
//
// §3. EXISTS / NOT EXISTS — "is there at least one…?"
// sql> SELECT c.name
// sql> FROM customers c
// sql> WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.id)
// name
// ---------------
// Edsger Dijkstra
// (1 row)
//
// sql> SELECT a.name
// sql> FROM authors a
// sql> WHERE EXISTS (
// sql>     SELECT 1
// sql>     FROM books b
// sql>     JOIN reviews r ON r.book_id = b.id
// sql>     WHERE b.author_id = a.id AND r.rating = 5
// sql> )
// sql> ORDER BY a.name
// name
// ----------------
// Brian Kernighan
// Frank Herbert
// Martin Kleppmann
// (3 rows)
//
// §4. Correlated subqueries — the inner query looks at the outer row
// sql> SELECT c.name,
// sql>        (SELECT max(o.created_at) FROM orders o WHERE o.customer_id = c.id) AS last_order
// sql> FROM customers c
// sql> ORDER BY c.id
// name              | last_order
// ------------------+---------------------
// Ada Lovelace      | 2025-09-10T08:45:00Z
// Alan Turing       | 2025-09-12T17:30:00Z
// Grace Hopper      | 2025-08-01T10:15:00Z
// Linus Torvalds    | 2025-08-20T18:00:00Z
// Margaret Hamilton | 2025-10-01T09:00:00Z
// Edsger Dijkstra   | NULL
// (6 rows)
//
// sql> SELECT b.title, b.price_cents, b.category_id
// sql> FROM books b
// sql> WHERE b.price_cents > (
// sql>     SELECT avg(b2.price_cents) FROM books b2 WHERE b2.category_id = b.category_id
// sql> )
// sql> ORDER BY b.category_id, b.title
// title                 | price_cents | category_id
// ----------------------+-------------+------------
// Dune                  | 1299        | 2
// Dune Messiah          | 1199        | 2
// Foundation            | 1199        | 2
// The Lord of the Rings | 2999        | 3
// Emma                  | 899         | 4
// (5 rows)
//
// §5. Derived tables — a subquery in FROM is a temporary table
// sql> SELECT customer_id, count(*) AS orders, round(avg(order_total)) AS avg_order_cents
// sql> FROM (
// sql>     SELECT o.id, o.customer_id, sum(oi.quantity * oi.unit_price_cents) AS order_total
// sql>     FROM orders o
// sql>     JOIN order_items oi ON oi.order_id = o.id
// sql>     WHERE o.status <> 'cancelled'
// sql>     GROUP BY o.id
// sql> ) AS order_totals
// sql> GROUP BY customer_id
// sql> ORDER BY customer_id
// customer_id | orders | avg_order_cents
// ------------+--------+----------------
// 1           | 3      | 3598.0
// 2           | 2      | 3597.0
// 3           | 1      | 3296.0
// 4           | 1      | 3999.0
// 5           | 2      | 6098.0
// (5 rows)
//
// §6. WITH — give each step a name (Common Table Expressions, CTEs)
// sql> WITH order_totals AS (
// sql>     SELECT o.id, o.customer_id, sum(oi.quantity * oi.unit_price_cents) AS total
// sql>     FROM orders o
// sql>     JOIN order_items oi ON oi.order_id = o.id
// sql>     WHERE o.status <> 'cancelled'
// sql>     GROUP BY o.id
// sql> ),
// sql> customer_spend AS (
// sql>     SELECT customer_id, sum(total) AS spent
// sql>     FROM order_totals
// sql>     GROUP BY customer_id
// sql> )
// sql> SELECT c.name, cs.spent
// sql> FROM customer_spend cs
// sql> JOIN customers c ON c.id = cs.customer_id
// sql> WHERE cs.spent > (SELECT avg(spent) FROM customer_spend)
// sql> ORDER BY cs.spent DESC
// name              | spent
// ------------------+------
// Margaret Hamilton | 12195
// Ada Lovelace      | 10795
// (2 rows)
//
// §7. WITH RECURSIVE — walking a tree
// sql> WITH RECURSIVE tree (id, name, depth, path) AS (
// sql>     SELECT id, name, 0, name
// sql>     FROM categories
// sql>     WHERE parent_id IS NULL                                   -- anchor: the roots
// sql>     UNION ALL
// sql>     SELECT c.id, c.name, t.depth + 1, t.path || ' > ' || c.name
// sql>     FROM categories c
// sql>     JOIN tree t ON c.parent_id = t.id                         -- step: children of what we found
// sql> )
// sql> SELECT depth, path FROM tree ORDER BY path
// depth | path
// ------+--------------------------------------
// 0     | Fiction
// 1     | Fiction > Classics
// 1     | Fiction > Fantasy
// 1     | Fiction > Science Fiction
// 0     | Non-fiction
// 1     | Non-fiction > History
// 1     | Non-fiction > Programming
// 2     | Non-fiction > Programming > Databases
// (8 rows)
//
// sql> WITH RECURSIVE ancestors (id, name, parent_id) AS (
// sql>     SELECT id, name, parent_id FROM categories WHERE name = 'Databases'
// sql>     UNION ALL
// sql>     SELECT c.id, c.name, c.parent_id
// sql>     FROM categories c
// sql>     JOIN ancestors a ON c.id = a.parent_id
// sql> )
// sql> SELECT name FROM ancestors
// name
// -----------
// Databases
// Programming
// Non-fiction
// (3 rows)
//
// sql> WITH RECURSIVE sub (id) AS (
// sql>     SELECT id FROM categories WHERE name = 'Non-fiction'
// sql>     UNION ALL
// sql>     SELECT c.id FROM categories c JOIN sub ON c.parent_id = sub.id
// sql> )
// sql> SELECT b.title FROM books b WHERE b.category_id IN (SELECT id FROM sub) ORDER BY b.title
// title
// -------------------------------------
// Designing Data-Intensive Applications
// SPQR
// The C Programming Language
// (3 rows)
//
// §8. WITH RECURSIVE — generating series
// sql> WITH RECURSIVE n (x) AS (
// sql>     SELECT 1
// sql>     UNION ALL
// sql>     SELECT x + 1 FROM n WHERE x < 5                           -- ⚠️ the WHERE stops it. Forget it = infinite loop!
// sql> )
// sql> SELECT x, x * x AS square FROM n
// x | square
// --+-------
// 1 | 1
// 2 | 4
// 3 | 9
// 4 | 16
// 5 | 25
// (5 rows)
//
// sql> WITH RECURSIVE days (day) AS (
// sql>     SELECT date('2025-09-08')
// sql>     UNION ALL
// sql>     SELECT date(day, '+1 day') FROM days WHERE day < '2025-09-14'
// sql> )
// sql> SELECT d.day, count(o.id) AS orders
// sql> FROM days d
// sql> LEFT JOIN orders o ON date(o.created_at) = d.day
// sql> GROUP BY d.day
// sql> ORDER BY d.day
// day        | orders
// -----------+-------
// 2025-09-08 | 0
// 2025-09-09 | 0
// 2025-09-10 | 1
// 2025-09-11 | 0
// 2025-09-12 | 1
// 2025-09-13 | 0
// 2025-09-14 | 0
// (7 rows)
