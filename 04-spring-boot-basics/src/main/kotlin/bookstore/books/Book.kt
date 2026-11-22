package bookstore.books

import kotlin.time.Instant

/** A row of `books` joined with its author's name. Plain immutable data — no framework annotations. */
data class Book(
    val id: Long,
    val title: String,
    val authorId: Long,
    val authorName: String,
    val isbn: String,
    val priceCents: Int,
    val stock: Int,
    val createdAt: Instant,
)

data class NewBook(
    val title: String,
    val authorId: Long,
    val isbn: String,
    val priceCents: Int,
    val stock: Int,
)

data class Author(val id: Long, val name: String, val createdAt: Instant)
