package exercises.ex08

import org.junit.jupiter.api.Tag
import solutions.ex08.Ex08Solution
import sqlkit.bookstoreDb
import sqlkit.exec
import sqlkit.rows
import java.sql.Connection
import kotlin.test.Test
import kotlin.test.assertFails
import kotlin.test.assertTrue

abstract class Ex08Spec(private val impl: Ex08) {
    private fun Connection.plan(sql: String): String = rows("EXPLAIN QUERY PLAN $sql").joinToString(" | ") { it.last().orEmpty() }

    @Test
    fun `08_1 orders by customer use an index`() {
        bookstoreDb().use { db ->
            db.exec(impl.indexOrdersByCustomer)
            val plan = db.plan("SELECT * FROM orders WHERE customer_id = 1")
            assertTrue("SEARCH orders USING INDEX" in plan, "plan was: $plan")
        }
    }

    @Test
    fun `08_2 case-insensitive unique email`() {
        bookstoreDb().use { db ->
            db.exec(impl.uniqueEmailIgnoringCase)
            assertFails("ADA@example.com must clash with ada@example.com") {
                db.exec("INSERT INTO customers (email, name, created_at) VALUES ('ADA@example.com', 'Copycat', '2026-01-01')")
            }
            val plan = db.plan("SELECT id FROM customers WHERE lower(email) = 'ada@example.com'")
            assertTrue("USING" in plan && "INDEX" in plan && "SCAN" !in plan, "plan was: $plan")
        }
    }

    @Test
    fun `08_3 covering index for the category listing`() {
        bookstoreDb().use { db ->
            db.exec(impl.coveringIndexForCategoryListing)
            val plan = db.plan("SELECT title, price_cents FROM books WHERE category_id = 2")
            assertTrue("SEARCH books USING COVERING INDEX" in plan, "plan was: $plan")
        }
    }

    @Test
    fun `08_4 partial index for in-stock books`() {
        bookstoreDb().use { db ->
            db.exec(impl.partialIndexInStock)
            val definition = db.rows("SELECT sql FROM sqlite_schema WHERE type = 'index' AND tbl_name = 'books'")
                .joinToString { it.single().orEmpty() }
            assertTrue("WHERE" in definition.uppercase(), "index must be partial (… WHERE stock > 0): $definition")
            val plan = db.plan("SELECT id FROM books WHERE stock > 0 AND price_cents < 1000")
            assertTrue("INDEX" in plan && "SCAN books" != plan, "plan was: $plan")
        }
    }
}

class Ex08SolutionTest : Ex08Spec(Ex08Solution)

@Tag("exercise")
class Ex08ExerciseTest : Ex08Spec(Ex08Exercise)
