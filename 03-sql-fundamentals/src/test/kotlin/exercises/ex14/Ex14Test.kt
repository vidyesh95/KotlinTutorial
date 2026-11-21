package exercises.ex14

import org.junit.jupiter.api.Tag
import solutions.ex14.Ex14Solution
import sqlkit.memoryDb
import sqlkit.rows
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith

abstract class Ex14Spec(private val impl: Ex14) {
    private val v1 = Migration(1, "create notes", "CREATE TABLE notes (id INTEGER PRIMARY KEY, text TEXT NOT NULL);")
    private val v2 = Migration(2, "seed notes", "INSERT INTO notes (text) VALUES ('a');\nINSERT INTO notes (text) VALUES ('b');")
    private val v3 = Migration(3, "add pinned", "ALTER TABLE notes ADD COLUMN pinned INTEGER NOT NULL DEFAULT 0;")

    @Test
    fun `14_1 parse file names`() {
        assertEquals(12 to "add user email", impl.parseFileName("V12__add_user_email.sql"))
        assertEquals(1 to "init", impl.parseFileName("V1__init.sql"))
        assertFailsWith<IllegalArgumentException> { impl.parseFileName("add_user.sql") }
        assertFailsWith<IllegalArgumentException> { impl.parseFileName("V1_init.sql") }
    }

    @Test
    fun `14_2 applies in order and is idempotent`() {
        memoryDb().use { db ->
            assertEquals(listOf(1, 2), impl.migrate(db, listOf(v2, v1)))
            assertEquals(emptyList(), impl.migrate(db, listOf(v1, v2)))
            assertEquals(listOf(3), impl.migrate(db, listOf(v1, v2, v3)))
            assertEquals(listOf(listOf("a", "0"), listOf("b", "0")), db.rows("SELECT text, pinned FROM notes ORDER BY id"))
            assertEquals(listOf(listOf("1", "create notes"), listOf("2", "seed notes"), listOf("3", "add pinned")),
                db.rows("SELECT version, description FROM schema_history ORDER BY version"))
        }
    }

    @Test
    fun `14_2 a failing migration is rolled back alone`() {
        memoryDb().use { db ->
            val broken = Migration(2, "half broken", "INSERT INTO notes (text) VALUES ('x');\nINSERT INTO nowhere VALUES (1);")
            assertFails { impl.migrate(db, listOf(v1, broken)) }
            assertEquals(listOf(listOf("1")), db.rows("SELECT version FROM schema_history"))
            assertEquals(listOf(listOf("0")), db.rows("SELECT count(*) FROM notes"), "the half-applied insert must be rolled back")
        }
    }

    @Test
    fun `14_3 current version`() {
        memoryDb().use { db ->
            assertEquals(0, impl.currentVersion(db))
            impl.migrate(db, listOf(v1, v2))
            assertEquals(2, impl.currentVersion(db))
        }
    }
}

class Ex14SolutionTest : Ex14Spec(Ex14Solution)

@Tag("exercise")
class Ex14ExerciseTest : Ex14Spec(Ex14Exercise)
