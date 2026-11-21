package exercises.ex09

/*
 * Exercise 09 — Upserts, JSON and dates       (lesson: lesson09_upsert_json_and_dates)
 *
 * Write SQL as Kotlin strings; tests run it on the bookstore database. Replace every TODO(...) then run:
 *     ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex09*'
 */
interface Ex09 {
    /**
     * 09.1 — The test creates `inventory (book_id INTEGER PRIMARY KEY, qty INTEGER NOT NULL)`.
     * Write ONE statement that adds 5 to the qty of book_id 1, or inserts (1, 5) when the row is missing.
     */
    val restockBookOne: String

    /**
     * 09.2 — ONE row, ONE column: a JSON array of objects {"title": …, "price_cents": …} for every book
     * by author 2, in id order. Expected text:
     * [{"title":"Pride and Prejudice","price_cents":799},{"title":"Emma","price_cents":899}]
     */
    val janeAustenAsJson: String

    /**
     * 09.3 — Number of NON-cancelled orders per month. Columns: month ('YYYY-MM'), orders. ORDER BY month.
     */
    val ordersPerMonth: String

    /**
     * 09.4 — Names of customers who had been registered for at least 120 days on 2025-07-01T00:00:00Z
     * (use julianday). Column: name. ORDER BY created_at.
     */
    val loyalCustomers: String
}

object Ex09Exercise : Ex09 {
    override val restockBookOne: String get() = TODO("09.1")
    override val janeAustenAsJson: String get() = TODO("09.2")
    override val ordersPerMonth: String get() = TODO("09.3")
    override val loyalCustomers: String get() = TODO("09.4")
}
