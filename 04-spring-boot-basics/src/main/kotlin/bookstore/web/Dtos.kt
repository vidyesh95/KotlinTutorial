package bookstore.web

import bookstore.books.Book
import bookstore.orders.Order
import jakarta.validation.Valid
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Positive
import jakarta.validation.constraints.PositiveOrZero
import jakarta.validation.constraints.Size

/*
 * Lessons 02–03 — DTOs (Data Transfer Objects): the JSON shapes of the API.
 *
 * Why not return `Book` directly? The API is a PUBLIC CONTRACT. DTOs let the database
 * and the API change independently, and keep internal fields (like updated_at) private.
 *
 * Validation annotations (lesson 03): `@field:` puts the annotation on the backing FIELD,
 * where Jakarta Validation looks. (On a constructor `val`, an annotation could target the
 * parameter, property, field or getter — `@field:` removes all doubt.)
 */

data class CreateBookRequest(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    @field:Positive
    val authorId: Long,
    @field:Pattern(regexp = "\\d{13}", message = "must be exactly 13 digits")
    val isbn: String,
    @field:PositiveOrZero
    val priceCents: Int,
    @field:PositiveOrZero
    val stock: Int = 0,                // optional in JSON thanks to the default value
)

/** PATCH = partial update: every field optional, null means "leave unchanged". */
data class UpdateBookRequest(
    @field:PositiveOrZero val priceCents: Int? = null,
    @field:PositiveOrZero val stock: Int? = null,
)

data class BookResponse(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val priceCents: Int,
    val price: String,
    val stock: Int,
    val lowStock: Boolean,
)

fun Book.toResponse(currency: String, lowStockThreshold: Int) = BookResponse(
    id = id,
    title = title,
    author = authorName,
    isbn = isbn,
    priceCents = priceCents,
    price = "%d.%02d %s".format(priceCents / 100, priceCents % 100, currency),
    stock = stock,
    lowStock = stock <= lowStockThreshold,
)

/** A generic page wrapper (module 01, lesson 23) reused by every list endpoint. */
data class PageResponse<T>(
    val items: List<T>,
    val page: Int,
    val size: Int,
    val totalItems: Long,
)

data class OrderLineRequest(
    @field:Positive val bookId: Long,
    @field:Positive @field:Max(100) val quantity: Int,
)

data class PlaceOrderRequest(
    @field:Email @field:NotBlank
    val customerEmail: String,
    @field:NotEmpty @field:Valid      // @Valid = "also validate each element"
    val lines: List<OrderLineRequest>,
)

data class OrderResponse(val id: Long, val customerEmail: String, val totalCents: Int, val createdAt: String)

fun Order.toResponse() = OrderResponse(id, customerEmail, totalCents, createdAt.toString())
