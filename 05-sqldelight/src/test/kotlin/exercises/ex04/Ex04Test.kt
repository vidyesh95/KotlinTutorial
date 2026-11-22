package exercises.ex04

import app.cash.sqldelight.db.SqlDriver
import bookstore.db.BookstoreDatabase
import bookstore.db.inMemoryBookstore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.yield
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import solutions.ex04.Ex04Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class Ex04Spec(private val impl: Ex04) {
    private val connection: Pair<SqlDriver, BookstoreDatabase> = inMemoryBookstore()
    private val db = connection.second

    private fun stock(id: Long) = db.booksQueries.selectById(id).executeAsOne().stock

    @AfterEach
    fun close() {
        connection.first.close()
    }

    @Test
    fun `04_1 restock adds stock`() {
        impl.restock(db, mapOf(1L to 3, 4L to 5))
        assertEquals(15, stock(1))
        assertEquals(5, stock(4))
    }

    @Test
    fun `04_1 restock is all or nothing`() {
        assertFailsWith<NoSuchElementException> { impl.restock(db, mapOf(1L to 3, 999L to 1)) }
        assertEquals(12, stock(1), "the first update must be rolled back")
    }

    @Test
    fun `04_2 transfer moves stock or changes nothing`() {
        assertTrue(impl.transferStock(db, fromId = 1, toId = 4, quantity = 2))
        assertEquals(10, stock(1))
        assertEquals(2, stock(4))
        assertFalse(impl.transferStock(db, fromId = 2, toId = 4, quantity = 5))   // Dune Messiah has only 2
        assertEquals(2, stock(2))
        assertEquals(2, stock(4))
    }

    @Test
    fun `04_3 low stock flow re-emits on changes`() {
        runBlocking {
            val emissions = mutableListOf<List<String>>()
            val job = launch(Dispatchers.Unconfined) {
                impl.lowStockTitles(db, threshold = 2, context = Dispatchers.Unconfined).collect { emissions += it }
            }
            withTimeout(5_000) { while (emissions.isEmpty()) yield() }
            db.booksQueries.takeStock(quantity = 3, id = 5)                    // DDIA: 4 → 1
            withTimeout(5_000) { while (emissions.size < 2) yield() }
            job.cancel()
            assertEquals(listOf("Emma", "Dune Messiah"), emissions[0])
            assertEquals(listOf("Emma", "Designing Data-Intensive Applications", "Dune Messiah"), emissions[1])
        }
    }
}

class Ex04SolutionTest : Ex04Spec(Ex04Solution)

@Tag("exercise")
class Ex04ExerciseTest : Ex04Spec(Ex04Exercise)
