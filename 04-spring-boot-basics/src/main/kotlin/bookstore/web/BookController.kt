package bookstore.web

import bookstore.books.BookService
import bookstore.books.NewBook
import bookstore.config.BookstoreProperties
import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

/**
 * Lesson 02 — a REST controller.
 *
 *  GET    /api/books?page=0&size=20   list (paged)          200
 *  GET    /api/books/{id}             one book              200 / 404
 *  POST   /api/books                  create (JSON body)    201 + Location header / 400 / 404 / 409
 *  PATCH  /api/books/{id}             change price/stock    200 / 400 / 404
 *  DELETE /api/books/{id}             delete                204 / 404
 *
 * @RestController = @Controller + @ResponseBody: return values are written as JSON (Jackson).
 * Try it: http/books.http (IntelliJ HTTP client) or the curl commands in lessons/02-rest-controllers.md.
 */
@RestController
@RequestMapping("/api/books")
class BookController(
    private val service: BookService,
    private val props: BookstoreProperties,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,      // constraint on a parameter →
        @RequestParam(defaultValue = "20") @Min(1) size: Int,     // automatic 400 if violated (lesson 03)
    ): PageResponse<BookResponse> {
        val pageSize = size.coerceAtMost(props.maxPageSize)
        val (books, total) = service.list(page, pageSize)
        return PageResponse(books.map { it.toDto() }, page, pageSize, total)
    }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): BookResponse = service.get(id).toDto()

    @PostMapping
    fun create(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse> {
        val book = service.create(
            NewBook(request.title.trim(), request.authorId, request.isbn, request.priceCents, request.stock),
        )
        // 201 Created + Location: /api/books/{newId} — the REST way to say "here's where it lives now".
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(book.id).toUri()
        return ResponseEntity.created(location).body(book.toDto())
    }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateBookRequest): BookResponse =
        service.update(id, request.priceCents, request.stock).toDto()

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)

    private fun bookstore.books.Book.toDto() = toResponse(props.currency, props.lowStockThreshold)
}
