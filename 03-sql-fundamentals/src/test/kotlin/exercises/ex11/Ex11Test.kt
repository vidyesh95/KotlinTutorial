package exercises.ex11

import org.junit.jupiter.api.Tag
import solutions.ex11.Ex11Solution
import sqlkit.bookstoreDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

abstract class Ex11Spec(private val impl: Ex11) {
    @Test
    fun `11_1 books by author`() {
        bookstoreDb().use { db ->
            assertEquals(listOf("Pride and Prejudice", "Emma"), impl.findBooksByAuthor(db, "Jane Austen").map { it.title })
            assertEquals(emptyList(), impl.findBooksByAuthor(db, "Nobody"))
        }
    }

    @Test
    fun `11_1 is safe against SQL injection`() {
        bookstoreDb().use { db ->
            assertEquals(emptyList(), impl.findBooksByAuthor(db, "x' OR '1'='1"))
            assertEquals(14, db.rows("SELECT count(*) FROM books").single().single()!!.toInt(), "books table must survive")
        }
    }

    @Test
    fun `11_2 find one book with nullable year`() {
        bookstoreDb().use { db ->
            assertEquals(Book(1, "Dune", 1299, 1965), impl.findBook(db, 1))
            assertEquals(Book(14, "Collected Notes", 500, null), impl.findBook(db, 14))
            assertNull(impl.findBook(db, 999))
        }
    }

    @Test
    fun `11_3 insert returns the generated id`() {
        bookstoreDb().use { db ->
            val id = impl.insertAuthor(db, "N. K. Jemisin", null)
            assertEquals(10L, id)
            assertEquals(listOf(listOf("N. K. Jemisin", null)), db.rows("SELECT name, country FROM authors WHERE id = ?", id))
        }
    }

    @Test
    fun `11_4 raise prices in one category`() {
        bookstoreDb().use { db ->
            assertEquals(3, impl.raisePrices(db, categoryId = 3, percent = 10))
            assertEquals(listOf(listOf("1098"), listOf("1648"), listOf("3298")),
                db.rows("SELECT price_cents FROM books WHERE category_id = 3 ORDER BY id"))
            assertEquals(listOf(listOf("1299")), db.rows("SELECT price_cents FROM books WHERE id = 1"))
        }
    }
}

class Ex11SolutionTest : Ex11Spec(Ex11Solution)

@Tag("exercise")
class Ex11ExerciseTest : Ex11Spec(Ex11Exercise)
