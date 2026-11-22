package bookstore.orders

import bookstore.db.BookstoreDatabase
import bookstore.domain.OrderStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock

data class OrderLine(val bookId: Long, val quantity: Int)

class OutOfStockException(val bookId: Long) : RuntimeException("Book $bookId is out of stock")
class UnknownBookException(val bookId: Long) : RuntimeException("Book $bookId not found")

data class PlacedOrder(val id: Long, val totalCents: Int, val status: OrderStatus)

/** Same rules as module 04's OrderService — only the data access changed to generated SQLDelight code. */
@Service
class OrderService(db: BookstoreDatabase, private val clock: Clock) {
    private val books = db.booksQueries
    private val orders = db.ordersQueries

    @Transactional
    fun placeOrder(customerEmail: String, lines: List<OrderLine>): PlacedOrder {
        require(lines.isNotEmpty()) { "An order needs at least one line" }
        var total = 0
        for (line in lines) {
            val book = books.selectById(line.bookId).executeAsOneOrNull() ?: throw UnknownBookException(line.bookId)
            val changed = books.takeStock(quantity = line.quantity, id = line.bookId).value   // rows changed
            if (changed == 0L) throw OutOfStockException(line.bookId)                       // → rollback
            total += book.price_cents * line.quantity
        }
        val orderId = orders.insertOrder(customerEmail, OrderStatus.PAID, total, clock.now()).executeAsOne()
        lines.forEach { orders.insertItem(orderId, it.quantity, it.bookId) }
        return PlacedOrder(orderId, total, OrderStatus.PAID)
    }
}
