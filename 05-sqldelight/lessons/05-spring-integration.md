# SQLDelight Lesson 05 — SQLDelight inside Spring Boot

**Goal:** run the generated SQLDelight queries inside a Spring Boot app, with Flyway applying the
*same* migrations and `@Transactional` controlling the transactions.
**Needs:** lessons 01–04 of this module; module 04 (Spring Boot, Flyway, `@Transactional`)
**Time:** ~30 min · **Next:** [06 — comparison](06-comparison.md) · **Exercises:** `src/main/kotlin/exercises/ex05/`

The app is small on purpose — module 04 already covered controllers, validation and testing. Here the
interesting part is how the pieces connect:

```
 .sqm migrations ──(build)──▶ SQLDelight checks every .sq query ──▶ generated Kotlin (BookstoreDatabase)
        │                                                                     │
        └─(build)─▶ cleaned V*.sql copies ──(startup)──▶ Flyway ──▶ SQLite ◀──┘ queries via Spring's DataSource
```

| File | Role |
|------|------|
| [`build.gradle.kts`](../build.gradle.kts) | SQLDelight config + puts the cleaned migrations on the classpath |
| [`config/DatabaseConfig.kt`](../src/main/kotlin/bookstore/config/DatabaseConfig.kt) | `SqlDriver` and `BookstoreDatabase` beans |
| [`db/DatabaseFactory.kt`](../src/main/kotlin/bookstore/db/DatabaseFactory.kt) | column adapters, `createDatabase(driver)` |
| [`catalog/CatalogService.kt`](../src/main/kotlin/bookstore/catalog/CatalogService.kt) | read queries mapped into a view model |
| [`orders/OrderService.kt`](../src/main/kotlin/bookstore/orders/OrderService.kt) | `@Transactional` order placement |
| [`web/ApiController.kt`](../src/main/kotlin/bookstore/web/ApiController.kt) | 3 endpoints + error mapping |
| [`SqlDelightSpringIntegrationTest.kt`](../src/test/kotlin/bookstore/SqlDelightSpringIntegrationTest.kt) | proves it all works, including rollback |

## §1. Flyway runs SQLDelight's migrations

Lesson 03 showed that `V1__create_tables.sqm` contains SQLDelight-only syntax (`AS Int`, `import …`).
SQLDelight writes plain-SQL copies, and the build puts them where Flyway looks:

```kotlin
// build.gradle.kts
sqldelight {
    databases {
        create("BookstoreDatabase") {
            // …
            deriveSchemaFromMigrations = true
            migrationOutputDirectory = layout.buildDirectory.dir("generated/flyway/db/migration")
            migrationOutputFileFormat = ".sql"
        }
    }
}

sourceSets.main { resources.srcDir(layout.buildDirectory.dir("generated/flyway")) }   // → classpath:db/migration
tasks.processResources { dependsOn("generateMainBookstoreDatabaseMigrations") }      // generate before packaging
```

Spring Boot's default `spring.flyway.locations` is `classpath:db/migration`; `application.yml` sets it explicitly
so you can see it. Startup log (real):

```
FlywayExecutor  : Database: jdbc:sqlite:build/sqldelight-bookstore.db?foreign_keys=true&journal_mode=wal&busy_timeout=5000 (SQLite 3.54)
DbMigrate       : Migrating schema "main" to version "1 - create tables"
DbMigrate       : Migrating schema "main" to version "2 - seed data"
DbMigrate       : Migrating schema "main" to version "3 - add reviews"
DbMigrate       : Successfully applied 3 migrations to schema "main", now at version v3 (execution time 00:00.000s)
```

🧒 **Like I'm 5:** one LEGO booklet, two readers. SQLDelight reads it while *building the program* to check
your queries; Flyway reads it while *starting the program* to build the real castle. Because it's the same
booklet, they can never disagree.

⚠️ **Gotcha — two numbering schemes.** Flyway says "now at version v3" (the last *migration* applied).
SQLDelight's `BookstoreDatabase.Schema.version` is `4` (the *database* version after V3; a fresh database is
version 1). Same state, different counting. In a Flyway app you never call `Schema.migrate` — Flyway owns the
schema, SQLDelight only runs queries.

## §2. Giving SQLDelight Spring's connections

```kotlin
@Bean
fun sqlDriver(dataSource: DataSource): SqlDriver = TransactionAwareDataSourceProxy(dataSource).asJdbcDriver()

@Bean
fun bookstoreDatabase(driver: SqlDriver): BookstoreDatabase = createDatabase(driver)
```

- `dataSource` is the HikariCP pool Spring Boot built from `spring.datasource.*` (module 04, lesson 05).
- `asJdbcDriver()` (from `app.cash.sqldelight:jdbc-driver`) adapts any `DataSource` to SQLDelight's `SqlDriver`.
- `TransactionAwareDataSourceProxy` is the important part. Inside an `@Transactional` method, Spring has already
  taken connection **C** from the pool and started a transaction on it. The proxy makes every
  `getConnection()` return that same **C**, and makes `close()` a no-op until Spring commits.

🧒 **Like I'm 5:** Spring is a receptionist who opens one shared notebook for your whole visit. Without the
proxy, SQLDelight would grab its *own* notebook from the shelf, and its writes would be saved even if
Spring later tears your page out.

## §3. Reading: mapper lambdas straight into a view model

```kotlin
data class BookView(val id: Long, val title: String, val author: String, val isbn: String,
                    val priceCents: Int, val stock: Int, val publishedYear: Int?)

fun all(): List<BookView> = books.selectAll(::BookView).executeAsList()
fun byId(id: Long): BookView? = books.selectById(id, ::BookView).executeAsOneOrNull()
```

`selectAll` in `Books.sq` returns 7 columns; `BookView`'s constructor takes 7 parameters of the same types in
the same order, so the constructor reference *is* the mapper (lesson 02, §4). If someone adds a column to the
query, this line stops compiling — exactly the safety we want. Jackson then turns `BookView` into JSON.

## §4. Writing: `@Transactional` around generated queries

`OrderService.placeOrder` is module 04's logic with SQLDelight calls:

```kotlin
@Transactional
fun placeOrder(customerEmail: String, lines: List<OrderLine>): PlacedOrder {
    var total = 0
    for (line in lines) {
        val book = books.selectById(line.bookId).executeAsOneOrNull() ?: throw UnknownBookException(line.bookId)
        val changed = books.takeStock(quantity = line.quantity, id = line.bookId).value
        if (changed == 0L) throw OutOfStockException(line.bookId)        // RuntimeException → ROLLBACK
        total += book.price_cents * line.quantity
    }
    val orderId = orders.insertOrder(customerEmail, OrderStatus.PAID, total, clock.now()).executeAsOne()
    lines.forEach { orders.insertItem(orderId, it.quantity, it.bookId) }
    return PlacedOrder(orderId, total, OrderStatus.PAID)
}
```

Every query above runs on the same connection, in the same transaction, thanks to §2.

⚠️ **Gotcha — one transaction manager at a time.** Inside Spring code, don't use SQLDelight's
`database.transaction {}` (lesson 04). `@Transactional` already controls commit/rollback on that connection;
a second manager calling `setAutoCommit`/`commit` on the same connection leads to confusing half-commits.
Rule: *Spring app → `@Transactional`; plain Kotlin / Android → `database.transaction {}`.*

## §5. Try it

```bash
./gradlew :05-sqldelight:bootRun      # http://localhost:8080
```

All responses below are real.

```bash
curl -s "http://localhost:8080/api/books/1"
```
```json
{"id":1,"title":"Dune","author":"Frank Herbert","isbn":"9780441172719","priceCents":1299,"stock":12,"publishedYear":1965}
```

```bash
curl -s "http://localhost:8080/api/books"        # first two of six shown
```
```json
[
  { "id": 1, "title": "Dune", "author": "Frank Herbert", "isbn": "9780441172719", "priceCents": 1299, "stock": 12, "publishedYear": 1965 },
  { "id": 2, "title": "Dune Messiah", "author": "Frank Herbert", "isbn": "9780593098233", "priceCents": 1199, "stock": 2, "publishedYear": 1969 }
]
```

```bash
curl -s -i "http://localhost:8080/api/books/999"
```
```
HTTP/1.1 404
{"detail":"Book 999 not found","status":404,"title":"Not Found"}
```

A successful order (2 × Pride and Prejudice at 799):

```bash
curl -s -i -X POST "http://localhost:8080/api/orders" -H 'Content-Type: application/json' \
  -d '{"customerEmail":"ada@example.com","lines":[{"bookId":3,"quantity":2}]}'
```
```
HTTP/1.1 201
{"id":1,"totalCents":1598,"status":"PAID"}
```

An order whose *second* line fails (Emma has stock 0). Dune's stock was already decremented by the first
line — and the rollback restores it:

```bash
curl -s -i -X POST "http://localhost:8080/api/orders" -H 'Content-Type: application/json' \
  -d '{"customerEmail":"bob@example.com","lines":[{"bookId":1,"quantity":1},{"bookId":4,"quantity":1}]}'
```
```
HTTP/1.1 409
{"detail":"Book 4 is out of stock","status":409,"title":"Conflict"}
```
```bash
curl -s "http://localhost:8080/api/books/1"      # "stock": 12 — unchanged
```

The database file lives in `05-sqldelight/build/sqldelight-bookstore.db`; delete it to start over.

## §6. The test that proves it

[`SqlDelightSpringIntegrationTest`](../src/test/kotlin/bookstore/SqlDelightSpringIntegrationTest.kt) starts the
whole app on a throw-away database (`application-test.yml` → random temp file) and checks three things:

1. Flyway seeded the books and `GET /api/books/1` returns Dune.
2. A successful order takes stock (`28` Pride and Prejudice left).
3. **`a failing line rolls back SQLDelight writes`** — the test that would fail if you removed
   `TransactionAwareDataSourceProxy`. Try it: replace the bean with `dataSource.asJdbcDriver()` and run
   `./gradlew :05-sqldelight:test` — Dune's stock ends up one lower (real result):
   ```
   AssertionFailedError: rollback must restore Dune's stock ==> expected: <12> but was: <11>
   ```
   SQLDelight took its own pooled connection in auto-commit mode, so `takeStock` committed immediately and
   Spring's rollback had nothing to undo.

🐘 **Postgres note:** only configuration changes — the `postgresql-dialect` for SQLDelight, the Postgres JDBC
driver and URL for Spring. `TransactionAwareDataSourceProxy`, Flyway and `@Transactional` work identically.

🏗️ **In the real world:** the same pattern works for any library that wants a plain `DataSource`
(jOOQ, raw JDBC helpers): wrap Spring's pool in `TransactionAwareDataSourceProxy` and it joins
`@Transactional` transactions.

## Recap

- `.sqm` migrations → SQLDelight checks queries at build time **and** produces plain-SQL copies Flyway runs at startup.
- `TransactionAwareDataSourceProxy(dataSource).asJdbcDriver()` puts SQLDelight on Spring's connection/transaction.
- Map rows straight into API types with constructor-reference mappers.
- In Spring use `@Transactional`, never `database.transaction {}`.
