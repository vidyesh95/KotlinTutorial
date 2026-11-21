package solutions.ex07

import exercises.ex07.Ex07

object Ex07Solution : Ex07 {
    override val fantasyByPrice = """
        SELECT title, price_cents, row_number() OVER (ORDER BY price_cents DESC) AS pos
        FROM books
        WHERE category_id = 3
        ORDER BY pos
    """.trimIndent()

    override val cheapestPerCategory = """
        WITH ranked AS (
            SELECT category_id, title,
                   row_number() OVER (PARTITION BY category_id ORDER BY price_cents, id) AS pos
            FROM books
            WHERE category_id IS NOT NULL
        )
        SELECT category_id, title FROM ranked WHERE pos = 1 ORDER BY category_id
    """.trimIndent()

    override val adaRunningSpend = """
        WITH totals AS (
            SELECT o.id, o.created_at, sum(oi.quantity * oi.unit_price_cents) AS total
            FROM orders o
            JOIN order_items oi ON oi.order_id = o.id
            WHERE o.customer_id = 1 AND o.status <> 'cancelled'
            GROUP BY o.id
        )
        SELECT id, total, sum(total) OVER (ORDER BY created_at) AS running
        FROM totals
        ORDER BY created_at
    """.trimIndent()

    override val alanDaysBetweenOrders = """
        SELECT id,
               CAST(round(julianday(created_at) - julianday(lag(created_at) OVER (ORDER BY created_at))) AS INTEGER)
                   AS days_since_previous
        FROM orders
        WHERE customer_id = 2
        ORDER BY created_at
    """.trimIndent()
}
