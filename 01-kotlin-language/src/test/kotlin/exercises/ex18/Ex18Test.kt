package exercises.ex18

import org.junit.jupiter.api.Tag
import solutions.ex18.Ex18Solution
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex18Spec(private val impl: Ex18) {

    @Test
    fun `18_1 area of every shape`() {
        assertEquals(PI * 4, impl.area(Shape.Circle(2.0)), 1e-9)
        assertEquals(6.0, impl.area(Shape.Rectangle(2.0, 3.0)), 1e-9)
        assertEquals(5.0, impl.area(Shape.Triangle(2.0, 5.0)), 1e-9)
    }

    @Test
    fun `18_2 applyAll runs commands in order`() {
        val commands = listOf(
            Command.Deposit(50),
            Command.Withdraw(500),   // skipped: not enough money
            Command.Withdraw(30),
            Command.Reset,
            Command.Deposit(7),
        )
        assertEquals(7, impl.applyAll(100, commands))
        assertEquals(120, impl.applyAll(100, listOf(Command.Withdraw(30), Command.Deposit(50))))
        assertEquals(10, impl.applyAll(10, emptyList()))
    }

    @Test
    fun `18_3 parseAge returns typed results`() {
        assertEquals(ParseResult.Ok(30), impl.parseAge("30"))
        assertEquals(ParseResult.Ok(0), impl.parseAge(" 0 "))
        assertEquals(ParseResult.Error("not a number"), impl.parseAge("thirty"))
        assertEquals(ParseResult.Error("out of range"), impl.parseAge("200"))
        assertEquals(ParseResult.Error("out of range"), impl.parseAge("-1"))
    }

    @Test
    fun `18_4 render each case`() {
        assertEquals("age=30", impl.render(ParseResult.Ok(30)))
        assertEquals("error: bad", impl.render(ParseResult.Error("bad")))
    }
}

class Ex18SolutionTest : Ex18Spec(Ex18Solution)

@Tag("exercise")
class Ex18ExerciseTest : Ex18Spec(Ex18Exercise)
