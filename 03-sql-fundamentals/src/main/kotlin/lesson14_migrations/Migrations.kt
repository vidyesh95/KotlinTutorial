/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 14 — Migrations: versioning your database schema
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : change a schema safely over time with numbered, append-only SQL
 *         files — by writing a ~60-line migration runner (a mini Flyway).
 *  Needs: lessons 01–02 (DDL), 11 (JDBC), 12 (transactions)
 *  Time : ~40 min
 *  Next : module 04 — Spring Boot (where Flyway runs these for you)
 *  Exercises: exercises/ex14/Ex14.kt
 */
package lesson14_migrations

import sqlkit.exec
import sqlkit.memoryDb
import sqlkit.printQuery
import sqlkit.resourceText
import sqlkit.rows
import java.sql.Connection
import java.util.zip.CRC32

fun main() {
    section1WhyMigrations()
    section2FirstRun()
    section3SecondRunDoesNothing()
    section4ANewMigrationLater()
    section5NeverEditAnAppliedMigration()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Why migrations?
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A LEGO castle booklet has numbered pages: 1, 2, 3… Everybody who follows
//    the pages in order ends up with the SAME castle. If you've already built
//    up to page 3, you just continue with page 4 — you don't start over. And
//    you never secretly change page 2 after people have built it: their
//    castles wouldn't match the booklet anymore.
//
// Your database schema is that castle. Your laptop, your colleague's laptop,
// the test server and production must all end up with the same tables. So:
//  • every change is a numbered SQL file: V1__create_books.sql, V2__add_isbn.sql…
//  • the database remembers which numbers it already has (a history table)
//  • on startup, apply only the missing ones, in order, each in a transaction
//  • applied files are NEVER edited — you add a new file instead
// Files for this lesson: src/main/resources/migrations/lesson14/

fun section1WhyMigrations() {
    println("§1")
    println("schema changes = numbered, append-only SQL files + a history table")
}

// ───────────────────────────────────────────────────────────────────────────
// The runner (~60 lines). Read it slowly — it's the heart of the lesson.
// ───────────────────────────────────────────────────────────────────────────

data class Migration(val version: Int, val description: String, val sql: String) {
    /** A fingerprint of the content. If the file changes, so does the checksum. */
    val checksum: Long = CRC32().apply { update(sql.toByteArray()) }.value
}

/** "V2__add_isbn.sql" → Migration(2, "add isbn", <file content>) */
fun migrationFromResource(folder: String, fileName: String): Migration {
    val match = Regex("""V(\d+)__(\w+)\.sql""").matchEntire(fileName)
        ?: throw IllegalArgumentException("Bad migration file name: $fileName (expected V<number>__<description>.sql)")
    val (version, description) = match.destructured
    return Migration(version.toInt(), description.replace('_', ' '), resourceText("/$folder/$fileName"))
}

inline fun <T> Connection.transaction(block: Connection.() -> T): T {   // from lesson 12
    val previous = autoCommit
    autoCommit = false
    try {
        return block().also { commit() }
    } catch (e: Throwable) {
        rollback()
        throw e
    } finally {
        autoCommit = previous
    }
}

class MigrationRunner(private val db: Connection, private val clock: () -> String) {

    /** Applies pending migrations in version order. Returns the versions applied by THIS run. */
    fun migrate(migrations: List<Migration>): List<Int> {
        db.exec(
            """
            CREATE TABLE IF NOT EXISTS schema_history (
                version     INTEGER PRIMARY KEY,
                description TEXT    NOT NULL,
                checksum    INTEGER NOT NULL,
                applied_at  TEXT    NOT NULL
            )
            """.trimIndent()
        )
        val applied: Map<Int, Long> = db.rows("SELECT version, checksum FROM schema_history")
            .associate { (version, checksum) -> version!!.toInt() to checksum!!.toLong() }

        val newlyApplied = mutableListOf<Int>()
        for (migration in migrations.sortedBy { it.version }) {
            val knownChecksum = applied[migration.version]
            if (knownChecksum != null) {
                check(knownChecksum == migration.checksum) {
                    "V${migration.version} was modified after it was applied (checksum mismatch). Add a new migration instead!"
                }
                continue                                   // already there → skip
            }
            db.transaction {                               // the change AND its history row: all or nothing
                exec(migration.sql)
                prepareStatement("INSERT INTO schema_history VALUES (?, ?, ?, ?)").use {
                    it.setInt(1, migration.version)
                    it.setString(2, migration.description)
                    it.setLong(3, migration.checksum)
                    it.setString(4, clock())
                    it.executeUpdate()
                }
            }
            newlyApplied += migration.version
        }
        return newlyApplied
    }
}

// Fixed timestamps so the output never changes — inject the clock (module 02!).
fun fakeClock(): () -> String {
    var minute = 0
    return { "2026-02-01T10:%02d:00Z".format(minute++) }
}

val lessonMigrations: List<Migration> =
    listOf("V1__create_books.sql", "V2__add_isbn.sql", "V3__backfill_isbn.sql")
        .map { migrationFromResource("migrations/lesson14", it) }

// ───────────────────────────────────────────────────────────────────────────
// §2. First run: an empty database gets V1, V2, V3
// ───────────────────────────────────────────────────────────────────────────

fun section2FirstRun() {
    println("§2")
    memoryDb().use { db ->
        val applied = MigrationRunner(db, fakeClock()).migrate(lessonMigrations)
        println("applied: $applied")
        db.printQuery("SELECT version, description, applied_at FROM schema_history ORDER BY version")
        db.printQuery("SELECT * FROM books ORDER BY id")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Second run: nothing to do (idempotent)
// ───────────────────────────────────────────────────────────────────────────
// "Idempotent" = running it again changes nothing. Apps run migrations at
// EVERY startup, so this is essential.

fun section3SecondRunDoesNothing() {
    println("§3")
    memoryDb().use { db ->
        val runner = MigrationRunner(db, fakeClock())
        println("first run:  ${runner.migrate(lessonMigrations)}")
        println("second run: ${runner.migrate(lessonMigrations)}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Weeks later: a new migration ships
// ───────────────────────────────────────────────────────────────────────────
// Production already has V1–V3. The new release adds V4. Only V4 runs there,
// while a brand-new developer laptop gets V1–V4 in one go. Same final schema.

fun section4ANewMigrationLater() {
    println("§4")
    val v4 = Migration(4, "add pages", "ALTER TABLE books ADD COLUMN pages INTEGER;")
    memoryDb().use { production ->
        val runner = MigrationRunner(production, fakeClock())
        runner.migrate(lessonMigrations)                       // last month's deploy
        println("production gets: ${runner.migrate(lessonMigrations + v4)}")
    }
    memoryDb().use { newLaptop ->
        println("new laptop gets: ${MigrationRunner(newLaptop, fakeClock()).migrate(lessonMigrations + v4)}")
        newLaptop.printQuery("SELECT name FROM pragma_table_info('books')")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Never edit an applied migration
// ───────────────────────────────────────────────────────────────────────────
// Someone "fixes" V2 in place. Databases that already ran the old V2 would
// silently differ from new ones. The checksum catches it.

fun section5NeverEditAnAppliedMigration() {
    println("§5")
    memoryDb().use { db ->
        val runner = MigrationRunner(db, fakeClock())
        runner.migrate(lessonMigrations)
        val edited = lessonMigrations.map {
            if (it.version == 2) it.copy(sql = "ALTER TABLE books ADD COLUMN isbn TEXT NOT NULL DEFAULT '';") else it
        }
        val error = runCatching { runner.migrate(edited) }.exceptionOrNull()
        println("refused: ${error?.message}")
    }
}

// Zero-downtime changes: expand → migrate → contract
//  While the new version deploys, OLD and NEW app versions run side by side
//  against ONE database. So never break the old code in a single step:
//   1. EXPAND   add the new thing, nullable/optional (V2: add isbn column)
//   2. MIGRATE  backfill data; deploy code that writes both / reads new (V3)
//   3. CONTRACT later, once no old code is left: drop the old column,
//               add NOT NULL, etc. (a future V5)
//  Renaming a column = add new + copy + switch code + drop old. Never a
//  plain rename while old code still runs.
//
// ⚠️ Gotcha: migrations run with your app's database user — on big tables an
// index build or table rewrite can lock it for minutes. Test on a copy of
// production-sized data.

// 🏗️ In the real world: this is Flyway (module 04). Same naming
// (V1__create_books.sql in src/main/resources/db/migration), same history table
// (flyway_schema_history), same checksums, runs automatically at startup.

// 🐘 Postgres note: Postgres DDL is transactional too (a failed migration
// leaves no half-created tables). `CREATE INDEX CONCURRENTLY` avoids locking
// writes but can't run inside a transaction — Flyway has a setting for that.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Schema changes are code: numbered SQL files, applied in order, once.
//  • A history table remembers what ran; reruns are no-ops (idempotent).
//  • Each migration + its history row run in one transaction.
//  • Checksums stop anyone editing applied migrations. Add a new one instead.
//  • Expand → migrate → contract for changes without downtime.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// schema changes = numbered, append-only SQL files + a history table
// §2
// applied: [1, 2, 3]
// version | description   | applied_at
// --------+---------------+---------------------
// 1       | create books  | 2026-02-01T10:00:00Z
// 2       | add isbn      | 2026-02-01T10:01:00Z
// 3       | backfill isbn | 2026-02-01T10:02:00Z
// (3 rows)
// id | title | isbn
// ---+-------+----------
// 1  | Dune  | unknown-1
// 2  | Emma  | unknown-2
// (2 rows)
// §3
// first run:  [1, 2, 3]
// second run: []
// §4
// production gets: [4]
// new laptop gets: [1, 2, 3, 4]
// name
// -----
// id
// title
// isbn
// pages
// (4 rows)
// §5
// refused: V2 was modified after it was applied (checksum mismatch). Add a new migration instead!
