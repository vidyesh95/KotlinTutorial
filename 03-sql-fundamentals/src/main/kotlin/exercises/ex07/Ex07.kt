package exercises.ex07

/*
 * Exercise 07 — Window functions              (lesson: lesson07_window_functions)
 *
 * Write SQL as Kotlin strings; tests run it on the bookstore database. Replace every TODO(...) then run:
 *     ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex07*'
 */
interface Ex07 {
    /**
     * 07.1 — Books in category 3 (Fantasy) with their position by price, most expensive first (ROW_NUMBER).
     * Columns: title, price_cents, pos. ORDER BY pos.
     */
    val fantasyByPrice: String

    /**
     * 07.2 — The CHEAPEST book in each category (skip books without a category). Break ties by the lower id.
     * Columns: category_id, title. ORDER BY category_id.
     */
    val cheapestPerCategory: String

    /**
     * 07.3 — Customer 1's NON-cancelled orders: order id, the order's total (sum of quantity × unit_price_cents)
     * and the running total of everything Ada spent so far. Columns: id, total, running. ORDER BY created_at.
     */
    val adaRunningSpend: String

    /**
     * 07.4 — Customer 2's orders with the number of days since that customer's previous order,
     * rounded and CAST to an INTEGER (NULL for the first order). Columns: id, days_since_previous. ORDER BY created_at.
     */
    val alanDaysBetweenOrders: String
}

object Ex07Exercise : Ex07 {
    override val fantasyByPrice: String get() = TODO("07.1")
    override val cheapestPerCategory: String get() = TODO("07.2")
    override val adaRunningSpend: String get() = TODO("07.3")
    override val alanDaysBetweenOrders: String get() = TODO("07.4")
}
