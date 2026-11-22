package bookstore

import bookstore.books.Book
import bookstore.books.BookService
import bookstore.config.BookstoreProperties
import bookstore.web.BookController
import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.time.Instant

/**
 * Lesson 07 — a SLICE test: @WebMvcTest starts only the web layer (controllers, JSON,
 * validation, exception handlers) — no database, no services. Much faster.
 * The service is replaced by a Mockito mock (@MockitoBean) that we program with `given(...)`.
 *
 * Mocks vs fakes (module 02): a fake is a working mini-implementation; a mock is a
 * programmable stand-in. Use mocks when building a fake would be more work than it's worth.
 */
@WebMvcTest(BookController::class)
@Import(BookControllerSliceTest.Props::class)
class BookControllerSliceTest(@Autowired private val mockMvc: MockMvc) {

    @MockitoBean
    private lateinit var service: BookService

    @TestConfiguration
    @EnableConfigurationProperties(BookstoreProperties::class)
    class Props

    @Test
    fun `maps a book to JSON`() {
        given(service.get(7)).willReturn(
            Book(7, "Test Book", 1, "Some Author", "1234567890123", 1050, 2, Instant.parse("2026-01-01T00:00:00Z")),
        )
        mockMvc.get("/api/books/7").andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Test Book") }
            jsonPath("$.price") { value("10.50 EUR") }
            jsonPath("$.lowStock") { value(true) }
        }
    }
}
