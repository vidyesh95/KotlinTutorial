# Lesson 07 — Testing a Spring Boot app

| | |
|---|---|
| **Goal** | Know the three test styles in this module (full integration, web slice, and a service with a real DB), when to use each, and the tricks that make them reliable: a fixed clock, a throw-away database, context caching. |
| **Needs** | Lessons 01–06; module 01, lesson 35 (testing basics); module 02 (fakes) |
| **Time** | ~40 min |
| **Next** | [08 — Actuator and operations](08-actuator-and-operations.md) |
| **Exercise** | `src/main/kotlin/exercises/ex07/` |

## §1. The test pyramid

```text
            ╱╲          few:   end-to-end (real HTTP, real browser…)        slow, brittle
           ╱  ╲
          ╱ IT ╲        some:  integration — the whole app, real DB          medium
         ╱──────╲
        ╱ slice  ╲      more:  slices — one layer (web, jdbc)                fast-ish
       ╱──────────╲
      ╱    unit    ╲    most:  plain Kotlin classes, fakes, no Spring        milliseconds
     ╱──────────────╲
```

> 🧒 **Like I'm 5:**
> Before a school play you practise each line alone (unit), then one scene with a few kids (slice), then a
> full dress rehearsal with lights and music (integration). Dress rehearsals catch the most problems but
> take ages, so you don't do one every five minutes.

Everything from module 01, lesson 35 applies: Arrange–Act–Assert, descriptive backtick names, and
`kotlin.test` assertions. Spring adds ways to start **part** or **all** of the app inside a test.

Run them:

```bash
./gradlew :04-spring-boot-basics:test                                            # all
./gradlew :04-spring-boot-basics:test --tests 'bookstore.OrderTransactionTest'   # one class
./gradlew :04-spring-boot-basics:test --tests '*duplicate isbn*'                 # by name pattern
```

In IntelliJ, click the ▶ next to a test class or method.

## §2. Style 1: full integration test with MockMvc

[`src/test/kotlin/bookstore/BookApiIntegrationTest.kt`](../src/test/kotlin/bookstore/BookApiIntegrationTest.kt):

```kotlin
@SpringBootTest                 // start the WHOLE app: every bean, Flyway, the real SQLite database
@AutoConfigureMockMvc           // + a MockMvc to send fake HTTP requests through the full stack
@ActiveProfiles("test")         // use application-test.yml (a random throw-away DB file)
@Import(TestClock::class)       // replace the clock (§4)
@DirtiesContext                 // throw this context away afterwards (§5)
class BookApiIntegrationTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `invalid body gives 400 with field errors`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":" ","authorId":1,"isbn":"123","priceCents":-5}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.title") { value("must not be blank") }
            jsonPath("$.errors.isbn") { value("must be exactly 13 digits") }
        }
    }
}
```

- **MockMvc** pushes requests through Spring MVC (filters, controllers, validation, JSON, exception
  handlers) **without opening a network port**. It's fast, and the behaviour is identical.
- The **Kotlin DSL** (`mockMvc.get(...) { } .andExpect { status { isOk() } }`) comes from
  `org.springframework.test.web.servlet.get/post/patch/delete`.
- `jsonPath("$.items[0].title")` reads into the JSON response: `$` is the root, `.items[0]` the first array
  element.
- Constructor injection works in tests too: `class X(@Autowired private val mockMvc: MockMvc)`. The test
  class is a bean-like thing created by Spring's JUnit extension.

Use this style for: "does the endpoint behave correctly end-to-end, with the real database?" It's the most
valuable style for a CRUD API.

## §3. Style 2: a web slice with a mocked service

[`BookControllerSliceTest.kt`](../src/test/kotlin/bookstore/BookControllerSliceTest.kt):

```kotlin
@WebMvcTest(BookController::class)                     // ONLY the web layer: this controller, JSON, validation, advice
@Import(BookControllerSliceTest.Props::class)
class BookControllerSliceTest(@Autowired private val mockMvc: MockMvc) {

    @MockitoBean
    private lateinit var service: BookService          // a Mockito mock replaces the real service bean

    @TestConfiguration
    @EnableConfigurationProperties(BookstoreProperties::class)
    class Props                                        // slices don't scan @ConfigurationProperties: register it

    @Test
    fun `maps a book to JSON`() {
        given(service.get(7)).willReturn(Book(7, "Test Book", 1, "Some Author", "1234567890123", 1050, 2, …))
        mockMvc.get("/api/books/7").andExpect {
            status { isOk() }
            jsonPath("$.price") { value("10.50 EUR") }
        }
    }
}
```

There's no database and no Flyway, so it starts faster. Use it for web-layer concerns: JSON shapes, status
codes, validation, error mapping.

**Mocks vs fakes** (module 02, lesson 02):
- A **fake** is a small working implementation you write (an in-memory repository, a `RecordingNotifier`).
- A **mock** is a generated stand-in that you *program* (`given(x).willReturn(y)`) and can *verify*
  (`then(service).should().delete(4)`).

Prefer fakes for your own interfaces: tests read like real code and survive refactoring. Mocks shine when
the real class is a concrete Spring bean you'd rather not build, like `BookService` here.

⚠️ **Gotcha:** `@MockitoBean` (from `org.springframework.test.context.bean.override.mockito`) replaced Boot 3's
deprecated `@MockBean`. Use `given(...)` (BDDMockito) in Kotlin, because `when` is a Kotlin keyword and
`Mockito.when` would need backticks.

## §4. Style 3: service + real database, proving behaviour

[`OrderTransactionTest.kt`](../src/test/kotlin/bookstore/OrderTransactionTest.kt) calls the service directly
(no HTTP) to prove the lesson 06 rollback:

```kotlin
@Test
fun `failing line rolls back the whole order`() {
    val duneBefore = books.findById(1)!!.stock
    assertFailsWith<OutOfStockException> {
        orders.placeOrder("bob@example.com", listOf(OrderLine(bookId = 1, quantity = 1), OrderLine(bookId = 4, quantity = 1)))
    }
    assertEquals(duneBefore, books.findById(1)!!.stock, "Dune's stock must be restored by the rollback")
}
```

You can't test transactions with mocks. You need a real database, and SQLite makes that free.

### Fixed time: [`TestClock.kt`](../src/test/kotlin/bookstore/TestClock.kt)

```kotlin
@TestConfiguration(proxyBeanMethods = false)
class TestClock {
    @Bean @Primary
    fun fixedClock(): Clock = object : Clock { override fun now(): Instant = NOW }
    companion object { val NOW: Instant = Instant.parse("2026-06-01T12:00:00Z") }
}
```

`@Import(TestClock::class)` adds this bean. Now there are **two** `Clock` beans, the real `clock` from
`TimeConfig` and `fixedClock`, and `@Primary` makes the fixed one win wherever a `Clock` is injected
(module 02, lesson 06). The assertion `assertEquals(TestClock.NOW, order.createdAt)` therefore works every
time.

## §5. Reliable test databases and context caching

[`application-test.yml`](../src/test/resources/application-test.yml):

```yaml
spring:
  datasource:
    url: jdbc:sqlite:${java.io.tmpdir}/bookstore-test-${random.uuid}.db?foreign_keys=true&journal_mode=wal&busy_timeout=5000
```

Each Spring context gets its **own fresh database file**, and Flyway migrates and seeds it. Tests never
touch your development `build/bookstore.db`.

**Context caching:** starting a Spring context takes a second or more. Spring's test framework **reuses** a
context across test classes with the same configuration (annotations, profiles, mocks). Our test classes
mutate data (create, delete, take stock), so each one is marked `@DirtiesContext`: "this context is now
dirty, don't reuse it". The next class gets a fresh context and therefore a fresh database.

⚠️ **Gotcha:** tests inside ONE class share the context and the database. Write them so the order doesn't
matter: `patch … delete` uses book 3 and book 4, which no other test in that class relies on. If tests start
failing only when run together, shared state is the usual suspect.

> 🏗️ **In the real world with Postgres:** use **Testcontainers** to start a real Postgres in Docker for tests
> (`spring-boot-testcontainers` is in the Boot BOM). An in-memory H2 "pretending to be Postgres" hides real
> SQL differences, so avoid it.

## §6. What to test where (cheat sheet)

| Question | Style |
|---|---|
| Is this pure calculation right? (price formatting, validation helpers) | unit test, no Spring |
| Does the endpoint return the right status/JSON/errors? | `@SpringBootTest` + MockMvc, or a `@WebMvcTest` slice |
| Does my SQL do what I think? | repository against a real SQLite DB (`@SpringBootTest` or `@JdbcTest`) |
| Does the transaction roll back / is it atomic? | service + real DB (style 3) |
| Does the whole app start? | any `@SpringBootTest`; a broken bean fails it |

## Recap

- Use a pyramid: many fast tests, fewer slow ones.
- `@SpringBootTest` + `@AutoConfigureMockMvc` gives the whole app with fake HTTP and the Kotlin MockMvc DSL.
- `@WebMvcTest` + `@MockitoBean` gives just the web layer with a mocked service.
- Transactions and SQL need a real database. SQLite plus a random file per context makes that cheap.
- `@Primary` test beans (`TestClock`) override production beans; `@DirtiesContext` gives a fresh context.
