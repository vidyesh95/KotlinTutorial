package exercises.ex04

import org.junit.jupiter.api.Tag
import solutions.ex04.Ex04Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

abstract class Ex04Spec(private val impl: Ex04) {
    private val shop = mapOf(
        "Controller" to listOf("Service"),
        "Service" to listOf("Repo", "Notifier"),
        "Repo" to listOf("Database"),
        "Notifier" to listOf("Config"),
        "Database" to listOf("Config"),
        "Config" to emptyList(),
    )
    private val cyclic = mapOf("A" to listOf("B"), "B" to listOf("C"), "C" to listOf("A"), "D" to listOf("A"))

    @Test
    fun `04_1 no cycle in a healthy graph`() {
        assertNull(impl.findCycle(shop))
    }

    @Test
    fun `04_1 finds a real cycle`() {
        val cycle = assertNotNull(impl.findCycle(cyclic))
        assertEquals(cycle.first(), cycle.last(), "cycle must end where it starts: $cycle")
        assertTrue(cycle.size >= 2)
        cycle.zipWithNext().forEach { (from, to) ->
            assertTrue(to in cyclic.getValue(from), "$from does not depend on $to")
        }
    }

    @Test
    fun `04_2 creation order puts dependencies first`() {
        val order = impl.creationOrder(shop)
        assertEquals(shop.keys, order.toSet())
        assertEquals(shop.size, order.size)
        for ((node, dependencies) in shop) {
            dependencies.forEach { assertTrue(order.indexOf(it) < order.indexOf(node), "$it must come before $node") }
        }
    }

    @Test
    fun `04_2 creation order refuses cycles`() {
        assertFailsWith<IllegalStateException> { impl.creationOrder(cyclic) }
    }

    @Test
    fun `04_3 missing bindings`() {
        val graph = mapOf("Service" to listOf("Repo", "Mailer"), "Repo" to listOf("DataSource", "Mailer"))
        assertEquals(listOf("DataSource", "Mailer"), impl.missingBindings(graph))
        assertEquals(emptyList(), impl.missingBindings(shop))
    }
}

class Ex04SolutionTest : Ex04Spec(Ex04Solution)

@Tag("exercise")
class Ex04ExerciseTest : Ex04Spec(Ex04Exercise)
