package exercises.ex13

import org.junit.jupiter.api.Tag
import solutions.ex13.Ex13Solution
import sqlkit.bookstoreDb
import sqlkit.exec
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/** Wraps a Connection and counts every statement created through it. */
class StatementCounter(private val target: Connection) {
    var count = 0
        private set

    val connection: Connection = Proxy.newProxyInstance(
        Connection::class.java.classLoader,
        arrayOf(Connection::class.java),
    ) { _, method, args ->
        if (method.name in setOf("prepareStatement", "createStatement", "prepareCall")) count++
        try {
            method.invoke(target, *(args ?: emptyArray()))
        } catch (e: InvocationTargetException) {
            throw e.targetException
        }
    } as Connection
}

abstract class Ex13Spec(private val impl: Ex13) {
    @Test
    fun `13_1 placeholders`() {
        assertEquals("?", impl.placeholders(1))
        assertEquals("?, ?, ?", impl.placeholders(3))
        assertFailsWith<IllegalArgumentException> { impl.placeholders(0) }
    }

    @Test
    fun `13_2 orders with items in at most two queries`() {
        bookstoreDb().use { db ->
            db.exec("INSERT INTO orders (id, customer_id, status, created_at) VALUES (11, 6, 'pending', '2025-11-01T00:00:00Z')")
            val counter = StatementCounter(db)
            val orders = impl.loadOrdersWithItems(counter.connection)
            assertTrue(counter.count <= 2, "used ${counter.count} statements — that's the N+1 problem!")
            assertEquals((1L..11L).toList(), orders.map { it.id })
            assertEquals(
                Order(3, "paid", listOf(OrderItem("Dune", 1), OrderItem("Foundation", 1), OrderItem("I, Robot", 1))),
                orders[2],
            )
            assertEquals(Order(11, "pending", emptyList()), orders.last())
        }
    }

    @Test
    fun `13_3 titles by ids in one query`() {
        bookstoreDb().use { db ->
            val counter = StatementCounter(db)
            assertEquals(mapOf(1L to "Dune", 8L to "The Hobbit"), impl.titlesByIds(counter.connection, listOf(8, 1, 999)))
            assertEquals(1, counter.count)
            assertEquals(emptyMap(), impl.titlesByIds(counter.connection, emptyList()))
            assertEquals(1, counter.count, "empty input must not query")
        }
    }
}

class Ex13SolutionTest : Ex13Spec(Ex13Solution)

@Tag("exercise")
class Ex13ExerciseTest : Ex13Spec(Ex13Exercise)
