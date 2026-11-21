package exercises.ex29

import org.junit.jupiter.api.Tag
import solutions.ex29.Ex29Solution
import kotlin.test.Test
import kotlin.test.assertEquals

/** The same checks run against YOUR code and against the reference solution. */
abstract class Ex29Spec(private val impl: Ex29) {

    @Test
    fun `29_1 csvLine joins cells with commas`() {
        assertEquals("Dune,1965,", impl.csvLine { cell("Dune"); cell(1965); cell(null) })
        assertEquals("", impl.csvLine { })
    }

    @Test
    fun `29_2 applyIf runs the block only when the condition holds`() {
        assertEquals(listOf(1, 2), impl.applyIf(mutableListOf(1), true) { add(2) })
        assertEquals(listOf(1), impl.applyIf(mutableListOf(1), false) { add(2) })
    }

    @Test
    fun `29_3 menu formats every item`() {
        assertEquals(
            listOf("Tea: 2.50", "Cake: 12.05"),
            impl.menu { item("Tea", 250); item("Cake", 1205) },
        )
    }
}

class Ex29SolutionTest : Ex29Spec(Ex29Solution)

@Tag("exercise")
class Ex29ExerciseTest : Ex29Spec(Ex29Exercise)
