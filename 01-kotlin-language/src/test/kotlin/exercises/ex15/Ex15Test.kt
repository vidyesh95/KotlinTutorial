package exercises.ex15

import org.junit.jupiter.api.Tag
import solutions.ex15.Ex15Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex15Spec(private val impl: Ex15) {

    @Test
    fun `15_1 account protects its balance`() {
        val account = impl.openAccount("Ada")
        assertEquals("Ada", account.owner)
        assertEquals(0, account.balance)
        account.deposit(100)
        assertTrue(account.withdraw(30))
        assertFalse(account.withdraw(500))
        assertEquals(70, account.balance)
        assertFailsWith<IllegalArgumentException> { account.deposit(0) }
        assertFailsWith<IllegalArgumentException> { impl.openAccount("  ") }
    }

    @Test
    fun `15_2 temperature converts both ways`() {
        val t = impl.temperature(100.0)
        assertEquals(212.0, t.fahrenheit, 0.0001)
        t.fahrenheit = 32.0
        assertEquals(0.0, t.celsius, 0.0001)
        t.celsius = 37.0
        assertEquals(98.6, t.fahrenheit, 0.0001)
    }

    @Test
    fun `15_3 cleanUsername trims lowercases and defaults`() {
        assertEquals("ada", impl.cleanUsername("  ADA "))
        assertEquals("anonymous", impl.cleanUsername("   "))
    }
}

class Ex15SolutionTest : Ex15Spec(Ex15Solution)

@Tag("exercise")
class Ex15ExerciseTest : Ex15Spec(Ex15Exercise)
