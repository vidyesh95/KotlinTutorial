package exercises.ex24

import org.junit.jupiter.api.Tag
import solutions.ex24.Ex24Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

abstract class Ex24Spec(private val impl: Ex24) {
    @Test
    fun `24_1 clamp keeps value in range`() {
        assertEquals(10, impl.clamp(15, 0, 10))
        assertEquals(0, impl.clamp(-3, 0, 10))
        assertEquals(5, impl.clamp(5, 0, 10))
        assertEquals("m", impl.clamp("z", "a", "m"))
    }

    @Test
    fun `24_2 isAscending`() {
        assertTrue(impl.isAscending(listOf(1, 2, 2, 5)))
        assertFalse(impl.isAscending(listOf(3, 1)))
        assertTrue(impl.isAscending(emptyList<String>()))
        assertTrue(impl.isAscending(listOf("solo")))
    }

    @Test
    fun `24_3 sumAsDouble mixes number types`() {
        assertEquals(6.5, impl.sumAsDouble(listOf<Number>(1, 2.5, 3L)))
        assertEquals(0.0, impl.sumAsDouble(emptyList<Int>()))
    }

    @Test
    fun `24_4 invert swaps keys and values`() {
        assertEquals(mapOf(1 to "a", 2 to "b"), impl.invert(mapOf("a" to 1, "b" to 2)))
        assertEquals(mapOf(1 to "b"), impl.invert(mapOf("a" to 1, "b" to 1)))
    }
}

class Ex24SolutionTest : Ex24Spec(Ex24Solution)

@Tag("exercise")
class Ex24ExerciseTest : Ex24Spec(Ex24Exercise)
