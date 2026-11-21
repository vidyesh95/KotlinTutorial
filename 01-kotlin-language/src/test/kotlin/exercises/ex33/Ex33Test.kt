package exercises.ex33

import org.junit.jupiter.api.Tag
import solutions.ex33.Ex33Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex33Spec(private val impl: Ex33) {

    @Test
    fun `33_1 isValidUsername`() {
        assertTrue(impl.isValidUsername("ada_99"))
        assertTrue(impl.isValidUsername("abc"))
        assertTrue(impl.isValidUsername("a234567890123456"))   // 16 chars
        assertFalse(impl.isValidUsername("a2345678901234567"))  // 17 chars
        assertFalse(impl.isValidUsername("9lives"))
        assertFalse(impl.isValidUsername("ab"))
        assertFalse(impl.isValidUsername("Ada"))
        assertFalse(impl.isValidUsername("ada!"))
    }

    @Test
    fun `33_2 hashtags`() {
        assertEquals(listOf("love", "kotlin2"), impl.hashtags("I #love #kotlin2!"))
        assertEquals(emptyList(), impl.hashtags("no tags here"))
    }

    @Test
    fun `33_3 parseLogLine`() {
        assertEquals(LogEntry("12:00:01", "WARN", "db", "Slow query"), impl.parseLogLine("12:00:01 WARN [db] Slow query"))
        assertNull(impl.parseLogLine("12:00:01 DEBUG [db] Slow query"))
        assertNull(impl.parseLogLine("garbage"))
    }
}

class Ex33SolutionTest : Ex33Spec(Ex33Solution)

@Tag("exercise")
class Ex33ExerciseTest : Ex33Spec(Ex33Exercise)
