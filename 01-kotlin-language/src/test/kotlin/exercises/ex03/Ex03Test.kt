package exercises.ex03

import org.junit.jupiter.api.Tag
import solutions.ex03.Ex03Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex03Spec(private val impl: Ex03) {

    @Test
    fun `03_1 averageOf avoids integer division and overflow`() {
        assertEquals(3.5, impl.averageOf(3, 4))
        assertEquals(-1.5, impl.averageOf(-1, -2))
        assertEquals(2147483647.0, impl.averageOf(Int.MAX_VALUE, Int.MAX_VALUE))
    }

    @Test
    fun `03_2 isEven`() {
        assertTrue(impl.isEven(0))
        assertTrue(impl.isEven(-4))
        assertFalse(impl.isEven(7))
        assertFalse(impl.isEven(-3))
    }

    @Test
    fun `03_3 roundToCents`() {
        assertEquals(2000L, impl.roundToCents(19.999))
        assertEquals(1999L, impl.roundToCents(19.99))
        assertEquals(13L, impl.roundToCents(0.125))
    }

    @Test
    fun `03_4 hasPermission`() {
        assertTrue(impl.hasPermission(1 or 2, 2))
        assertFalse(impl.hasPermission(1, 2))
        assertTrue(impl.hasPermission(7, 1 or 4))
        assertFalse(impl.hasPermission(1 or 2, 1 or 4))
    }
}

class Ex03SolutionTest : Ex03Spec(Ex03Solution)

@Tag("exercise")
class Ex03ExerciseTest : Ex03Spec(Ex03Exercise)
