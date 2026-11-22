# 04 — Spring Boot basics

The bookstore from module 03 becomes a **REST API**: Spring Boot 4.2, Kotlin 2.4, `JdbcClient` with plain SQL,
Flyway migrations, SQLite, Problem Details errors, transactions, tests and Actuator.

Unlike modules 01–03 this module is **one real application**. Each lesson is a guide in [`lessons/`](lessons) that
walks through the part of the app it teaches, with real `curl` output.

```bash
./gradlew :04-spring-boot-basics:bootRun          # http://localhost:8080/api/books
./gradlew :04-spring-boot-basics:test             # app tests + reference solutions
./gradlew :04-spring-boot-basics:exercises        # your exercises (red until solved)
```

Requests to try: [`http/books.http`](http/books.http) (IntelliJ HTTP Client) or the `curl` lines in each lesson.

| ✓ | # | Lesson | Main code | Exercise |
|---|---|--------|-----------|----------|
| ☐ | 01 | [Hello, Spring Boot](lessons/01-hello-spring-boot.md) | [BookstoreApplication.kt](src/main/kotlin/bookstore/BookstoreApplication.kt) | [ex01](src/main/kotlin/exercises/ex01) |
| ☐ | 02 | [REST controllers](lessons/02-rest-controllers.md) | [BookController.kt](src/main/kotlin/bookstore/web/BookController.kt), [Dtos.kt](src/main/kotlin/bookstore/web/Dtos.kt) | [ex02](src/main/kotlin/exercises/ex02) |
| ☐ | 03 | [Validation and errors](lessons/03-validation-and-errors.md) | [ApiExceptionHandler.kt](src/main/kotlin/bookstore/web/ApiExceptionHandler.kt) | [ex03](src/main/kotlin/exercises/ex03) |
| ☐ | 04 | [Configuration](lessons/04-configuration.md) | [application.yml](src/main/resources/application.yml), [BookstoreProperties.kt](src/main/kotlin/bookstore/config/BookstoreProperties.kt) | [ex04](src/main/kotlin/exercises/ex04) |
| ☐ | 05 | [JdbcClient and Flyway](lessons/05-jdbc-client-and-flyway.md) | [BookRepository.kt](src/main/kotlin/bookstore/books/BookRepository.kt), [migrations](src/main/resources/db/migration) | [ex05](src/main/kotlin/exercises/ex05) |
| ☐ | 06 | [Transactions](lessons/06-transactions.md) | [OrderService.kt](src/main/kotlin/bookstore/orders/OrderService.kt) | [ex06](src/main/kotlin/exercises/ex06) |
| ☐ | 07 | [Testing](lessons/07-testing.md) | [src/test/kotlin/bookstore](src/test/kotlin/bookstore) | [ex07](src/test/kotlin/exercises/ex07) |
| ☐ | 08 | [Actuator and operations](lessons/08-actuator-and-operations.md) | [LowStockHealthIndicator.kt](src/main/kotlin/bookstore/ops/LowStockHealthIndicator.kt) | [ex08](src/main/kotlin/exercises/ex08) |

How exercises work here: see [src/main/kotlin/exercises/README.md](src/main/kotlin/exercises/README.md) —
your code serves `/exercises/exNN/…`, the reference solution serves `/solutions/exNN/…`, and one test spec checks both.

Architecture:

```
HTTP ─▶ web/ (controllers, DTOs, errors) ─▶ books/, orders/ (services: rules, @Transactional)
                                              ─▶ repositories (JdbcClient + SQL) ─▶ SQLite (Flyway-managed)
```

Next: `05-sqldelight` — the same queries, but written in `.sq` files and checked by the compiler.
