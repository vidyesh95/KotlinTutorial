package bookstore.orders

import bookstore.NotFoundException
import bookstore.OutOfStockException
import bookstore.books.BookRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock
import kotlin.time.Instant

data class OrderLine(val bookId: Long, val quantity: Int)

data class Order(val id: Long, val customerEmail: String, val totalCents: Int, val lines: List<OrderLine>, val createdAt: Instant)

/**
 * Lesson 06 — transactions in Spring.
 *
 * placeOrder touches several rows: take stock for EACH line, insert the order, insert
 * every line. If line 3 is out of stock, lines 1 and 2 must NOT stay decremented.
 * @Transactional makes the whole method one database transaction (module 03, lesson 12):
 *   method returns normally         → COMMIT
 *   method throws RuntimeException  → ROLLBACK (everything undone)
 *
 * 🧒 Like I'm 5: a shop till. Scan all the items, then pay. If the card is declined, the
 * cashier cancels the WHOLE receipt — you don't leave with half your shopping.
 *
 * How? Spring wraps this bean in a *proxy* (a stand-in object, module 02 lesson 05's
 * "what Spring adds"): proxy.placeOrder() = begin → call the real method → commit/rollback.
 */
@Service
class OrderService(
    private val books: BookRepository,
    private val jdbc: JdbcClient,
    private val clock: Clock,
) {
    @Transactional
    fun placeOrder(customerEmail: String, lines: List<OrderLine>): Order {
        require(lines.isNotEmpty()) { "An order needs at least one line" }
        var total = 0
        for (line in lines) {
            val book = books.findById(line.bookId) ?: throw NotFoundException("Book", line.bookId)
            if (books.takeStock(book.id, line.quantity) == 0) {
                throw OutOfStockException(book.id, line.quantity, book.stock)   // → rollback of earlier lines
            }
            total += book.priceCents * line.quantity
        }
        val now = clock.now()
        val orderId = jdbc.sql("INSERT INTO orders (customer_email, total_cents, created_at) VALUES (:email, :total, :now) RETURNING id")
            .param("email", customerEmail)
            .param("total", total)
            .param("now", now.toString())
            .query(Long::class.java)
            .single()
        for (line in lines) {
            jdbc.sql(
                """
                INSERT INTO order_items (order_id, book_id, quantity, unit_price_cents)
                SELECT :orderId, id, :quantity, price_cents FROM books WHERE id = :bookId
                """.trimIndent(),
            )
                .param("orderId", orderId)
                .param("quantity", line.quantity)
                .param("bookId", line.bookId)
                .update()
        }
        return Order(orderId, customerEmail, total, lines, now)
    }
}
