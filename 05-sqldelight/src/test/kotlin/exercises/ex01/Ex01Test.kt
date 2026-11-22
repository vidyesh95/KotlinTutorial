package exercises.ex01

import app.cash.sqldelight.db.SqlDriver
import bookstore.db.BookstoreDatabase
import bookstore.db.inMemoryBookstore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import kotlin.test.Test
import kotlin.test.assertEquals

/** One spec, two implementations: your Ex01Exercise.sq and the reference Ex01Solution.sq. */
abstract class Ex01Spec {
    private val connection: Pair<SqlDriver, BookstoreDatabase> = inMemoryBookstore()
    protected val db: BookstoreDatabase = connection.second

    abstract fun allTitles(): List<String>
    abstract fun booksCheaperThan(maxCents: Int): List<Triple<Long, String, Int>>
    abstract fun countBooks(): Long
    abstract fun insertAuthor(name: String): Long

    @AfterEach
    fun close() {
        connection.first.close()
    }

    @Test
    fun `01_1 all titles alphabetically`() {
        assertEquals(
            listOf("A Wizard of Earthsea", "Designing Data-Intensive Applications", "Dune", "Dune Messiah", "Emma", "Pride and Prejudice"),
            allTitles(),
        )
    }

    @Test
    fun `01_2 books cheaper than a price, cheapest first`() {
        assertEquals(
            listOf(Triple(3L, "Pride and Prejudice", 799), Triple(4L, "Emma", 899), Triple(6L, "A Wizard of Earthsea", 999)),
            booksCheaperThan(1000),
        )
        assertEquals(emptyList(), booksCheaperThan(799))
    }

    @Test
    fun `01_3 count books`() {
        assertEquals(6, countBooks())
    }

    @Test
    fun `01_4 insert returns the new id`() {
        val id = insertAuthor("Octavia E. Butler")
        assertEquals("Octavia E. Butler", db.authorsQueries.selectById(id).executeAsOne().name)
        assertEquals(5L, id)
    }
}

class Ex01SolutionTest : Ex01Spec() {
    private val q = db.ex01SolutionQueries
    override fun allTitles() = q.allTitles().executeAsList()
    override fun booksCheaperThan(maxCents: Int) = q.booksCheaperThan(maxCents) { id, title, price -> Triple(id, title, price) }.executeAsList()
    override fun countBooks() = q.countBooks().executeAsOne()
    override fun insertAuthor(name: String) = q.insertAuthor(name).executeAsOne()
}

@Tag("exercise")
class Ex01ExerciseTest : Ex01Spec() {
    private val q = db.ex01ExerciseQueries
    override fun allTitles() = q.allTitles().executeAsList()
    override fun booksCheaperThan(maxCents: Int) = q.booksCheaperThan(maxCents) { id, title, price -> Triple(id, title, price) }.executeAsList()
    override fun countBooks() = q.countBooks().executeAsOne()
    override fun insertAuthor(name: String) = q.insertAuthor(name).executeAsOne()
}
