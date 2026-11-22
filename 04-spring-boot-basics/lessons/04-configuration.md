# Lesson 04 — Configuration and profiles

| | |
|---|---|
| **Goal** | Move every changeable value out of code: `application.yml`, type-safe `@ConfigurationProperties`, profiles, environment variables. Keep secrets out of git. |
| **Needs** | Lessons 01–03 |
| **Time** | ~25 min |
| **Next** | [05 — JdbcClient and Flyway](05-jdbc-client-and-flyway.md) |
| **Exercise** | `src/main/kotlin/exercises/ex04/` |

## §1. Code vs configuration

The same jar runs on your laptop, in CI, in staging and in production. What differs between them is the
database URL, the port, passwords, feature flags and limits. Those values are **configuration**, and they
don't belong in code.

> 🧒 **Like I'm 5:**
> A toy car is the same car everywhere, but the batteries are different: fresh ones at home, a borrowed set
> at grandma's. Configuration is the batteries: you swap them without opening up the car.

## §2. `application.yml`

[`src/main/resources/application.yml`](../src/main/resources/application.yml) holds Spring's own settings
and ours:

```yaml
spring:
  application:
    name: bookstore-api
  datasource:
    url: jdbc:sqlite:build/bookstore.db?foreign_keys=true&journal_mode=wal&busy_timeout=5000
    hikari:
      maximum-pool-size: 4
  mvc:
    problemdetails:
      enabled: true
  threads:
    virtual:
      enabled: true

server:
  shutdown: graceful

bookstore:            # ← our own settings
  name: Kotlin Books
  currency: EUR
  max-page-size: 50
  low-stock-threshold: 3
```

Nested YAML keys are just a tree of dotted names. `server.shutdown`, `spring.datasource.url` and
`bookstore.max-page-size` are what you'll see in docs and error messages.

> 🏗️ **In the real world:** IntelliJ autocompletes Spring properties in `application.yml`, and Boot's
> reference docs list every one ("Common Application Properties").

## §3. Type-safe settings: `@ConfigurationProperties`

[`config/BookstoreProperties.kt`](../src/main/kotlin/bookstore/config/BookstoreProperties.kt):

```kotlin
@ConfigurationProperties(prefix = "bookstore")
data class BookstoreProperties(
    val name: String,
    val currency: String = "EUR",
    val maxPageSize: Int = 50,
    val lowStockThreshold: Int = 3,
)
```

Everything under `bookstore:` is **bound** to this data class at startup, via its constructor:
- **Relaxed binding:** `low-stock-threshold` (YAML), `lowStockThreshold`, and `BOOKSTORE_LOW_STOCK_THRESHOLD`
  (environment variable) all map to `lowStockThreshold`.
- Kotlin **defaults** apply when a key is missing, and **non-null** types without a default (`name`) are
  required, so startup fails if it's missing. That's a good thing ("fail fast").
- Types are converted for you, and a wrong type (`max-page-size: lots`) is a startup error, not a surprise
  at 3 a.m.

`@ConfigurationPropertiesScan` on `BookstoreApplication` finds the class. After that it's an ordinary bean,
so you inject it (module 02):

```kotlin
class BookController(private val service: BookService, private val props: BookstoreProperties) {
    …
    val pageSize = size.coerceAtMost(props.maxPageSize)
}
```

You can have as many properties classes as you like, one per feature. The exercises add a second one bound
to its own prefix:

```yaml
exercises:
  shipping:
    flat-fee-cents: 499
    free-above-cents: 3000
```

`@ConfigurationPropertiesScan("bookstore", "exercises", "solutions")` on the application class finds it in the
exercise packages too.

Compare with sprinkling `@Value("\${bookstore.max-page-size}") val maxPageSize: Int` across classes. That
works too, but the settings end up scattered, untyped as a group, and easy to typo. Prefer one properties
class per feature.

## §4. Where values come from (and who wins)

Spring Boot reads many *property sources*. Later ones override earlier ones:

```text
lowest  ┌─ default values in @ConfigurationProperties (Kotlin defaults)
        ├─ application.yml
        ├─ application-<profile>.yml           (for each active profile)
        ├─ environment variables                BOOKSTORE_LOW_STOCK_THRESHOLD=10
highest └─ command-line arguments              --bookstore.low-stock-threshold=10
```

### Try it

```bash
# Env var (UPPER_SNAKE_CASE, dots → underscores, dashes removed):
BOOKSTORE_LOW_STOCK_THRESHOLD=20 ./gradlew :04-spring-boot-basics:bootRun

# Command line through Gradle:
./gradlew :04-spring-boot-basics:bootRun --args='--bookstore.low-stock-threshold=20 --server.port=9090'
```

Then `curl -s "localhost:8080/api/books/1"` (or port 9090). With a threshold of 20, Dune (stock 12) now counts
as low stock. Real response:

```json
{"id":1,"title":"Dune","author":"Frank Herbert","isbn":"9780441172719","priceCents":1299,"price":"12.99 EUR","stock":12,"lowStock":true}
```

## §5. Profiles

A **profile** is a named set of overrides. `application-test.yml` is only read when the `test` profile is
active. This module's tests activate it with `@ActiveProfiles("test")`, and
[`src/test/resources/application-test.yml`](../src/test/resources/application-test.yml) points every test
context at its own throw-away database file:

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${java.io.tmpdir}/bookstore-test-${random.uuid}.db?foreign_keys=true&journal_mode=wal&busy_timeout=5000
```

`${…}` placeholders refer to other properties or system properties, and `${random.uuid}` is a built-in random
value.

Activate profiles with `--spring.profiles.active=dev` or `SPRING_PROFILES_ACTIVE=dev`. The startup log tells
you which are active: `No active profile set, falling back to 1 default profile: "default"`.

⚠️ **Gotcha:** don't put a file named `application.yml` in `src/test/resources`. It doesn't *merge* with the
main one, it **replaces** it on the test classpath, and half your settings silently vanish. Use
`application-test.yml` plus `@ActiveProfiles("test")` instead.

## §6. 🔐 Secrets

Database passwords, API keys and signing secrets:
- **Never** commit them in `application.yml` (git history is forever, and gets cloned).
- Read them from the environment: `password: ${DB_PASSWORD}` in YAML, with `DB_PASSWORD` set by your
  deployment (Docker or Kubernetes secrets, a cloud secret manager).
- For local development, use an untracked `application-local.yml` (already in this repo's `.gitignore`) or
  env vars in your IntelliJ run configuration.

Startup fails if `DB_PASSWORD` is missing, and that's what you want. Don't give secrets a default value.

## §7. Even the clock is configuration

[`config/TimeConfig.kt`](../src/main/kotlin/bookstore/config/TimeConfig.kt):

```kotlin
@Configuration(proxyBeanMethods = false)
class TimeConfig {
    @Bean
    fun clock(): Clock = Clock.System   // kotlin.time.Clock
}
```

`BookService` and `OrderService` take a `Clock` in their constructor instead of calling `Clock.System.now()`
themselves (module 02, lesson 02: time is a dependency). Production gets the real clock. Tests register a
`@Primary` fixed clock, so `order.createdAt` is always `2026-06-01T12:00:00Z` (lesson 07).

> 🏗️ **In the real world:** this is the "12-factor app" rule *"store config in the environment"*. One
> artifact, many environments, and no rebuild to change a setting.

## Recap

- Settings live in `application.yml`; Spring's own and yours look the same.
- Bind your settings to a `@ConfigurationProperties` data class: typed, defaulted, validated at startup, injectable.
- Precedence: defaults < `application.yml` < profile files < env vars < command line.
- Profiles (`application-test.yml`, `@ActiveProfiles`) switch whole groups of settings.
- Secrets come from the environment, never from git.
