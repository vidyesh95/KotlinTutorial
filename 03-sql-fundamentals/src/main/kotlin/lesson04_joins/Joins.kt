/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 04 — Joins
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson04_joins.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it; the
 *  transcript (every query + its result) is shown in the Output block.
 *  Exercises: exercises/ex04/Ex04.kt
 */
package lesson04_joins

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson04_joins.sql", withBookstore = true)

// Output:
// §1. INNER JOIN — only matching pairs
// sql> SELECT b.title, a.name AS author
// sql> FROM books b
// sql> JOIN authors a ON a.id = b.author_id     -- JOIN means INNER JOIN
// sql> ORDER BY b.id
// sql> LIMIT 5
// title                     | author
// --------------------------+------------------
// Dune                      | Frank Herbert
// Pride and Prejudice       | Jane Austen
// Emma                      | Jane Austen
// The Left Hand of Darkness | Ursula K. Le Guin
// A Wizard of Earthsea      | Ursula K. Le Guin
// (5 rows)
//
// sql> SELECT count(*) AS books_total FROM books
// books_total
// -----------
// 14
// (1 row)
//
// sql> SELECT count(*) AS books_with_category FROM books b JOIN categories c ON c.id = b.category_id
// books_with_category
// -------------------
// 13
// (1 row)
//
// §2. LEFT JOIN — keep everything from the left table
// sql> SELECT b.title, c.name AS category
// sql> FROM books b
// sql> LEFT JOIN categories c ON c.id = b.category_id
// sql> WHERE b.id >= 12
// sql> ORDER BY b.id
// title           | category
// ----------------+----------------
// SPQR            | History
// Dune Messiah    | Science Fiction
// Collected Notes | NULL
// (3 rows)
//
// sql> SELECT a.name
// sql> FROM authors a
// sql> LEFT JOIN books b ON b.author_id = a.id
// sql> WHERE b.id IS NULL
// name
// -----------------
// Octavia E. Butler
// (1 row)
//
// sql> SELECT b.title
// sql> FROM books b
// sql> LEFT JOIN order_items oi ON oi.book_id = b.id
// sql> WHERE oi.order_id IS NULL
// title
// ---------------
// Collected Notes
// (1 row)
//
// sql> SELECT c.name, o.id AS shipped_order
// sql> FROM customers c
// sql> LEFT JOIN orders o ON o.customer_id = c.id
// sql> WHERE o.status = 'shipped'
// sql> ORDER BY c.id
// name              | shipped_order
// ------------------+--------------
// Ada Lovelace      | 2
// Alan Turing       | 9
// Grace Hopper      | 5
// Margaret Hamilton | 10
// (4 rows)
//
// sql> SELECT c.name, o.id AS shipped_order
// sql> FROM customers c
// sql> LEFT JOIN orders o ON o.customer_id = c.id AND o.status = 'shipped'
// sql> ORDER BY c.id, o.id
// name              | shipped_order
// ------------------+--------------
// Ada Lovelace      | 2
// Alan Turing       | 9
// Grace Hopper      | 5
// Linus Torvalds    | NULL
// Margaret Hamilton | 10
// Edsger Dijkstra   | NULL
// (6 rows)
//
// §3. Self join — a table joined to itself
// sql> SELECT child.name AS category, parent.name AS parent
// sql> FROM categories child
// sql> LEFT JOIN categories parent ON parent.id = child.parent_id
// sql> ORDER BY child.id
// category        | parent
// ----------------+------------
// Fiction         | NULL
// Science Fiction | Fiction
// Fantasy         | Fiction
// Classics        | Fiction
// Non-fiction     | NULL
// Programming     | Non-fiction
// History         | Non-fiction
// Databases       | Programming
// (8 rows)
//
// §4. Many-to-many through a link table
// sql> SELECT o.id AS order_id, b.title, oi.quantity
// sql> FROM orders o
// sql> JOIN order_items oi ON oi.order_id = o.id
// sql> JOIN books b ON b.id = oi.book_id
// sql> WHERE o.id IN (3, 5)
// sql> ORDER BY o.id, b.title
// order_id | title               | quantity
// ---------+---------------------+---------
// 3        | Dune                | 1
// 3        | Foundation          | 1
// 3        | I, Robot            | 1
// 5        | Emma                | 1
// 5        | Pride and Prejudice | 3
// (5 rows)
//
// §5. Joining four tables: who bought what, from whom?
// sql> SELECT c.name AS customer, b.title, a.name AS author, o.status
// sql> FROM customers c
// sql> JOIN orders o       ON o.customer_id = c.id
// sql> JOIN order_items oi ON oi.order_id = o.id
// sql> JOIN books b        ON b.id = oi.book_id
// sql> JOIN authors a      ON a.id = b.author_id
// sql> WHERE c.id = 1
// sql> ORDER BY o.created_at, b.title
// customer     | title                                 | author           | status
// -------------+---------------------------------------+------------------+--------
// Ada Lovelace | Designing Data-Intensive Applications | Martin Kleppmann | paid
// Ada Lovelace | Dune                                  | Frank Herbert    | paid
// Ada Lovelace | The Hobbit                            | J.R.R. Tolkien   | shipped
// Ada Lovelace | SPQR                                  | Mary Beard       | pending
// (4 rows)
//
// sql> SELECT DISTINCT book_id
// sql> FROM order_items
// sql> JOIN reviews USING (book_id)
// sql> ORDER BY book_id
// book_id
// -------
// 1
// 2
// 6
// 8
// 9
// 10
// 11
// 12
// (8 rows)
//
// §6. CROSS JOIN — every combination (rarely what you want)
// sql> SELECT s.size, col.color
// sql> FROM (SELECT 'S' AS size UNION ALL SELECT 'M') s
// sql> CROSS JOIN (SELECT 'red' AS color UNION ALL SELECT 'blue') col
// size | color
// -----+------
// S    | red
// S    | blue
// M    | red
// M    | blue
// (4 rows)
//
// sql> SELECT a.name, b.title
// sql> FROM authors a
// sql> FULL OUTER JOIN books b ON b.author_id = a.id
// sql> WHERE a.id IS NULL OR b.id IS NULL
// name              | title
// ------------------+------
// Octavia E. Butler | NULL
// (1 row)
