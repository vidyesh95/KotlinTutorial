package exercises.ex30

import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Tag
import solutions.ex30.Ex30Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex30Spec(private val impl: Ex30) {

    @Test
    fun `30_1 fetchAll runs concurrently and keeps order`() = runTest {
        var running = 0
        var maxRunning = 0
        val result = impl.fetchAll(listOf(3, 1, 2)) { id ->
            running++
            maxRunning = maxOf(maxRunning, running)
            delay(100L * id)                  // different durations, order must still match input
            running--
            "item-$id"
        }
        assertEquals(listOf("item-3", "item-1", "item-2"), result)
        assertEquals(3, maxRunning, "all fetches should be in flight at the same time")
    }

    @Test
    fun `30_2 sumInBackground adds up`() = runTest {
        assertEquals(5_000_050_000L, impl.sumInBackground((1..100_000).toList()))
    }

    @Test
    fun `30_3 retry tries again until success`() = runTest {
        var calls = 0
        val result = impl.retry(times = 3, delayMillis = 1_000) {
            calls++
            if (calls < 3) error("flaky") else "ok"
        }
        assertEquals("ok", result)
        assertEquals(3, calls)
    }

    @Test
    fun `30_3 retry gives up after the last attempt`() = runTest {
        var calls = 0
        assertFailsWith<IllegalStateException> {
            impl.retry(times = 2, delayMillis = 10) { calls++; error("always broken") }
        }
        assertEquals(2, calls)
    }
}

class Ex30SolutionTest : Ex30Spec(Ex30Solution)

@Tag("exercise")
class Ex30ExerciseTest : Ex30Spec(Ex30Exercise)
