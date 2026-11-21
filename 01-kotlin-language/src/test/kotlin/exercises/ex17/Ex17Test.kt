package exercises.ex17

import org.junit.jupiter.api.Tag
import solutions.ex17.Ex17Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex17Spec(private val impl: Ex17) {

    @Test
    fun `17_1 discount returns a new product`() {
        val coffee = Product("C1", "Coffee", 1000)
        assertEquals(Product("C1", "Coffee", 850), impl.applyDiscount(coffee, 15))
        assertEquals(1000, coffee.priceCents)
        assertEquals(329, impl.applyDiscount(Product("X", "x", 999), 67).priceCents)
    }

    @Test
    fun `17_2 uniqueProducts compares by content`() {
        val a = Product("A", "Apple", 50)
        val b = Product("B", "Banana", 20)
        assertEquals(listOf(a, b), impl.uniqueProducts(listOf(a, b, Product("A", "Apple", 50), b)))
        val cheaperA = Product("A", "Apple", 40)
        assertEquals(listOf(a, cheaperA), impl.uniqueProducts(listOf(a, cheaperA)))
    }

    @Test
    fun `17_3 parseCupSize ignores case and spaces`() {
        assertEquals(CupSize.LARGE, impl.parseCupSize(" Large "))
        assertEquals(CupSize.SMALL, impl.parseCupSize("small"))
        assertNull(impl.parseCupSize("venti"))
    }

    @Test
    fun `17_4 format money`() {
        assertEquals("$12.05", impl.format(Cents(1205)))
        assertEquals("$0.07", impl.format(Cents(7)))
        assertEquals("$1000.00", impl.format(Cents(100_000)))
    }
}

class Ex17SolutionTest : Ex17Spec(Ex17Solution)

@Tag("exercise")
class Ex17ExerciseTest : Ex17Spec(Ex17Exercise)
