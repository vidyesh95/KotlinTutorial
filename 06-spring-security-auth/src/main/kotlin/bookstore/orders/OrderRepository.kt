package bookstore.orders

import bookstore.ResourceNotFoundException
import bookstore.books.BookRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Repository
class OrderRepository(
    private val jdbcClient: JdbcClient,
    private val bookRepository: BookRepository,
) {

    @Transactional
    fun createOrder(userId: String, request: CreateOrderRequest): OrderResponse {
        var totalCents = 0
        val itemsToInsert = mutableListOf<Triple<Long, Int, Int>>() // bookId, quantity, unitPrice

        for (item in request.items) {
            val book = bookRepository.findById(item.bookId)
                ?: throw ResourceNotFoundException("Book with ID ${item.bookId} not found")
            val itemTotal = book.priceCents * item.quantity
            totalCents += itemTotal
            itemsToInsert.add(Triple(item.bookId, item.quantity, book.priceCents))
        }

        val now = Instant.now()
        val keyHolder = org.springframework.jdbc.support.GeneratedKeyHolder()

        jdbcClient.sql("INSERT INTO orders (user_id, total_cents, created_at) VALUES (:userId, :totalCents, :createdAt)")
            .param("userId", userId)
            .param("totalCents", totalCents)
            .param("createdAt", now.toString())
            .update(keyHolder)

        val orderId = keyHolder.key?.toLong() ?: throw IllegalStateException("Failed to retrieve generated order ID")

        for ((bookId, qty, unitPrice) in itemsToInsert) {
            jdbcClient.sql(
                """
                INSERT INTO order_items (order_id, book_id, quantity, unit_price_cents)
                VALUES (:orderId, :bookId, :quantity, :unitPriceCents)
                """.trimIndent()
            )
                .param("orderId", orderId)
                .param("bookId", bookId)
                .param("quantity", qty)
                .param("unitPriceCents", unitPrice)
                .update()
        }

        return OrderResponse(
            id = orderId,
            userId = userId,
            totalCents = totalCents,
            createdAt = now,
            items = itemsToInsert.map { OrderItemResponse(it.first, it.second, it.third) },
        )
    }

    fun findOrdersByUserId(userId: String): List<OrderResponse> {
        val orders = jdbcClient.sql("SELECT id, user_id, total_cents, created_at FROM orders WHERE user_id = :userId ORDER BY id DESC")
            .param("userId", userId)
            .query { rs, _ ->
                OrderResponse(
                    id = rs.getLong("id"),
                    userId = rs.getString("user_id"),
                    totalCents = rs.getInt("total_cents"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    items = emptyList(),
                )
            }
            .list()

        return orders.map { order ->
            val items = findOrderItems(order.id)
            order.copy(items = items)
        }
    }

    fun findAllOrders(): List<OrderResponse> {
        val orders = jdbcClient.sql("SELECT id, user_id, total_cents, created_at FROM orders ORDER BY id DESC")
            .query { rs, _ ->
                OrderResponse(
                    id = rs.getLong("id"),
                    userId = rs.getString("user_id"),
                    totalCents = rs.getInt("total_cents"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    items = emptyList(),
                )
            }
            .list()

        return orders.map { order ->
            val items = findOrderItems(order.id)
            order.copy(items = items)
        }
    }

    private fun findOrderItems(orderId: Long): List<OrderItemResponse> =
        jdbcClient.sql("SELECT book_id, quantity, unit_price_cents FROM order_items WHERE order_id = :orderId")
            .param("orderId", orderId)
            .query { rs, _ ->
                OrderItemResponse(
                    bookId = rs.getLong("book_id"),
                    quantity = rs.getInt("quantity"),
                    unitPriceCents = rs.getInt("unit_price_cents"),
                )
            }
            .list()
}
