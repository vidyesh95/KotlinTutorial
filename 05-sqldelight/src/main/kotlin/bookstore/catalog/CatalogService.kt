package bookstore.catalog

import bookstore.db.BookstoreDatabase
import org.springframework.stereotype.Service

/** A view model for the API. The mapper lambda turns query columns straight into it (lesson 02). */
data class BookView(
    val id: Long,
    val title: String,
    val author: String,
    val isbn: String,
    val priceCents: Int,
    val stock: Int,
    val publishedYear: Int?,
)

@Service
class CatalogService(db: BookstoreDatabase) {
    private val books = db.booksQueries

    // `selectAll(::BookView)`: SQLDelight calls BookView's constructor with the typed columns,
    // in SELECT order. If the query and the class disagree, it doesn't compile.
    fun all(): List<BookView> = books.selectAll(::BookView).executeAsList()

    fun byId(id: Long): BookView? = books.selectById(id, ::BookView).executeAsOneOrNull()
}
