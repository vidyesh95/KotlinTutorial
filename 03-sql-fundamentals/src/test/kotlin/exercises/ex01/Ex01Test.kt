package exercises.ex01

import org.junit.jupiter.api.Tag
import solutions.ex01.Ex01Solution
import sqlkit.bookstoreDb
import sqlkit.exec
import sqlkit.memoryDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

abstract class Ex01Spec(private val impl: Ex01) {
    @Test
    fun `01_1 products table has the right columns and rules`() {
        memoryDb().use { db ->
            db.exec(impl.createProducts)
            db.exec("INSERT INTO products (name, price_cents) VALUES ('pen', 150)")
            assertEquals(listOf(listOf("1", "pen", "150", "1")), db.rows("SELECT id, name, price_cents, in_stock FROM products"))
            assertFails("name must be required") { db.exec("INSERT INTO products (price_cents) VALUES (1)") }
            assertFails("table must be STRICT") { db.exec("INSERT INTO products (name, price_cents) VALUES ('x', 'cheap')") }
        }
    }

    @Test
    fun `01_2 sku column is added and nullable`() {
        memoryDb().use { db ->
            db.exec(impl.createProducts)
            db.exec(impl.addSkuColumn)
            val columns = db.rows("SELECT name, type, \"notnull\" FROM pragma_table_info('products') WHERE name = 'sku'")
            assertEquals(listOf(listOf("sku", "TEXT", "0")), columns)
        }
    }

    @Test
    fun `01_3 unknown years`() {
        bookstoreDb().use { db ->
            val rows = db.rows(impl.booksWithYear)
            assertEquals(14, rows.size)
            assertEquals(listOf("Dune", "1965"), rows.first())
            assertEquals(listOf("Collected Notes", "unknown"), rows.last())
        }
    }
}

class Ex01SolutionTest : Ex01Spec(Ex01Solution)

@Tag("exercise")
class Ex01ExerciseTest : Ex01Spec(Ex01Exercise)
