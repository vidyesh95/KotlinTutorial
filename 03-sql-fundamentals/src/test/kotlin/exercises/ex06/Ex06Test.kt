package exercises.ex06

import org.junit.jupiter.api.Tag
import solutions.ex06.Ex06Solution
import sqlkit.bookstoreDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex06Spec(private val impl: Ex06) {
    private fun query(sql: String) = bookstoreDb().use { it.rows(sql) }

    @Test
    fun `06_1 books never ordered`() {
        assertEquals(listOf(listOf<String?>("Collected Notes")), query(impl.booksNeverOrdered))
    }

    @Test
    fun `06_2 authors above average price`() {
        val expected = listOf("Brian Kernighan", "J.R.R. Tolkien", "Martin Kleppmann", "Mary Beard").map { listOf<String?>(it) }
        assertEquals(expected, query(impl.authorsAboveAveragePrice))
    }

    @Test
    fun `06_3 books per top category, recursively`() {
        assertEquals(listOf(listOf<String?>("Fiction", "10"), listOf("Non-fiction", "3")), query(impl.booksPerTopCategory))
    }

    @Test
    fun `06_4 calendar of the first week of June`() {
        val expected = listOf(
            "2025-06-01" to "1", "2025-06-02" to "0", "2025-06-03" to "1", "2025-06-04" to "0",
            "2025-06-05" to "0", "2025-06-06" to "0", "2025-06-07" to "0",
        ).map { (day, n) -> listOf<String?>(day, n) }
        assertEquals(expected, query(impl.ordersFirstWeekOfJune))
    }
}

class Ex06SolutionTest : Ex06Spec(Ex06Solution)

@Tag("exercise")
class Ex06ExerciseTest : Ex06Spec(Ex06Exercise)
