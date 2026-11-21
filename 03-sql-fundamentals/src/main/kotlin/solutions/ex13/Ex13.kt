package solutions.ex13

import exercises.ex13.Ex13
import exercises.ex13.Order
import exercises.ex13.OrderItem
import java.sql.Connection
import java.sql.ResultSet

private fun <T> Connection.query(sql: String, params: List<Any?> = emptyList(), map: (ResultSet) -> T): List<T> =
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { i, p -> statement.setObject(i + 1, p) }
        statement.executeQuery().use { rs -> buildList { while (rs.next()) add(map(rs)) } }
    }

object Ex13Solution : Ex13 {
    override fun placeholders(count: Int): String {
        require(count >= 1) { "need at least one placeholder" }
        return List(count) { "?" }.joinToString(", ")
    }

    override fun loadOrdersWithItems(db: Connection): List<Order> {
        val headers = db.query("SELECT id, status FROM orders ORDER BY id") { it.getLong(1) to it.getString(2) }
        if (headers.isEmpty()) return emptyList()
        val ids = headers.map { it.first }
        val items = db.query(
            """
            SELECT oi.order_id, b.title, oi.quantity
            FROM order_items oi JOIN books b ON b.id = oi.book_id
            WHERE oi.order_id IN (${placeholders(ids.size)})
            ORDER BY oi.order_id, oi.book_id
            """.trimIndent(),
            ids,
        ) { it.getLong(1) to OrderItem(it.getString(2), it.getInt(3)) }.groupBy({ it.first }, { it.second })
        return headers.map { (id, status) -> Order(id, status, items[id].orEmpty()) }
    }

    override fun titlesByIds(db: Connection, ids: List<Long>): Map<Long, String> {
        if (ids.isEmpty()) return emptyMap()
        return db.query("SELECT id, title FROM books WHERE id IN (${placeholders(ids.size)})", ids) {
            it.getLong(1) to it.getString(2)
        }.toMap()
    }
}
