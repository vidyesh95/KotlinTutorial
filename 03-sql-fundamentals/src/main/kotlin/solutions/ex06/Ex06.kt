package solutions.ex06

import exercises.ex06.Ex06

object Ex06Solution : Ex06 {
    override val booksNeverOrdered = """
        SELECT b.title
        FROM books b
        WHERE NOT EXISTS (SELECT 1 FROM order_items oi WHERE oi.book_id = b.id)
        ORDER BY b.title
    """.trimIndent()

    override val authorsAboveAveragePrice = """
        SELECT a.name
        FROM authors a
        JOIN books b ON b.author_id = a.id
        GROUP BY a.id
        HAVING avg(b.price_cents) > (SELECT avg(price_cents) FROM books)
        ORDER BY a.name
    """.trimIndent()

    override val booksPerTopCategory = """
        WITH RECURSIVE tree (root_id, id) AS (
            SELECT id, id FROM categories WHERE parent_id IS NULL
            UNION ALL
            SELECT t.root_id, c.id FROM categories c JOIN tree t ON c.parent_id = t.id
        )
        SELECT root.name, count(b.id) AS books
        FROM tree t
        JOIN categories root ON root.id = t.root_id
        LEFT JOIN books b ON b.category_id = t.id
        GROUP BY root.id
        ORDER BY root.name
    """.trimIndent()

    override val ordersFirstWeekOfJune = """
        WITH RECURSIVE days (day) AS (
            SELECT '2025-06-01'
            UNION ALL
            SELECT date(day, '+1 day') FROM days WHERE day < '2025-06-07'
        )
        SELECT d.day, count(o.id) AS orders
        FROM days d
        LEFT JOIN orders o ON date(o.created_at) = d.day
        GROUP BY d.day
        ORDER BY d.day
    """.trimIndent()
}
