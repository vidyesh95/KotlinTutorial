package bookstore.books

import bookstore.ConflictException
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class BookRepository(private val jdbcClient: JdbcClient) {

    fun findAll(): List<Book> =
        jdbcClient.sql("SELECT id, title, author_id, isbn, price_cents, stock, created_at, updated_at FROM books ORDER BY id")
            .query { rs, _ ->
                Book(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    authorId = rs.getLong("author_id"),
                    isbn = rs.getString("isbn"),
                    priceCents = rs.getInt("price_cents"),
                    stock = rs.getInt("stock"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .list()

    fun findById(id: Long): Book? =
        jdbcClient.sql("SELECT id, title, author_id, isbn, price_cents, stock, created_at, updated_at FROM books WHERE id = :id")
            .param("id", id)
            .query { rs, _ ->
                Book(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    authorId = rs.getLong("author_id"),
                    isbn = rs.getString("isbn"),
                    priceCents = rs.getInt("price_cents"),
                    stock = rs.getInt("stock"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun findByIsbn(isbn: String): Book? =
        jdbcClient.sql("SELECT id, title, author_id, isbn, price_cents, stock, created_at, updated_at FROM books WHERE isbn = :isbn")
            .param("isbn", isbn)
            .query { rs, _ ->
                Book(
                    id = rs.getLong("id"),
                    title = rs.getString("title"),
                    authorId = rs.getLong("author_id"),
                    isbn = rs.getString("isbn"),
                    priceCents = rs.getInt("price_cents"),
                    stock = rs.getInt("stock"),
                    createdAt = Instant.parse(rs.getString("created_at")),
                    updatedAt = Instant.parse(rs.getString("updated_at")),
                )
            }
            .optional()
            .orElse(null)

    fun create(book: CreateBookRequest): Book {
        if (findByIsbn(book.isbn) != null) {
            throw ConflictException("Book with ISBN ${book.isbn} already exists")
        }
        val now = Instant.now()
        val keyHolder = org.springframework.jdbc.support.GeneratedKeyHolder()

        jdbcClient.sql(
            """
            INSERT INTO books (title, author_id, isbn, price_cents, stock, created_at, updated_at)
            VALUES (:title, :authorId, :isbn, :priceCents, :stock, :createdAt, :updatedAt)
            """.trimIndent()
        )
            .param("title", book.title)
            .param("authorId", book.authorId)
            .param("isbn", book.isbn)
            .param("priceCents", book.priceCents)
            .param("stock", book.stock)
            .param("createdAt", now.toString())
            .param("updatedAt", now.toString())
            .update(keyHolder)

        val id = keyHolder.key?.toLong() ?: throw IllegalStateException("Failed to retrieve generated book ID")
        return Book(id, book.title, book.authorId, book.isbn, book.priceCents, book.stock, now, now)
    }

    fun delete(id: Long): Boolean =
        jdbcClient.sql("DELETE FROM books WHERE id = :id")
            .param("id", id)
            .update() > 0
}
