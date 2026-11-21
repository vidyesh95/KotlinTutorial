package exercises.ex14

import org.junit.jupiter.api.Tag
import solutions.ex14.Ex14Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex14Spec(private val impl: Ex14) {

    @Test
    fun `14_1 jsonPost`() {
        assertEquals(
            HttpRequest("https://x.dev", "POST", mutableMapOf("Accept" to "application/json")),
            impl.jsonPost("https://x.dev"),
        )
    }

    @Test
    fun `14_2 domainOf`() {
        assertEquals("example.com", impl.domainOf("ada@example.com"))
        assertEquals("unknown", impl.domainOf(null))
        assertEquals("unknown", impl.domainOf("no-at-sign"))
    }

    @Test
    fun `14_3 parsePositive`() {
        assertEquals(42, impl.parsePositive("42"))
        assertNull(impl.parsePositive("0"))
        assertNull(impl.parsePositive("-3"))
        assertNull(impl.parsePositive("abc"))
    }

    @Test
    fun `14_4 normalize logs with also`() {
        val log = mutableListOf<String>()
        assertEquals(listOf("ada", "linus"), impl.normalize(listOf("  Ada ", " ", "LINUS"), log))
        assertEquals(listOf("normalized 2 names"), log)
    }
}

class Ex14SolutionTest : Ex14Spec(Ex14Solution)

@Tag("exercise")
class Ex14ExerciseTest : Ex14Spec(Ex14Exercise)
