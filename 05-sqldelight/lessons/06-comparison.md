# SQLDelight Lesson 06 — Choosing a data-access approach

**Goal:** compare everything you've used (and a few you'll meet) so you can choose deliberately.
**Needs:** modules 03–05 · **Time:** ~20 min · **Next:** module 06 — Spring Security

You have now written the same bookstore queries four ways:

| | Where it came up |
|---|---|
| Raw JDBC | module 03, lessons 11–14 |
| Spring `JdbcClient` | module 04 |
| SQLDelight | this module |
| (Rust sqlx) | the inspiration for this module |

## §1. The big table

| | **JDBC** | **JdbcClient** | **SQLDelight** | **Rust sqlx** | **jOOQ** | **Exposed** | **JPA / Hibernate** |
|---|---|---|---|---|---|---|---|
| You write | SQL strings | SQL strings | SQL in `.sq` files | SQL strings in `query!` | Kotlin DSL that *is* SQL | Kotlin DSL or DAO | Annotated entity classes (+ JPQL) |
| SQL checked at compile time | ❌ | ❌ | ✅ against migrations | ✅ against a live DB / cache | ✅ against generated schema classes | ⚠️ typed DSL, not real SQL | ❌ (JPQL errors at startup/runtime) |
| Result types | manual `ResultSet` | row-mapper lambdas | generated data classes / mappers | generated records | generated records / your classes | DSL rows / DAO entities | managed entities |
| Boilerplate | high | low | very low | very low | low | low | lowest — until it isn't |
| Learning curve | low (but tedious) | low | low–medium | medium | medium | medium | **high** (sessions, lazy loading, dirty checking, caches) |
| N+1 risk | only if you write it | only if you write it | only if you write it | only if you write it | low | medium (DAO) | **high** (lazy associations) |
| Migrations | your own / Flyway | Flyway / Liquibase | `.sqm` → Flyway copies | `sqlx migrate` | Flyway, then codegen from DB | Flyway or built-in | Flyway (don't use `ddl-auto` in prod!) |
| Transactions in Spring | manual | `@Transactional` | `@Transactional` via proxy (lesson 05) | — | `@Transactional` | its own `transaction {}` / Spring integration | `@Transactional` |
| Multiplatform (Android/iOS) | ❌ | ❌ | ✅ | ❌ | ❌ | ❌ | ❌ |

🧒 **Like I'm 5:** three ways to order food.
- **SQL strings (JDBC/JdbcClient):** you shout your order across the room. Fast, and usually fine — until someone
  mishears "titel".
- **SQLDelight / sqlx:** you write the order on a form, and someone checks every item against the menu *before*
  the kitchen opens.
- **JPA:** a butler orders for you based on what you "probably want". Lovely — until he fetches every dish
  separately (N+1) or reheats yesterday's soup (stale persistence context).

## §2. When to pick which

- **Learning SQL / small tools / one-off scripts:** `JdbcClient` (or plain JDBC). Nothing hides the SQL.
- **A service whose data layer you want to *trust*:** SQLDelight (or jOOQ). Refactor a column and the compiler
  lists every query you broke. This is the closest thing to sqlx in Kotlin.
- **Very dynamic queries** (search screens with 15 optional filters): jOOQ's DSL or JdbcClient with carefully built
  SQL. SQLDelight queries are static; you'd write several, or use `(:filter IS NULL OR col = :filter)` tricks.
- **Kotlin Multiplatform app sharing a database layer:** SQLDelight, no contest.
- **Large CRUD-heavy enterprise app, team already fluent in it:** JPA/Hibernate — but learn its pitfalls first
  (N+1, `open-in-view`, lazy-loading exceptions, flush timing). This tutorial deliberately skips it so you learn
  the SQL that JPA generates for you.
- **Kotlin-only DSL fans:** Exposed (from JetBrains).

You can mix: many teams use JdbcClient or SQLDelight for most things and drop to raw JDBC for bulk loads.

## §3. 🐘 Moving this module to PostgreSQL

1. **SQLDelight dialect** — in `build.gradle.kts`: `dialect("app.cash.sqldelight:postgresql-dialect:2.4.1")`.
   Queries are now checked as *Postgres* SQL.
2. **Types in migrations** become real Postgres types (and fewer adapters are needed):

   | SQLite here | Postgres |
   |---|---|
   | `INTEGER PRIMARY KEY` | `BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY` (or `UUID` with `gen_random_uuid()`) |
   | `TEXT AS Instant` | `TIMESTAMPTZ` (maps to `OffsetDateTime`; adapt to `Instant`) |
   | `INTEGER AS Boolean` | `BOOLEAN` |
   | `TEXT AS OrderStatus` | `TEXT` + `CHECK`, or a Postgres `ENUM` type |
   | JSON in `TEXT` | `JSONB` (+ GIN index) |
   | `TEXT` ids | `UUID` |

3. **SQL differences you'd fix in `.sq` files:** `||` works in both; `LIKE` is case-sensitive in Postgres
   (use `ILIKE`); `group_concat` → `string_agg`; booleans are real `true/false`. `RETURNING` works the same.
4. **Spring:** `org.postgresql:postgresql` driver, `spring.datasource.url=jdbc:postgresql://localhost:5432/bookstore`,
   username/password from environment variables, Flyway as before. Remove the SQLite URL pragmas and raise the pool
   size — Postgres handles many concurrent writers.
5. **Tests:** use Testcontainers (`spring-boot-testcontainers`) to start a real Postgres in Docker per test run.

## Recap

- Strings (JDBC/JdbcClient) are simple but unchecked; SQLDelight/sqlx/jOOQ check SQL before it runs.
- SQLDelight = SQL-first, compile-time checked against migrations, generated Kotlin, multiplatform.
- JPA hides SQL — powerful, but N+1 and lifecycle surprises; learn SQL first (you just did).
- Switching to Postgres = dialect + types in migrations + driver/URL; the architecture stays the same.
