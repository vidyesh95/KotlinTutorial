package bookstore

import bookstore.books.BookRepository
import bookstore.orders.OrderLine
import bookstore.orders.OrderService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** Lesson 06/07 — proves @Transactional really rolls back earlier work when a later step fails. */
@SpringBootTest
@ActiveProfiles("test")
@Import(TestClock::class)
@DirtiesContext
class OrderTransactionTest(
    @Autowired private val orders: OrderService,
    @Autowired private val books: BookRepository,
) {
    @Test
    fun `successful order takes stock and totals the price`() {
        val order = orders.placeOrder("ada@example.com", listOf(OrderLine(bookId = 3, quantity = 2)))
        assertEquals(2 * 799, order.totalCents)
        assertEquals(TestClock.NOW, order.createdAt)
        assertEquals(28, books.findById(3)!!.stock)
    }

    @Test
    fun `failing line rolls back the whole order`() {
        val duneBefore = books.findById(1)!!.stock
        assertFailsWith<OutOfStockException> {
            orders.placeOrder(
                "bob@example.com",
                listOf(OrderLine(bookId = 1, quantity = 1), OrderLine(bookId = 4, quantity = 1)),   // Emma: stock 0
            )
        }
        assertEquals(duneBefore, books.findById(1)!!.stock, "Dune's stock must be restored by the rollback")
    }
}
