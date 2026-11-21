package exercises.ex04

/*
 * Exercise 04 — Joins                            (lesson: lesson04_joins)
 * All tasks run on the bookstore database (resources/sql/bookstore.sql).
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex04*'
 */
interface Ex04 {
    /** 04.1 — `title` and author `name` of every book written by an author from the 'UK', ORDER BY title. */
    val ukBooks: String

    /** 04.2 — `name` of customers who never placed any order, ORDER BY name. */
    val customersWithoutOrders: String

    /**
     * 04.3 — Every category: `name` and its parent's name as `parent`, showing 'top level' when there is
     * no parent. ORDER BY name.
     */
    val categoriesWithParent: String

    /**
     * 04.4 — The DISTINCT `title`s that customer 1 (Ada) bought in orders that were NOT cancelled,
     * ORDER BY title.
     */
    val adaPurchases: String
}

object Ex04Exercise : Ex04 {
    override val ukBooks: String get() = TODO("04.1")
    override val customersWithoutOrders: String get() = TODO("04.2")
    override val categoriesWithParent: String get() = TODO("04.3")
    override val adaPurchases: String get() = TODO("04.4")
}
