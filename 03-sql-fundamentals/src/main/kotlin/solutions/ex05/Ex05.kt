package solutions.ex05

import exercises.ex05.Ex05

object Ex05Solution : Ex05 {
    override val booksPerCategory = """
        SELECT c.name, count(*) AS books
        FROM categories c
        JOIN books b ON b.category_id = c.id
        GROUP BY c.id, c.name
        ORDER BY books DESC, c.name
    """.trimIndent()

    override val wellReviewedBooks = """
        SELECT b.title, count(*) AS reviews, round(avg(r.rating), 1) AS average
        FROM books b
        JOIN reviews r ON r.book_id = b.id
        GROUP BY b.id, b.title
        HAVING count(*) >= 2
        ORDER BY b.title
    """.trimIndent()

    override val revenuePerStatus = """
        SELECT o.status, sum(oi.quantity * oi.unit_price_cents) AS revenue_cents
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id
        GROUP BY o.status
        ORDER BY o.status
    """.trimIndent()

    override val loyalCustomers = """
        SELECT c.name
        FROM customers c
        JOIN orders o ON o.customer_id = c.id
        WHERE o.status <> 'cancelled'
        GROUP BY c.id, c.name
        HAVING count(*) >= 2
        ORDER BY c.name
    """.trimIndent()
}
