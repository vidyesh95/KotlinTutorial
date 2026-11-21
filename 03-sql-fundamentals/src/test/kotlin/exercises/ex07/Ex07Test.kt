package exercises.ex07

import org.junit.jupiter.api.Tag
import solutions.ex07.Ex07Solution
import sqlkit.bookstoreDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex07Spec(private val impl: Ex07) {
    private fun query(sql: String) = bookstoreDb().use { it.rows(sql) }

    @Test
    fun `07_1 fantasy books ranked by price`() {
        val expected = listOf(
            listOf<String?>("The Lord of the Rings", "2999", "1"),
            listOf("The Hobbit", "1499", "2"),
            listOf("A Wizard of Earthsea", "999", "3"),
        )
        assertEquals(expected, query(impl.fantasyByPrice))
    }

    @Test
    fun `07_2 cheapest book per category`() {
        val expected = listOf(
            "2" to "I, Robot", "3" to "A Wizard of Earthsea", "4" to "Pride and Prejudice",
            "6" to "The C Programming Language", "7" to "SPQR", "8" to "Designing Data-Intensive Applications",
        ).map { (c, t) -> listOf<String?>(c, t) }
        assertEquals(expected, query(impl.cheapestPerCategory))
    }

    @Test
    fun `07_3 running total of Ada's spending`() {
        val expected = listOf(
            listOf<String?>("1", "5898", "5898"),
            listOf("2", "2998", "8896"),
            listOf("8", "1899", "10795"),
        )
        assertEquals(expected, query(impl.adaRunningSpend))
    }

    @Test
    fun `07_4 days between Alan's orders`() {
        assertEquals(listOf(listOf<String?>("3", null), listOf("9", "101")), query(impl.alanDaysBetweenOrders))
    }
}

class Ex07SolutionTest : Ex07Spec(Ex07Solution)

@Tag("exercise")
class Ex07ExerciseTest : Ex07Spec(Ex07Exercise)
