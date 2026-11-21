package solutions.ex09

import exercises.ex09.Ex09

object Ex09Solution : Ex09 {
    override val restockBookOne = """
        INSERT INTO inventory (book_id, qty) VALUES (1, 5)
        ON CONFLICT (book_id) DO UPDATE SET qty = qty + excluded.qty
    """.trimIndent()

    override val janeAustenAsJson = """
        SELECT json_group_array(json_object('title', title, 'price_cents', price_cents))
        FROM (SELECT title, price_cents FROM books WHERE author_id = 2 ORDER BY id)
    """.trimIndent()

    override val ordersPerMonth = """
        SELECT strftime('%Y-%m', created_at) AS month, count(*) AS orders
        FROM orders
        WHERE status <> 'cancelled'
        GROUP BY month
        ORDER BY month
    """.trimIndent()

    override val loyalCustomers = """
        SELECT name
        FROM customers
        WHERE julianday('2025-07-01T00:00:00Z') - julianday(created_at) >= 120
        ORDER BY created_at
    """.trimIndent()
}
