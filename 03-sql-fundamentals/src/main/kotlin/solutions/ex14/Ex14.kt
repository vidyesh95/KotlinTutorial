package solutions.ex14

import exercises.ex14.Ex14
import exercises.ex14.Migration
import sqlkit.exec
import sqlkit.rows
import java.sql.Connection

object Ex14Solution : Ex14 {
    override fun parseFileName(fileName: String): Pair<Int, String> {
        val match = Regex("""V(\d+)__(\w+)\.sql""").matchEntire(fileName)
            ?: throw IllegalArgumentException("not a migration file name: $fileName")
        val (version, description) = match.destructured
        return version.toInt() to description.replace('_', ' ')
    }

    override fun migrate(db: Connection, migrations: List<Migration>): List<Int> {
        db.exec("CREATE TABLE IF NOT EXISTS schema_history (version INTEGER PRIMARY KEY, description TEXT NOT NULL)")
        val applied = db.rows("SELECT version FROM schema_history").map { it.single()!!.toInt() }.toSet()
        val newlyApplied = mutableListOf<Int>()
        for (migration in migrations.sortedBy { it.version }) {
            if (migration.version in applied) continue
            val previous = db.autoCommit
            db.autoCommit = false
            try {
                db.exec(migration.sql)
                db.prepareStatement("INSERT INTO schema_history (version, description) VALUES (?, ?)").use {
                    it.setInt(1, migration.version)
                    it.setString(2, migration.description)
                    it.executeUpdate()
                }
                db.commit()
            } catch (e: Throwable) {
                db.rollback()
                throw e
            } finally {
                db.autoCommit = previous
            }
            newlyApplied += migration.version
        }
        return newlyApplied
    }

    override fun currentVersion(db: Connection): Int {
        val exists = db.rows("SELECT count(*) FROM sqlite_schema WHERE type = 'table' AND name = 'schema_history'")
            .single().single() == "1"
        if (!exists) return 0
        return db.rows("SELECT coalesce(max(version), 0) FROM schema_history").single().single()!!.toInt()
    }
}
