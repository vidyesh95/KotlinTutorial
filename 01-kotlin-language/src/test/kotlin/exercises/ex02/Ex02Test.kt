package exercises.ex02

import org.junit.jupiter.api.Tag
import solutions.ex02.Ex02Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex02Spec(private val impl: Ex02) {

    @Test
    fun `02_1 secondsToClock`() {
        assertEquals(Triple(1, 2, 5), impl.secondsToClock(3725))
        assertEquals(Triple(0, 0, 59), impl.secondsToClock(59))
        assertEquals(Triple(24, 0, 0), impl.secondsToClock(86_400))
    }

    @Test
    fun `02_2 celsiusToFahrenheit`() {
        assertEquals(212.0, impl.celsiusToFahrenheit(100.0))
        assertEquals(32.0, impl.celsiusToFahrenheit(0.0))
        assertEquals(-40.0, impl.celsiusToFahrenheit(-40.0))
    }

    @Test
    fun `02_3 scoreAfter`() {
        assertEquals(27, impl.scoreAfter(5))
        assertEquals(17, impl.scoreAfter(0))
    }
}

class Ex02SolutionTest : Ex02Spec(Ex02Solution)

@Tag("exercise")
class Ex02ExerciseTest : Ex02Spec(Ex02Exercise)
