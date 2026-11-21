# Kotlin → Backend Tutorial <img src="https://github.com/vidyesh95/KotlinTutorial/assets/13806493/29c11d34-f2ad-45f1-b70f-14db2215c64a" alt="drawing" width="24"/>

A hands-on path from **"what is a variable?"** to **a secured, AI-powered Spring Boot backend**:

Kotlin 2.4 → Dependency Injection → SQL → Spring Boot → SQLDelight (compile-checked SQL, like Rust's sqlx)
→ Spring Security (better-auth-style schema) → Spring AI (Ollama) → real backend concepts.

The database is **SQLite** everywhere (zero setup). Every SQL lesson carries a 🐘 *Postgres note* so the skills
transfer directly when you move to PostgreSQL.

Read [docs/HOW-TO-STUDY.md](docs/HOW-TO-STUDY.md) first — it explains the lesson format and the emoji legend.

## Roadmap

Tick the boxes as you go.

| # | Module | You will learn | Status |
|---|--------|----------------|--------|
| 01 | [`01-kotlin-language`](01-kotlin-language) | 35 lessons: types, null safety, functions, OOP, **generics (variance, reified)**, context parameters, coroutines & Flow, DSLs, time/UUID, regex, files, testing | ✅ |
| 02 | [`02-dependency-injection`](02-dependency-injection) | DI explained like you're 5 (the lunchbox), lifetimes, anti-patterns, build your own container, then Spring's `ApplicationContext` | ✅ |
| 03 | [`03-sql-fundamentals`](03-sql-fundamentals) | SQL from zero on SQLite (🐘 Postgres notes): keys, joins, aggregation, CTEs, window functions, indexes & query plans, upsert/JSON/dates, normalization, JDBC, transactions, N+1, migrations | ✅ |
| 04 | [`04-spring-boot-basics`](04-spring-boot-basics) | Spring Boot 4.2 bookstore API: REST, validation & Problem Details, config & profiles, `JdbcClient` + Flyway on SQLite, `@Transactional`, testing (MockMvc, slices), Actuator | ✅ |
| 05 | `05-sqldelight` | Write SQL in `.sq` files, the compiler checks it and generates typed Kotlin | ⏳ |
| 06 | `06-spring-security-auth` | Sessions, password hashing, filter chain, better-auth `user/session/account/verification` schema | ⏳ |
| 07 | `07-spring-ai` | `ChatClient`, structured output, tool calling, chat memory, RAG — with a local Ollama model | ⏳ |
| 08 | `08-capstone` | Everything together + pagination, idempotency, caching, background jobs, observability | ⏳ |
| — | [`aoc`](aoc) | Advent of Code playground for extra practice | ✅ |

Extra reference: [docs/kotlin-keywords-and-operators.md](docs/kotlin-keywords-and-operators.md).

## Requirements

- **JDK 25** (IntelliJ / Android Studio's bundled JBR works).
- Nothing else: the Gradle wrapper (`./gradlew`) downloads Gradle and all libraries.
- From module 07: [Ollama](https://ollama.com) for running an LLM locally.

## Commands

```bash
./gradlew build                                   # compile everything + run all non-exercise tests
./gradlew :01-kotlin-language:lesson -Plesson=<fully.qualified.FileKt>   # run one lesson's main()
./gradlew :01-kotlin-language:exercises           # check YOUR homework answers
./gradlew :aoc:lesson -Plesson=aoc.InputKt        # run an Advent of Code file
```

In IntelliJ IDEA: *File → Open* this folder (it imports as a Gradle project), then click the green ▶ next to any
`fun main()`.

## Versions

| Tool | Version |
|------|---------|
| Kotlin | 2.4.21 |
| Gradle | 9.9.2 |
| JDK | 25 |
| Spring Boot | 4.2.0 |
| Spring AI | 2.1.0 |
| SQLDelight | 2.4.1 |
| SQLite JDBC | 3.54.1.0 |

All versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

Official docs: [Kotlin](https://kotlinlang.org/docs/home.html) ·
[Spring Boot](https://docs.spring.io/spring-boot/) · [Spring AI](https://docs.spring.io/spring-ai/reference/) ·
[SQLDelight](https://sqldelight.github.io/sqldelight/) · [SQLite](https://sqlite.org/lang.html)
