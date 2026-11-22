/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQLDelight Lesson 03 — Migrations: one source of truth for the schema
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : understand how the .sqm migration files drive BOTH compile-time checks
 *         (SQLDelight) and runtime schema changes (SQLDelight or Flyway).
 *  Needs: lesson 02; module 03 lesson 14 (you wrote a migration runner there!)
 *  Time : ~30 min
 *  Next : lesson 04 — transactions and reactive queries
 *  Exercises: exercises/ex03
 */
package lesson03_migrations

import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import bookstore.db.BookstoreDatabase
import bookstore.db.createDatabase

fun main() {
    section1OneSourceOfTruth()
    section2CopiesForFlyway()
    section3CreateVersusMigrate()
    section4UpgradingAnOldDatabase()
    section5Rules()
}

/** Small helper: run a single-value SQL query through the raw driver. */
fun SqlDriver.scalar(sql: String): String? =
    executeQuery(null, sql, { cursor -> QueryResult.Value(if (cursor.next().value) cursor.getString(0) else null) }, 0).value

fun SqlDriver.tables(): String? =
    scalar("SELECT group_concat(name, ', ') FROM (SELECT name FROM sqlite_schema WHERE type = 'table' ORDER BY name)")

// ───────────────────────────────────────────────────────────────────────────
// §1. The migration files ARE the schema
// ───────────────────────────────────────────────────────────────────────────
// build.gradle.kts says `deriveSchemaFromMigrations = true`, so SQLDelight builds its
// picture of the database by replaying, in order:
//
//   V1__create_tables.sqm   authors, books, orders, order_items
//   V2__seed_data.sqm       starter rows
//   V3__add_reviews.sqm     reviews (added "later")
//
// Every query in the .sq files is checked against the schema AFTER the last migration.
// Delete a column in V4 that Books.sq still uses → the build fails. That's the safety net.
//
// 🧒 Like I'm 5: a LEGO instruction booklet with numbered pages. Anyone who follows
// pages 1, 2, 3 in order ends up with the same castle. SQLDelight reads the booklet to
// know what the castle looks like; Flyway follows it to actually build it.
//
// ⚠️ Off-by-one: SQLDelight counts DATABASE versions. A fresh database is version 1;
// V1 takes it 1 → 2, V2 2 → 3, V3 3 → 4. So Schema.version = (last migration) + 1.

fun section1OneSourceOfTruth() {
    println("§1")
    println("Schema.version = ${BookstoreDatabase.Schema.version} (after V3)")
}

// ───────────────────────────────────────────────────────────────────────────
// §2. Plain-SQL copies for Flyway
// ───────────────────────────────────────────────────────────────────────────
// `AS Int`, `AS Instant`, `import …` are SQLDelight-only syntax; real SQLite would choke.
// The task `generateMainBookstoreDatabaseMigrations` writes cleaned copies to
//   build/generated/flyway/db/migration/V1__create_tables.sql …
// and build.gradle.kts puts that folder on the classpath, where Flyway finds it (lesson 05).
//
//   in V1__create_tables.sqm:   price_cents    INTEGER AS Int NOT NULL CHECK (price_cents >= 0),
//                               status         TEXT AS OrderStatus NOT NULL,

fun section2CopiesForFlyway() {
    println("§2")
    val v1 = object {}.javaClass.getResource("/db/migration/V1__create_tables.sql")!!.readText()
    v1.lines()
        .filter { it.trimStart().startsWith("price_cents") || it.trimStart().startsWith("status") }
        .forEach { println("in V1__create_tables.sql: ${it.trim()}") }
    println("first line is an import? ${v1.trimStart().startsWith("import")}")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Schema.create vs Schema.migrate
// ───────────────────────────────────────────────────────────────────────────
//  Schema.create(driver)          → creates the FINAL tables in one go. Fast, but it only
//                                   runs CREATE statements — the INSERTs in V2/V3 are skipped!
//  Schema.migrate(driver, 1, N)   → replays every migration from version 1 to N, INSERTs included.
//
// That's why `inMemoryBookstore()` (DatabaseFactory.kt) uses migrate().

fun section3CreateVersusMigrate() {
    println("§3")
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
        BookstoreDatabase.Schema.create(driver)
        println("create():  tables = ${driver.tables()}; books = ${driver.scalar("SELECT count(*) FROM books")}")
    }
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
        BookstoreDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = BookstoreDatabase.Schema.version)
        println("migrate(): tables = ${driver.tables()}; books = ${driver.scalar("SELECT count(*) FROM books")}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. Upgrading a database that's already in use
// ───────────────────────────────────────────────────────────────────────────
// Real life: version 3 of the app is running in production (V1, V2 applied). We ship V3.
// On startup, only the MISSING migrations may run — never V1 again.
//
// Who remembers "which version is this database at"?
//  • Flyway:      the `flyway_schema_history` table (lesson 05)
//  • SQLDelight:  SQLite's built-in `PRAGMA user_version` number, if you use the
//                 JdbcSqliteDriver(url, properties, schema) constructor — or you track it yourself
//  • module 03:   your own `schema_history` table (lesson 14)
//
// `AfterVersion(n) { driver -> … }` runs Kotlin code in the middle of the upgrade —
// for data migrations that are too fiddly in SQL.

fun section4UpgradingAnOldDatabase() {
    println("§4")
    JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY).use { driver ->
        // "Production" database from the previous release: only V1 and V2 applied (version 3).
        BookstoreDatabase.Schema.migrate(driver, oldVersion = 1, newVersion = 3)
        driver.execute(null, "PRAGMA user_version = 3", 0)
        println("before: version ${driver.scalar("PRAGMA user_version")}, tables = ${driver.tables()}")

        // New release starts: apply what's missing, 3 → 4, i.e. just V3.
        val current = driver.scalar("PRAGMA user_version")!!.toLong()
        BookstoreDatabase.Schema.migrate(
            driver, oldVersion = current, newVersion = BookstoreDatabase.Schema.version,
            AfterVersion(3) { d -> println("  AfterVersion(3): reviews now = ${d.scalar("SELECT count(*) FROM reviews")}") },
        )
        driver.execute(null, "PRAGMA user_version = ${BookstoreDatabase.Schema.version}", 0)
        println("after:  version ${driver.scalar("PRAGMA user_version")}, tables = ${driver.tables()}")

        val db = createDatabase(driver)
        println("Dune reviews: ${db.reviewsQueries.selectForBook(1).executeAsList().map { "${it.reviewer}=${it.rating}" }}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. The rules (same as module 03, lesson 14 — they matter even more here)
// ───────────────────────────────────────────────────────────────────────────
//  1. NEVER edit a migration that has run anywhere. Fix mistakes with a NEW migration.
//     (Flyway checks a checksum and refuses to start if an applied file changed.)
//  2. Migrations only move FORWARD. "Undo" = another migration.
//  3. Expand → migrate → contract for zero-downtime changes. Renaming a column:
//       V4 add new column · deploy code writing both · V5 backfill · deploy code reading new ·
//       V6 drop old column.  Never rename in one step while old code is still running.
//  4. `./gradlew :05-sqldelight:verifySqlDelightMigration` (runs on `check`) makes sure the
//     migrations produce a valid schema and stay consistent with your .sq queries.
//
// 🐘 Postgres note: with Postgres, DDL is transactional — a failing migration rolls back
// completely. SQLite is mostly transactional too, but some ALTER TABLE changes aren't
// possible at all (you recreate the table). Flyway and SQLDelight work with both.

fun section5Rules() {
    println("§5")
    println("never edit applied migrations; add a new one")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • .sqm migrations = single source of truth; SQLDelight derives the schema from them.
//  • Cleaned .sql copies (AS Type removed) feed Flyway at runtime.
//  • create() = final tables only; migrate(driver, from, to) = replay, data included.
//  • Track the database version (Flyway history / PRAGMA user_version); apply only what's missing.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// Schema.version = 4 (after V3)
// §2
// in V1__create_tables.sql: price_cents    INTEGER NOT NULL CHECK (price_cents >= 0),
// in V1__create_tables.sql: status         TEXT NOT NULL,
// first line is an import? false
// §3
// create():  tables = authors, books, order_items, orders, reviews; books = 0
// migrate(): tables = authors, books, order_items, orders, reviews; books = 6
// §4
// before: version 3, tables = authors, books, order_items, orders
//   AfterVersion(3): reviews now = 4
// after:  version 4, tables = authors, books, order_items, orders, reviews
// Dune reviews: [ada=5, alan=4]
// §5
// never edit applied migrations; add a new one
