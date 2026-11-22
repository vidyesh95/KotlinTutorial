# Lesson 05 — Data access: DataSource, Flyway and JdbcClient

| | |
|---|---|
| **Goal** | Connect Spring Boot to SQLite through a connection pool, version the schema with Flyway, and write repositories with `JdbcClient`: plain SQL from module 03, minus the JDBC boilerplate. |
| **Needs** | Lessons 01–04; module 03 (especially lessons 11 JDBC and 14 migrations) |
| **Time** | ~45 min |
| **Next** | [06 — Transactions](06-transactions.md) |
| **Exercise** | `src/main/kotlin/exercises/ex05/` |

## §1. DataSource and the connection pool

In module 03, lesson 11 every function did `DriverManager.getConnection(...)` and closed the connection
afterwards. Opening a real database connection is slow: a network handshake, authentication, memory on the
server. A web app serving 500 requests a second can't open 500 connections a second.

A **connection pool** opens a few connections once and **lends** them out:

> 🧒 **Like I'm 5:**
> A taxi rank. Four taxis wait at the station. A passenger (a request) takes a taxi, rides, and the taxi
> comes back to the rank for the next passenger. Nobody builds a new car for every trip. If all four are
> out, the next passenger waits a moment at the rank.

Spring Boot sees the SQLite driver on the classpath plus `spring.datasource.url` and creates a `DataSource`
backed by **HikariCP**, the default pool. You saw it in the startup log:

```text
HikariPool-1 - Starting...
HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@25589735
HikariPool-1 - Start completed.
```

[`application.yml`](../src/main/resources/application.yml):

```yaml
spring:
  datasource:
    url: jdbc:sqlite:build/bookstore.db?foreign_keys=true&journal_mode=wal&busy_timeout=5000
    hikari:
      maximum-pool-size: 4
```

The URL parameters are SQLite **pragmas**, applied to every pooled connection:

| Parameter | Why (module 03) |
|---|---|
| `foreign_keys=true` | SQLite does **not** enforce foreign keys unless asked, per connection (lesson 02) |
| `journal_mode=wal` | write-ahead log: readers don't block the writer (lesson 12) |
| `busy_timeout=5000` | wait up to 5 s for the write lock instead of failing immediately with `SQLITE_BUSY` (lesson 12) |

**Why only 4 connections?** SQLite allows **one writer at a time**, so more connections don't give more write
throughput, just more waiting. For Postgres you'd typically use about 10–20 per app instance.

## §2. Flyway: the schema is code

Module 03, lesson 14 built a 60-line migration runner with a `schema_history` table and checksums. **Flyway
is that runner, industrial-strength.** Same idea, same file naming:

```text
src/main/resources/db/migration/
  V1__create_tables.sql     ← version 1, description "create tables"
  V2__seed_data.sql         ← version 2, description "seed data"
  V3__reviews.sql           ← version 3, adds a `reviews` table (used by exercise 05)
```

At startup Flyway:
1. creates `flyway_schema_history` if missing;
2. checks the checksums of already-applied migrations (an edited applied file means startup fails);
3. runs the pending ones in version order, each in a transaction;
4. records them.

From the real startup log:

```text
Creating Schema History table "main"."flyway_schema_history" ...
Current version of schema "main": << Empty Schema >>
Migrating schema "main" to version "1 - create tables"
Migrating schema "main" to version "2 - seed data"
Migrating schema "main" to version "3 - reviews"
Successfully applied 3 migrations to schema "main", now at version v3
```

Start the app again and Flyway does nothing: everything is already applied.

### Try it

```bash
curl -s "localhost:8080/actuator/flyway"
```

Real response, shortened:

```json
{"contexts":{"bookstore-api":{"flywayBeans":{"flyway":{"migrations":[
  {"script":"V1__create_tables.sql","version":"1","description":"create tables","checksum":424429405,"state":"SUCCESS"},
  {"script":"V2__seed_data.sql","version":"2","description":"seed data","checksum":-350163689,"state":"SUCCESS"},
  {"script":"V3__reviews.sql","version":"3","description":"reviews","checksum":1326941532,"state":"SUCCESS"}
]}}}}}
```

⚠️ **Gotchas:**
- `V3__reviews.sql` shows the right way to evolve the schema: it's a NEW file. Editing V1 to add the table
  would break every database where V1 had already run.
- **Never edit a migration that has run anywhere** (a teammate's laptop, CI, production). Its checksum is
  recorded, and Flyway refuses to start: *"Migration checksum mismatch"*. To change the schema, add `V3__….sql`.
- Two underscores: `V3__add_reviews.sql`. With one underscore, the file is silently ignored.
- Locally, during early development, it's fine to delete `build/bookstore.db` and start fresh.

> 🏗️ **In the real world:** migrations run on deploy, before the new code serves traffic. That's why
> changes must be backwards-compatible: add a nullable column first, deploy, backfill, then add constraints
> (the expand/contract pattern from module 03, lesson 14).

## §3. `JdbcClient`: SQL without the ceremony

[`books/BookRepository.kt`](../src/main/kotlin/bookstore/books/BookRepository.kt). Spring Boot auto-configures
a `JdbcClient` bean, and we inject it:

```kotlin
@Repository
class BookRepository(private val jdbc: JdbcClient) {

    fun findById(id: Long): Book? =
        jdbc.sql("$selectBooks WHERE b.id = :id")      // named parameter :id
            .param("id", id)                          // bound safely, so no SQL injection (module 03, lesson 11)
            .query { rs, _ -> rs.toBook() }           // row mapper: ResultSet row → Book
            .optional()                               // 0 or 1 rows → Optional<Book>
            .orElse(null)                             // → Kotlin's Book?
}
```

The fluent chain is always **sql → params → what to do**:

| Step | Options |
|---|---|
| `.sql("…")` | the SQL, with `:named` parameters (or `?` positional) |
| `.param("name", value)` | bind a value; `null` binds SQL NULL |
| `.query(Long::class.java)` | map a single column to a simple type |
| `.query { rs, rowNum -> … }` | map each row with a lambda (a `RowMapper`) |
| `.query(Book::class.java)` | map columns to constructor parameters by name (works with data classes) |
| then `.list()` / `.single()` / `.optional()` | many rows / exactly one row (else exception) / zero or one |
| `.update()` | for INSERT/UPDATE/DELETE: returns the number of rows changed |

More patterns from the repository:

```kotlin
// RETURNING (module 03, lesson 03) gives back the generated id in one round trip
fun insert(book: NewBook, now: Instant): Long =
    jdbc.sql("INSERT INTO books (…) VALUES (:title, :authorId, …) RETURNING id")
        .param("title", book.title)
        …
        .query(Long::class.java)
        .single()

// update() returns rows changed: 0 means "no such book", and the service turns that into a 404
fun delete(id: Long): Int = jdbc.sql("DELETE FROM books WHERE id = :id").param("id", id).update()

// PATCH semantics in SQL: NULL parameter = keep the current value
SET price_cents = coalesce(:priceCents, price_cents)
```

Compare with module 03, lesson 11 (raw JDBC). There is no `Connection`, no `PreparedStatement`, no `use {}`,
no `rs.next()` loop. JdbcClient:
- borrows a pooled connection and returns it (even on exceptions);
- **joins the current transaction** if one is open (lesson 06), which raw JDBC can't do by itself;
- translates `SQLException` into Spring's `DataAccessException` hierarchy (next section).

Timestamps are stored as ISO-8601 TEXT (`now.toString()`, `Instant.parse(...)`), the SQLite convention from
module 03, lesson 09.

> 🧒 **Like I'm 5:** raw JDBC is cooking with a campfire: gather wood, light it, put it out. JdbcClient is a
> stove: you still cook the same food (SQL), but the fire is handled for you.

## §4. Exceptions: `DataAccessException`

`@Repository` (instead of plain `@Component`) also turns on **exception translation**. Vendor-specific
`SQLException`s become meaningful Spring exceptions:

| Database says | You get |
|---|---|
| UNIQUE / PRIMARY KEY violated | `DuplicateKeyException` |
| FOREIGN KEY / CHECK / NOT NULL violated | `DataIntegrityViolationException` |
| `single()` found 0 rows | `EmptyResultDataAccessException` |
| busy, lock timeout | `PessimisticLockingFailureException` / `CannotAcquireLockException` |

They're all `RuntimeException`s, so a failure inside `@Transactional` rolls back (lesson 06).

Our `BookService.create` checks `existsByIsbn` first to return a friendly 409. The `UNIQUE` constraint on
`books.isbn` is still the **real** guarantee: two simultaneous requests can both pass the check, and only the
database can stop the second one. Rule: **validate in code for good messages, enforce in the database for
correctness.**

## §5. Which data-access tool?

| | Raw JDBC (module 03) | **JdbcClient** (this module) | **SQLDelight** (module 05) | JPA / Hibernate |
|---|---|---|---|---|
| You write | SQL + all plumbing | SQL | SQL in `.sq` files | Kotlin classes, generated SQL |
| SQL checked at compile time | ❌ | ❌ | ✅ (like Rust's sqlx) | n/a |
| Mapping | manual | lambda or by name | generated | automatic |
| Transactions | manual | Spring `@Transactional` | Spring `@Transactional` | Spring `@Transactional` |
| Hidden queries / N+1 risk | none | none | none | **high** (lazy loading) |
| Learning curve | low | low | low–medium | high (sessions, dirty checking, proxies) |

**Why this tutorial skips JPA:** it hides SQL. That's productive once you're an expert, but confusing while
learning. Lazy loading causes N+1 queries (module 03, lesson 13), and entity rules fight Kotlin's immutable
data classes. With SQL in front of you, you always know what hits the database. You'll still meet JPA at
work; this foundation makes it much easier to understand.

## §6. 🐘 Switching to Postgres

The code barely changes:
1. Driver: replace `libs.sqlite.jdbc` with `org.postgresql:postgresql` (version from the Boot BOM).
2. URL: `spring.datasource.url: jdbc:postgresql://localhost:5432/bookstore`, plus `username` and
   `password: ${DB_PASSWORD}`.
3. Remove the SQLite pragmas; Postgres always enforces foreign keys, and MVCC replaces WAL tuning.
4. Migrations: `INTEGER PRIMARY KEY` → `bigint GENERATED ALWAYS AS IDENTITY`, TEXT timestamps → `timestamptz`,
   and add `flyway-database-postgresql`.
5. `RETURNING`, `coalesce`, named parameters and `JdbcClient` code all stay the same.

## Recap

- `DataSource` = a HikariCP pool of connections, auto-configured from `spring.datasource.*`. Keep it small
  for SQLite.
- Flyway runs `db/migration/V<n>__<desc>.sql` once each, in order, tracked in `flyway_schema_history`.
  Never edit applied migrations.
- `JdbcClient`: `.sql().param().query().list/single/optional()` or `.update()`. It's real SQL that is
  transaction-aware, with translated exceptions.
- Enforce rules with constraints in the database, and check them in code for nice messages.
