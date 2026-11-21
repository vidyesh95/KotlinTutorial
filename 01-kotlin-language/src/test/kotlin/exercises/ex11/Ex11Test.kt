package exercises.ex11

import org.junit.jupiter.api.Tag
import solutions.ex11.Ex11Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex11Spec(private val impl: Ex11) {

    @Test
    fun `11_1 formatPrice with defaults`() {
        assertEquals("12.50 EUR", impl.formatPrice(12.5))
        assertEquals("3 USD", impl.formatPrice(3.0, "USD", 0))
        assertEquals("0.333 EUR", impl.formatPrice(1.0 / 3, decimals = 3))
    }

    @Test
    fun `11_2 maxOfAll`() {
        assertEquals(9, impl.maxOfAll(3, 9, -1))
        assertNull(impl.maxOfAll())
    }

    @Test
    fun `11_3 power`() {
        assertEquals(1024, impl.power(2, 10))
        assertEquals(1, impl.power(7, 0))
    }

    @Test
    fun `11_4 fibonacci`() {
        assertEquals(listOf(0L, 1, 1, 2, 3, 5, 8), (0..6).map(impl::fibonacci))
        assertEquals(2880067194370816120, impl.fibonacci(90))
    }
}

class Ex11SolutionTest : Ex11Spec(Ex11Solution)

@Tag("exercise")
class Ex11ExerciseTest : Ex11Spec(Ex11Exercise)
