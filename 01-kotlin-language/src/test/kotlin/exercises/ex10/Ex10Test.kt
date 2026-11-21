package exercises.ex10

import org.junit.jupiter.api.Tag
import solutions.ex10.Ex10Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertIs
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex10Spec(private val impl: Ex10) {

    @Test
    fun `10_1 safeDivide`() {
        assertEquals(3, impl.safeDivide(7, 2))
        assertNull(impl.safeDivide(1, 0))
    }

    @Test
    fun `10_2 parseAge`() {
        assertEquals(42, impl.parseAge("42"))
        assertEquals(0, impl.parseAge("0"))
        assertFailsWith<IllegalArgumentException> { impl.parseAge("forty") }
        assertFailsWith<IllegalArgumentException> { impl.parseAge("151") }
        assertFailsWith<IllegalArgumentException> { impl.parseAge("-1") }
    }

    @Test
    fun `10_3 parseAll`() {
        assertEquals(listOf(1, 2, 3), impl.parseAll(listOf("1", "2", "3")).getOrThrow())
        val failure = impl.parseAll(listOf("1", "two", "x"))
        assertIs<NumberFormatException>(failure.exceptionOrNull())
        assertEquals("For input string: \"two\"", failure.exceptionOrNull()?.message)
    }

    @Test
    fun `10_4 withdraw`() {
        assertEquals(70, impl.withdraw(100, 30))
        assertFailsWith<IllegalArgumentException> { impl.withdraw(100, 0) }
        val e = assertFailsWith<InsufficientFundsException> { impl.withdraw(100, 130) }
        assertEquals(30, e.missing)
    }
}

class Ex10SolutionTest : Ex10Spec(Ex10Solution)

@Tag("exercise")
class Ex10ExerciseTest : Ex10Spec(Ex10Exercise)
