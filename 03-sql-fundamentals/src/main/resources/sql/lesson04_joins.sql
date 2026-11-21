-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 04 — Joins
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : combine rows from several tables: INNER, LEFT, self joins,
--         many-to-many through a link table, and the classic LEFT JOIN trap.
--  Run  : the green ▶ in lesson04_joins/Joins.kt (bookstore database)
--  Next : lesson 05 — aggregation (COUNT, SUM, GROUP BY)
--
-- Why joins at all? Lesson 02 split data into tables (authors, books…) so
-- each fact is stored ONCE. Joins glue the pieces back together for reading.
--
-- 🧒 Like I'm 5:
--    Kids sit at one table, lunchboxes are stacked on another. Each lunchbox
--    has a sticker with a kid's number. A JOIN walks along and pairs every kid
--    with the lunchbox whose sticker matches.
--    • INNER JOIN: only kids WITH a matching lunchbox (and lunchboxes WITH a kid).
--    • LEFT JOIN:  EVERY kid, plus their lunchbox — or an empty tray (NULL)
--                  for kids who forgot theirs.

-- §1. INNER JOIN — only matching pairs
-- `b` and `a` are table aliases: shorter to type, and needed when two tables
-- have columns with the same name (both have `id`!).
SELECT b.title, a.name AS author
FROM books b
JOIN authors a ON a.id = b.author_id     -- JOIN means INNER JOIN
ORDER BY b.id
LIMIT 5;

-- Book 14 has NO category, so an inner join to categories DROPS it:
SELECT count(*) AS books_total FROM books;
SELECT count(*) AS books_with_category FROM books b JOIN categories c ON c.id = b.category_id;

-- §2. LEFT JOIN — keep everything from the left table
SELECT b.title, c.name AS category
FROM books b
LEFT JOIN categories c ON c.id = b.category_id
WHERE b.id >= 12
ORDER BY b.id;

-- The famous "find what's missing" pattern: LEFT JOIN, then keep rows where
-- the right side is NULL. Authors without books:
SELECT a.name
FROM authors a
LEFT JOIN books b ON b.author_id = a.id
WHERE b.id IS NULL;

-- Books that were never ordered:
SELECT b.title
FROM books b
LEFT JOIN order_items oi ON oi.book_id = b.id
WHERE oi.order_id IS NULL;

-- ⚠️ Gotcha: ON vs WHERE in a LEFT JOIN.
-- Customers and their SHIPPED orders. Filter in WHERE → customers without a
-- shipped order disappear (the LEFT JOIN quietly became an INNER JOIN):
SELECT c.name, o.id AS shipped_order
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
WHERE o.status = 'shipped'
ORDER BY c.id;

-- Filter in ON → every customer stays; non-matching ones get NULL:
SELECT c.name, o.id AS shipped_order
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id AND o.status = 'shipped'
ORDER BY c.id, o.id;

-- §3. Self join — a table joined to itself
-- categories.parent_id points at another row of categories. Use two aliases:
SELECT child.name AS category, parent.name AS parent
FROM categories child
LEFT JOIN categories parent ON parent.id = child.parent_id
ORDER BY child.id;

-- §4. Many-to-many through a link table
-- An order has many books, a book is in many orders. order_items is the
-- "link" (or "junction") table between them.
SELECT o.id AS order_id, b.title, oi.quantity
FROM orders o
JOIN order_items oi ON oi.order_id = o.id
JOIN books b ON b.id = oi.book_id
WHERE o.id IN (3, 5)
ORDER BY o.id, b.title;

-- §5. Joining four tables: who bought what, from whom?
SELECT c.name AS customer, b.title, a.name AS author, o.status
FROM customers c
JOIN orders o       ON o.customer_id = c.id
JOIN order_items oi ON oi.order_id = o.id
JOIN books b        ON b.id = oi.book_id
JOIN authors a      ON a.id = b.author_id
WHERE c.id = 1
ORDER BY o.created_at, b.title;

-- USING (col) is shorthand for ON a.col = b.col when both tables name the
-- column the same. order_items and reviews both have `book_id`:
-- books that were ordered AND reviewed (by anyone):
SELECT DISTINCT book_id
FROM order_items
JOIN reviews USING (book_id)
ORDER BY book_id;
-- (books.id vs order_items.book_id have different names → use ON there.)

-- §6. CROSS JOIN — every combination (rarely what you want)
SELECT s.size, col.color
FROM (SELECT 'S' AS size UNION ALL SELECT 'M') s
CROSS JOIN (SELECT 'red' AS color UNION ALL SELECT 'blue') col;
-- ⚠️ A forgotten ON condition with large tables is an accidental cross join:
-- 10 000 × 10 000 = 100 million rows.

-- RIGHT and FULL OUTER JOIN exist too (SQLite 3.39+). FULL keeps unmatched
-- rows from BOTH sides — e.g. authors without books AND books without authors.
SELECT a.name, b.title
FROM authors a
FULL OUTER JOIN books b ON b.author_id = a.id
WHERE a.id IS NULL OR b.id IS NULL;

-- 🐘 Postgres note:
--  • Same JOIN syntax. Postgres has had RIGHT/FULL JOIN forever.
--  • `JOIN LATERAL (subquery)` lets the subquery use columns of the row on the
--    left — e.g. "each customer's 2 latest orders". SQLite lacks LATERAL;
--    window functions (lesson 07) solve the same problem.
--  • The query planner picks join order and algorithm (hash join, merge join,
--    nested loop). SQLite only does nested loops — so indexes on join columns
--    (lesson 08) matter even more there.

-- Recap
--  • JOIN … ON matches rows; aliases keep it readable.
--  • LEFT JOIN keeps all left rows; `WHERE right.id IS NULL` finds missing ones.
--  • Conditions on the right table of a LEFT JOIN belong in ON, not WHERE.
--  • Self joins for trees; link tables for many-to-many.
