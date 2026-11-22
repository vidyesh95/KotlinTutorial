package bookstore.web

import bookstore.catalog.BookView
import bookstore.catalog.CatalogService
import bookstore.orders.OrderLine
import bookstore.orders.OrderService
import bookstore.orders.OutOfStockException
import bookstore.orders.PlacedOrder
import bookstore.orders.UnknownBookException
import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

data class PlaceOrderRequest(val customerEmail: String, val lines: List<OrderLine>)

/** A deliberately small API — module 04 already covered controllers in depth. */
@RestController
class ApiController(private val catalog: CatalogService, private val orders: OrderService) {

    @GetMapping("/api/books")
    fun books(): List<BookView> = catalog.all()

    @GetMapping("/api/books/{id}")
    fun book(@PathVariable id: Long): BookView = catalog.byId(id) ?: throw UnknownBookException(id)

    @PostMapping("/api/orders")
    @ResponseStatus(HttpStatus.CREATED)
    fun order(@RequestBody request: PlaceOrderRequest): PlacedOrder = orders.placeOrder(request.customerEmail, request.lines)

    @ExceptionHandler
    fun notFound(e: UnknownBookException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler
    fun outOfStock(e: OutOfStockException): ProblemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message)
}
