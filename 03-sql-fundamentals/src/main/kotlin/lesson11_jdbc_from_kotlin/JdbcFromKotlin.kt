/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQL Lesson 11 — JDBC from Kotlin: talking to a database from code
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : open connections, run queries with parameters, turn rows into Kotlin
 *         data classes, handle NULLs, get generated ids — and never, ever build
 *         SQL by gluing strings together (SQL injection).
 *  Needs: SQL lessons 01–10, module 01 lessons 10 (use), 12–13 (lambdas), 23 (generics)
 *  Time : ~40 min
 *  Next : lesson 12 — transactions
 *  Exercises: exercises/ex11/Ex11.kt
 */
package lesson11_jdbc_from_kotlin

import sqlkit.bookstoreDb
import sqlkit.exec
import sqlkit.memoryDb
import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

fun main() {
    section1Connecting()
    section2PreparedStatements()
    section3MappingRowsToDataClasses()
    section4Nulls()
    section5GeneratedIdsAndBatches()
    section6SqlInjection()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Connecting: DriverManager → Connection → Statement → ResultSet
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    JDBC is a universal travel plug. Your Kotlin code always uses the same
//    plug shape (Connection, PreparedStatement, ResultSet). Each database ships
//    an adapter — the *driver* — that fits its own wall socket (SQLite,
//    Postgres, MySQL…). Change the adapter and the URL; your code stays the same.
//
// The URL picks the driver:  jdbc:sqlite::memory:   jdbc:postgresql://host/db
// Connections, statements and result sets hold real resources (files,
// sockets). ALWAYS close them — Kotlin's `use { }` closes even on exceptions.

fun section1Connecting() {
    println("§1")
    DriverManager.getConnection("jdbc:sqlite::memory:").use { connection ->
        println("connected to ${connection.metaData.databaseProductName}")
        connection.createStatement().use { statement ->
            statement.executeQuery("SELECT 1 + 1 AS answer").use { rows ->
                rows.next()                            // move the cursor to the first row
                println("answer = ${rows.getInt("answer")}")
            }
        }
    }   // ← everything closed here, in reverse order
}

// ───────────────────────────────────────────────────────────────────────────
// §2. PreparedStatement: SQL with `?` holes, values bound separately
// ───────────────────────────────────────────────────────────────────────────
// Write the SQL ONCE with `?` placeholders, then bind values by position
// (1-based!). The database receives the SQL text and the values SEPARATELY —
// values can never change the meaning of the SQL. (Hold that thought for §6.)

fun section2PreparedStatements() {
    println("§2")
    bookstoreDb().use { db ->
        val sql = "SELECT title, price_cents FROM books WHERE author_id = ? AND price_cents < ? ORDER BY id"
        db.prepareStatement(sql).use { statement ->
            statement.setInt(1, 4)        // first ?  → author 4 (Asimov)
            statement.setInt(2, 1500)     // second ? → cheaper than 15.00
            statement.executeQuery().use { rows ->
                while (rows.next()) {     // next() returns false after the last row
                    println("${rows.getString("title")} costs ${rows.getInt("price_cents")} cents")
                }
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Rows → data classes, with one small generic helper
// ───────────────────────────────────────────────────────────────────────────
// Writing prepare/bind/loop/close every time is noisy. A generic extension
// function hides it: YOU only say how to turn ONE row into ONE object.
// (`T` = generics, module 01 lesson 23; `map` = a lambda, lesson 12.)

data class Book(val id: Long, val title: String, val priceCents: Int, val year: Int?)

fun <T> Connection.query(sql: String, vararg params: Any?, map: (ResultSet) -> T): List<T> =
    prepareStatement(sql).use { statement ->
        params.forEachIndexed { index, value -> statement.setObject(index + 1, value) }
        statement.executeQuery().use { rows ->
            buildList { while (rows.next()) add(map(rows)) }
        }
    }

/** JDBC's getInt returns 0 for NULL (see §4) — this asks "was it NULL?" right after. */
fun ResultSet.getIntOrNull(column: String): Int? = getInt(column).takeUnless { wasNull() }

fun ResultSet.toBook() = Book(
    id = getLong("id"),
    title = getString("title"),
    priceCents = getInt("price_cents"),
    year = getIntOrNull("published_year"),
)

fun section3MappingRowsToDataClasses() {
    println("§3")
    bookstoreDb().use { db ->
        val fantasy = db.query("SELECT * FROM books WHERE category_id = ? ORDER BY id", 3) { it.toBook() }
        fantasy.forEach(::println)

        val one = db.query("SELECT * FROM books WHERE id = ?", 999) { it.toBook() }.singleOrNull()
        println("book 999: $one")
    }
}
// 🏗️ In the real world: this helper IS Spring's JdbcClient (module 04):
//    jdbcClient.sql("SELECT * FROM books WHERE id = ?").param(id).query { rs, _ -> rs.toBook() }.list()

// ───────────────────────────────────────────────────────────────────────────
// §4. NULLs: the getInt trap
// ───────────────────────────────────────────────────────────────────────────
// ⚠️ Gotcha: getInt/getLong/getDouble return primitive Java values, which
// can't be null — so a SQL NULL comes back as 0! Book 14 has no year:

fun section4Nulls() {
    println("§4")
    bookstoreDb().use { db ->
        db.prepareStatement("SELECT published_year FROM books WHERE id = 14").use { statement ->
            statement.executeQuery().use { rows ->
                rows.next()
                val raw = rows.getInt("published_year")
                println("getInt says $raw, wasNull() says ${rows.wasNull()}")
                println("getIntOrNull says ${rows.getIntOrNull("published_year")}")
                println("getString says ${rows.getString("published_year")}")   // objects CAN be null
            }
        }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. Getting generated ids, and batching many inserts
// ───────────────────────────────────────────────────────────────────────────
// `INSERT … RETURNING id` (SQLite 3.35+, and Postgres) hands back the new
// row like a query does. Batches send many parameter sets in one go — much
// faster than one round trip per row.

fun section5GeneratedIdsAndBatches() {
    println("§5")
    memoryDb().use { db ->
        db.exec("CREATE TABLE notes (id INTEGER PRIMARY KEY, text TEXT NOT NULL)")

        val newId = db.query("INSERT INTO notes (text) VALUES (?) RETURNING id", "buy milk") { it.getLong("id") }.single()
        println("new note id = $newId")

        db.prepareStatement("INSERT INTO notes (text) VALUES (?)").use { statement ->
            for (text in listOf("call mum", "learn JDBC", "sleep")) {
                statement.setString(1, text)
                statement.addBatch()                  // queue it…
            }
            val counts = statement.executeBatch()     // …send them all
            println("batch inserted ${counts.sum()} rows")
        }
        println(db.query("SELECT id, text FROM notes ORDER BY id") { "${it.getLong("id")}:${it.getString("text")}" })
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. 🔐 SQL injection — the most famous security bug there is
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    The teacher hands out cards: "My name is ______". The teacher then reads
//    every card OUT LOUD AS AN INSTRUCTION to the class. One kid writes:
//    "Tim. Also, everybody give Tim your cookies." The teacher reads it…
//    and everybody gives Tim their cookies. The blank was supposed to hold a
//    NAME, but it was treated as part of the INSTRUCTIONS.
//
// That's what happens when you paste user input into SQL text.

fun unsafeLogin(db: Connection, username: String, password: String): Boolean {
    // ❌ NEVER DO THIS: user input glued into the SQL string
    val sql = "SELECT count(*) FROM users WHERE username = '$username' AND password = '$password'"
    println("  built SQL: $sql")
    return db.createStatement().use { it.executeQuery(sql).use { rows -> rows.next() && rows.getInt(1) > 0 } }
}

fun safeLogin(db: Connection, username: String, password: String): Boolean =
    // ✅ placeholders: input is ONLY ever treated as data
    db.query("SELECT count(*) AS n FROM users WHERE username = ? AND password = ?", username, password) {
        it.getInt("n")
    }.single() > 0

fun section6SqlInjection() {
    println("§6")
    memoryDb().use { db ->
        // (Plain-text passwords only to keep the demo short — module 06 stores HASHES.)
        db.exec(
            """
            CREATE TABLE users (id INTEGER PRIMARY KEY, username TEXT NOT NULL UNIQUE, password TEXT NOT NULL);
            INSERT INTO users (username, password) VALUES ('admin', 'correct horse battery staple');
            """.trimIndent()
        )
        val attack = "' OR '1'='1"
        println("unsafe, wrong password: ${unsafeLogin(db, "admin", "guess")}")
        println("unsafe, attack string:  ${unsafeLogin(db, "admin", attack)}")
        println("safe,   attack string:  ${safeLogin(db, "admin", attack)}")
        println("safe,   real password:  ${safeLogin(db, "admin", "correct horse battery staple")}")
    }
}
// Why the attack works: the text becomes
//   … WHERE username = 'admin' AND password = '' OR '1'='1'
// AND binds tighter than OR, so it's (… AND …) OR TRUE → always true.
// Attackers use the same trick to read other tables, or `; DROP TABLE …`.
//
// 🔐 Rules:
//  • ALWAYS bind values with `?` (PreparedStatement / JdbcClient .param()).
//  • Things that can't be bound (table/column names, ORDER BY direction) →
//    pick from a fixed allow-list in code, never from raw input.
//  • Escaping quotes by hand is not a fix. Placeholders are.

// 🐘 Postgres note: identical JDBC code; only the URL and driver change
// (jdbc:postgresql://localhost:5432/shop, org.postgresql:postgresql).
// Postgres also supports `INSERT … RETURNING`, and its driver supports batches.

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • Connection → PreparedStatement (with `?`) → ResultSet; close all with `use`.
//  • Map each row to a data class with a tiny generic helper (= JdbcClient later).
//  • getInt returns 0 for NULL — check wasNull() or read as an object.
//  • RETURNING / generated keys for new ids; addBatch/executeBatch for bulk inserts.
//  • 🔐 Never concatenate input into SQL. Bind it.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// connected to SQLite
// answer = 2
// §2
// Foundation costs 1199 cents
// I, Robot costs 899 cents
// Collected Notes costs 500 cents
// §3
// Book(id=5, title=A Wizard of Earthsea, priceCents=999, year=1968)
// Book(id=8, title=The Hobbit, priceCents=1499, year=1937)
// Book(id=9, title=The Lord of the Rings, priceCents=2999, year=1954)
// book 999: null
// §4
// getInt says 0, wasNull() says true
// getIntOrNull says null
// getString says null
// §5
// new note id = 1
// batch inserted 3 rows
// [1:buy milk, 2:call mum, 3:learn JDBC, 4:sleep]
// §6
//   built SQL: SELECT count(*) FROM users WHERE username = 'admin' AND password = 'guess'
// unsafe, wrong password: false
//   built SQL: SELECT count(*) FROM users WHERE username = 'admin' AND password = '' OR '1'='1'
// unsafe, attack string:  true
// safe,   attack string:  false
// safe,   real password:  true
