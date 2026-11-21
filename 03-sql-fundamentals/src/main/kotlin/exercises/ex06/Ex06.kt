package exercises.ex06

/*
 * Exercise 06 — Subqueries and CTEs           (lesson: lesson06_subqueries_and_ctes)
 *
 * Write SQL as Kotlin strings; tests run it on the bookstore database. Replace every TODO(...) then run:
 *     ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex06*'
 */
interface Ex06 {
    /** 06.1 — Titles of books that were NEVER ordered (use NOT EXISTS). Column: title. ORDER BY title. */
    val booksNeverOrdered: String

    /**
     * 06.2 — Authors whose AVERAGE book price is higher than the average price of ALL books.
     * Column: name. ORDER BY name.
     */
    val authorsAboveAveragePrice: String

    /**
     * 06.3 — For each TOP-LEVEL category (parent_id IS NULL): how many books are in it OR in any of its
     * sub-categories, at any depth (WITH RECURSIVE). Columns: name, books. ORDER BY name.
     */
    val booksPerTopCategory: String

    /**
     * 06.4 — Every day from 2025-06-01 to 2025-06-07 (column `day`, format 'YYYY-MM-DD') with the
     * number of orders created that day (column `orders`, 0 when none). ORDER BY day.
     */
    val ordersFirstWeekOfJune: String
}

object Ex06Exercise : Ex06 {
    override val booksNeverOrdered: String get() = TODO("06.1")
    override val authorsAboveAveragePrice: String get() = TODO("06.2")
    override val booksPerTopCategory: String get() = TODO("06.3")
    override val ordersFirstWeekOfJune: String get() = TODO("06.4")
}
