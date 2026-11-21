package exercises.ex08

/*
 * Exercise 08 — Indexes and query plans       (lesson: lesson08_indexes_and_query_plans)
 *
 * Each task is a CREATE INDEX statement. Tests run it on the bookstore database and check the
 * EXPLAIN QUERY PLAN of a query. Replace every TODO(...) then run:
 *     ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex08*'
 */
interface Ex08 {
    /** 08.1 — An index so that `SELECT * FROM orders WHERE customer_id = ?` does a SEARCH using your index. */
    val indexOrdersByCustomer: String

    /**
     * 08.2 — A UNIQUE index on customers' email IGNORING CASE (an expression index on lower(email)):
     * inserting 'ADA@example.com' must fail, and `WHERE lower(email) = ?` must use the index.
     */
    val uniqueEmailIgnoringCase: String

    /**
     * 08.3 — An index that makes `SELECT title, price_cents FROM books WHERE category_id = ?`
     * a COVERING index search (the table itself is never read).
     */
    val coveringIndexForCategoryListing: String

    /**
     * 08.4 — A PARTIAL index on books(price_cents) that only contains books IN STOCK (stock > 0), so that
     * `SELECT id FROM books WHERE stock > 0 AND price_cents < ?` uses it.
     */
    val partialIndexInStock: String
}

object Ex08Exercise : Ex08 {
    override val indexOrdersByCustomer: String get() = TODO("08.1")
    override val uniqueEmailIgnoringCase: String get() = TODO("08.2")
    override val coveringIndexForCategoryListing: String get() = TODO("08.3")
    override val partialIndexInStock: String get() = TODO("08.4")
}
