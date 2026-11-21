package exercises.ex23

import org.junit.jupiter.api.Tag
import solutions.ex23.Ex23Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex23Spec(private val impl: Ex23) {

    @Test
    fun `23_1 doubleEach repeats every element`() {
        assertEquals(listOf(1, 1, 2, 2), impl.doubleEach(listOf(1, 2)))
        assertEquals(emptyList(), impl.doubleEach(emptyList<String>()))
    }

    @Test
    fun `23_2 swap flips a pair`() {
        assertEquals("a" to 1, impl.swap(1 to "a"))
    }

    @Test
    fun `23_3 stack is last-in-first-out`() {
        val stack = impl.newStack<String>()
        assertNull(stack.pop())
        stack.push("a")
        stack.push("b")
        assertEquals(2, stack.size)
        assertEquals("b", stack.peek())
        assertEquals("b", stack.pop())
        assertEquals("a", stack.pop())
        assertNull(stack.peek())
        assertEquals(0, stack.size)
    }
}

class Ex23SolutionTest : Ex23Spec(Ex23Solution)

@Tag("exercise")
class Ex23ExerciseTest : Ex23Spec(Ex23Exercise)
