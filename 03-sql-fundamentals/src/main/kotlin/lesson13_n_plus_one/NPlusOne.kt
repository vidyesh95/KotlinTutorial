/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 13 — The N+1 query problem
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : spot the most common performance bug in backend code — one query
 *         for a list, then one MORE query per item — and fix it with a JOIN
 *         or a single `IN (…)` query.
 *  Needs: lessons 04 (joins), 11 (JDBC)
 *  Time : ~30 min
 *  Next : lesson 14 — migrations
 *  Exercises: exercises/ex13/Ex13.kt
 */
package lesson13_n_plus_one

import sqlkit.bookstoreDb
import java.sql.Connection
import java.sql.ResultSet

fun main() {
    section1TheProblem()
    section2FixWithAJoin()
    section3FixWithIn()
    section4Compare()
}

data class OrderItem(val bookTitle: String, val quantity: Int)
data class Order(val id: Long, val status: String, val items: List<OrderItem>)

/**
 * A tiny data-access helper that COUNTS every query it runs, so we can see
 * the problem instead of guessing. (Real apps see it in SQL logs or APM tools.)
 */
class CountingDb(private val db: Connection) {
    var queries = 0
        private set

    fun <T> query(sql: String, params: List<Any?> = emptyList(), map: (ResultSet) -> T): List<T> {
        queries++
        return db.prepareStatement(sql).use { statement ->
            params.forEachIndexed { i, value -> statement.setObject(i + 1, value) }
            statement.executeQuery().use { rows -> buildList { while (rows.next()) add(map(rows)) } }
        }
    }
}

fun Order.summary() = "order $id ($status): " + items.joinToString { "${it.bookTitle} x${it.quantity}" }

// ───────────────────────────────────────────────────────────────────────────
// §1. The problem
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    Mum sends you to the shop with a list of 10 things. You walk to the
//    shop, buy ONE thing, walk home, read the next line, walk back… 10 trips
//    (+1 to fetch the list). Or: take the whole list, ONE trip, done.
//
// Code that looks perfectly innocent:
//    val orders = loadOrders()                 // 1 query
//    for (order in orders) loadItems(order.id)  // + N queries, one per order
// 10 orders → 11 queries. 1 000 orders → 1 001. Each query is a network round
// trip to the database (~1 ms on a LAN, more in the cloud), so it adds up fast.

fun loadNPlusOne(db: CountingDb): List<Order> {
    val headers = db.query("SELECT id, status FROM orders ORDER BY id") { it.getLong("id") to it.getString("status") }
    return headers.map { (id, status) ->
        val items = db.query(                                     // ← runs once PER order
            """
            SELECT b.title, oi.quantity
            FROM order_items oi JOIN books b ON b.id = oi.book_id
            WHERE oi.order_id = ?
            ORDER BY oi.book_id
            """.trimIndent(),
            listOf(id),
        ) { OrderItem(it.getString("title"), it.getInt("quantity")) }
        Order(id, status, items)
    }
}

fun section1TheProblem() {
    println("§1")
    bookstoreDb().use { connection ->
        val db = CountingDb(connection)
        val orders = loadNPlusOne(db)
        println(orders.first().summary())
        println("${orders.size} orders loaded with ${db.queries} queries")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Fix 1: one JOIN, group in Kotlin
// ───────────────────────────────────────────────────────────────────────────
// Fetch orders AND their items in one query. Each order appears once per item
// ("repeated header columns"), so we group the rows back into objects.
// LEFT JOIN keeps orders that have no items at all.

fun loadWithJoin(db: CountingDb): List<Order> {
    data class Row(val id: Long, val status: String, val item: OrderItem?)

    val rows = db.query(
        """
        SELECT o.id, o.status, b.title, oi.quantity
        FROM orders o
        LEFT JOIN order_items oi ON oi.order_id = o.id
        LEFT JOIN books b        ON b.id = oi.book_id
        ORDER BY o.id, oi.book_id
        """.trimIndent()
    ) { rs ->
        val title: String? = rs.getString("title")
        Row(rs.getLong("id"), rs.getString("status"), title?.let { OrderItem(it, rs.getInt("quantity")) })
    }
    return rows.groupBy { it.id }                 // groupBy keeps first-seen order (LinkedHashMap)
        .map { (id, group) -> Order(id, group.first().status, group.mapNotNull { it.item }) }
}

fun section2FixWithAJoin() {
    println("§2")
    bookstoreDb().use { connection ->
        val db = CountingDb(connection)
        val orders = loadWithJoin(db)
        println(orders.first().summary())
        println("${orders.size} orders loaded with ${db.queries} query")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Fix 2: two queries — parents, then ALL children with IN (…)
// ───────────────────────────────────────────────────────────────────────────
// Often cleaner than a big JOIN (no duplicated parent columns; works well
// with pagination). Query 1: the orders. Query 2: items WHERE order_id IN
// (all those ids). Then attach children to parents in memory.
//
// ⚠️ Gotcha: you can't bind a list to ONE `?`. Generate one `?` per value —
// still safe, because the VALUES are bound, only the count of `?` is generated.
// (Postgres + JDBC can also bind an array: `WHERE order_id = ANY(?)`.)

fun placeholders(count: Int): String {
    require(count > 0) { "IN () with zero values is invalid SQL — handle the empty case before querying" }
    return List(count) { "?" }.joinToString(", ")
}

fun loadWithIn(db: CountingDb): List<Order> {
    val headers = db.query("SELECT id, status FROM orders ORDER BY id") { it.getLong("id") to it.getString("status") }
    if (headers.isEmpty()) return emptyList()
    val ids = headers.map { it.first }

    val itemsByOrder: Map<Long, List<OrderItem>> = db.query(
        """
        SELECT oi.order_id, b.title, oi.quantity
        FROM order_items oi JOIN books b ON b.id = oi.book_id
        WHERE oi.order_id IN (${placeholders(ids.size)})
        ORDER BY oi.order_id, oi.book_id
        """.trimIndent(),
        ids,
    ) { it.getLong("order_id") to OrderItem(it.getString("title"), it.getInt("quantity")) }
        .groupBy({ it.first }, { it.second })

    return headers.map { (id, status) -> Order(id, status, itemsByOrder[id].orEmpty()) }
}

fun section3FixWithIn() {
    println("§3")
    println("placeholders(3) = ${placeholders(3)}")
    bookstoreDb().use { connection ->
        val db = CountingDb(connection)
        val orders = loadWithIn(db)
        println(orders.first().summary())
        println("${orders.size} orders loaded with ${db.queries} queries")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Same answer, very different cost
// ───────────────────────────────────────────────────────────────────────────

fun section4Compare() {
    println("§4")
    bookstoreDb().use { connection ->
        val results = listOf(::loadNPlusOne, ::loadWithJoin, ::loadWithIn).map { load ->
            val db = CountingDb(connection)
            load(db) to db.queries
        }
        println("all three return the same orders: ${results.map { it.first }.distinct().size == 1}")
        println("queries used: N+1=${results[0].second}, JOIN=${results[1].second}, IN=${results[2].second}")
        println("with 1 000 orders: N+1=1001, JOIN=1, IN=2")
    }
}

// 🏗️ In the real world: ORMs (JPA/Hibernate) cause N+1 *invisibly*:
// `order.items` looks like a plain property but runs a query the first time
// you touch it ("lazy loading"). Loop over 100 orders → 100 hidden queries.
// That's a big reason this course writes SQL explicitly (JdbcClient in
// module 04, SQLDelight in module 05): every query is visible in the code.
// Tip: log SQL in development and look for the same statement repeated.

// 🐘 Postgres note: identical problem and fixes. Postgres also offers
// `WHERE order_id = ANY(?)` with an array parameter, and `json_agg` to build
// nested JSON (order + items) in a single query.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • N+1 = 1 query for the list + 1 per item. Fine for 3 rows, deadly for 3 000.
//  • Fix with one JOIN (group rows in code) or two queries using IN (…).
//  • Generate one `?` per value; still bind the values. Handle the empty list.
//  • Count your queries — make the invisible visible.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// order 1 (paid): Dune x1, Designing Data-Intensive Applications x1
// 10 orders loaded with 11 queries
// §2
// order 1 (paid): Dune x1, Designing Data-Intensive Applications x1
// 10 orders loaded with 1 query
// §3
// placeholders(3) = ?, ?, ?
// order 1 (paid): Dune x1, Designing Data-Intensive Applications x1
// 10 orders loaded with 2 queries
// §4
// all three return the same orders: true
// queries used: N+1=11, JOIN=1, IN=2
// with 1 000 orders: N+1=1001, JOIN=1, IN=2
