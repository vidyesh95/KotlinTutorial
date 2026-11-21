package exercises.ex19

import org.junit.jupiter.api.Tag
import solutions.ex19.Ex19Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertSame

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex19Spec(private val impl: Ex19) {

    @Test
    fun `19_1 generators count independently`() {
        val users = impl.idGenerator("usr")
        val orders = impl.idGenerator("ord")
        assertEquals("usr-1", users.next())
        assertEquals("usr-2", users.next())
        assertEquals("ord-1", orders.next())
        assertEquals("usr-3", users.next())
    }

    @Test
    fun `19_2 parseHex`() {
        assertEquals(Rgb(255, 136, 0), impl.parseHex("#FF8800"))
        assertEquals(Rgb(0, 122, 255), impl.parseHex("#007aff"))
        assertNull(impl.parseHex("FF8800"))
        assertNull(impl.parseHex("#FF88"))
        assertNull(impl.parseHex("#GG8800"))
    }

    @Test
    fun `19_3 defaultTheme is a singleton`() {
        val theme = impl.defaultTheme()
        assertEquals(Rgb(0, 122, 255), theme.primaryColor)
        assertSame(theme, impl.defaultTheme())
    }
}

class Ex19SolutionTest : Ex19Spec(Ex19Solution)

@Tag("exercise")
class Ex19ExerciseTest : Ex19Spec(Ex19Exercise)
