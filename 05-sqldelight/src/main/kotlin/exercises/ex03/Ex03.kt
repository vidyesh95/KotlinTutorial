package exercises.ex03

import app.cash.sqldelight.db.SqlDriver

/*
 * Exercise 03 — migrations and the raw driver          (lesson: lesson03_migrations)
 * Check: ./gradlew :05-sqldelight:exercises --tests 'exercises.ex03*'
 *
 * Useful driver calls:
 *   driver.execute(null, "SQL", 0)                                         // run a statement
 *   driver.executeQuery(null, "SQL", { cursor -> … QueryResult.Value(x) }, 0)   // read rows
 *   cursor.next().value  /  cursor.getString(0)  /  cursor.getLong(0)
 */
interface Ex03 {
    /** 03.1 — The schema version SQLDelight derived from the .sqm files (V1, V2, V3 …). Hint: BookstoreDatabase.Schema. */
    fun schemaVersion(): Long

    /** 03.2 — Names of all user tables in the database, sorted A → Z (skip names starting with "sqlite_"). */
    fun tableNames(driver: SqlDriver): List<String>

    /**
     * 03.3 — A tiny migration runner (like Flyway, module 03 lesson 14).
     * [migrations] maps version → ONE SQL statement. Create `schema_history(version INTEGER PRIMARY KEY)` if
     * missing, then apply every version not yet recorded, in ascending order, recording each.
     * Return the versions applied by THIS call (empty list when nothing was pending).
     */
    fun migrate(driver: SqlDriver, migrations: Map<Int, String>): List<Int>
}

object Ex03Exercise : Ex03 {
    override fun schemaVersion(): Long = TODO("03.1")
    override fun tableNames(driver: SqlDriver): List<String> = TODO("03.2")
    override fun migrate(driver: SqlDriver, migrations: Map<Int, String>): List<Int> = TODO("03.3")
}
