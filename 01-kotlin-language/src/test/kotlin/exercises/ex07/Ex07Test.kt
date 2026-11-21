package exercises.ex07

import org.junit.jupiter.api.Tag
import solutions.ex07.Ex07Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex07Spec(private val impl: Ex07) {

    @Test
    fun `07_1 wordFrequencies`() {
        assertEquals(mapOf("the" to 2, "cat" to 1), impl.wordFrequencies("The cat  the"))
        assertEquals(emptyMap(), impl.wordFrequencies(""))
    }

    @Test
    fun `07_2 topSpenders`() {
        val purchases = listOf(
            Purchase("bob", 10),
            Purchase("ann", 5),
            Purchase("cid", 30),
            Purchase("ann", 25),
            Purchase("dan", 1),
        )
        assertEquals(listOf("ann", "cid"), impl.topSpenders(purchases, 2))   // ann=30, cid=30 → alphabetical
        assertEquals(listOf("ann", "cid", "bob", "dan"), impl.topSpenders(purchases, 10))
        assertEquals(emptyList(), impl.topSpenders(emptyList(), 3))
    }

    @Test
    fun `07_3 neighbourDifferences`() {
        assertEquals(listOf(3, 5, 7), impl.neighbourDifferences(listOf(1, 4, 9, 16)))
        assertEquals(listOf(-2), impl.neighbourDifferences(listOf(5, 3)))
        assertEquals(emptyList(), impl.neighbourDifferences(listOf(1)))
    }

    @Test
    fun `07_4 firstSquareAbove`() {
        assertEquals(64, impl.firstSquareAbove(50))
        assertEquals(1, impl.firstSquareAbove(0))
        assertEquals(25, impl.firstSquareAbove(16))
    }
}

class Ex07SolutionTest : Ex07Spec(Ex07Solution)

@Tag("exercise")
class Ex07ExerciseTest : Ex07Spec(Ex07Exercise)
