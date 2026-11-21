package exercises.ex20

import org.junit.jupiter.api.Tag
import solutions.ex20.Ex20Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex20Spec(private val impl: Ex20) {

    @Test
    fun `20_1 capitalizeWords`() {
        assertEquals("Hello Big World", impl.capitalizeWords("hello big world"))
        assertEquals("Kotlin", impl.capitalizeWords("kotlin"))
        assertEquals("", impl.capitalizeWords(""))
    }

    @Test
    fun `20_2 add reduces the result`() {
        assertEquals(Fraction(5, 6), impl.add(Fraction(1, 2), Fraction(1, 3)))
        assertEquals(Fraction(1, 2), impl.add(Fraction(1, 4), Fraction(1, 4)))
        assertEquals(Fraction(0, 1), impl.add(Fraction(1, 2), Fraction(-1, 2)))
    }

    @Test
    fun `20_3 compare by value`() {
        assertTrue(impl.compare(Fraction(1, 3), Fraction(1, 2)) < 0)
        assertTrue(impl.compare(Fraction(3, 4), Fraction(2, 3)) > 0)
        assertEquals(0, impl.compare(Fraction(1, 2), Fraction(2, 4)))
    }
}

class Ex20SolutionTest : Ex20Spec(Ex20Solution)

@Tag("exercise")
class Ex20ExerciseTest : Ex20Spec(Ex20Exercise)
