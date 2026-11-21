package exercises.ex16

import org.junit.jupiter.api.Tag
import solutions.ex16.Ex16Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex16Spec(private val impl: Ex16) {

    @Test
    fun `16_1 salaried pays a twelfth per month`() {
        val e = impl.salaried("Ada", 60_000)
        assertEquals(5_000, e.monthlyPay())
        assertEquals("Ada: 5000", e.payslip())
    }

    @Test
    fun `16_2 hourly pays overtime at one and a half`() {
        assertEquals(1_000, impl.hourly("Bob", 100, 10).monthlyPay())
        assertEquals(1_750, impl.hourly("Bob", 170, 10).monthlyPay())
    }

    @Test
    fun `16_3 payroll sums every kind of employee`() {
        val staff = listOf(impl.salaried("A", 12_000), impl.hourly("B", 10, 5))
        assertEquals(1_050, impl.payroll(staff))
        assertEquals(0, impl.payroll(emptyList()))
    }

    @Test
    fun `16_4 robot greets and walks`() {
        val robot = impl.robot("R2")
        assertEquals("Hello, I am R2", robot.greet())
        val walker = assertIs<Walker>(robot)
        assertEquals("R2 walked 3 steps", walker.walk(3))
    }
}

class Ex16SolutionTest : Ex16Spec(Ex16Solution)

@Tag("exercise")
class Ex16ExerciseTest : Ex16Spec(Ex16Exercise)
