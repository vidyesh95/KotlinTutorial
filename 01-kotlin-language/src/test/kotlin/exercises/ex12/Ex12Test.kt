package exercises.ex12

import org.junit.jupiter.api.Tag
import solutions.ex12.Ex12Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex12Spec(private val impl: Ex12) {

    @Test
    fun `12_1 applyTwice`() {
        assertEquals(7, impl.applyTwice({ it + 3 }, 1))
        assertEquals(16, impl.applyTwice({ it * it }, 2))
    }

    @Test
    fun `12_2 makeAdder`() {
        val addFive = impl.makeAdder(5)
        assertEquals(15, addFive(10))
        assertEquals(listOf(6, 7), listOf(1, 2).map(addFive))
    }

    @Test
    fun `12_3 counters are independent closures`() {
        val a = impl.counter()
        val b = impl.counter()
        assertEquals(listOf(1, 2, 3), listOf(a(), a(), a()))
        assertEquals(1, b())
    }

    @Test
    fun `12_4 percentOff`() {
        assertEquals(40.0, impl.percentOff(20).apply(50.0), 1e-9)
        assertEquals(50.0, impl.percentOff(0).apply(50.0), 1e-9)
    }
}

class Ex12SolutionTest : Ex12Spec(Ex12Solution)

@Tag("exercise")
class Ex12ExerciseTest : Ex12Spec(Ex12Exercise)
