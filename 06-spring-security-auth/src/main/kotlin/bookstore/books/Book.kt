package bookstore.books

import java.time.Instant

data class Book(
    val id: Long,
    val title: String,
    val authorId: Long,
    val isbn: String,
    val priceCents: Int,
    val stock: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CreateBookRequest(
    val title: String,
    val authorId: Long,
    val isbn: String,
    val priceCents: Int,
    val stock: Int = 0,
)
