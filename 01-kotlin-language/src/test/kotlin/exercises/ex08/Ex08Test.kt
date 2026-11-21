package exercises.ex08

import org.junit.jupiter.api.Tag
import solutions.ex08.Ex08Solution
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex08Spec(private val impl: Ex08) {

    @Test
    fun `08_1 fizzBuzz`() {
        assertEquals(listOf("1", "2", "Fizz", "4", "Buzz", "Fizz", "FizzBuzz"), listOf(1, 2, 3, 4, 5, 6, 15).map(impl::fizzBuzz))
    }

    @Test
    fun `08_2 bmiCategory`() {
        assertEquals(listOf("underweight", "normal", "overweight", "obese"), listOf(17.0, 18.5, 29.9, 30.0).map(impl::bmiCategory))
    }

    @Test
    fun `08_3 statusText`() {
        assertEquals(
            listOf("OK", "Created", "No Content", "Not Found", "Redirect", "Client Error", "Server Error", "Unknown"),
            listOf(200, 201, 204, 404, 301, 418, 503, 999).map(impl::statusText),
        )
    }

    @Test
    fun `08_4 area`() {
        assertEquals(PI * 4, impl.area(Shape.Circle(2.0)), 1e-9)
        assertEquals(6.0, impl.area(Shape.Rectangle(2.0, 3.0)), 1e-9)
        assertEquals(9.0, impl.area(Shape.Square(3.0)), 1e-9)
    }
}

class Ex08SolutionTest : Ex08Spec(Ex08Solution)

@Tag("exercise")
class Ex08ExerciseTest : Ex08Spec(Ex08Exercise)
