package bookstore

/**
 * Lesson 03 — domain errors. Services throw these; ApiExceptionHandler turns them into
 * HTTP responses. Services stay HTTP-free (they don't know about status codes).
 *
 * They extend RuntimeException on purpose: Spring's @Transactional rolls back on
 * RuntimeException by default (lesson 06).
 */
sealed class BookstoreException(message: String) : RuntimeException(message)

class NotFoundException(what: String, id: Any) : BookstoreException("$what $id not found")

class ConflictException(message: String) : BookstoreException(message)

class OutOfStockException(val bookId: Long, requested: Int, available: Int) :
    BookstoreException("Book $bookId: requested $requested, only $available in stock")
