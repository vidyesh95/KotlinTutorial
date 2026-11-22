package bookstore.books

import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import java.sql.ResultSet
import kotlin.time.Instant

/**
 * Lesson 05 — data access with Spring's JdbcClient: plain SQL (module 03!) with
 * named parameters, a fluent API and automatic connection/statement handling.
 *
 * Compare with module 03, lesson 11 (raw JDBC): no Connection, no PreparedStatement,
 * no `use {}` — Spring borrows a pooled connection, joins the current transaction if
 * there is one, and translates SQLExceptions into Spring's DataAccessException hierarchy.
 *
 * @Repository = @Component + "translate persistence exceptions".
 */
@Repository
class BookRepository(private val jdbc: JdbcClient) {

    private val selectBooks = """
        SELECT b.id, b.title, b.author_id, a.name AS author_name, b.isbn, b.price_cents, b.stock, b.created_at
        FROM books b
        JOIN authors a ON a.id = b.author_id
    """.trimIndent()

    fun findAll(limit: Int, offset: Int): List<Book> =
        jdbc.sql("$selectBooks ORDER BY b.id LIMIT :limit OFFSET :offset")
            .param("limit", limit)
            .param("offset", offset)
            .query { rs, _ -> rs.toBook() }
            .list()

    fun count(): Long = jdbc.sql("SELECT count(*) FROM books").query(Long::class.java).single()

    /** `optional()` gives a java.util.Optional; `.orElse(null)` turns it into Kotlin's `Book?`. */
    fun findById(id: Long): Book? =
        jdbc.sql("$selectBooks WHERE b.id = :id")
            .param("id", id)
            .query { rs, _ -> rs.toBook() }
            .optional()
            .orElse(null)

    fun existsByIsbn(isbn: String): Boolean =
        jdbc.sql("SELECT EXISTS (SELECT 1 FROM books WHERE isbn = :isbn)")
            .param("isbn", isbn)
            .query(Boolean::class.java)
            .single()

    /** RETURNING id (module 03, lesson 03) hands back the generated primary key. */
    fun insert(book: NewBook, now: Instant): Long =
        jdbc.sql(
            """
            INSERT INTO books (title, author_id, isbn, price_cents, stock, created_at, updated_at)
            VALUES (:title, :authorId, :isbn, :priceCents, :stock, :now, :now)
            RETURNING id
            """.trimIndent(),
        )
            .param("title", book.title)
            .param("authorId", book.authorId)
            .param("isbn", book.isbn)
            .param("priceCents", book.priceCents)
            .param("stock", book.stock)
            .param("now", now.toString())       // timestamps stored as ISO-8601 TEXT
            .query(Long::class.java)
            .single()

    /** Returns how many rows changed: 0 means "no such book". */
    fun update(id: Long, priceCents: Int?, stock: Int?, now: Instant): Int =
        jdbc.sql(
            """
            UPDATE books
            SET price_cents = coalesce(:priceCents, price_cents),
                stock       = coalesce(:stock, stock),
                updated_at  = :now
            WHERE id = :id
            """.trimIndent(),
        )
            .param("id", id)
            .param("priceCents", priceCents)
            .param("stock", stock)
            .param("now", now.toString())
            .update()

    fun delete(id: Long): Int = jdbc.sql("DELETE FROM books WHERE id = :id").param("id", id).update()

    /**
     * Lesson 06 — the stock check and the decrement happen in ONE statement, so two
     * customers can't both buy the last copy ("check-then-act" race avoided by the database).
     * Returns 1 if the stock was taken, 0 if there wasn't enough.
     */
    fun takeStock(id: Long, quantity: Int): Int =
        jdbc.sql("UPDATE books SET stock = stock - :quantity WHERE id = :id AND stock >= :quantity")
            .param("id", id)
            .param("quantity", quantity)
            .update()

    fun findLowStock(threshold: Int): List<Book> =
        jdbc.sql("$selectBooks WHERE b.stock <= :threshold ORDER BY b.stock, b.id")
            .param("threshold", threshold)
            .query { rs, _ -> rs.toBook() }
            .list()

    /** Row mapper: one ResultSet row → one Book (same idea as module 03, lesson 11). */
    private fun ResultSet.toBook() = Book(
        id = getLong("id"),
        title = getString("title"),
        authorId = getLong("author_id"),
        authorName = getString("author_name"),
        isbn = getString("isbn"),
        priceCents = getInt("price_cents"),
        stock = getInt("stock"),
        createdAt = Instant.parse(getString("created_at")),
    )
}

@Repository
class AuthorRepository(private val jdbc: JdbcClient) {
    fun findById(id: Long): Author? =
        jdbc.sql("SELECT id, name, created_at FROM authors WHERE id = :id")
            .param("id", id)
            .query { rs, _ -> Author(rs.getLong("id"), rs.getString("name"), Instant.parse(rs.getString("created_at"))) }
            .optional()
            .orElse(null)
}
