/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 03 — CRUD: Create, Read, Update, Delete
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson03_crud.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it; the
 *  transcript (every query + its result) is shown in the Output block.
 *  Exercises: exercises/ex03/Ex03.kt
 */
package lesson03_crud

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson03_crud.sql", withBookstore = true)

// Output:
// §1. SELECT: pick columns, name them, compute things
// sql> SELECT title, price_cents FROM books LIMIT 3
// title               | price_cents
// --------------------+------------
// Dune                | 1299
// Pride and Prejudice | 799
// Emma                | 899
// (3 rows)
//
// sql> SELECT title,
// sql>        price_cents / 100 AS wrong_euros,
// sql>        price_cents / 100.0 AS euros,
// sql>        stock * price_cents AS stock_value_cents
// sql> FROM books
// sql> LIMIT 3
// title               | wrong_euros | euros | stock_value_cents
// --------------------+-------------+-------+------------------
// Dune                | 12          | 12.99 | 15588
// Pride and Prejudice | 7           | 7.99  | 23970
// Emma                | 8           | 8.99  | 0
// (3 rows)
//
// §2. WHERE: filter rows
// sql> SELECT title, price_cents FROM books WHERE price_cents < 1000
// title                | price_cents
// ---------------------+------------
// Pride and Prejudice  | 799
// Emma                 | 899
// A Wizard of Earthsea | 999
// I, Robot             | 899
// Collected Notes      | 500
// (5 rows)
//
// sql> SELECT title, published_year, stock
// sql> FROM books
// sql> WHERE (published_year < 1960 OR published_year > 2000) AND stock > 5
// title                 | published_year | stock
// ----------------------+----------------+------
// Pride and Prejudice   | 1813           | 30
// Foundation            | 1951           | 15
// The Hobbit            | 1937           | 20
// The Lord of the Rings | 1954           | 7
// SPQR                  | 2015           | 6
// (5 rows)
//
// sql> SELECT title FROM books WHERE author_id IN (1, 3)
// title
// -------------------------
// Dune
// The Left Hand of Darkness
// A Wizard of Earthsea
// Dune Messiah
// (4 rows)
//
// sql> SELECT title, published_year FROM books WHERE published_year BETWEEN 1960 AND 1970
// title                     | published_year
// --------------------------+---------------
// Dune                      | 1965
// The Left Hand of Darkness | 1969
// A Wizard of Earthsea      | 1968
// Dune Messiah              | 1969
// (4 rows)
//
// sql> SELECT title FROM books WHERE title LIKE 'The %'
// title
// --------------------------
// The Left Hand of Darkness
// The Hobbit
// The Lord of the Rings
// The C Programming Language
// (4 rows)
//
// sql> SELECT title FROM books WHERE category_id = NULL
// title
// -----
// (0 rows)
//
// sql> SELECT title FROM books WHERE category_id IS NULL
// title
// ---------------
// Collected Notes
// (1 row)
//
// §3. ORDER BY, LIMIT, OFFSET, DISTINCT
// sql> SELECT title, price_cents FROM books ORDER BY price_cents DESC LIMIT 3
// title                                 | price_cents
// --------------------------------------+------------
// Designing Data-Intensive Applications | 4599
// The C Programming Language            | 3999
// The Lord of the Rings                 | 2999
// (3 rows)
//
// sql> SELECT author_id, published_year, title FROM books WHERE author_id <= 2 ORDER BY author_id, published_year DESC
// author_id | published_year | title
// ----------+----------------+--------------------
// 1         | 1969           | Dune Messiah
// 1         | 1965           | Dune
// 2         | 1815           | Emma
// 2         | 1813           | Pride and Prejudice
// (4 rows)
//
// sql> SELECT title, published_year FROM books ORDER BY published_year NULLS LAST LIMIT 3
// title               | published_year
// --------------------+---------------
// Pride and Prejudice | 1813
// Emma                | 1815
// The Hobbit          | 1937
// (3 rows)
//
// sql> SELECT title, published_year FROM books ORDER BY published_year DESC NULLS LAST LIMIT 2
// title                                 | published_year
// --------------------------------------+---------------
// Designing Data-Intensive Applications | 2017
// SPQR                                  | 2015
// (2 rows)
//
// sql> SELECT id, title FROM books ORDER BY id LIMIT 4 OFFSET 4
// id | title
// ---+---------------------
// 5  | A Wizard of Earthsea
// 6  | Foundation
// 7  | I, Robot
// 8  | The Hobbit
// (4 rows)
//
// sql> SELECT DISTINCT country FROM authors ORDER BY country
// country
// -------
// UK
// USA
// (2 rows)
//
// §4. CASE — if/else inside a query
// sql> SELECT title,
// sql>        stock,
// sql>        CASE
// sql>            WHEN stock = 0 THEN 'sold out'
// sql>            WHEN stock < 5 THEN 'only a few left'
// sql>            ELSE 'in stock'
// sql>        END AS availability
// sql> FROM books
// sql> ORDER BY stock
// sql> LIMIT 5
// title                      | stock | availability
// ---------------------------+-------+----------------
// Emma                       | 0     | sold out
// Dune Messiah               | 0     | sold out
// Collected Notes            | 1     | only a few left
// The C Programming Language | 2     | only a few left
// I, Robot                   | 3     | only a few left
// (5 rows)
//
// §5. INSERT … RETURNING
// sql> INSERT INTO authors (name, country) VALUES ('N. K. Jemisin', 'USA'), ('Terry Pratchett', 'UK')
// → 2 rows inserted
//
// sql> INSERT INTO books (title, author_id, category_id, price_cents, published_year, stock)
// sql> VALUES ('The Fifth Season', 10, 3, 1399, 2015, 9)
// sql> RETURNING id, title
// id | title
// ---+-----------------
// 15 | The Fifth Season
// (1 row)
//
// §6. UPDATE … WHERE
// sql> UPDATE books SET stock = stock + 10 WHERE id = 3
// → 1 row updated
//
// sql> UPDATE books SET price_cents = price_cents * 90 / 100 WHERE category_id = 4 RETURNING title, price_cents
// title               | price_cents
// --------------------+------------
// Pride and Prejudice | 719
// Emma                | 809
// (2 rows)
//
// §7. DELETE
// sql> DELETE FROM books WHERE id = 1
// ✗ error: [SQLITE_CONSTRAINT_FOREIGNKEY] A foreign key constraint failed (FOREIGN KEY constraint failed)
//
// sql> DELETE FROM books WHERE id = 15 RETURNING title
// title
// ----------------
// The Fifth Season
// (1 row)
//
// sql> DELETE FROM authors WHERE id IN (10, 11)
// → 2 rows deleted
//
// sql> SELECT count(*) AS authors_left FROM authors
// authors_left
// ------------
// 9
// (1 row)
