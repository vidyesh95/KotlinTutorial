# 05 — SQLDelight: SQL checked by the compiler

> 🧒 A spell-checker for your SQL that runs before your program even exists.

In module 04, SQL lived in Kotlin strings: a typo in a column name compiled fine and failed at runtime.
Here SQL lives in **`.sq` files**. SQLDelight reads the schema from your **migration files** (`.sqm`), checks every
query against it **at compile time**, and generates type-safe Kotlin functions and data classes —
the same idea as Rust's `sqlx::query!`, without needing a live database during the build.

```
src/main/sqldelight/bookstore/db/
  V1__create_tables.sqm   ┐ schema (SQLDelight reads them at compile time,
  V2__seed_data.sqm       │ Flyway runs cleaned copies at runtime — ONE source of truth)
  V3__add_reviews.sqm     ┘
  Books.sq, Authors.sq, Orders.sq, Reviews.sq  → bookstore.db.BooksQueries, …
```

```bash
./gradlew :05-sqldelight:lesson -Plesson=lesson01_why_sqldelight.WhySqlDelightKt
./gradlew :05-sqldelight:bootRun                     # Spring app on :8080 (lesson 05)
./gradlew :05-sqldelight:verifySqlDelightMigration   # checks migrations replay into the expected schema
./gradlew :05-sqldelight:exercises --tests 'exercises.ex02*'
```

Generated code lands in `build/generated/sqldelight/` — open it, it's readable Kotlin.

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 01 | [Why SQLDelight](src/main/kotlin/lesson01_why_sqldelight/WhySqlDelight.kt) | [ex01](src/main/sqldelight/exercises/ex01/Ex01Exercise.sq) — your first `.sq` queries |
| ☐ | 02 | [Queries and types](src/main/kotlin/lesson02_queries_and_types/QueriesAndTypes.kt) | [ex02 SQL](src/main/sqldelight/exercises/ex02/Ex02Exercise.sq) + [ex02 Kotlin](src/main/kotlin/exercises/ex02/Ex02.kt) — params, `IN ?`, adapters |
| ☐ | 03 | [Migrations](src/main/kotlin/lesson03_migrations/Migrations.kt) | [ex03](src/main/kotlin/exercises/ex03/Ex03.kt) — schema versions, a tiny migrator |
| ☐ | 04 | [Transactions and Flows](src/main/kotlin/lesson04_transactions_and_flows/TransactionsAndFlows.kt) | [ex04](src/main/kotlin/exercises/ex04/Ex04.kt) — rollback, reactive queries |
| ☐ | 05 | [Spring integration](lessons/05-spring-integration.md) — code in [src/main/kotlin/bookstore](src/main/kotlin/bookstore) | [ex05](src/main/kotlin/exercises/ex05/Ex05.kt) — an atomic service |
| ☐ | 06 | [JDBC vs JdbcClient vs SQLDelight vs sqlx vs jOOQ vs JPA](lessons/06-comparison.md) | — |

Exercise convention: [src/main/sqldelight/exercises/README.md](src/main/sqldelight/exercises/README.md) — exercise
queries are placeholders with the final signature (so everything compiles) that return nothing until you write the SQL.

Next: `06-spring-security-auth` — sessions and passwords with a better-auth-style schema.
