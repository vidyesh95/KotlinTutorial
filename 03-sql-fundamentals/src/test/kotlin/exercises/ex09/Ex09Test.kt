package exercises.ex09

import org.junit.jupiter.api.Tag
import solutions.ex09.Ex09Solution
import sqlkit.bookstoreDb
import sqlkit.exec
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex09Spec(private val impl: Ex09) {
    private fun query(sql: String) = bookstoreDb().use { it.rows(sql) }

    @Test
    fun `09_1 upsert restocks`() {
        bookstoreDb().use { db ->
            db.exec("CREATE TABLE inventory (book_id INTEGER PRIMARY KEY, qty INTEGER NOT NULL)")
            db.exec(impl.restockBookOne)
            assertEquals(listOf(listOf<String?>("1", "5")), db.rows("SELECT book_id, qty FROM inventory"))
            db.exec(impl.restockBookOne)
            assertEquals(listOf(listOf<String?>("1", "10")), db.rows("SELECT book_id, qty FROM inventory"))
        }
    }

    @Test
    fun `09_2 books as a JSON array`() {
        val expected = """[{"title":"Pride and Prejudice","price_cents":799},{"title":"Emma","price_cents":899}]"""
        assertEquals(listOf(listOf<String?>(expected)), query(impl.janeAustenAsJson))
    }

    @Test
    fun `09_3 orders per month`() {
        val expected = listOf("2025-06" to "2", "2025-07" to "1", "2025-08" to "2", "2025-09" to "3", "2025-10" to "1")
            .map { (m, n) -> listOf<String?>(m, n) }
        assertEquals(expected, query(impl.ordersPerMonth))
    }

    @Test
    fun `09_4 loyal customers`() {
        assertEquals(listOf(listOf<String?>("Ada Lovelace"), listOf("Alan Turing")), query(impl.loyalCustomers))
    }
}

class Ex09SolutionTest : Ex09Spec(Ex09Solution)

@Tag("exercise")
class Ex09ExerciseTest : Ex09Spec(Ex09Exercise)
