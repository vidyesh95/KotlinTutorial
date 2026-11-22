/*
 * ═══════════════════════════════════════════════════════════════════════════
 *  SQLDelight Lesson 02 — Queries, parameters and types
 * ═══════════════════════════════════════════════════════════════════════════
 *  Goal : read Books.sq / Orders.sq side by side with this file and learn how
 *         each SQL feature becomes a typed Kotlin function.
 *  Needs: lesson 01
 *  Time : ~35 min
 *  Next : lesson 03 — migrations
 *  Exercises: exercises/ex02
 */
package lesson02_queries_and_types

import app.cash.sqldelight.db.QueryResult
import bookstore.db.inMemoryBookstore
import bookstore.domain.OrderStatus
import kotlin.time.Instant

fun main() {
    section1Parameters()
    section2InCollections()
    section3Nullability()
    section4ProjectionsAndMappers()
    section5OneOrMany()
    section6Writes()
    section7AggregatesAndInferredTypes()
    section8ColumnAdapters()
}

// ───────────────────────────────────────────────────────────────────────────
// §1. Parameters: `:name` and `?`
// ───────────────────────────────────────────────────────────────────────────
// In Books.sq:
//   search:   … WHERE title LIKE '%' || :text || '%'      → fun search(text: String)
//   lowStock: … WHERE stock <= :threshold                 → fun lowStock(threshold: Int)
// SQLDelight works out each parameter's TYPE from the column it's compared with
// (`stock` is `AS Int` → threshold: Int). A plain `?` gets a generated name.
// Parameters are always BOUND, never glued into the SQL → no SQL injection (module 03, lesson 11).

fun section1Parameters() {
    println("§1")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        println(db.booksQueries.search(text = "Dune").executeAsList().map { it.title })
        println(db.booksQueries.lowStock(threshold = 2).executeAsList())
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §2. `IN ?` takes a whole collection
// ───────────────────────────────────────────────────────────────────────────
// selectByIds: … WHERE id IN ?   → fun selectByIds(id: Collection<Long>)
// SQLDelight expands it to the right number of placeholders for you — the thing
// we did by hand in module 03, lesson 13 (N+1 fix).

fun section2InCollections() {
    println("§2")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        db.booksQueries.selectByIds(listOf(1L, 3L, 5L)).executeAsList().forEach { println(it) }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §3. Nullability flows from the schema
// ───────────────────────────────────────────────────────────────────────────
// `published_year INTEGER AS Int` (no NOT NULL) → Kotlin `Int?` everywhere it appears,
// including PARAMETERS compared with it: inPrintSince(year: Int?).
// `in_print INTEGER AS Boolean NOT NULL` → Boolean. The compiler won't let you forget a null.

fun section3Nullability() {
    println("§3")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val recent = db.booksQueries.inPrintSince(year = 1960).executeAsList()   // List<Books>
        recent.forEach { println("${it.published_year} ${it.title} (in print: ${it.in_print})") }
        val earthsea = db.booksQueries.selectById(6).executeAsOne()
        println("${earthsea.title}: year = ${earthsea.published_year}")
    }
}
// ⚠️ Gotcha: Emma (1815) is missing above because in_print = 0, and A Wizard of Earthsea
// because its year is NULL — `NULL >= 1960` is NULL, not true (module 03, lesson 01).

// ───────────────────────────────────────────────────────────────────────────
// §4. Generated classes vs your own types (mapper lambdas)
// ───────────────────────────────────────────────────────────────────────────
// Every query function has TWO versions:
//   selectAll()                    → List<SelectAll>   (generated data class)
//   selectAll { id, title, … -> }  → List<YourType>    (you build each row yourself)
// The mapper receives the columns in SELECT order, already typed. Use it to map
// straight into your domain / API classes — no intermediate copy.
//
// 🧒 Like I'm 5: the factory sends parts in a box with its own label (SelectAll), or
// you hand the factory YOUR box and it puts the parts straight into it.

data class PriceTag(val title: String, val author: String, val euros: String)

fun section4ProjectionsAndMappers() {
    println("§4")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val tags = db.booksQueries.selectAll { _, title, authorName, _, priceCents, _, _ ->
            PriceTag(title, authorName, "%d.%02d €".format(priceCents / 100, priceCents % 100))
        }.executeAsList()
        tags.take(2).forEach(::println)
    }
}
// 🏗️ In the real world: src/main/kotlin/bookstore/catalog/CatalogService.kt uses
// `selectAll(::BookView)` — a constructor reference works as the mapper.

// ───────────────────────────────────────────────────────────────────────────
// §5. One row or many?
// ───────────────────────────────────────────────────────────────────────────
//   executeAsList()       → List<T> (maybe empty)
//   executeAsOne()        → T, throws if there are 0 rows or more than 1
//   executeAsOneOrNull()  → T?, throws if there is more than 1

fun section5OneOrMany() {
    println("§5")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        println(db.booksQueries.search("Emma").executeAsOne())
        println(db.booksQueries.search("Zebra").executeAsOneOrNull())
        val tooMany = runCatching { db.booksQueries.search("Dune").executeAsOne() }
        println("two Dunes: ${tooMany.exceptionOrNull()?.message}")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §6. Writes: RETURNING, and "how many rows changed?"
// ───────────────────────────────────────────────────────────────────────────
//  insert: INSERT … RETURNING id  → ExecutableQuery<Long>: run it with executeAsOne()
//  updatePrice / takeStock / delete → QueryResult<Long>: `.value` = number of rows changed

fun section6Writes() {
    println("§6")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val books = db.booksQueries
        val newId = books.insert(
            title = "Children of Dune", authorId = 1, isbn = "9780593098240",
            priceCents = 1399, stock = 5, publishedYear = 1976, createdAt = Instant.parse("2026-03-01T09:00:00Z"),
        ).executeAsOne()
        println("new book id: $newId")
        println("price updated: ${books.updatePrice(priceCents = 1299, id = newId).value} row")
        println("take 3 of 5: ${books.takeStock(quantity = 3, id = newId).value} row")
        println("take 3 of 2: ${books.takeStock(quantity = 3, id = newId).value} rows (not enough stock)")
        println("delete id 999: ${books.delete(999).value} rows")
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §7. Aggregates — and checking what SQLDelight inferred
// ───────────────────────────────────────────────────────────────────────────
// For computed columns SQLDelight infers a type from the expression:
//   count(*)                 → Long
//   round(avg(rating), 2)    → Double?   (avg of zero rows is NULL)
//   coalesce(sum(stock), 0)  → Double    ⚠️ surprise! sum() is typed as a number that may be REAL
// Always look at the generated class (or hover in the IDE) for computed columns.
// Fix when needed: CAST(coalesce(sum(stock), 0) AS INTEGER) → Long.

fun section7AggregatesAndInferredTypes() {
    println("§7")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        db.authorsQueries.bookCountPerAuthor().executeAsList().forEach { println(it) }
        db.reviewsQueries.averageRatings().executeAsList().forEach { println("${it.title}: ${it.average_rating} (${it.review_count})") }
    }
}

// ───────────────────────────────────────────────────────────────────────────
// §8. Column adapters: Kotlin types SQLite doesn't have
// ───────────────────────────────────────────────────────────────────────────
// In V1__create_tables.sqm:  status TEXT AS OrderStatus,  created_at TEXT AS Instant,
//                            price_cents INTEGER AS Int,   in_print INTEGER AS Boolean
// SQLite only stores INTEGER/REAL/TEXT/BLOB. `AS Type` tells SQLDelight "in Kotlin this
// column is a <Type>", and a ColumnAdapter converts both ways at runtime:
//
//   IntAdapter        Long  ⇄ Int          (SQLite INTEGERs arrive as Long)
//   InstantAdapter    String ⇄ Instant     (ISO-8601 text)
//   EnumColumnAdapter String ⇄ OrderStatus (stores the enum NAME — never reorder-safe ordinal)
//   Boolean           built in (0/1)
//
// They are plugged in ONCE, in src/main/kotlin/bookstore/db/DatabaseFactory.kt.
// Idea for your own code: `AS BookId` with `@JvmInline value class BookId(val value: Long)`
// + an adapter → book ids and author ids can never be mixed up (module 01, lesson 17).

fun section8ColumnAdapters() {
    println("§8")
    val (driver, db) = inMemoryBookstore()
    driver.use {
        val at = Instant.parse("2026-04-01T12:30:00Z")
        val id = db.ordersQueries.insertOrder("ada@example.com", OrderStatus.PAID, 2598, at).executeAsOne()
        println(db.ordersQueries.selectById(id).executeAsOne())          // typed: OrderStatus, Int, Instant

        // What SQLite really stored (raw query through the driver, no adapters):
        val raw = driver.executeQuery(null, "SELECT status, total_cents, created_at FROM orders", { cursor ->
            cursor.next()
            QueryResult.Value("${cursor.getString(0)} | ${cursor.getLong(1)} | ${cursor.getString(2)}")
        }, 0).value
        println("stored: $raw")
    }
}

// ═══════════════════════════════════════════════════════════════════════════
// Recap
//  • :name / ? parameters → typed function parameters; `IN ?` → Collection.
//  • Nullable columns → nullable Kotlin types, in results AND parameters.
//  • Generated result classes, or your own types via mapper lambdas.
//  • executeAsList / executeAsOne / executeAsOneOrNull; writes return rows changed.
//  • Check inferred types of computed columns; ColumnAdapters map `AS Type` columns.
// ═══════════════════════════════════════════════════════════════════════════

// Output:
// §1
// [Dune, Dune Messiah]
// [LowStock(id=4, title=Emma, stock=0), LowStock(id=2, title=Dune Messiah, stock=2)]
// §2
// SelectByIds(id=1, title=Dune, price_cents=1299)
// SelectByIds(id=3, title=Pride and Prejudice, price_cents=799)
// SelectByIds(id=5, title=Designing Data-Intensive Applications, price_cents=4599)
// §3
// 1965 Dune (in print: true)
// 1969 Dune Messiah (in print: true)
// 2017 Designing Data-Intensive Applications (in print: true)
// A Wizard of Earthsea: year = null
// §4
// PriceTag(title=Dune, author=Frank Herbert, euros=12.99 €)
// PriceTag(title=Dune Messiah, author=Frank Herbert, euros=11.99 €)
// §5
// Search(id=4, title=Emma)
// null
// two Dunes: ResultSet returned more than 1 row for Books.sq:search
// §6
// new book id: 7
// price updated: 1 row
// take 3 of 5: 1 row
// take 3 of 2: 0 rows (not enough stock)
// delete id 999: 0 rows
// §7
// BookCountPerAuthor(name=Frank Herbert, book_count=2, total_stock=14.0)
// BookCountPerAuthor(name=Jane Austen, book_count=2, total_stock=30.0)
// BookCountPerAuthor(name=Martin Kleppmann, book_count=1, total_stock=4.0)
// BookCountPerAuthor(name=Ursula K. Le Guin, book_count=1, total_stock=8.0)
// Designing Data-Intensive Applications: 5.0 (1)
// Dune: 4.5 (2)
// Pride and Prejudice: 3.0 (1)
// §8
// Orders(id=1, customer_email=ada@example.com, status=PAID, total_cents=2598, created_at=2026-04-01T12:30:00Z)
// stored: PAID | 2598 | 2026-04-01T12:30:00Z
