package exercises.ex12

import org.junit.jupiter.api.Tag
import solutions.ex12.Ex12Solution
import sqlkit.exec
import sqlkit.memoryDb
import sqlkit.rows
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

abstract class Ex12Spec(private val impl: Ex12) {
    private fun withAccounts(block: (Connection) -> Unit) {
        memoryDb().use { db ->
            db.exec(
                """
                CREATE TABLE accounts (id INTEGER PRIMARY KEY, owner TEXT NOT NULL, balance_cents INTEGER NOT NULL);
                INSERT INTO accounts VALUES (1, 'ada', 10000), (2, 'bob', 5000), (3, 'cy', 0);
                """.trimIndent()
            )
            block(db)
        }
    }

    private fun Connection.balances() = rows("SELECT balance_cents FROM accounts ORDER BY id").map { it.single()!!.toInt() }

    @Test
    fun `12_1 commits on success and restores autoCommit`() {
        withAccounts { db ->
            val result = impl.inTransaction(db) { db.exec("UPDATE accounts SET balance_cents = 1 WHERE id = 3"); "done" }
            assertEquals("done", result)
            assertTrue(db.autoCommit)
            assertEquals(listOf(10000, 5000, 1), db.balances())
        }
    }

    @Test
    fun `12_1 rolls back and rethrows on failure`() {
        withAccounts { db ->
            assertFailsWith<IllegalStateException> {
                impl.inTransaction(db) {
                    db.exec("UPDATE accounts SET balance_cents = 0 WHERE id = 1")
                    error("boom")
                }
            }
            assertTrue(db.autoCommit)
            assertEquals(listOf(10000, 5000, 0), db.balances())
        }
    }

    @Test
    fun `12_2 transfer moves money`() {
        withAccounts { db ->
            impl.transfer(db, 1, 2, 2500)
            assertEquals(listOf(7500, 7500, 0), db.balances())
        }
    }

    @Test
    fun `12_2 transfer refuses and changes nothing`() {
        withAccounts { db ->
            assertFailsWith<IllegalArgumentException> { impl.transfer(db, 3, 1, 1) }     // too poor
            assertFailsWith<IllegalArgumentException> { impl.transfer(db, 1, 99, 100) }  // no such target
            assertFailsWith<IllegalArgumentException> { impl.transfer(db, 99, 1, 100) }  // no such source
            assertEquals(listOf(10000, 5000, 0), db.balances())
        }
    }

    @Test
    fun `12_3 transferAll is all or nothing`() {
        withAccounts { db ->
            impl.transferAll(db, listOf(Transfer(1, 3, 1000), Transfer(3, 2, 500)))
            assertEquals(listOf(9000, 5500, 500), db.balances())

            assertFailsWith<IllegalArgumentException> {
                impl.transferAll(db, listOf(Transfer(1, 2, 1000), Transfer(3, 1, 999_999)))
            }
            assertEquals(listOf(9000, 5500, 500), db.balances())
        }
    }
}

class Ex12SolutionTest : Ex12Spec(Ex12Solution)

@Tag("exercise")
class Ex12ExerciseTest : Ex12Spec(Ex12Exercise)
