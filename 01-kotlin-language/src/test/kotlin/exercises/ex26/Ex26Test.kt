package exercises.ex26

import org.junit.jupiter.api.Tag
import solutions.ex26.Ex26Solution
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex26Spec(private val impl: Ex26) {
    @Test
    fun `26_1 copyAll into a wider list`() {
        val numbers = mutableListOf<Number>(0.5)
        impl.copyAll(listOf(1, 2), numbers)
        assertEquals(listOf<Number>(0.5, 1, 2), numbers)
    }

    @Test
    fun `26_2 moveAll empties the source`() {
        val ints = mutableListOf(1, 2)
        val anything = mutableListOf<Any>("x")
        impl.moveAll(ints, anything)
        assertEquals(listOf<Any>("x", 1, 2), anything)
        assertEquals(emptyList(), ints)
    }

    @Test
    fun `26_3 kind`() {
        assertEquals("null", impl.kind(null))
        assertEquals("list(2)", impl.kind(listOf(1, 2)))
        assertEquals("set(1)", impl.kind(setOf("a")))
        assertEquals("map(0)", impl.kind(emptyMap<String, Int>()))
        assertEquals("other", impl.kind(42))
    }

    @Test
    fun `26_4 countNonNull`() {
        assertEquals(2, impl.countNonNull(listOf("a", null, "b")))
        assertEquals(0, impl.countNonNull(setOf(null)))
    }
}

class Ex26SolutionTest : Ex26Spec(Ex26Solution)

@Tag("exercise")
class Ex26ExerciseTest : Ex26Spec(Ex26Exercise)
