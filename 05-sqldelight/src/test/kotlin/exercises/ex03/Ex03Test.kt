package exercises.ex03

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import bookstore.db.inMemoryBookstore
import org.junit.jupiter.api.Tag
import solutions.ex03.Ex03Solution
import kotlin.test.Test
import kotlin.test.assertEquals

abstract class Ex03Spec(private val impl: Ex03) {
    @Test
    fun `03_1 schema version`() {
        assertEquals(4L, impl.schemaVersion())   // SQLDelight's version = number of migrations + 1
    }

    @Test
    fun `03_2 table names`() {
        val (driver, _) = inMemoryBookstore()
        driver.use {
            assertEquals(listOf("authors", "books", "order_items", "orders", "reviews"), impl.tableNames(it))
        }
    }

    @Test
    fun `03_3 migration runner applies only pending versions, in order`() {
        JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
            val migrations = mutableMapOf(
                2 to "CREATE TABLE b (id INTEGER PRIMARY KEY, a_id INTEGER REFERENCES a (id))",
                1 to "CREATE TABLE a (id INTEGER PRIMARY KEY)",
            )
            assertEquals(listOf(1, 2), impl.migrate(driver, migrations))
            assertEquals(emptyList(), impl.migrate(driver, migrations))
            migrations[3] = "CREATE TABLE c (id INTEGER PRIMARY KEY)"
            assertEquals(listOf(3), impl.migrate(driver, migrations))
            assertEquals(listOf("a", "b", "c", "schema_history"), Ex03Solution.tableNames(driver))
        }
    }
}

class Ex03SolutionTest : Ex03Spec(Ex03Solution)

@Tag("exercise")
class Ex03ExerciseTest : Ex03Spec(Ex03Exercise)
