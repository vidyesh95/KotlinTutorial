package exercises.ex06

import org.junit.jupiter.api.Tag
import solutions.ex06.Ex06Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

abstract class Ex06Spec(private val impl: Ex06) {
    @Test
    fun `06_1 spring wires the greeting service`() {
        assertEquals("Hello, Ada!", impl.welcomeWithSpring("Ada"))
    }

    @Test
    fun `06_2 primary bean wins`() {
        assertEquals("Hola, Ada!", impl.welcomeWithPrimary("Ada"))
    }

    @Test
    fun `06_3 prototype scope`() {
        assertTrue(impl.prototypeGivesNewInstances())
    }
}

class Ex06SolutionTest : Ex06Spec(Ex06Solution)

@Tag("exercise")
class Ex06ExerciseTest : Ex06Spec(Ex06Exercise)
