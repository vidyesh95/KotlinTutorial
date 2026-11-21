-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 05 — Aggregation: COUNT, SUM, AVG, GROUP BY, HAVING
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : turn many rows into a few numbers: totals, averages, counts per
--         group — the stuff every dashboard and report is made of.
--  Run  : the green ▶ in lesson05_aggregation/Aggregation.kt (bookstore database)
--  Next : lesson 06 — subqueries and CTEs
--
-- 🧒 Like I'm 5:
--    You dump a bag of LEGO on the floor. "How many bricks?" is COUNT.
--    "Sort them into piles by colour, then count each pile" is GROUP BY.
--    "Only tell me about piles with more than 10 bricks" is HAVING.

-- §1. Aggregate functions over the whole table
SELECT count(*)         AS books,
       sum(stock)       AS items_in_stock,
       min(price_cents) AS cheapest,
       max(price_cents) AS dearest,
       avg(price_cents) AS average_price
FROM books;

-- COUNT(*) counts rows. COUNT(column) counts NON-NULL values. COUNT(DISTINCT x)
-- counts different values. Aggregates (except count(*)) IGNORE NULLs.
SELECT count(*)                    AS reviews,
       count(body)                 AS reviews_with_text,
       count(DISTINCT customer_id) AS distinct_reviewers
FROM reviews;

-- ⚠️ Gotcha: avg() of INTEGERs returns a REAL in SQLite, but sum()/count()
-- is integer division. And money averages want rounding:
SELECT sum(rating) / count(rating)        AS integer_division,
       avg(rating)                        AS avg_rating,
       round(avg(rating), 2)              AS rounded,
       round(avg(price_cents) / 100.0, 2) AS avg_reviewed_price_euros
FROM reviews JOIN books ON books.id = reviews.book_id;   -- one row per review

-- §2. GROUP BY — one result row per group
-- Every selected column must be either in GROUP BY or inside an aggregate.
SELECT status, count(*) AS orders
FROM orders
GROUP BY status
ORDER BY orders DESC, status;

-- Revenue per customer: join first, then group.
SELECT c.name,
       count(DISTINCT o.id)                    AS orders,
       sum(oi.quantity * oi.unit_price_cents)  AS revenue_cents
FROM customers c
JOIN orders o       ON o.customer_id = c.id
JOIN order_items oi ON oi.order_id = o.id
WHERE o.status <> 'cancelled'
GROUP BY c.id, c.name
ORDER BY revenue_cents DESC;

-- ⚠️ Gotcha: SQLite lets you select a column that is neither grouped nor
-- aggregated (it picks a value from some row). Postgres refuses with
-- "column must appear in the GROUP BY clause". Write it the Postgres way.

-- §3. WHERE vs HAVING
-- WHERE filters ROWS before grouping. HAVING filters GROUPS after grouping
-- (so it can use aggregates).
SELECT a.name, count(b.id) AS books, sum(b.stock) AS stock
FROM authors a
JOIN books b ON b.author_id = a.id
WHERE b.stock > 0               -- row filter: ignore sold-out books
GROUP BY a.id, a.name
HAVING count(b.id) >= 2         -- group filter: authors with 2+ such books
ORDER BY a.name;

-- §4. Grouping with LEFT JOIN — keep the zeros
-- count(*) would count the NULL row as 1. Count a column from the right table:
SELECT c.name, count(o.id) AS orders
FROM customers c
LEFT JOIN orders o ON o.customer_id = c.id
GROUP BY c.id, c.name
ORDER BY orders DESC, c.name;

-- §5. Building lists and conditional counts
-- group_concat glues values of a group into one string. Without ORDER BY
-- inside it, the order of the pieces is not guaranteed (SQLite 3.44+ allows it):
SELECT a.name, group_concat(b.title, ', ' ORDER BY b.title) AS titles
FROM authors a
JOIN books b ON b.author_id = a.id
WHERE a.id IN (1, 2, 5)
GROUP BY a.id, a.name
ORDER BY a.name;

-- FILTER (WHERE …) counts only some rows of each group — many counts in one pass.
SELECT c.name,
       count(*) FILTER (WHERE o.status = 'shipped') AS shipped,
       count(*) FILTER (WHERE o.status = 'pending') AS pending
FROM customers c
JOIN orders o ON o.customer_id = c.id
GROUP BY c.id, c.name
ORDER BY c.id;

-- The old-school way (works everywhere): sum a CASE.
SELECT sum(CASE WHEN stock = 0 THEN 1 ELSE 0 END) AS sold_out,
       sum(CASE WHEN stock > 0 THEN 1 ELSE 0 END) AS available
FROM books;

-- §6. A small sales report: revenue per top-level category
SELECT coalesce(parent.name, c.name)            AS section,
       count(DISTINCT oi.order_id)              AS orders,
       sum(oi.quantity)                         AS copies_sold,
       sum(oi.quantity * oi.unit_price_cents)   AS revenue_cents
FROM order_items oi
JOIN orders o           ON o.id = oi.order_id AND o.status <> 'cancelled'
JOIN books b            ON b.id = oi.book_id
JOIN categories c       ON c.id = b.category_id
LEFT JOIN categories parent ON parent.id = c.parent_id
GROUP BY section
ORDER BY revenue_cents DESC;
-- (Databases → Programming → Non-fiction is THREE levels deep; this report
--  only climbs one level. Lesson 06's recursive CTE climbs any number.)

-- 🐘 Postgres note:
--  • string_agg(title, ', ' ORDER BY title) instead of group_concat.
--  • FILTER (WHERE …) works the same (Postgres had it first).
--  • avg(integer) returns numeric (exact decimal), not a float.
--  • Strict GROUP BY rules (see §2 gotcha).
--  • GROUP BY ROLLUP / CUBE / GROUPING SETS give subtotals and grand totals.

-- Recap
--  • count/sum/avg/min/max; aggregates skip NULLs; count(*) counts rows.
--  • GROUP BY makes one row per group; select only grouped or aggregated columns.
--  • WHERE filters rows before grouping; HAVING filters groups after.
--  • LEFT JOIN + count(right.col) keeps zero-count groups.
--  • FILTER / sum(CASE …) for several conditional counts at once.
