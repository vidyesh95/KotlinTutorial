package exercises.ex09

import org.junit.jupiter.api.Tag
import solutions.ex09.Ex09Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex09Spec(private val impl: Ex09) {

    @Test
    fun `09_1 sumOfMultiples`() {
        assertEquals(23, impl.sumOfMultiples(10))
        assertEquals(233168, impl.sumOfMultiples(1000))
        assertEquals(0, impl.sumOfMultiples(1))
    }

    @Test
    fun `09_2 evensDown`() {
        assertEquals(listOf(8, 6, 4, 2, 0), impl.evensDown(9))
        assertEquals(listOf(10, 8, 6, 4, 2, 0), impl.evensDown(10))
        assertEquals(listOf(0), impl.evensDown(0))
    }

    @Test
    fun `09_3 firstDuplicate`() {
        assertEquals("a", impl.firstDuplicate(listOf("a", "b", "a", "b")))
        assertEquals("b", impl.firstDuplicate(listOf("a", "b", "c", "b", "a")))
        assertNull(impl.firstDuplicate(listOf("x", "y")))
    }

    @Test
    fun `09_4 findPair`() {
        assertEquals(0 to 3, impl.findPair(listOf(3, 1, 4, 2), 5))
        assertEquals(1 to 2, impl.findPair(listOf(10, 2, 3), 5))
        assertNull(impl.findPair(listOf(1, 2), 10))
    }
}

class Ex09SolutionTest : Ex09Spec(Ex09Solution)

@Tag("exercise")
class Ex09ExerciseTest : Ex09Spec(Ex09Exercise)
