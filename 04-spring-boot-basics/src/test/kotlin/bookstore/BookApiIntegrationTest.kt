package bookstore

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.patch
import org.springframework.test.web.servlet.post

/**
 * Lesson 07 — a FULL integration test: the real app (controllers, services, Flyway,
 * SQLite) starts once, and MockMvc sends fake HTTP requests through the whole stack
 * without opening a network port.
 *
 * Uses Spring's Kotlin MockMvc DSL: `mockMvc.get("/x") { … }.andExpect { status { isOk() } }`.
 * Each test class gets a fresh database file (application-test.yml) — @DirtiesContext
 * throws the context away afterwards so the next class starts clean.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestClock::class)
@DirtiesContext
class BookApiIntegrationTest(@Autowired private val mockMvc: MockMvc) {

    @Test
    fun `lists the seeded books, paged`() {
        mockMvc.get("/api/books?page=0&size=2").andExpect {
            status { isOk() }
            jsonPath("$.items.length()") { value(2) }
            jsonPath("$.items[0].title") { value("Dune") }
            jsonPath("$.items[0].price") { value("12.99 EUR") }
            jsonPath("$.totalItems") { value(5) }
        }
    }

    @Test
    fun `unknown book gives 404 problem details`() {
        mockMvc.get("/api/books/999").andExpect {
            status { isNotFound() }
            content { contentType(MediaType.APPLICATION_PROBLEM_JSON) }
            jsonPath("$.detail") { value("Book 999 not found") }
        }
    }

    @Test
    fun `creates a book and returns 201 with Location`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Children of Dune","authorId":1,"isbn":"9780593098240","priceCents":1399}"""
        }.andExpect {
            status { isCreated() }
            header { string("Location", org.hamcrest.Matchers.matchesPattern(".*/api/books/\\d+")) }
            jsonPath("$.author") { value("Frank Herbert") }
            jsonPath("$.stock") { value(0) }
            jsonPath("$.lowStock") { value(true) }
        }
    }

    @Test
    fun `invalid body gives 400 with field errors`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":" ","authorId":1,"isbn":"123","priceCents":-5}"""
        }.andExpect {
            status { isBadRequest() }
            jsonPath("$.errors.title") { value("must not be blank") }
            jsonPath("$.errors.isbn") { value("must be exactly 13 digits") }
            jsonPath("$.errors.priceCents") { exists() }
        }
    }

    @Test
    fun `duplicate isbn gives 409`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Dune again","authorId":1,"isbn":"9780441172719","priceCents":100}"""
        }.andExpect {
            status { isConflict() }
        }
    }

    @Test
    fun `unknown author gives 404`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Ghost","authorId":42,"isbn":"9999999999999","priceCents":100}"""
        }.andExpect {
            status { isNotFound() }
            jsonPath("$.detail") { value("Author 42 not found") }
        }
    }

    @Test
    fun `patch changes only the given fields, delete removes`() {
        mockMvc.patch("/api/books/3") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"priceCents":999}"""
        }.andExpect {
            status { isOk() }
            jsonPath("$.priceCents") { value(999) }
            jsonPath("$.stock") { value(30) }
        }
        mockMvc.delete("/api/books/4").andExpect { status { isNoContent() } }
        mockMvc.get("/api/books/4").andExpect { status { isNotFound() } }
    }

    @Test
    fun `negative page is rejected`() {
        mockMvc.get("/api/books?page=-1").andExpect { status { isBadRequest() } }
    }
}
