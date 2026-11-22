package exercises.ex02

import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.db.SqlDriver
import bookstore.db.BookstoreDatabase
import bookstore.db.inMemoryBookstore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import solutions.ex02.IsbnAdapterSolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

data class PriceStatsRow(val cheapest: Int?, val dearest: Int?, val average: Double?)

abstract class Ex02Spec {
    private val connection: Pair<SqlDriver, BookstoreDatabase> = inMemoryBookstore()
    protected val db: BookstoreDatabase = connection.second

    abstract fun booksByAuthor(authorName: String): List<Pair<Long, String>>
    abstract fun titlesByIds(ids: Collection<Long>): List<String>
    abstract fun yearsForAuthor(authorId: Long): List<Pair<String, Int?>>
    abstract fun priceStats(): PriceStatsRow
    abstract val isbnAdapter: ColumnAdapter<Isbn, String>

    @AfterEach
    fun close() {
        connection.first.close()
    }

    @Test
    fun `02_1 books by author name`() {
        assertEquals(listOf(1L to "Dune", 2L to "Dune Messiah"), booksByAuthor("Frank Herbert"))
        assertEquals(emptyList(), booksByAuthor("Nobody"))
    }

    @Test
    fun `02_2 titles by a collection of ids`() {
        assertEquals(listOf("Dune", "Emma", "A Wizard of Earthsea"), titlesByIds(listOf(6, 1, 4)))
    }

    @Test
    fun `02_3 nullable published year`() {
        assertEquals(listOf("A Wizard of Earthsea" to null), yearsForAuthor(4))
        assertEquals(listOf("Pride and Prejudice" to 1813, "Emma" to 1815), yearsForAuthor(2))
    }

    @Test
    fun `02_4 price statistics`() {
        val stats = priceStats()
        assertEquals(799, stats.cheapest)
        assertEquals(4599, stats.dearest)
        assertEquals(1632.33, stats.average!!, 0.01)
    }

    @Test
    fun `02_5 isbn adapter`() {
        assertEquals(Isbn("9780441172719"), isbnAdapter.decode("9780441172719"))
        assertEquals("9780441172719", isbnAdapter.encode(Isbn("9780441172719")))
        assertFailsWith<IllegalArgumentException> { isbnAdapter.decode("not-an-isbn") }
    }
}

class Ex02SolutionTest : Ex02Spec() {
    private val q = db.ex02SolutionQueries
    override fun booksByAuthor(authorName: String) = q.booksByAuthor(authorName) { id, title -> id to title }.executeAsList()
    override fun titlesByIds(ids: Collection<Long>) = q.titlesByIds(ids).executeAsList()
    override fun yearsForAuthor(authorId: Long) = q.yearsForAuthor(authorId) { title, year -> title to year }.executeAsList()
    override fun priceStats() = q.priceStats(::PriceStatsRow).executeAsOne()
    override val isbnAdapter: ColumnAdapter<Isbn, String> = IsbnAdapterSolution
}

@Tag("exercise")
class Ex02ExerciseTest : Ex02Spec() {
    private val q = db.ex02ExerciseQueries
    override fun booksByAuthor(authorName: String) = q.booksByAuthor(authorName) { id, title -> id to title }.executeAsList()
    override fun titlesByIds(ids: Collection<Long>) = q.titlesByIds(ids).executeAsList()
    override fun yearsForAuthor(authorId: Long) = q.yearsForAuthor(authorId) { title, year -> title to year }.executeAsList()
    override fun priceStats() = q.priceStats(::PriceStatsRow).executeAsOne()
    override val isbnAdapter: ColumnAdapter<Isbn, String> = IsbnAdapterExercise
}
