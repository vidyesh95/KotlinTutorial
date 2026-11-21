package exercises.ex27

import org.junit.jupiter.api.Tag
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import solutions.ex27.countOfType as solCountOfType
import solutions.ex27.filterByClass as solFilterByClass
import solutions.ex27.firstOfType as solFirstOfType
import solutions.ex27.isMapOf as solIsMapOf

/**
 * Reified functions can't be passed around as values, so each subclass
 * plugs in its own calls (still the same idea: one spec, two implementations).
 */
abstract class Ex27Spec {
    abstract fun firstString(items: List<Any?>): String?
    abstract fun firstInt(items: List<Any?>): Int?
    abstract fun countStrings(items: List<Any?>): Int
    abstract fun isStringToIntMap(value: Any?): Boolean
    abstract fun onlyStrings(items: List<Any?>): List<String>

    private val mixed = listOf(null, 1, "two", 3.0, "four")

    @Test
    fun `27_1 firstOfType`() {
        assertEquals("two", firstString(mixed))
        assertEquals(1, firstInt(mixed))
        assertNull(firstString(listOf(1, 2)))
    }

    @Test
    fun `27_2 countOfType`() {
        assertEquals(2, countStrings(mixed))
    }

    @Test
    fun `27_3 isMapOf`() {
        assertTrue(isStringToIntMap(mapOf("a" to 1)))
        assertTrue(isStringToIntMap(emptyMap<Any, Any>()))
        assertFalse(isStringToIntMap(mapOf("a" to "1")))
        assertFalse(isStringToIntMap(listOf(1)))
    }

    @Test
    fun `27_4 filterByClass`() {
        assertEquals(listOf("two", "four"), onlyStrings(mixed))
    }
}

class Ex27SolutionTest : Ex27Spec() {
    override fun firstString(items: List<Any?>) = solFirstOfType<String>(items)
    override fun firstInt(items: List<Any?>) = solFirstOfType<Int>(items)
    override fun countStrings(items: List<Any?>) = solCountOfType<String>(items)
    override fun isStringToIntMap(value: Any?) = solIsMapOf<String, Int>(value)
    override fun onlyStrings(items: List<Any?>) = solFilterByClass(items, String::class.java)
}

@Tag("exercise")
class Ex27ExerciseTest : Ex27Spec() {
    override fun firstString(items: List<Any?>) = firstOfType<String>(items)
    override fun firstInt(items: List<Any?>) = firstOfType<Int>(items)
    override fun countStrings(items: List<Any?>) = countOfType<String>(items)
    override fun isStringToIntMap(value: Any?) = isMapOf<String, Int>(value)
    override fun onlyStrings(items: List<Any?>) = filterByClass(items, String::class.java)
}
