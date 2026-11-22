package exercises.ex06

import bookstore.books.BookRepository
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/*
 * Exercise 06 — Transactions                    (lesson: lessons/06-transactions.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex06*'
 *
 * Move copies of a book from one listing to another (e.g. "Dune" → "Dune Messiah" display shelf).
 */

data class MoveStockRequest(val fromBookId: Long, val toBookId: Long, val quantity: Int)

data class MoveStockResult(val fromStock: Int, val toStock: Int)

@Service
class Ex06StockService(
    private val books: BookRepository,
    private val jdbc: JdbcClient,
) {
    /**
     * 06.1 — In THIS order:
     *   1. take [quantity] from the source — books.takeStock(…) returns 0 when there isn't enough →
     *      throw bookstore.OutOfStockException(fromBookId, quantity, currentStock)      (→ 409)
     *   2. add [quantity] to the target with an UPDATE; if it changed 0 rows the target doesn't
     *      exist → throw bookstore.NotFoundException("Book", toBookId)                  (→ 404)
     *   3. return both new stock levels
     *
     * 06.2 — Make it ATOMIC: when step 2 fails, step 1 must be undone. One annotation does it.
     * (The test moves stock to book 999 and then checks the source stock is unchanged.)
     */
    fun move(fromBookId: Long, toBookId: Long, quantity: Int): MoveStockResult = TODO("06.1")
}

@RestController
@RequestMapping("/exercises/ex06")
class Ex06StockController(private val service: Ex06StockService) {
    @PostMapping("/move-stock")
    fun move(@RequestBody request: MoveStockRequest): MoveStockResult =
        service.move(request.fromBookId, request.toBookId, request.quantity)
}
