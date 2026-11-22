package bookstore.books

import bookstore.ConflictException
import bookstore.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.time.Clock

/**
 * The service layer holds business rules ("ISBNs are unique", "author must exist").
 * Controllers stay thin (HTTP in/out), repositories stay dumb (SQL only).
 *
 *   HTTP ─▶ BookController ─▶ BookService ─▶ BookRepository ─▶ SQLite
 *            (web layer)       (rules)         (data access)
 *
 * @Service = @Component with a more descriptive name. Constructor injection, no @Autowired (module 02).
 */
@Service
class BookService(
    private val books: BookRepository,
    private val authors: AuthorRepository,
    private val clock: Clock,
) {
    fun list(page: Int, size: Int): Pair<List<Book>, Long> = books.findAll(limit = size, offset = page * size) to books.count()

    fun get(id: Long): Book = books.findById(id) ?: throw NotFoundException("Book", id)

    /** Lesson 06: several queries, one unit of work → @Transactional. */
    @Transactional
    fun create(newBook: NewBook): Book {
        authors.findById(newBook.authorId) ?: throw NotFoundException("Author", newBook.authorId)
        if (books.existsByIsbn(newBook.isbn)) throw ConflictException("ISBN ${newBook.isbn} already exists")
        val id = books.insert(newBook, clock.now())
        return get(id)
    }

    @Transactional
    fun update(id: Long, priceCents: Int?, stock: Int?): Book {
        if (books.update(id, priceCents, stock, clock.now()) == 0) throw NotFoundException("Book", id)
        return get(id)
    }

    fun delete(id: Long) {
        if (books.delete(id) == 0) throw NotFoundException("Book", id)
    }
}
