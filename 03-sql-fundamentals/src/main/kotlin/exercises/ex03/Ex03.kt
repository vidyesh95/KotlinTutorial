package exercises.ex03

/*
 * Exercise 03 — CRUD                             (lesson: lesson03_crud)
 * All tasks run on the bookstore database (resources/sql/bookstore.sql).
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex03*'
 */
interface Ex03 {
    /** 03.1 — `title`, `price_cents` of books cheaper than 1000 cents that are in stock (stock > 0), ORDER BY price_cents, title. */
    val cheapBooksInStock: String

    /**
     * 03.2 — Paging: `id`, `title`, `published_year` of books with a known year, newest first
     * (ties: lower id first). Return page 2 when a page has 3 books.
     */
    val secondPageNewest: String

    /**
     * 03.3 — `title` and `band` for books by authors 5 and 6, ORDER BY id, where band is
     * 'budget' below 1000 cents, 'standard' below 2000 cents, otherwise 'premium'.
     */
    val priceBands: String

    /** 03.4 — An UPDATE that sets stock to 5 for every sold-out book (stock = 0) and leaves all others alone. */
    val restockSoldOut: String
}

object Ex03Exercise : Ex03 {
    override val cheapBooksInStock: String get() = TODO("03.1")
    override val secondPageNewest: String get() = TODO("03.2")
    override val priceBands: String get() = TODO("03.3")
    override val restockSoldOut: String get() = TODO("03.4")
}
