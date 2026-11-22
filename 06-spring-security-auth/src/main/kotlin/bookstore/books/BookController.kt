package bookstore.books

import bookstore.ResourceNotFoundException
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/books")
class BookController(
    private val bookRepository: BookRepository,
) {

    @GetMapping
    fun getAllBooks(): List<Book> = bookRepository.findAll()

    @GetMapping("/{id}")
    fun getBookById(@PathVariable id: Long): Book =
        bookRepository.findById(id) ?: throw ResourceNotFoundException("Book with ID $id not found")

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createBook(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<Book> {
        val created = bookRepository.create(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(created)
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteBook(@PathVariable id: Long) {
        if (!bookRepository.delete(id)) {
            throw ResourceNotFoundException("Book with ID $id not found")
        }
    }
}
