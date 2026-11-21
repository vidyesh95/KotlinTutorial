# 03 — SQL fundamentals

SQL from zero on **SQLite** (in-process, nothing to install), with a 🐘 *Postgres note* in every lesson so the
knowledge transfers directly to PostgreSQL.

- Lessons 01–10 are real `.sql` files in `src/main/resources/sql/` — readable on their own and runnable in any SQLite
  tool (`sqlite3`, DB Browser, IntelliJ's Database tab). Each has a tiny Kotlin runner; press ▶ and you get a
  transcript of every query with its result table (also shown in the runner's `// Output:` block).
- Lessons 11–14 are Kotlin + JDBC: the bridge to Spring's `JdbcClient` in module 04.
- Most lessons and all query exercises use one shared dataset: [`bookstore.sql`](src/main/resources/sql/bookstore.sql)
  (authors, books, a category tree, customers, orders, order items, reviews — with deliberate edge cases).

```bash
./gradlew :03-sql-fundamentals:lesson -Plesson=lesson04_joins.JoinsKt
./gradlew :03-sql-fundamentals:exercises --tests 'exercises.ex04*'    # you write SQL strings; tests run them on SQLite
./gradlew :03-sql-fundamentals:test
```

| ✓ | # | Lesson | Exercise |
|---|---|--------|----------|
| ☐ | 01 | [Tables and types](src/main/resources/sql/lesson01_tables_and_types.sql) · [runner](src/main/kotlin/lesson01_tables_and_types/TablesAndTypes.kt) | [ex01](src/main/kotlin/exercises/ex01/Ex01.kt) |
| ☐ | 02 | [Constraints and keys](src/main/resources/sql/lesson02_constraints_and_keys.sql) · [runner](src/main/kotlin/lesson02_constraints_and_keys/ConstraintsAndKeys.kt) | [ex02](src/main/kotlin/exercises/ex02/Ex02.kt) |
| ☐ | 03 | [CRUD: Create, Read, Update, Delete](src/main/resources/sql/lesson03_crud.sql) · [runner](src/main/kotlin/lesson03_crud/Crud.kt) | [ex03](src/main/kotlin/exercises/ex03/Ex03.kt) |
| ☐ | 04 | [Joins](src/main/resources/sql/lesson04_joins.sql) · [runner](src/main/kotlin/lesson04_joins/Joins.kt) | [ex04](src/main/kotlin/exercises/ex04/Ex04.kt) |
| ☐ | 05 | [Aggregation: COUNT, SUM, AVG, GROUP BY, HAVING](src/main/resources/sql/lesson05_aggregation.sql) · [runner](src/main/kotlin/lesson05_aggregation/Aggregation.kt) | [ex05](src/main/kotlin/exercises/ex05/Ex05.kt) |
| ☐ | 06 | [Subqueries and CTEs (WITH, including recursive)](src/main/resources/sql/lesson06_subqueries_and_ctes.sql) · [runner](src/main/kotlin/lesson06_subqueries_and_ctes/SubqueriesAndCtes.kt) | [ex06](src/main/kotlin/exercises/ex06/Ex06.kt) |
| ☐ | 07 | [Window functions](src/main/resources/sql/lesson07_window_functions.sql) · [runner](src/main/kotlin/lesson07_window_functions/WindowFunctions.kt) | [ex07](src/main/kotlin/exercises/ex07/Ex07.kt) |
| ☐ | 08 | [Indexes and query plans](src/main/resources/sql/lesson08_indexes_and_query_plans.sql) · [runner](src/main/kotlin/lesson08_indexes_and_query_plans/IndexesAndQueryPlans.kt) | [ex08](src/main/kotlin/exercises/ex08/Ex08.kt) |
| ☐ | 09 | [Upserts, JSON and dates](src/main/resources/sql/lesson09_upsert_json_and_dates.sql) · [runner](src/main/kotlin/lesson09_upsert_json_and_dates/UpsertJsonAndDates.kt) | [ex09](src/main/kotlin/exercises/ex09/Ex09.kt) |
| ☐ | 10 | [Normalization and schema design](src/main/resources/sql/lesson10_normalization_and_design.sql) · [runner](src/main/kotlin/lesson10_normalization_and_design/NormalizationAndDesign.kt) | [ex10](src/main/kotlin/exercises/ex10/Ex10.kt) |
| ☐ | 11 | [JDBC from Kotlin: talking to a database from code](src/main/kotlin/lesson11_jdbc_from_kotlin/JdbcFromKotlin.kt) | [ex11](src/main/kotlin/exercises/ex11/Ex11.kt) |
| ☐ | 12 | [Transactions: all or nothing](src/main/kotlin/lesson12_transactions/Transactions.kt) | [ex12](src/main/kotlin/exercises/ex12/Ex12.kt) |
| ☐ | 13 | [The N+1 query problem](src/main/kotlin/lesson13_n_plus_one/NPlusOne.kt) | [ex13](src/main/kotlin/exercises/ex13/Ex13.kt) |
| ☐ | 14 | [Migrations: versioning your database schema](src/main/kotlin/lesson14_migrations/Migrations.kt) | [ex14](src/main/kotlin/exercises/ex14/Ex14.kt) |

Next: `04-spring-boot-basics` — the same bookstore behind a REST API, with `JdbcClient` and Flyway migrations.
