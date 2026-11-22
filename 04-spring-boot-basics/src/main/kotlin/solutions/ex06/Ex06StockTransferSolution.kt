package solutions.ex06

import bookstore.NotFoundException
import bookstore.OutOfStockException
import bookstore.books.BookRepository
import exercises.ex06.MoveStockRequest
import exercises.ex06.MoveStockResult
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Service
class Ex06StockSolutionService(
    private val books: BookRepository,
    private val jdbc: JdbcClient,
) {
    @Transactional
    fun move(fromBookId: Long, toBookId: Long, quantity: Int): MoveStockResult {
        if (books.takeStock(fromBookId, quantity) == 0) {
            val current = books.findById(fromBookId)?.stock ?: throw NotFoundException("Book", fromBookId)
            throw OutOfStockException(fromBookId, quantity, current)
        }
        val changed = jdbc.sql("UPDATE books SET stock = stock + :quantity WHERE id = :id")
            .param("quantity", quantity)
            .param("id", toBookId)
            .update()
        if (changed == 0) throw NotFoundException("Book", toBookId)   // RuntimeException → ROLLBACK of step 1
        return MoveStockResult(books.findById(fromBookId)!!.stock, books.findById(toBookId)!!.stock)
    }
}

@RestController
@RequestMapping("/solutions/ex06")
class Ex06StockSolutionController(private val service: Ex06StockSolutionService) {
    @PostMapping("/move-stock")
    fun move(@RequestBody request: MoveStockRequest): MoveStockResult =
        service.move(request.fromBookId, request.toBookId, request.quantity)
}
