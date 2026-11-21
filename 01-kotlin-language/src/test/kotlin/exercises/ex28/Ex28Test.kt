package exercises.ex28

import org.junit.jupiter.api.Tag
import solutions.ex28.Ex28Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex28Spec(private val impl: Ex28) {

    @Test
    fun `28_1 formatPrice uses the currency in context`() {
        context(Currency("€")) {
            assertEquals("€12.34", impl.formatPrice(1234))
            assertEquals("€0.05", impl.formatPrice(5))
            assertEquals("€1.00", impl.formatPrice(100))
        }
    }

    @Test
    fun `28_2 formatTotal sums and formats`() {
        context(Currency("$")) {
            assertEquals("$4.00", impl.formatTotal(listOf(150, 250)))
            assertEquals("$0.00", impl.formatTotal(emptyList()))
        }
    }

    @Test
    fun `28_3 inEuro provides the euro currency`() {
        assertEquals("€", impl.inEuro { contextOf<Currency>().symbol })
        assertEquals("€0.99", impl.inEuro { impl.formatPrice(99) })
    }
}

class Ex28SolutionTest : Ex28Spec(Ex28Solution)

@Tag("exercise")
class Ex28ExerciseTest : Ex28Spec(Ex28Exercise)
