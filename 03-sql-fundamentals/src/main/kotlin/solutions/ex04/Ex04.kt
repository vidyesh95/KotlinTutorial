package solutions.ex04

import exercises.ex04.Ex04

object Ex04Solution : Ex04 {
    override val ukBooks = """
        SELECT b.title, a.name
        FROM books b
        JOIN authors a ON a.id = b.author_id
        WHERE a.country = 'UK'
        ORDER BY b.title
    """.trimIndent()

    override val customersWithoutOrders = """
        SELECT c.name
        FROM customers c
        LEFT JOIN orders o ON o.customer_id = c.id
        WHERE o.id IS NULL
        ORDER BY c.name
    """.trimIndent()

    override val categoriesWithParent = """
        SELECT child.name, coalesce(parent.name, 'top level') AS parent
        FROM categories child
        LEFT JOIN categories parent ON parent.id = child.parent_id
        ORDER BY child.name
    """.trimIndent()

    override val adaPurchases = """
        SELECT DISTINCT b.title
        FROM orders o
        JOIN order_items oi ON oi.order_id = o.id
        JOIN books b ON b.id = oi.book_id
        WHERE o.customer_id = 1 AND o.status <> 'cancelled'
        ORDER BY b.title
    """.trimIndent()
}
