package exercises.ex06

import org.junit.jupiter.api.Tag
import solutions.ex06.Ex06Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex06Spec(private val impl: Ex06) {
    private val ada = User(1, Profile("Ada", "ada@example.com"))
    private val noEmail = User(2, Profile("Bob", null))
    private val badEmail = User(3, Profile("Cy", "not-an-email"))
    private val noProfile = User(4, null)

    @Test
    fun `06_1 emailDomain`() {
        assertEquals("example.com", impl.emailDomain(ada))
        assertNull(impl.emailDomain(noEmail))
        assertNull(impl.emailDomain(badEmail))
        assertNull(impl.emailDomain(noProfile))
        assertNull(impl.emailDomain(null))
    }

    @Test
    fun `06_2 displayName`() {
        assertEquals("Ada", impl.displayName(ada))
        assertEquals("Guest", impl.displayName(noProfile))
        assertEquals("Guest", impl.displayName(null))
    }

    @Test
    fun `06_3 sumOfValid`() {
        assertEquals(42, impl.sumOfValid(listOf("1", null, "x", "41")))
        assertEquals(0, impl.sumOfValid(listOf(null, null)))
        assertEquals(0, impl.sumOfValid(emptyList()))
    }

    @Test
    fun `06_4 firstLongWord`() {
        assertEquals("kotlin", impl.firstLongWord(listOf("hi", "kotlin", "spring"), 3))
        assertNull(impl.firstLongWord(listOf("a", "bb"), 3))
        assertNull(impl.firstLongWord(null, 3))
    }
}

class Ex06SolutionTest : Ex06Spec(Ex06Solution)

@Tag("exercise")
class Ex06ExerciseTest : Ex06Spec(Ex06Exercise)
