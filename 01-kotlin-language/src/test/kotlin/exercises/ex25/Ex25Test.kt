package exercises.ex25

import org.junit.jupiter.api.Tag
import solutions.ex25.Ex25Solution
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex25Spec(private val impl: Ex25) {
    @Test
    fun `25_1 and 25_3 take from a counting source`() {
        assertEquals(listOf(5, 6, 7), impl.take(impl.countingSource(5), 3))
    }

    @Test
    fun `25_3 Source of Int is usable as Source of Number (out)`() {
        val numbers: Source<Number> = impl.countingSource(1)   // compiles only because of `out T`
        assertEquals(listOf<Number>(1, 2), impl.take(numbers, 2))
    }

    @Test
    fun `25_2 and 25_4 drain into a collecting sink`() {
        val collected = mutableListOf<String>()
        impl.drain(listOf("a", "b"), impl.collectingSink(collected))
        assertEquals(listOf("a", "b"), collected)
    }

    @Test
    fun `25_4 Sink of Any is usable as Sink of Int (in)`() {
        val anything = mutableListOf<Any>("start")
        val intSink: Sink<Int> = impl.collectingSink<Any>(anything)  // compiles only because of `in T`
        impl.drain(listOf(1, 2), intSink)
        assertEquals(listOf<Any>("start", 1, 2), anything)
    }
}

class Ex25SolutionTest : Ex25Spec(Ex25Solution)

@Tag("exercise")
class Ex25ExerciseTest : Ex25Spec(Ex25Exercise)
