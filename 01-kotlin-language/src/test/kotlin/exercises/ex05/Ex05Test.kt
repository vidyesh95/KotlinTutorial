package exercises.ex05

import org.junit.jupiter.api.Tag
import solutions.ex05.Ex05Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex05Spec(private val impl: Ex05) {

    @Test
    fun `05_1 parseNumbers`() {
        assertEquals(listOf(3, 4, 5), impl.parseNumbers("3 4 5"))
        assertEquals(listOf(1, 2), impl.parseNumbers("  1   2 "))
        assertEquals(listOf(-7), impl.parseNumbers("-7"))
        assertEquals(emptyList(), impl.parseNumbers(""))
    }

    @Test
    fun `05_2 parseAge`() {
        assertEquals(36, impl.parseAge(" 36 "))
        assertEquals(0, impl.parseAge("0"))
        assertEquals(150, impl.parseAge("150"))
        assertNull(impl.parseAge("abc"))
        assertNull(impl.parseAge("-1"))
        assertNull(impl.parseAge("151"))
        assertNull(impl.parseAge("4.5"))
    }

    @Test
    fun `05_3 parseKeyValue`() {
        assertEquals("port" to "8080", impl.parseKeyValue(" port = 8080 "))
        assertEquals("url" to "a=b", impl.parseKeyValue("url=a=b"))
        assertEquals("empty" to "", impl.parseKeyValue("empty="))
        assertNull(impl.parseKeyValue("no separator"))
        assertNull(impl.parseKeyValue(" =value"))
    }
}

class Ex05SolutionTest : Ex05Spec(Ex05Solution)

@Tag("exercise")
class Ex05ExerciseTest : Ex05Spec(Ex05Exercise)
