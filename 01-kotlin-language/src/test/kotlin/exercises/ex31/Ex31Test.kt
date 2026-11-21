package exercises.ex31

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Tag
import solutions.ex31.Ex31Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex31Spec(private val impl: Ex31) {

    @Test
    fun `31_1 countdown emits down to zero`() = runTest {
        assertEquals(listOf(2, 1, 0), impl.countdown(2).toList())
        assertEquals(listOf(0), impl.countdown(0).toList())
    }

    @Test
    fun `31_2 firstEvenSquares filters, maps and takes`() = runTest {
        assertEquals(listOf(4, 16), impl.firstEvenSquares(listOf(1, 2, 3, 4, 5, 6, 8).asFlow(), 2))
    }

    @Test
    fun `31_2 firstEvenSquares stops an infinite flow`() = runTest {
        val naturals = flow { var i = 1; while (true) emit(i++) }
        assertEquals(listOf(4, 16, 36), impl.firstEvenSquares(naturals, 3))
    }

    @Test
    fun `31_3 orNullAfter returns value or null`() = runTest {
        assertEquals("fast", impl.orNullAfter(1_000) { delay(10); "fast" })
        assertNull(impl.orNullAfter(1_000) { delay(5_000); "slow" })   // virtual time: instant
    }
}

class Ex31SolutionTest : Ex31Spec(Ex31Solution)

@Tag("exercise")
class Ex31ExerciseTest : Ex31Spec(Ex31Exercise)
