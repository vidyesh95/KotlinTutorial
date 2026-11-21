-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 06 — Subqueries and CTEs (WITH, including recursive)
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : use the result of one query inside another — as a value, a list, a
--         yes/no test or a temporary table — and name those steps with WITH.
--         Then walk trees and generate series with WITH RECURSIVE.
--  Run  : the green ▶ in lesson06_subqueries_and_ctes/SubqueriesAndCtes.kt
--  Next : lesson 07 — window functions
--
-- 🧒 Like I'm 5:
--    A subquery is a question you must answer FIRST to answer the big question.
--    "Which books cost more than average?" → first: "what IS the average?"
--    You whisper the small question to a helper, they come back with the
--    answer, and you use it in your big question.

-- §1. Scalar subquery — a query that returns ONE value
-- Wrap it in ( ) and use it anywhere a single value fits.
SELECT title, price_cents
FROM books
WHERE price_cents > (SELECT avg(price_cents) FROM books)
ORDER BY price_cents DESC;

-- It also works in the SELECT list: each book next to the shop average.
SELECT title,
       price_cents,
       (SELECT round(avg(price_cents)) FROM books) AS shop_avg
FROM books
WHERE author_id = 1
ORDER BY id;

-- ⚠️ Gotcha: a scalar subquery must return at most one row. In SQLite extra
-- rows are silently ignored; in Postgres it's an ERROR ("more than one row
-- returned by a subquery used as an expression"). Make sure it's one row.

-- §2. IN (subquery) — a query that returns a LIST
-- Books written by UK authors:
SELECT title
FROM books
WHERE author_id IN (SELECT id FROM authors WHERE country = 'UK')
ORDER BY title;

-- ⚠️ Gotcha: NOT IN + a NULL in the list = no rows at all! `x NOT IN (1, NULL)`
-- is "x <> 1 AND x <> NULL" → unknown → filtered out. Prefer NOT EXISTS.
SELECT count(*) AS books_not_in_list_with_null
FROM books
WHERE category_id NOT IN (SELECT category_id FROM books WHERE id = 14);

-- §3. EXISTS / NOT EXISTS — "is there at least one…?"
-- EXISTS only asks whether the subquery returns ANY row; the columns don't
-- matter (people write SELECT 1). It stops at the first match → fast.
-- Customers who never placed an order:
SELECT c.name
FROM customers c
WHERE NOT EXISTS (SELECT 1 FROM orders o WHERE o.customer_id = c.id);

-- Authors with at least one book that was reviewed 5 stars:
SELECT a.name
FROM authors a
WHERE EXISTS (
    SELECT 1
    FROM books b
    JOIN reviews r ON r.book_id = b.id
    WHERE b.author_id = a.id AND r.rating = 5
)
ORDER BY a.name;

-- §4. Correlated subqueries — the inner query looks at the outer row
-- The `o.customer_id = c.id` above already was one: the inner query is
-- re-evaluated for EVERY outer row. Here: each customer's latest order date.
SELECT c.name,
       (SELECT max(o.created_at) FROM orders o WHERE o.customer_id = c.id) AS last_order
FROM customers c
ORDER BY c.id;

-- Books priced above the average OF THEIR OWN CATEGORY:
SELECT b.title, b.price_cents, b.category_id
FROM books b
WHERE b.price_cents > (
    SELECT avg(b2.price_cents) FROM books b2 WHERE b2.category_id = b.category_id
)
ORDER BY b.category_id, b.title;

-- §5. Derived tables — a subquery in FROM is a temporary table
-- Total spent per order first, then the average order value per customer.
SELECT customer_id, count(*) AS orders, round(avg(order_total)) AS avg_order_cents
FROM (
    SELECT o.id, o.customer_id, sum(oi.quantity * oi.unit_price_cents) AS order_total
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    WHERE o.status <> 'cancelled'
    GROUP BY o.id
) AS order_totals
GROUP BY customer_id
ORDER BY customer_id;

-- §6. WITH — give each step a name (Common Table Expressions, CTEs)
-- Same query, but readable top to bottom like a recipe. Several CTEs can
-- follow each other, and later ones may use earlier ones.
WITH order_totals AS (
    SELECT o.id, o.customer_id, sum(oi.quantity * oi.unit_price_cents) AS total
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    WHERE o.status <> 'cancelled'
    GROUP BY o.id
),
customer_spend AS (
    SELECT customer_id, sum(total) AS spent
    FROM order_totals
    GROUP BY customer_id
)
SELECT c.name, cs.spent
FROM customer_spend cs
JOIN customers c ON c.id = cs.customer_id
WHERE cs.spent > (SELECT avg(spent) FROM customer_spend)
ORDER BY cs.spent DESC;

-- 🏗️ In the real world: reports and analytics queries are mostly CTEs. They
-- read like small functions — "first compute this, then that".

-- §7. WITH RECURSIVE — walking a tree
-- categories.parent_id points to the parent category, forming a tree:
--   Fiction → Science Fiction, Fantasy, Classics
--   Non-fiction → Programming → Databases, and History
--
-- 🧒 Like I'm 5:
--    Russian dolls. Open the big doll (the "anchor": the top categories), and
--    inside you find smaller dolls (their children). Open each of those, and
--    so on, until a doll is empty. A recursive CTE = "start here, then keep
--    repeating this step on what you just found, until nothing new appears".
--
-- Shape:  anchor query  UNION ALL  recursive step that joins to the CTE itself.
WITH RECURSIVE tree (id, name, depth, path) AS (
    SELECT id, name, 0, name
    FROM categories
    WHERE parent_id IS NULL                                   -- anchor: the roots
    UNION ALL
    SELECT c.id, c.name, t.depth + 1, t.path || ' > ' || c.name
    FROM categories c
    JOIN tree t ON c.parent_id = t.id                         -- step: children of what we found
)
SELECT depth, path FROM tree ORDER BY path;

-- Walking UP: all ancestors of 'Databases' (ask your parent, who asks theirs…)
WITH RECURSIVE ancestors (id, name, parent_id) AS (
    SELECT id, name, parent_id FROM categories WHERE name = 'Databases'
    UNION ALL
    SELECT c.id, c.name, c.parent_id
    FROM categories c
    JOIN ancestors a ON c.id = a.parent_id
)
SELECT name FROM ancestors;

-- Every book in 'Non-fiction' or ANY of its sub-categories:
WITH RECURSIVE sub (id) AS (
    SELECT id FROM categories WHERE name = 'Non-fiction'
    UNION ALL
    SELECT c.id FROM categories c JOIN sub ON c.parent_id = sub.id
)
SELECT b.title FROM books b WHERE b.category_id IN (SELECT id FROM sub) ORDER BY b.title;

-- §8. WITH RECURSIVE — generating series
-- No table needed: count from 1 to 5…
WITH RECURSIVE n (x) AS (
    SELECT 1
    UNION ALL
    SELECT x + 1 FROM n WHERE x < 5                           -- ⚠️ the WHERE stops it. Forget it = infinite loop!
)
SELECT x, x * x AS square FROM n;

-- …or a calendar: every day of a week, LEFT JOINed to orders, so days with
-- zero orders still show up (a classic reporting trick).
WITH RECURSIVE days (day) AS (
    SELECT date('2025-09-08')
    UNION ALL
    SELECT date(day, '+1 day') FROM days WHERE day < '2025-09-14'
)
SELECT d.day, count(o.id) AS orders
FROM days d
LEFT JOIN orders o ON date(o.created_at) = d.day
GROUP BY d.day
ORDER BY d.day;

-- 🐘 Postgres note: subqueries, EXISTS, WITH and WITH RECURSIVE have the same
-- syntax. Extras: generate_series(1, 5) and generate_series(date1, date2,
-- '1 day') replace §8's recursion; `WITH x AS MATERIALIZED (…)` / NOT
-- MATERIALIZED tells the planner whether to compute a CTE once or inline it
-- (SQLite accepts the same hint). Postgres also has LATERAL joins — a
-- correlated subquery that can return several columns/rows.

-- Recap
--  • ( SELECT … ) can be a value (scalar), a list (IN), a test (EXISTS) or a table (FROM).
--  • Correlated = inner query uses the outer row; runs per row.
--  • Prefer NOT EXISTS over NOT IN when NULLs are possible.
--  • WITH names steps; WITH RECURSIVE = anchor UNION ALL step — trees and series.
