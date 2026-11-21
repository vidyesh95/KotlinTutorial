package exercises.ex05

/*
 * Exercise 05 — Aggregation                      (lesson: lesson05_aggregation)
 * All tasks run on the bookstore database (resources/sql/bookstore.sql).
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex05*'
 */
interface Ex05 {
    /**
     * 05.1 — For every category that has at least one book: category `name` and number of books,
     * ORDER BY the count descending, then name.
     */
    val booksPerCategory: String

    /**
     * 05.2 — For books with at least 2 reviews: `title`, number of reviews, and the average rating
     * rounded to 1 decimal. ORDER BY title.
     */
    val wellReviewedBooks: String

    /** 05.3 — For every order `status`: total revenue in cents (sum of quantity × unit_price_cents). ORDER BY status. */
    val revenuePerStatus: String

    /** 05.4 — `name` of customers with 2 or more orders that were NOT cancelled. ORDER BY name. */
    val loyalCustomers: String
}

object Ex05Exercise : Ex05 {
    override val booksPerCategory: String get() = TODO("05.1")
    override val wellReviewedBooks: String get() = TODO("05.2")
    override val revenuePerStatus: String get() = TODO("05.3")
    override val loyalCustomers: String get() = TODO("05.4")
}
