package exercises.ex03

import org.junit.jupiter.api.Tag
import solutions.ex03.Ex03Solution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import kotlin.test.assertTrue

abstract class Ex03Spec(private val impl: Ex03) {
    @Test
    fun `03_1 nothing is built up front`() {
        val log = mutableListOf<String>()
        impl.createModule(ShopConfig("EUR"), log::add)
        assertTrue(log.isEmpty(), "module built objects too early: $log")
    }

    @Test
    fun `03_1 singletons are built once, dependencies first`() {
        val log = mutableListOf<String>()
        val module = impl.createModule(ShopConfig("EUR"), log::add)
        val service = module.service
        assertEquals(listOf("repository", "service"), log)
        assertSame(service, module.service)
        assertSame(module.repository, service.repository)
        assertEquals("EUR", service.currency)
        assertEquals(listOf("repository", "service"), log)
    }

    @Test
    fun `03_1 carts are transient`() {
        val log = mutableListOf<String>()
        val module = impl.createModule(ShopConfig("USD"), log::add)
        val first = module.newCart()
        val second = module.newCart()
        assertNotSame(first, second)
        assertSame(first.service, second.service)
        assertEquals(listOf("repository", "service", "cart", "cart"), log)
    }
}

class Ex03SolutionTest : Ex03Spec(Ex03Solution)

@Tag("exercise")
class Ex03ExerciseTest : Ex03Spec(Ex03Exercise)
