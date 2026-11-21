package exercises.ex21

import org.junit.jupiter.api.Tag
import solutions.ex21.Ex21Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex21Spec(private val impl: Ex21) {

    @Test
    fun `21_1 loggingList logs adds and still works as a list`() {
        val log = mutableListOf<String>()
        val list = impl.loggingList(log)
        list.add("a")
        list.add("b")
        list.remove("a")
        assertEquals(listOf("b"), list)
        assertEquals(listOf("add:a", "add:b"), log)
    }

    @Test
    fun `21_2 memo computes once and only when read`() {
        var calls = 0
        val holder = impl.memo { calls++; "result" }
        assertEquals(0, calls)
        assertEquals("result", holder.value)
        assertEquals("result", holder.value)
        assertEquals(1, calls)
    }

    @Test
    fun `21_3 clamped keeps values in range`() {
        val h = impl.clamped(0, 10)
        assertEquals(0, h.value)
        h.value = 7
        assertEquals(7, h.value)
        h.value = 99
        assertEquals(10, h.value)
        h.value = -3
        assertEquals(0, h.value)
    }

    @Test
    fun `21_4 tracked records every change`() {
        val t = impl.tracked()
        assertEquals("", t.name)
        t.name = "Ada"
        t.name = "Grace"
        assertEquals("Grace", t.name)
        assertEquals(listOf("->Ada", "Ada->Grace"), t.changes)
    }
}

class Ex21SolutionTest : Ex21Spec(Ex21Solution)

@Tag("exercise")
class Ex21ExerciseTest : Ex21Spec(Ex21Exercise)
