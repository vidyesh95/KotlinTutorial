/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 02 — Constraints and keys
 * ═══════════════════════════════════════════════════════════════════════════
 *  The lesson itself is SQL: src/main/resources/sql/lesson02_constraints_and_keys.sql
 *  Open it side by side with this file. Run ▶ main() below to execute it; the
 *  transcript (every query + its result) is shown in the Output block.
 *  Exercises: exercises/ex02/Ex02.kt
 */
package lesson02_constraints_and_keys

import sqlkit.runSqlLesson

fun main() = runSqlLesson("lesson02_constraints_and_keys.sql")

// Output:
// §1. PRIMARY KEY — every row's unique name tag
// sql> INSERT INTO members (name, email) VALUES ('Ada', 'ada@example.com'), ('Alan', 'alan@example.com')
// → 2 rows inserted
//
// sql> INSERT INTO members (id, name, email) VALUES (10, 'Grace', 'grace@example.com')
// → 1 row inserted
//
// sql> INSERT INTO members (name, email) VALUES ('Linus', 'linus@example.com')
// → 1 row inserted
//
// sql> SELECT id, name FROM members
// id | name
// ---+------
// 1  | Ada
// 2  | Alan
// 10 | Grace
// 11 | Linus
// (4 rows)
//
// sql> INSERT INTO members (id, name, email) VALUES (1, 'Copycat', 'copy@example.com')
// ✗ error: [SQLITE_CONSTRAINT_PRIMARYKEY] A PRIMARY KEY constraint failed (UNIQUE constraint failed: members.id)
//
// sql> DELETE FROM members WHERE id = 11
// → 1 row deleted
//
// sql> INSERT INTO members (name, email) VALUES ('Margaret', 'margaret@example.com')
// → 1 row inserted
//
// sql> SELECT id, name FROM members WHERE name = 'Margaret'
// id | name
// ---+---------
// 11 | Margaret
// (1 row)
//
// §2. NOT NULL and UNIQUE
// sql> INSERT INTO members (name, email) VALUES (NULL, 'nobody@example.com')
// ✗ error: [SQLITE_CONSTRAINT_NOTNULL] A NOT NULL constraint failed (NOT NULL constraint failed: members.name)
//
// sql> INSERT INTO members (name, email) VALUES ('Ada Again', 'ada@example.com')
// ✗ error: [SQLITE_CONSTRAINT_UNIQUE] A UNIQUE constraint failed (UNIQUE constraint failed: members.email)
//
// sql> INSERT INTO votes VALUES (1, 1, 'yes'), (1, 2, 'no'), (2, 1, 'no')
// → 3 rows inserted
//
// sql> INSERT INTO votes VALUES (1, 1, 'changed my mind')
// ✗ error: [SQLITE_CONSTRAINT_PRIMARYKEY] A PRIMARY KEY constraint failed (UNIQUE constraint failed: votes.poll_id, votes.member_id)
//
// §3. CHECK — any rule you can write as an expression
// sql> INSERT INTO products (name, price_cents) VALUES ('Pen', 150)
// → 1 row inserted
//
// sql> INSERT INTO products (name, price_cents) VALUES ('Broken', -5)
// ✗ error: [SQLITE_CONSTRAINT_CHECK] A CHECK constraint failed (CHECK constraint failed: price_cents >= 0)
//
// sql> INSERT INTO products (name, price_cents, status) VALUES ('Mug', 900, 'deleted')
// ✗ error: [SQLITE_CONSTRAINT_CHECK] A CHECK constraint failed (CHECK constraint failed: status IN ('draft', 'active', 'archived'))
//
// sql> SELECT * FROM products
// id | name | price_cents | status
// ---+------+-------------+-------
// 1  | Pen  | 150         | draft
// (1 row)
//
// §4. FOREIGN KEY — "this label must point to something that exists"
// sql> INSERT INTO shelves VALUES (1, 'Sci-Fi'), (2, 'Classics')
// → 2 rows inserted
//
// sql> INSERT INTO library_books VALUES (1, 'Dune', 1), (2, 'Emma', 2), (3, 'Foundation', 1)
// → 3 rows inserted
//
// sql> INSERT INTO library_books VALUES (4, 'Ghost Book', 99)
// ✗ error: [SQLITE_CONSTRAINT_FOREIGNKEY] A foreign key constraint failed (FOREIGN KEY constraint failed)
//
// sql> INSERT INTO loans VALUES (1, 1, 'ada')
// → 1 row inserted
//
// sql> INSERT INTO book_notes VALUES (1, 2, 'first edition'), (2, 2, 'slightly torn')
// → 2 rows inserted
//
// sql> DELETE FROM library_books WHERE id = 1
// ✗ error: [SQLITE_CONSTRAINT_TRIGGER] A RAISE function within a trigger fired, causing the SQL statement to abort (FOREIGN KEY constraint failed)
//
// sql> DELETE FROM library_books WHERE id = 2
// → 1 row deleted
//
// sql> SELECT count(*) AS notes_left FROM book_notes
// notes_left
// ----------
// 0
// (1 row)
//
// sql> DELETE FROM shelves WHERE id = 1
// → 1 row deleted
//
// sql> SELECT id, title, shelf_id FROM library_books
// id | title      | shelf_id
// ---+------------+---------
// 1  | Dune       | NULL
// 3  | Foundation | NULL
// (2 rows)
