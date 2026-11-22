package exercises.ex07

import bookstore.BookstoreApplication
import bookstore.NotFoundException
import bookstore.TestClock
import bookstore.books.Book
import bookstore.books.BookService
import bookstore.config.BookstoreProperties
import bookstore.web.BookController
import bookstore.web.toResponse
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals
import kotlin.time.Instant

/*
 * Exercise 07 — Testing                          (lesson: lessons/07-testing.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex07*'
 *
 * This time YOU write the tests. Replace each TODO(...) with a real test body.
 * The classes are already set up (annotations, injected MockMvc, mock). Three kinds of test:
 *   07.1 plain unit test   — no Spring at all, milliseconds
 *   07.2 slice test        — only the web layer, service mocked
 *   07.3 integration test  — whole app + real SQLite
 * Reference answers: the *SolutionTest classes at the bottom of this file. Try first!
 */

private val sampleBook = Book(
    id = 1, title = "Sample", authorId = 1, authorName = "Some Author", isbn = "1234567890123",
    priceCents = 5, stock = 3, createdAt = Instant.parse("2026-01-01T00:00:00Z"),
)

// ── Your tests ────────────────────────────────────────────────────────────────

@Tag("exercise")
class Ex07UnitExerciseTest {
    /**
     * 07.1 — `sampleBook.toResponse(currency = "EUR", lowStockThreshold = 3)` (from bookstore.web)
     * Assert: price is "0.05 EUR" (leading zero + 2 decimals) and lowStock is true (3 <= 3).
     * Then with lowStockThreshold = 2, lowStock must be false.
     */
    @Test
    fun `07_1 toResponse formats price and low stock`() {
        TODO("07.1")
    }
}

@WebMvcTest(BookController::class)
@ContextConfiguration(classes = [BookstoreApplication::class])   // tests are outside the `bookstore` package
@Import(Ex07SliceExerciseTest.Props::class)
@Tag("exercise")
class Ex07SliceExerciseTest(@Autowired private val mockMvc: MockMvc) {
    @MockitoBean
    private lateinit var service: BookService

    @TestConfiguration
    @EnableConfigurationProperties(BookstoreProperties::class)
    class Props

    /**
     * 07.2 — Program the mock: `given(service.get(42)).willThrow(NotFoundException("Book", 42))`.
     * Then GET /api/books/42 must answer 404 with "$.detail" == "Book 42 not found".
     * (This proves the controller + ApiExceptionHandler wiring, without any database.)
     */
    @Test
    fun `07_2 missing book maps to 404`() {
        TODO("07.2")
    }
}

@SpringBootTest(classes = [BookstoreApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClock::class)
@DirtiesContext
@Tag("exercise")
class Ex07IntegrationExerciseTest(@Autowired private val mockMvc: MockMvc) {
    /**
     * 07.3 — POST /api/orders with {"customerEmail":"ada@example.com","lines":[{"bookId":1,"quantity":2}]}
     * Assert 201 and "$.totalCents" == 2598, then GET /api/books/1 shows "$.stock" == 10 (was 12).
     */
    @Test
    fun `07_3 placing an order lowers stock`() {
        TODO("07.3")
    }
}

// ── Reference answers ─────────────────────────────────────────────────────────

class Ex07UnitSolutionTest {
    @Test
    fun `07_1 toResponse formats price and low stock`() {
        val response = sampleBook.toResponse(currency = "EUR", lowStockThreshold = 3)
        assertEquals("0.05 EUR", response.price)
        assertEquals(true, response.lowStock)
        assertEquals(false, sampleBook.toResponse("EUR", lowStockThreshold = 2).lowStock)
    }
}

@WebMvcTest(BookController::class)
@ContextConfiguration(classes = [BookstoreApplication::class])   // tests are outside the `bookstore` package
@Import(Ex07SliceSolutionTest.Props::class)
class Ex07SliceSolutionTest(@Autowired private val mockMvc: MockMvc) {
    @MockitoBean
    private lateinit var service: BookService

    @TestConfiguration
    @EnableConfigurationProperties(BookstoreProperties::class)
    class Props

    @Test
    fun `07_2 missing book maps to 404`() {
        given(service.get(42)).willThrow(NotFoundException("Book", 42))
        mockMvc.get("/api/books/42").andExpect {
            status { isNotFound() }
            jsonPath("$.detail") { value("Book 42 not found") }
        }
    }
}

@SpringBootTest(classes = [BookstoreApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClock::class)
@DirtiesContext
class Ex07IntegrationSolutionTest(@Autowired private val mockMvc: MockMvc) {
    @Test
    fun `07_3 placing an order lowers stock`() {
        mockMvc.post("/api/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerEmail":"ada@example.com","lines":[{"bookId":1,"quantity":2}]}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.totalCents") { value(2598) }
        }
        mockMvc.get("/api/books/1").andExpect { jsonPath("$.stock") { value(10) } }
    }
}
