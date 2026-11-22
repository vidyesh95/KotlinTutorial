package bookstore

import bookstore.catalog.CatalogService
import bookstore.orders.OrderLine
import bookstore.orders.OrderService
import bookstore.orders.OutOfStockException
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Lesson 05 — proves the whole chain: Flyway ran SQLDelight's migrations, generated queries
 * work through Spring's DataSource, and @Transactional rolls back SQLDelight writes.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class SqlDelightSpringIntegrationTest(
    @Autowired private val mockMvc: MockMvc,
    @Autowired private val catalog: CatalogService,
    @Autowired private val orders: OrderService,
) {
    @Test
    fun `flyway seeded the books and the API serves them`() {
        mockMvc.get("/api/books/1").andExpect {
            status { isOk() }
            jsonPath("$.title") { value("Dune") }
            jsonPath("$.author") { value("Frank Herbert") }
            jsonPath("$.publishedYear") { value(1965) }
        }
    }

    @Test
    fun `placing an order takes stock`() {
        mockMvc.post("/api/orders") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"customerEmail":"ada@example.com","lines":[{"bookId":3,"quantity":2}]}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.totalCents") { value(1598) }
            jsonPath("$.status") { value("PAID") }
        }
        assertEquals(28, catalog.byId(3)!!.stock)
    }

    @Test
    fun `a failing line rolls back SQLDelight writes`() {
        val before = catalog.byId(1)!!.stock
        assertFailsWith<OutOfStockException> {
            orders.placeOrder("bob@example.com", listOf(OrderLine(1, 1), OrderLine(4, 1)))   // Emma: stock 0
        }
        assertEquals(before, catalog.byId(1)!!.stock, "rollback must restore Dune's stock")
    }
}
