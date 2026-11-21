package solutions.ex01

import exercises.ex01.Ex01

object Ex01Solution : Ex01 {
    override val createProducts = """
        CREATE TABLE products (
            id          INTEGER PRIMARY KEY,
            name        TEXT    NOT NULL,
            price_cents INTEGER NOT NULL,
            in_stock    INTEGER NOT NULL DEFAULT 1
        ) STRICT;
    """.trimIndent()

    override val addSkuColumn = "ALTER TABLE products ADD COLUMN sku TEXT;"

    override val booksWithYear = """
        SELECT title, coalesce(published_year, 'unknown')
        FROM books
        ORDER BY id;
    """.trimIndent()
}
