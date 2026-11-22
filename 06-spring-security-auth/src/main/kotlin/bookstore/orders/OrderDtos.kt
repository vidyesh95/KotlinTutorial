package bookstore.orders

import java.time.Instant

data class OrderItemRequest(
    val bookId: Long,
    val quantity: Int,
)

data class CreateOrderRequest(
    val items: List<OrderItemRequest>,
)

data class OrderItemResponse(
    val bookId: Long,
    val quantity: Int,
    val unitPriceCents: Int,
)

data class OrderResponse(
    val id: Long,
    val userId: String,
    val totalCents: Int,
    val createdAt: Instant,
    val items: List<OrderItemResponse>,
)
