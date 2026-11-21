/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 01 — Tables and types
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson01_tables_and_types.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it; the
 *  transcript (every query + its result) is shown in the Output block.
 *  Exercises: exercises/ex01/Ex01.kt
 */
package lesson01_tables_and_types

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson01_tables_and_types.sql")

// Output:
// §1. Creating a table
// sql> INSERT INTO notes (title, body) VALUES ('Learn SQL', 'start with tables')
// → 1 row inserted
//
// sql> INSERT INTO notes (title, pinned) VALUES ('Buy milk', 1)
// → 1 row inserted
//
// sql> SELECT * FROM notes
// id | title     | body              | pinned | created_at
// ---+-----------+-------------------+--------+---------------------
// 1  | Learn SQL | start with tables | 0      | 2026-01-01T00:00:00Z
// 2  | Buy milk  | NULL              | 1      | 2026-01-01T00:00:00Z
// (2 rows)
//
// §2. Types — SQLite is relaxed, Postgres is strict
// sql> INSERT INTO loose (n, label) VALUES
// sql>     (42, 'a number'),
// sql>     ('42', 'numeric text, converted'),
// sql>     ('hello', 'text kept as TEXT!'),
// sql>     (4.0, 'real 4.0 becomes 4'),
// sql>     (4.5, 'real stays real')
// → 5 rows inserted
//
// sql> SELECT n, typeof(n) AS stored_as, label FROM loose
// n     | stored_as | label
// ------+-----------+------------------------
// 42    | integer   | a number
// 42    | integer   | numeric text, converted
// hello | text      | text kept as TEXT!
// 4     | integer   | real 4.0 becomes 4
// 4.5   | real      | real stays real
// (5 rows)
//
// sql> INSERT INTO strict_numbers (n) VALUES ('42')
// → 1 row inserted
//
// sql> INSERT INTO strict_numbers (n) VALUES ('hello')
// ✗ error: [SQLITE_CONSTRAINT_DATATYPE] An insert or update attempted to store a value inconsistent with the column's declared type in a table defined as STRICT (cannot store TEXT value in INTEGER column strict_numbers.n)
//
// sql> SELECT TRUE AS yes, FALSE AS no, typeof(TRUE) AS stored_as, 3 > 2 AS comparison
// yes | no | stored_as | comparison
// ----+----+-----------+-----------
// 1   | 0  | integer   | 1
// (1 row)
//
// §3. NULL — "unknown / missing"
// sql> SELECT title, body, body IS NULL AS no_body, coalesce(body, '(empty)') AS shown FROM notes
// title     | body              | no_body | shown
// ----------+-------------------+---------+------------------
// Learn SQL | start with tables | 0       | start with tables
// Buy milk  | NULL              | 1       | (empty)
// (2 rows)
//
// sql> SELECT NULL = NULL AS equals, NULL IS NULL AS is_null, NULL + 1 AS plus_one
// equals | is_null | plus_one
// -------+---------+---------
// NULL   | 1       | NULL
// (1 row)
//
// §4. Changing and inspecting tables
// sql> SELECT id, title, color FROM notes
// id | title     | color
// ---+-----------+-------
// 1  | Learn SQL | yellow
// 2  | Buy milk  | yellow
// (2 rows)
//
// sql> SELECT name FROM sqlite_schema WHERE type = 'table' ORDER BY name
// name
// --------------
// loose
// notes
// strict_numbers
// (3 rows)
//
// sql> SELECT name, type, "notnull", dflt_value, pk FROM pragma_table_info('notes')
// name       | type    | notnull | dflt_value             | pk
// -----------+---------+---------+------------------------+---
// id         | INTEGER | 0       | NULL                   | 1
// title      | TEXT    | 1       | NULL                   | 0
// body       | TEXT    | 0       | NULL                   | 0
// pinned     | INTEGER | 1       | 0                      | 0
// created_at | TEXT    | 1       | '2026-01-01T00:00:00Z' | 0
// color      | TEXT    | 0       | 'yellow'               | 0
// (6 rows)
//
// sql> SELECT count(*) AS tables_left FROM sqlite_schema WHERE type = 'table'
// tables_left
// -----------
// 2
// (1 row)
