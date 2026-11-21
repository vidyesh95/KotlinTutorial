package exercises.ex04

import org.junit.jupiter.api.Tag
import solutions.ex04.Ex04Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex04Spec(private val impl: Ex04) {

    @Test
    fun `04_1 initials`() {
        assertEquals("AL", impl.initials("ada  lovelace"))
        assertEquals("G", impl.initials("grace"))
        assertEquals("JRT", impl.initials(" john ronald tolkien "))
    }

    @Test
    fun `04_2 isPalindrome`() {
        assertTrue(impl.isPalindrome("Race car!"))
        assertTrue(impl.isPalindrome("A man, a plan, a canal: Panama"))
        assertFalse(impl.isPalindrome("Kotlin"))
    }

    @Test
    fun `04_3 orderNumber`() {
        assertEquals("ORD-00042", impl.orderNumber(42))
        assertEquals("ORD-12345", impl.orderNumber(12345))
    }

    @Test
    fun `04_4 formatPrice`() {
        assertEquals("\$19.99", impl.formatPrice(1999))
        assertEquals("\$0.05", impl.formatPrice(5))
        assertEquals("\$3.00", impl.formatPrice(300))
    }
}

class Ex04SolutionTest : Ex04Spec(Ex04Solution)

@Tag("exercise")
class Ex04ExerciseTest : Ex04Spec(Ex04Exercise)
