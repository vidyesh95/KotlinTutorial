package exercises.ex22

import org.junit.jupiter.api.Tag
import solutions.ex22.Ex22Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex22Spec(private val impl: Ex22) {

    @Test
    fun `22_1 sumNumbers picks numbers out of mixed values`() {
        assertEquals(6L, impl.sumNumbers(listOf(1, "2", 3L, "x", 4.5, null)))
        assertEquals(0L, impl.sumNumbers(emptyList()))
    }

    @Test
    fun `22_2 intOrDefault never throws`() {
        val map = mapOf("a" to 1, "b" to "2", "c" to null)
        assertEquals(1, impl.intOrDefault(map, "a", -1))
        assertEquals(-1, impl.intOrDefault(map, "b", -1))
        assertEquals(-1, impl.intOrDefault(map, "c", -1))
        assertEquals(-1, impl.intOrDefault(map, "zzz", -1))
    }

    @Test
    fun `22_3 require throws with a clear message`() {
        assertEquals("8080", impl.require(mapOf("port" to "8080"), "port"))
        val error = assertFailsWith<IllegalArgumentException> { impl.require(emptyMap(), "port") }
        assertEquals("missing port", error.message)
    }

    @Test
    fun `22_4 samePoint compares content`() {
        assertTrue(impl.samePoint(Point(1, 2), Point(1, 2)))
        assertFalse(impl.samePoint(Point(1, 2), Point(2, 1)))
    }
}

class Ex22SolutionTest : Ex22Spec(Ex22Solution)

@Tag("exercise")
class Ex22ExerciseTest : Ex22Spec(Ex22Exercise)
