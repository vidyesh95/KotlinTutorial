# Lesson 01 — Hello, Spring Boot

| | |
|---|---|
| **Goal** | Understand what Spring Boot adds on top of the plain Spring container from module 02, read the startup log, and run the bookstore API. |
| **Needs** | Module 02 (DI, Spring's `ApplicationContext`), module 03 (SQL) |
| **Time** | ~25 min |
| **Next** | [02 — REST controllers](02-rest-controllers.md) |
| **Exercise** | `src/main/kotlin/exercises/ex01/` |

## §1. What Spring Boot is (and isn't)

In module 02, lesson 06 you built a Spring container by hand: you listed the classes, wrote `@Bean` methods,
and called `AnnotationConfigApplicationContext(...)`. A real web app needs much more: a web server, a JSON
converter, a database connection pool, a migration tool, error pages, health checks… Each of those is a
bean, and each one needs configuring.

**Spring Boot is Spring plus sensible defaults.** It looks at what is on your classpath and creates those
beans for you ("auto-configuration"), and you only override what you care about.

> 🧒 **Like I'm 5:**
> Plain Spring is like building a house: you get the bricks, the pipes and the wires, and you put
> everything together yourself. Spring Boot is a **furnished apartment**: the fridge is plugged in, the
> lights work, the bed is made. You move in on day one. Don't like the sofa? Swap it. You don't have to
> rebuild the kitchen to do that.

Boot is **not** a different framework. Every Boot app is a Spring app: everything from module 02
(`@Component`, constructor injection, `@Bean`, scopes) still applies.

## §2. The whole app starts in one file

[`src/main/kotlin/bookstore/BookstoreApplication.kt`](../src/main/kotlin/bookstore/BookstoreApplication.kt):

```kotlin
@SpringBootApplication(scanBasePackages = ["bookstore", "exercises", "solutions"])
@ConfigurationPropertiesScan("bookstore", "exercises", "solutions")
class BookstoreApplication

fun main(args: Array<String>) {
    runApplication<BookstoreApplication>(*args)
}
```

`@SpringBootApplication` is three annotations in one:

| Part | Meaning | Module 02 equivalent |
|---|---|---|
| `@SpringBootConfiguration` | this class is a `@Configuration` (it may hold `@Bean` methods) | `@Configuration` |
| `@EnableAutoConfiguration` | "look at my classpath and configure what you find" | (nothing: you did it by hand) |
| `@ComponentScan` | find `@Component`, `@Service`, `@Repository` and `@RestController` classes in the scanned packages and below | listing classes in `AnnotationConfigApplicationContext(...)` |

`runApplication<BookstoreApplication>(*args)` is a Kotlin helper with a `reified` type parameter (module 01,
lesson 27). It creates the `ApplicationContext`, starts the embedded web server, and keeps running until you
stop it.

By default, scanning starts at the main class's package (`bookstore`). This module also lists `exercises`
and `solutions` in `scanBasePackages` (and in `@ConfigurationPropertiesScan`), so the exercise controllers and
services become beans too.

⚠️ **Gotcha:** component scanning only covers the scanned packages **and their sub-packages**. A `@Service`
in any other package (say `com.other`) is silently ignored, and you get "No qualifying bean of type…" when
something needs it. Keep the application class at the root of your packages (`bookstore`), with features
below it (`bookstore.books`, `bookstore.web`, …), or list extra packages explicitly like we do.

## §3. Auto-configuration: "if X is on the classpath, create Y"

There's nothing magic about it. Boot ships hundreds of `@Configuration` classes, each guarded by conditions:

```text
JDBC driver + spring-jdbc on the classpath   → create a DataSource (HikariCP pool) from spring.datasource.*
DataSource exists                            → create JdbcClient, JdbcTemplate, a transaction manager
Flyway on the classpath + a DataSource       → run db/migration/*.sql at startup
spring-webmvc + Tomcat on the classpath      → start Tomcat on port 8080 and register your @RestControllers
Jackson on the classpath                     → JSON conversion for request and response bodies
```

Each one backs off as soon as **you** define the same bean. That's how you customise Boot: you don't fight
the defaults, you replace them.

> 🏗️ **In the real world:** to see which auto-configurations applied and why, start the app with
> `--debug`. It prints a "CONDITIONS EVALUATION REPORT".

## §4. Starters and the BOM: dependencies without version headaches

[`build.gradle.kts`](../build.gradle.kts) lists *starters*. A starter is a curated bundle of libraries plus
their auto-configuration:

```kotlin
implementation("org.springframework.boot:spring-boot-starter-webmvc")      // REST controllers + embedded Tomcat
implementation("org.springframework.boot:spring-boot-starter-validation")  // @NotBlank, @Positive …
implementation("org.springframework.boot:spring-boot-starter-jdbc")        // DataSource, HikariCP, JdbcClient, @Transactional
implementation("org.springframework.boot:spring-boot-starter-flyway")      // versioned SQL migrations
implementation("org.springframework.boot:spring-boot-starter-actuator")    // /actuator/health, metrics, info
```

None of them has a version number. Our convention plugin
([`build-logic/src/main/kotlin/tutorial.spring-boot.gradle.kts`](../../build-logic/src/main/kotlin/tutorial.spring-boot.gradle.kts))
imports Spring Boot's **BOM** (Bill of Materials), a list of hundreds of libraries at versions known to work
together. Boot 4.2.0 picks Spring Framework 7.1, Tomcat 11, Jackson 3, HikariCP 7, Flyway 12, and also
Kotlin 2.4.21 itself.

🆕 **Spring Boot 4:** starters are now *modular*. Boot 3 had one big `spring-boot-starter-web` and one huge
autoconfigure jar. Boot 4 splits these into focused modules (`spring-boot-starter-webmvc`,
`spring-boot-starter-flyway`, `spring-boot-starter-webmvc-test`, …). The jars are smaller, startup is
faster, and you get only what you asked for.

The same plugin applies `kotlin("plugin.spring")`. Kotlin classes are `final` by default, but Spring needs to
subclass some beans (proxies for `@Configuration` and `@Transactional`, see lesson 06). The plugin opens any
class carrying a Spring annotation.

## §5. Run it and read the startup log

```bash
./gradlew :04-spring-boot-basics:bootRun
```

Or in IntelliJ, click the green ▶ next to `fun main` in `BookstoreApplication.kt`.

Here is the real startup log (timestamps and PIDs trimmed), with what each line means:

```text
 :: Spring Boot ::                (v4.2.0)

Starting BookstoreApplicationKt using Java 25.0.4 …
No active profile set, falling back to 1 default profile: "default"      ← lesson 04: profiles
Tomcat initialized with port 8080 (http)                                 ← embedded web server, no install needed
Starting Servlet engine: [Apache Tomcat/11.0.27]
Root WebApplicationContext: initialization completed in 436 ms           ← the ApplicationContext from module 02
HikariPool-1 - Starting...                                               ← lesson 05: connection pool
HikariPool-1 - Added connection org.sqlite.jdbc4.JDBC4Connection@25589735
HikariPool-1 - Start completed.
Database: jdbc:sqlite:build/bookstore.db?foreign_keys=true&… (SQLite 3.54)    ← lesson 05: Flyway
Successfully validated 3 migrations (execution time 00:00.005s)
Creating Schema History table "main"."flyway_schema_history" ...
Migrating schema "main" to version "1 - create tables"
Migrating schema "main" to version "2 - seed data"
Migrating schema "main" to version "3 - reviews"
Successfully applied 3 migrations to schema "main", now at version v3
Exposing 4 endpoints beneath base path '/actuator'                       ← lesson 08: health, info, metrics, flyway
Tomcat started on port 8080 (http) with context path '/'
Started BookstoreApplicationKt in 1.317 seconds (process running for 1.499)
```

All of that comes from five starter lines and a dozen lines of YAML.

### Try it

With the app running, in a second terminal:

```bash
curl -s "localhost:8080/actuator/health/liveness"
# {"status":"UP"}

curl -s "localhost:8080/api/books/1"
# {"id":1,"title":"Dune","author":"Frank Herbert","isbn":"9780441172719","priceCents":1299,"price":"12.99 EUR","stock":12,"lowStock":false}
```

Stop the app with `Ctrl+C`. The database lives in `04-spring-boot-basics/build/bookstore.db`. Delete it to
start over with fresh seed data, and Flyway will recreate it on the next start.

> ⚠️ **Gotcha:** "Port 8080 was already in use" means another copy is still running (often a forgotten
> IntelliJ run). Stop it, or start this one with `--server.port=9090`.

## §6. The tour: where everything lives

```text
src/main/kotlin/bookstore/
  BookstoreApplication.kt      lesson 01  entry point
  BookstoreExceptions.kt       lesson 03  domain errors
  config/                      lesson 04  @ConfigurationProperties, Clock bean
  books/                       lesson 05  Book, BookRepository (JdbcClient), BookService
  orders/                      lesson 06  OrderService (@Transactional)
  web/                         lessons 02–03  controllers, DTOs, ApiExceptionHandler
  ops/                         lesson 08  custom health indicator
src/main/resources/
  application.yml              lesson 04  configuration
  db/migration/V1__…, V2__…, V3__…   lesson 05  Flyway migrations
src/test/kotlin/bookstore/     lesson 07  integration, slice and transaction tests
src/main/kotlin/exercises/     your exercises (solutions/ has the answers)
http/books.http                every request, ready to click in IntelliJ
```

The request flow you'll follow through lessons 02–06:

```text
HTTP ─▶ BookController ─▶ BookService ─▶ BookRepository ─▶ SQLite
         (web layer)       (rules)         (data access)
```

## Recap

- Spring Boot = Spring + auto-configuration + starters + an embedded server. It's still the
  `ApplicationContext` from module 02.
- `@SpringBootApplication` = configuration + auto-configuration + component scan of its package.
- Starters pull in libraries; the BOM picks versions that work together; Boot 4 starters are modular.
- `bootRun` (or ▶) starts everything, and the startup log tells you what was configured.
