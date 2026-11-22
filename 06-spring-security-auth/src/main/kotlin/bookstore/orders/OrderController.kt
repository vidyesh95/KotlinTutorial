package bookstore.orders

import bookstore.auth.domain.UserPrincipal
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api")
class OrderController(
    private val orderRepository: OrderRepository,
) {

    @PostMapping("/orders")
    fun createOrder(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: CreateOrderRequest,
    ): ResponseEntity<OrderResponse> {
        val created = orderRepository.createOrder(principal.id, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @GetMapping("/orders/mine")
    fun getMyOrders(
        @AuthenticationPrincipal principal: UserPrincipal,
    ): List<OrderResponse> = orderRepository.findOrdersByUserId(principal.id)

    @GetMapping("/admin/orders")
    @PreAuthorize("hasRole('ADMIN')")
    fun getAllOrders(): List<OrderResponse> = orderRepository.findAllOrders()
}
