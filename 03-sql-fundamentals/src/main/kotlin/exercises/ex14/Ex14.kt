package exercises.ex14

import java.sql.Connection

/*
 * Exercise 14 — Migrations                      (lesson: lesson14_migrations)
 * Replace every TODO(...) then run: ./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex14*'
 */

data class Migration(val version: Int, val description: String, val sql: String)

interface Ex14 {
    /**
     * 14.1 — Parse a Flyway-style file name: "V12__add_user_email.sql" → (12, "add user email").
     * Anything else → IllegalArgumentException.
     */
    fun parseFileName(fileName: String): Pair<Int, String>

    /**
     * 14.2 — Apply pending [migrations] in version order (the list may be unsorted). Keep a table
     * `schema_history(version INTEGER PRIMARY KEY, description TEXT NOT NULL)` (create it if missing).
     * Each migration + its history row in ONE transaction. Return the versions applied by this call.
     * If a migration fails: roll it back, keep the earlier ones, and rethrow.
     * Tip: sqlkit's `db.exec(sql)` runs a script with several statements.
     */
    fun migrate(db: Connection, migrations: List<Migration>): List<Int>

    /** 14.3 — Highest applied version, or 0 if nothing was applied (or schema_history doesn't exist yet). */
    fun currentVersion(db: Connection): Int
}

object Ex14Exercise : Ex14 {
    override fun parseFileName(fileName: String): Pair<Int, String> = TODO("14.1")
    override fun migrate(db: Connection, migrations: List<Migration>): List<Int> = TODO("14.2")
    override fun currentVersion(db: Connection): Int = TODO("14.3")
}
