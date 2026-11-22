package solutions.ex03

import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import bookstore.db.BookstoreDatabase
import exercises.ex03.Ex03

object Ex03Solution : Ex03 {
    override fun schemaVersion(): Long = BookstoreDatabase.Schema.version

    override fun tableNames(driver: SqlDriver): List<String> =
        driver.executeQuery(
            identifier = null,
            sql = "SELECT name FROM sqlite_schema WHERE type = 'table' AND name NOT LIKE 'sqlite_%' ORDER BY name",
            mapper = { cursor ->
                val names = mutableListOf<String>()
                while (cursor.next().value) names += cursor.getString(0)!!
                QueryResult.Value(names)
            },
            parameters = 0,
        ).value

    override fun migrate(driver: SqlDriver, migrations: Map<Int, String>): List<Int> {
        driver.execute(null, "CREATE TABLE IF NOT EXISTS schema_history (version INTEGER PRIMARY KEY)", 0)
        val applied = driver.executeQuery(
            identifier = null,
            sql = "SELECT version FROM schema_history",
            mapper = { cursor ->
                val versions = mutableSetOf<Int>()
                while (cursor.next().value) versions += cursor.getLong(0)!!.toInt()
                QueryResult.Value(versions)
            },
            parameters = 0,
        ).value
        val pending = migrations.keys.filter { it !in applied }.sorted()
        for (version in pending) {
            driver.execute(null, migrations.getValue(version), 0)
            driver.execute(null, "INSERT INTO schema_history (version) VALUES (?)", 1) { bindLong(0, version.toLong()) }
        }
        return pending
    }
}
