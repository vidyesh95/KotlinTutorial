-- ═══════════════════════════════════════════════════════════════════════════
--  SQL Lesson 07 — Window functions
-- ═══════════════════════════════════════════════════════════════════════════
--  Goal : compute rankings, running totals, "previous/next row" and shares of
--         a total WITHOUT collapsing rows like GROUP BY does.
--  Run  : the green ▶ in lesson07_window_functions/WindowFunctions.kt
--  Next : lesson 08 — indexes and query plans
--
-- 🧒 Like I'm 5:
--    GROUP BY is like the teacher collecting everyone's test and announcing
--    ONE number: "the class average is 7". Your own test disappears.
--    A window function is like getting your test BACK with a note on it:
--    "you got 8, the class average is 7, you're 3rd in your row". You keep
--    your own row AND you can see the whole class list (the "window").

-- §1. OVER () — aggregate without collapsing
-- `sum(x) OVER ()` = the sum over ALL rows, written next to EVERY row.
SELECT title,
       price_cents,
       sum(price_cents) OVER ()   AS all_books_total,
       round(avg(price_cents) OVER ()) AS avg_price
FROM books
WHERE author_id IN (1, 2)
ORDER BY id;

-- Share of total: each book's percentage of the whole stock value.
SELECT title,
       price_cents * stock AS stock_value,
       round(100.0 * price_cents * stock / sum(price_cents * stock) OVER (), 1) AS pct_of_total
FROM books
ORDER BY stock_value DESC
LIMIT 5;

-- §2. PARTITION BY — a separate window per group
-- Like GROUP BY, but every row stays. Each book next to its category's average.
SELECT category_id,
       title,
       price_cents,
       round(avg(price_cents) OVER (PARTITION BY category_id)) AS category_avg,
       count(*) OVER (PARTITION BY category_id) AS books_in_category
FROM books
WHERE category_id IN (2, 3)
ORDER BY category_id, price_cents DESC, id;

-- §3. Ranking: ROW_NUMBER vs RANK vs DENSE_RANK
-- ORDER BY inside OVER decides the order of the ranking.
--  ROW_NUMBER  1,2,3,4   always unique — ties broken arbitrarily
--  RANK        1,2,2,4   ties share a rank, then a GAP
--  DENSE_RANK  1,2,2,3   ties share a rank, NO gap
SELECT title,
       price_cents,
       row_number() OVER (ORDER BY price_cents DESC, id) AS row_num,
       rank()       OVER (ORDER BY price_cents DESC)     AS rnk,
       dense_rank() OVER (ORDER BY price_cents DESC)     AS dense
FROM books
WHERE category_id = 2
ORDER BY row_num;

-- §4. Top-N per group (the most asked interview question in SQL 🙂)
-- "The most expensive book in each category". Rank inside each category,
-- then keep rank 1. Window functions can't go in WHERE, so wrap in a CTE.
WITH ranked AS (
    SELECT c.name AS category,
           b.title,
           b.price_cents,
           row_number() OVER (PARTITION BY b.category_id ORDER BY b.price_cents DESC, b.id) AS pos
    FROM books b
    JOIN categories c ON c.id = b.category_id
)
SELECT category, title, price_cents
FROM ranked
WHERE pos = 1
ORDER BY category;

-- §5. Running totals — ORDER BY inside OVER makes it cumulative
-- With ORDER BY, sum() OVER adds up "everything up to and including this row".
WITH order_totals AS (
    SELECT o.id, o.created_at, sum(oi.quantity * oi.unit_price_cents) AS total
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    WHERE o.status <> 'cancelled'
    GROUP BY o.id
)
SELECT id,
       date(created_at) AS day,
       total,
       sum(total) OVER (ORDER BY created_at) AS running_revenue
FROM order_totals
ORDER BY created_at;

-- §6. LAG / LEAD — peek at the previous / next row
-- How many days between each customer's orders? LAG gives the previous
-- row's value inside the same partition (NULL for the first one).
SELECT customer_id,
       date(created_at) AS ordered_on,
       date(lag(created_at) OVER (PARTITION BY customer_id ORDER BY created_at)) AS previous,
       round(julianday(created_at)
             - julianday(lag(created_at) OVER (PARTITION BY customer_id ORDER BY created_at))) AS days_since_previous
FROM orders
WHERE customer_id IN (1, 2, 5)
ORDER BY customer_id, created_at;

-- §7. Naming a window and moving frames
-- `WINDOW w AS (…)` avoids repeating the same OVER clause.
-- A *frame* limits which rows the function sees: here, a moving average of
-- the current order and the one before it ("ROWS BETWEEN 1 PRECEDING AND CURRENT ROW").
WITH order_totals AS (
    SELECT o.id, o.created_at, sum(oi.quantity * oi.unit_price_cents) AS total
    FROM orders o
    JOIN order_items oi ON oi.order_id = o.id
    GROUP BY o.id
)
SELECT id,
       total,
       round(avg(total) OVER last_two) AS moving_avg_2,
       first_value(total) OVER w       AS first_order_total
FROM order_totals
WINDOW w AS (ORDER BY created_at),
       last_two AS (ORDER BY created_at ROWS BETWEEN 1 PRECEDING AND CURRENT ROW)
ORDER BY created_at
LIMIT 5;

-- ⚠️ Gotcha: with ORDER BY in OVER, the DEFAULT frame is "from the first row
-- up to the current row (and its ties)". That's why sum() became a running
-- total in §5 — and why last_value() surprisingly returns the CURRENT row.
-- Spell out the frame when it matters.

-- 🐘 Postgres note: identical syntax (window functions are standard SQL).
-- Postgres adds `FILTER (WHERE …)` on aggregates (SQLite has it too), RANGE
-- frames with intervals (`RANGE BETWEEN INTERVAL '7 days' PRECEDING AND
-- CURRENT ROW`), and percent_rank / ntile / percentile_cont for statistics.

-- Recap
--  • f() OVER (…) computes across rows but keeps every row.
--  • PARTITION BY = per group; ORDER BY = order inside the window (cumulative by default).
--  • row_number / rank / dense_rank for rankings; top-N per group = rank in a CTE, filter outside.
--  • lag / lead look at neighbouring rows; frames (ROWS BETWEEN …) make moving windows.
