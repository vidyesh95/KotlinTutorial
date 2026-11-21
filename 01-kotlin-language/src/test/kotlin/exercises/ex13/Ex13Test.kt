package exercises.ex13

import org.junit.jupiter.api.Tag
import solutions.ex13.Ex13Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex13Spec(private val impl: Ex13) {

    @Test
    fun `13_1 myFilter`() {
        assertEquals(listOf(2, 4), impl.myFilter(listOf(1, 2, 3, 4)) { it % 2 == 0 })
        assertEquals(emptyList(), impl.myFilter(listOf("a")) { false })
    }

    @Test
    fun `13_2 myMap`() {
        assertEquals(listOf(3, 5), impl.myMap(listOf("abc", "hello")) { it.length })
    }

    @Test
    fun `13_3 andThen`() {
        assertEquals(50, impl.andThen({ it + 1 }, { it * 10 })(4))
        assertEquals(41, impl.andThen({ it * 10 }, { it + 1 })(4))
    }

    @Test
    fun `13_4 firstNegativeIndex`() {
        assertEquals(2, impl.firstNegativeIndex(listOf(5, 0, -3, -8)))
        assertEquals(-1, impl.firstNegativeIndex(listOf(1, 2)))
    }
}

class Ex13SolutionTest : Ex13Spec(Ex13Solution)

@Tag("exercise")
class Ex13ExerciseTest : Ex13Spec(Ex13Exercise)
