package bookstore.web

import bookstore.orders.OrderLine
import bookstore.orders.OrderService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

/** Lesson 06 — POST /api/orders runs OrderService.placeOrder inside ONE transaction. */
@RestController
@RequestMapping("/api/orders")
class OrderController(private val orders: OrderService) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun place(@Valid @RequestBody request: PlaceOrderRequest): OrderResponse =
        orders.placeOrder(request.customerEmail, request.lines.map { OrderLine(it.bookId, it.quantity) }).toResponse()
}
