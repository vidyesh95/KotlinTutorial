/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 08 — Indexes and query plans
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson08_indexes_and_query_plans.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it on the
 *  bookstore database; the transcript (every query + its result) is the Output block.
 *  Exercises: exercises/ex08/Ex08.kt
 */
package lesson08_indexes_and_query_plans

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson08_indexes_and_query_plans.sql", withBookstore = true)

// Output:
// §1. A bigger table to play with
// sql> INSERT INTO big_customers (id, email, city, vip, created_at)
// sql> WITH RECURSIVE n (i) AS (SELECT 1 UNION ALL SELECT i + 1 FROM n WHERE i < 10000)
// sql> SELECT i,
// sql>        'user' || i || '@example.com',
// sql>        CASE i % 4 WHEN 0 THEN 'London' WHEN 1 THEN 'Paris' WHEN 2 THEN 'Berlin' ELSE 'Madrid' END,
// sql>        CASE WHEN i % 100 = 0 THEN 1 ELSE 0 END,
// sql>        date('2025-01-01', '+' || (i % 365) || ' days')
// sql> FROM n
// → 10000 rows inserted
//
// §2. Reading a plan: SCAN vs SEARCH
// sql> EXPLAIN QUERY PLAN SELECT * FROM big_customers WHERE id = 42
// id | parent | notused | detail
// ---+--------+---------+---------------------------------------------------------
// 2  | 0      | 33      | SEARCH big_customers USING INTEGER PRIMARY KEY (rowid=?)
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT * FROM big_customers WHERE email = 'user42@example.com'
// id | parent | notused | detail
// ---+--------+---------+-------------------
// 2  | 0      | 216     | SCAN big_customers
// (1 row)
//
// §3. CREATE INDEX — and the plan changes
// sql> EXPLAIN QUERY PLAN SELECT * FROM big_customers WHERE email = 'user42@example.com'
// id | parent | notused | detail
// ---+--------+---------+-------------------------------------------------------------------
// 3  | 0      | 62      | SEARCH big_customers USING INDEX idx_big_customers_email (email=?)
// (1 row)
//
// sql> SELECT id, email, city FROM big_customers WHERE email = 'user42@example.com'
// id | email              | city
// ---+--------------------+-------
// 42 | user42@example.com | Berlin
// (1 row)
//
// sql> INSERT INTO big_customers (email, city, vip, created_at) VALUES ('user42@example.com', 'Rome', 0, '2025-01-01')
// ✗ error: [SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: big_customers.email)
//
// §4. Index your foreign keys
// sql> EXPLAIN QUERY PLAN SELECT title FROM books WHERE author_id = 2
// id | parent | notused | detail
// ---+--------+---------+-----------
// 2  | 0      | 216     | SCAN books
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT title FROM books WHERE author_id = 2
// id | parent | notused | detail
// ---+--------+---------+-----------------------------------------------------------
// 3  | 0      | 61      | SEARCH books USING INDEX idx_books_author_id (author_id=?)
// (1 row)
//
// §5. Composite indexes and the "leftmost prefix" rule
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE city = 'Paris' AND created_at >= '2025-12-01'
// id | parent | notused | detail
// ---+--------+---------+---------------------------------------------------------------------------------------------------
// 2  | 0      | 47      | SEARCH big_customers USING COVERING INDEX idx_big_customers_city_created (city=? AND created_at>?)
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE city = 'Paris'
// id | parent | notused | detail
// ---+--------+---------+----------------------------------------------------------------------------------
// 2  | 0      | 55      | SEARCH big_customers USING COVERING INDEX idx_big_customers_city_created (city=?)
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE created_at = '2025-06-01'
// id | parent | notused | detail
// ---+--------+---------+-----------------------------------------------------------------------
// 2  | 0      | 214     | SCAN big_customers USING COVERING INDEX idx_big_customers_city_created
// (1 row)
//
// §6. Covering indexes — answer from the index alone
// sql> EXPLAIN QUERY PLAN SELECT id, created_at FROM big_customers WHERE city = 'Berlin'
// id | parent | notused | detail
// ---+--------+---------+----------------------------------------------------------------------------------
// 2  | 0      | 55      | SEARCH big_customers USING COVERING INDEX idx_big_customers_city_created (city=?)
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT id, email FROM big_customers WHERE city = 'Berlin'
// id | parent | notused | detail
// ---+--------+---------+-------------------------------------------------------------------------
// 3  | 0      | 62      | SEARCH big_customers USING INDEX idx_big_customers_city_created (city=?)
// (1 row)
//
// §7. Partial and expression indexes
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE vip = 1 AND created_at > '2025-06-01'
// id | parent | notused | detail
// ---+--------+---------+-------------------------------------------------------------------------------
// 2  | 0      | 182     | SEARCH big_customers USING COVERING INDEX idx_big_customers_vip (created_at>?)
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE lower(email) = 'user7@example.com'
// id | parent | notused | detail
// ---+--------+---------+-----------------------------------------------------------------------------------
// 3  | 0      | 52      | SEARCH big_customers USING COVERING INDEX idx_big_customers_email_lower (<expr>=?)
// (1 row)
//
// §8. When an index can't help
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE substr(email, 1, 6) = 'user42'
// id | parent | notused | detail
// ---+--------+---------+---------------------------------------------------------------
// 2  | 0      | 212     | SCAN big_customers USING COVERING INDEX ux_big_customers_email
// (1 row)
//
// sql> EXPLAIN QUERY PLAN SELECT id FROM big_customers WHERE email LIKE '%42@example.com'
// id | parent | notused | detail
// ---+--------+---------+---------------------------------------------------------------
// 2  | 0      | 212     | SCAN big_customers USING COVERING INDEX ux_big_customers_email
// (1 row)
//
// sql> SELECT vip, count(*) AS customers FROM big_customers GROUP BY vip
// vip | customers
// ----+----------
// 0   | 9900
// 1   | 100
// (2 rows)
//
// §9. The cost: writes and space
// sql> SELECT name FROM sqlite_schema WHERE type = 'index' AND tbl_name = 'big_customers' ORDER BY name
// name
// ------------------------------
// idx_big_customers_city_created
// idx_big_customers_email_lower
// idx_big_customers_vip
// ux_big_customers_email
// (4 rows)
