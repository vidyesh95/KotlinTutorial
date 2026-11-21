package exercises.ex13

import java.sql.Connection

/*
 * Exercise 13 — The N+1 problem                 (lesson: lesson13_n_plus_one)
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex13*'
 * The tests COUNT the statements you create — loops of queries will fail them.
 */

data class OrderItem(val bookTitle: String, val quantity: Int)
data class Order(val id: Long, val status: String, val items: List<OrderItem>)

interface Ex13 {
    /** 13.1 — "?, ?, ?" for count = 3. Throw IllegalArgumentException for count < 1. */
    fun placeholders(count: Int): String

    /**
     * 13.2 — Every order (ordered by id) with its items (ordered by book id), using AT MOST 2 queries.
     * Orders without items get an empty list.
     */
    fun loadOrdersWithItems(db: Connection): List<Order>

    /** 13.3 — Titles for the given book ids in ONE query: id → title. Unknown ids are left out. Empty input → empty map, no query. */
    fun titlesByIds(db: Connection, ids: List<Long>): Map<Long, String>
}

object Ex13Exercise : Ex13 {
    override fun placeholders(count: Int): String = TODO("13.1")
    override fun loadOrdersWithItems(db: Connection): List<Order> = TODO("13.2")
    override fun titlesByIds(db: Connection, ids: List<Long>): Map<Long, String> = TODO("13.3")
}
