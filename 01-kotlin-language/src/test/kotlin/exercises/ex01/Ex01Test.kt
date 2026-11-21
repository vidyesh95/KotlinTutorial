package exercises.ex01

import org.junit.jupiter.api.Tag
import solutions.ex01.Ex01Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex01Spec(private val impl: Ex01) {

    @Test
    fun `01_1 greeting`() {
        assertEquals("Hello, Ada!", impl.greeting("Ada"))
        assertEquals("Hello, World!", impl.greeting("World"))
    }

    @Test
    fun `01_2 banner`() {
        assertEquals("******\n* Hi *\n******", impl.banner("Hi"))
        assertEquals("**********\n* Kotlin *\n**********", impl.banner("Kotlin"))
    }
}

class Ex01SolutionTest : Ex01Spec(Ex01Solution)

@Tag("exercise")
class Ex01ExerciseTest : Ex01Spec(Ex01Exercise)
