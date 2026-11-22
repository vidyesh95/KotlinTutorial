/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQLDelight Lesson 01 — Why SQLDelight? SQL that the compiler checks
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : see why SQL hidden in strings breaks at RUNTIME, and how SQLDelight
 *         moves those mistakes to COMPILE time — like Rust's sqlx.
 *  Needs: module 03 (SQL, JDBC), module 04 (JdbcClient)
 *  Time : ~25 min
 *  Next : lesson 02 — queries, parameters and types
 *  Exercises: exercises/ex01 (SQL in src/main/sqldelight/exercises/ex01)
 */
package lesson01_why_sqldelight

import bookstore.db.inMemoryBookstore
import java.sql.DriverManager
import java.sql.SQLException

fun main() {
    section1SqlInStrings()
    section2TheIdea()
    section3FirstQueries()
    section4GeneratedTypes()
    section5SqlxComparison()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. The problem: SQL inside strings
// ───────────────────────────────────────────────────────────────────────────
// In modules 03 and 04 every query was a String. To the Kotlin compiler,
// "SELECT titel FROM books" is just text — it can't know `titel` is a typo.
// You find out when that line RUNS: in a test if you're lucky, in production
// at 3 a.m. if you're not.

fun section1SqlInStrings() {
    println("§1")
    DriverManager.getConnection("jdbc:sqlite::memory:").use { connection ->
        connection.createStatement().use { it.execute("CREATE TABLE books (id INTEGER PRIMARY KEY, title TEXT NOT NULL)") }
        try {
            connection.createStatement().use { it.executeQuery("SELECT titel FROM books") }   // typo!
        } catch (e: SQLException) {
            println("found at RUNTIME: ${e.message}")
        }
    }
    // Other things only discovered at runtime with string SQL:
    //  • wrong number/order of `?` parameters      • reading column "price" as a String
    //  • a column renamed in a migration           • a nullable column read as non-null
}

// ───────────────────────────────────────────────────────────────────────────
// §2. The idea: SQL first, Kotlin generated, checked at compile time
// ───────────────────────────────────────────────────────────────────────────
// 🧒 Like I'm 5:
//    A spell-checker for SQL that runs BEFORE your program even exists. You
//    write SQL in its own files; while building, SQLDelight reads every query,
//    checks each table and column really exists and has the right type, and
//    then writes the Kotlin code that runs it for you. Typo → the build fails,
//    with a red line in the IDE pointing at the exact word.
//
// The files in this module:
//
//   src/main/sqldelight/bookstore/db/
//     V1__create_tables.sqm   ← migrations: CREATE TABLE … (the schema, lesson 03)
//     V2__seed_data.sqm
//     V3__add_reviews.sqm
//     Books.sq                ← labelled queries:   selectAll:  SELECT … ;
//     Authors.sq, Orders.sq, Reviews.sq
//
//   build/generated/sqldelight/code/BookstoreDatabase/main/bookstore/db/   (written by the build)
//     BookstoreDatabase.kt    ← entry point: database.booksQueries, database.authorsQueries, …
//     BooksQueries.kt         ← one Kotlin function per label: selectAll(), insert(…), takeStock(…)
//     Books.kt, SelectAll.kt  ← data classes for table rows and query results
//
// Try it: rename `title` to `titel` in Books.sq and run `./gradlew :05-sqldelight:build`. You get
//   Books.sq: … No column found with name titel
// and NOTHING runs until you fix it.

fun section2TheIdea() {
    println("§2")
    println("SQL lives in .sq files; Kotlin calls generated functions")
}

// ───────────────────────────────────────────────────────────────────────────
// §3. First queries
// ───────────────────────────────────────────────────────────────────────────
// `inMemoryBookstore()` (src/main/kotlin/bookstore/db/DatabaseFactory.kt) creates an
// in-memory SQLite database, runs the migrations and returns the generated
// BookstoreDatabase. Then:  database.<file>Queries.<label>(params).executeAsList()

fun section3FirstQueries() {
    println("§3")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val books = db.booksQueries.selectAll().executeAsList()      // List<SelectAll>
        for (book in books.take(3)) {
            println("${book.title} by ${book.author_name}: ${book.price_cents} cents")
        }
        val authors = db.authorsQueries.selectAll().executeAsList()  // List<Authors>
        println("authors: ${authors.map { it.name }}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §4. The generated types
// ───────────────────────────────────────────────────────────────────────────
// Every query returns a TYPED object — no `getString("title")` guessing:
//  • `SELECT books.*`             → the table's data class `Books`
//  • a custom column list / JOIN  → a NEW data class named after the label (`SelectById`)
// Nullable columns become nullable Kotlin types (`published_year: Int?`), and
// `AS Int` / `AS Instant` in the migrations become Int / Instant fields (lesson 02).

fun section4GeneratedTypes() {
    println("§4")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val dune = db.booksQueries.selectById(id = 1).executeAsOne()
        println(dune)
        val year: Int? = dune.published_year        // the compiler KNOWS it may be null
        println("published: ${year ?: "unknown"}")
        val missing = db.booksQueries.selectById(id = 999).executeAsOneOrNull()
        println("book 999: $missing")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §5. SQLDelight vs Rust's sqlx
// ───────────────────────────────────────────────────────────────────────────
// Both say "write real SQL, let the compiler check it". The difference is HOW:
//
//  ┌──────────────────────┬──────────────────────────────┬───────────────────────────────┐
//  │                      │ Rust sqlx `query!`            │ SQLDelight                    │
//  ├──────────────────────┼──────────────────────────────┼───────────────────────────────┤
//  │ where SQL lives      │ inline in Rust code           │ separate .sq files            │
//  │ checked against      │ a LIVE database at compile    │ the schema derived from your  │
//  │                      │ time (or a cached snapshot)   │ migration files — no DB needed│
//  │ generated            │ anonymous record types        │ named data classes + functions│
//  │ migrations           │ `sqlx migrate` folder         │ .sqm files (+ Flyway copies)  │
//  └──────────────────────┴──────────────────────────────┴───────────────────────────────┘
//
// 🏗️ In the real world: SQLDelight comes from Cash App (Block) and is popular in
// Kotlin Multiplatform apps too — the same .sq files run on Android, iOS and JVM.
// 🐘 Postgres note: switch the dialect to `app.cash.sqldelight:postgresql-dialect` and
// the same checks apply to Postgres SQL (lesson 06).

fun section5SqlxComparison() {
    println("§5")
    println("sqlx checks against a database; SQLDelight checks against your migrations")
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • SQL in strings fails at runtime; SQLDelight makes SQL mistakes compile errors.
//  • .sqm = schema (migrations), .sq = labelled queries → generated Kotlin functions.
//  • Results are typed data classes; nullable columns become nullable types.
//  • Same philosophy as sqlx, checked against migrations instead of a live DB.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// found at RUNTIME: [SQLITE_ERROR] SQL error or missing database (no such column: titel)
// §2
// SQL lives in .sq files; Kotlin calls generated functions
// §3
// Dune by Frank Herbert: 1299 cents
// Dune Messiah by Frank Herbert: 1199 cents
// Pride and Prejudice by Jane Austen: 799 cents
// authors: [Frank Herbert, Jane Austen, Martin Kleppmann, Ursula K. Le Guin]
// §4
// SelectById(id=1, title=Dune, author_name=Frank Herbert, isbn=9780441172719, price_cents=1299, stock=12, published_year=1965)
// published: 1965
// book 999: null
// §5
// sqlx checks against a database; SQLDelight checks against your migrations
