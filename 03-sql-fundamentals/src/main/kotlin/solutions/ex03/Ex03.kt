package solutions.ex03

import exercises.ex03.Ex03

object Ex03Solution : Ex03 {
    override val cheapBooksInStock = """
        SELECT title, price_cents
        FROM books
        WHERE price_cents < 1000 AND stock > 0
        ORDER BY price_cents, title
    """.trimIndent()

    override val secondPageNewest = """
        SELECT id, title, published_year
        FROM books
        WHERE published_year IS NOT NULL
        ORDER BY published_year DESC, id
        LIMIT 3 OFFSET 3
    """.trimIndent()

    override val priceBands = """
        SELECT title,
               CASE
                   WHEN price_cents < 1000 THEN 'budget'
                   WHEN price_cents < 2000 THEN 'standard'
                   ELSE 'premium'
               END AS band
        FROM books
        WHERE author_id IN (5, 6)
        ORDER BY id
    """.trimIndent()

    override val restockSoldOut = "UPDATE books SET stock = 5 WHERE stock = 0"
}
