/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 10 — Normalization and schema design
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson10_normalization_and_design.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it on an
 *  EMPTY database; the transcript (every query + its result) is the Output block.
 *  Exercises: exercises/ex10/Ex10.kt
 */
package lesson10_normalization_and_design

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson10_normalization_and_design.sql", withBookstore = false)

// Output:
// §1. The messy starting point
// sql> INSERT INTO orders_flat VALUES
// sql>     (1, 'ada@example.com', 'Ada Lovelace', 'London', 'Dune', 'Frank Herbert', 1299, 1, 'gift,express'),
// sql>     (1, 'ada@example.com', 'Ada Lovelace', 'London', 'Emma', 'Jane Austen', 899, 2, 'gift,express'),
// sql>     (2, 'ada@example.com', 'Ada Lovelace', 'London', 'Dune', 'Frank Herbert', 1299, 1, ''),
// sql>     (3, 'alan@example.com', 'Alan Turing', 'Manchester', 'Dune', 'Frank Herbert', 1299, 3, 'express')
// → 4 rows inserted
//
// sql> SELECT order_id, customer_email, customer_city, book_title, tags FROM orders_flat
// order_id | customer_email   | customer_city | book_title | tags
// ---------+------------------+---------------+------------+-------------
// 1        | ada@example.com  | London        | Dune       | gift,express
// 1        | ada@example.com  | London        | Emma       | gift,express
// 2        | ada@example.com  | London        | Dune       |
// 3        | alan@example.com | Manchester    | Dune       | express
// (4 rows)
//
// §2. What goes wrong ("anomalies")
// sql> UPDATE orders_flat SET customer_city = 'Paris' WHERE order_id = 2
// → 1 row updated
//
// sql> SELECT DISTINCT customer_email, customer_city FROM orders_flat WHERE customer_email = 'ada@example.com'
// customer_email  | customer_city
// ----------------+--------------
// ada@example.com | London
// ada@example.com | Paris
// (2 rows)
//
// sql> SELECT DISTINCT order_id FROM orders_flat WHERE tags LIKE '%express%'
// order_id
// --------
// 1
// 3
// (2 rows)
//
// §3. First normal form (1NF): one value per cell, no repeating lists
// sql> INSERT INTO order_tags (order_id, tag)
// sql> SELECT DISTINCT f.order_id, t.value
// sql> FROM orders_flat f, json_each('["' || replace(f.tags, ',', '","') || '"]') t
// sql> WHERE f.tags <> ''
// → 3 rows inserted
//
// sql> SELECT order_id FROM order_tags WHERE tag = 'express' ORDER BY order_id
// order_id
// --------
// 1
// 3
// (2 rows)
//
// §4. Second normal form (2NF): every column depends on the WHOLE key
// §5. Third normal form (3NF): no facts about OTHER facts
// sql> INSERT INTO customers (email, name, city)
// sql> SELECT customer_email, customer_name, 'London' FROM orders_flat WHERE customer_email = 'ada@example.com' LIMIT 1
// → 1 row inserted
//
// sql> INSERT INTO customers (email, name, city)
// sql> SELECT DISTINCT customer_email, customer_name, customer_city FROM orders_flat WHERE customer_email <> 'ada@example.com'
// → 1 row inserted
//
// sql> INSERT INTO authors (name) SELECT DISTINCT author_name FROM orders_flat ORDER BY author_name
// → 2 rows inserted
//
// sql> INSERT INTO books (title, author_id, price_cents)
// sql> SELECT DISTINCT f.book_title, a.id, f.price_cents FROM orders_flat f JOIN authors a ON a.name = f.author_name ORDER BY f.book_title
// → 2 rows inserted
//
// sql> INSERT INTO orders (id, customer_id)
// sql> SELECT DISTINCT f.order_id, c.id FROM orders_flat f JOIN customers c ON c.email = f.customer_email ORDER BY f.order_id
// → 3 rows inserted
//
// sql> INSERT INTO order_items (order_id, book_id, quantity, unit_price_cents)
// sql> SELECT f.order_id, b.id, f.quantity, f.price_cents FROM orders_flat f JOIN books b ON b.title = f.book_title
// → 4 rows inserted
//
// sql> UPDATE customers SET city = 'Paris' WHERE email = 'ada@example.com'
// → 1 row updated
//
// sql> SELECT o.id AS order_id, c.email, c.city, b.title, a.name AS author, oi.quantity
// sql> FROM order_items oi
// sql> JOIN orders o ON o.id = oi.order_id
// sql> JOIN customers c ON c.id = o.customer_id
// sql> JOIN books b ON b.id = oi.book_id
// sql> JOIN authors a ON a.id = b.author_id
// sql> ORDER BY o.id, b.title
// order_id | email            | city       | title | author        | quantity
// ---------+------------------+------------+-------+---------------+---------
// 1        | ada@example.com  | Paris      | Dune  | Frank Herbert | 1
// 1        | ada@example.com  | Paris      | Emma  | Jane Austen   | 2
// 2        | ada@example.com  | Paris      | Dune  | Frank Herbert | 1
// 3        | alan@example.com | Manchester | Dune  | Frank Herbert | 3
// (4 rows)
//
// sql> INSERT INTO customers (email, name) VALUES ('grace@example.com', 'Grace Hopper')
// → 1 row inserted
//
// sql> SELECT count(*) AS customers FROM customers
// customers
// ---------
// 3
// (1 row)
//
// §6. Denormalizing on purpose
// sql> UPDATE books SET price_cents = 1599 WHERE title = 'Dune'
// → 1 row updated
//
// sql> SELECT b.title, b.price_cents AS price_today, oi.unit_price_cents AS price_paid, oi.order_id
// sql> FROM order_items oi JOIN books b ON b.id = oi.book_id
// sql> WHERE b.title = 'Dune'
// sql> ORDER BY oi.order_id
// title | price_today | price_paid | order_id
// ------+-------------+------------+---------
// Dune  | 1599        | 1299       | 1
// Dune  | 1599        | 1299       | 2
// Dune  | 1599        | 1299       | 3
// (3 rows)
//
// §7. Keys and naming conventions
// §8. A real-world schema: authentication (better-auth style)
// sql> INSERT INTO "user" VALUES ('u_1', 'Ada', 'ada@example.com', 1, NULL, '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
// → 1 row inserted
//
// sql> INSERT INTO account (id, user_id, provider_id, account_id, password, created_at, updated_at)
// sql> VALUES ('a_1', 'u_1', 'credential', 'u_1', '$argon2id$…hash…', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
// sql>        ('a_2', 'u_1', 'github', '583231', NULL, '2026-01-02T00:00:00Z', '2026-01-02T00:00:00Z')
// → 2 rows inserted
//
// sql> INSERT INTO session (id, user_id, token, expires_at, created_at, updated_at)
// sql> VALUES ('s_1', 'u_1', 'tok_laptop', '2026-01-08T00:00:00Z', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z'),
// sql>        ('s_2', 'u_1', 'tok_phone', '2026-01-08T00:00:00Z', '2026-01-01T00:00:00Z', '2026-01-01T00:00:00Z')
// → 2 rows inserted
//
// sql> SELECT u.email, a.provider_id, count(DISTINCT s.id) AS active_sessions
// sql> FROM "user" u
// sql> JOIN account a ON a.user_id = u.id
// sql> LEFT JOIN session s ON s.user_id = u.id
// sql> GROUP BY u.id, a.provider_id
// sql> ORDER BY a.provider_id
// email           | provider_id | active_sessions
// ----------------+-------------+----------------
// ada@example.com | credential  | 2
// ada@example.com | github      | 2
// (2 rows)
//
// sql> INSERT INTO account (id, user_id, provider_id, account_id, created_at, updated_at)
// sql> VALUES ('a_3', 'u_1', 'github', '583231', '2026-01-03T00:00:00Z', '2026-01-03T00:00:00Z')
// ✗ error: [SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: account.provider_id, account.account_id)
