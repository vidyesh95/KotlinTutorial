package bookstore

import bookstore.auth.security.SessionTokenAuthenticationFilter
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.http.MediaType
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import tools.jackson.databind.ObjectMapper

@SpringBootTest(classes = [BookstoreSecurityApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class AuthApiIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `01 public can view books without authentication`() {
        mockMvc.get("/api/books")
            .andExpect {
                status { isOk() }
                jsonPath("$[0].title") { value("Dune") }
            }
    }

    @Test
    fun `02 unauthenticated request to protected endpoint returns 401 Problem Details`() {
        mockMvc.post("/api/books") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Neuromancer","authorId":1,"isbn":"9780441569595","priceCents":1199}"""
        }.andExpect {
            status { isUnauthorized() }
            header { string("Content-Type", "application/problem+json;charset=UTF-8") }
            jsonPath("$.status") { value(401) }
            jsonPath("$.title") { value("Unauthorized") }
        }
    }

    @Test
    fun `03 user can sign in and obtain token`() {
        mockMvc.post("/api/auth/sign-in") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@bookstore.test","password":"password123"}"""
        }.andExpect {
            status { isOk() }
            cookie { exists(SessionTokenAuthenticationFilter.COOKIE_NAME) }
            jsonPath("$.user.email") { value("alice@bookstore.test") }
            jsonPath("$.user.role") { value("USER") }
            jsonPath("$.token") { isNotEmpty() }
        }
    }

    @Test
    fun `04 alice can place order with bearer token and view it`() {
        // Sign in first
        val signInResult = mockMvc.post("/api/auth/sign-in") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@bookstore.test","password":"password123"}"""
        }.andReturn()

        val token = org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.token")
        val responseBody = signInResult.response.contentAsString
        val tokenRegex = """"token":"([^"]+)"""".toRegex()
        val sessionToken = tokenRegex.find(responseBody)!!.groupValues[1]

        // Access current session
        mockMvc.get("/api/auth/session") {
            header("Authorization", "Bearer $sessionToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$.user.email") { value("alice@bookstore.test") }
            jsonPath("$.session.token") { value(sessionToken) }
        }

        // Place order
        mockMvc.post("/api/orders") {
            header("Authorization", "Bearer $sessionToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"items":[{"bookId":1,"quantity":2}]}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.totalCents") { value(2598) }
            jsonPath("$.userId") { value("usr_alice") }
        }

        // Alice views her orders
        mockMvc.get("/api/orders/mine") {
            header("Authorization", "Bearer $sessionToken")
        }.andExpect {
            status { isOk() }
            jsonPath("$[0].userId") { value("usr_alice") }
        }

        // Alice cannot access admin routes -> 403 Forbidden
        mockMvc.get("/api/admin/orders") {
            header("Authorization", "Bearer $sessionToken")
        }.andExpect {
            status { isForbidden() }
            jsonPath("$.status") { value(403) }
            jsonPath("$.title") { value("Forbidden") }
        }
    }

    @Test
    fun `05 admin can sign in, add book and access admin orders`() {
        val signInResult = mockMvc.post("/api/auth/sign-in") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"admin@bookstore.test","password":"admin123"}"""
        }.andReturn()

        val responseBody = signInResult.response.contentAsString
        val tokenRegex = """"token":"([^"]+)"""".toRegex()
        val adminToken = tokenRegex.find(responseBody)!!.groupValues[1]

        // Admin adds book
        mockMvc.post("/api/books") {
            header("Authorization", "Bearer $adminToken")
            contentType = MediaType.APPLICATION_JSON
            content = """{"title":"Foundation","authorId":1,"isbn":"9780553293357","priceCents":1499,"stock":10}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.title") { value("Foundation") }
        }

        // Admin views all orders
        mockMvc.get("/api/admin/orders") {
            header("Authorization", "Bearer $adminToken")
        }.andExpect {
            status { isOk() }
        }
    }

    @Test
    fun `06 signing out revokes token immediately`() {
        val signInResult = mockMvc.post("/api/auth/sign-in") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"email":"alice@bookstore.test","password":"password123"}"""
        }.andReturn()

        val tokenRegex = """"token":"([^"]+)"""".toRegex()
        val token = tokenRegex.find(signInResult.response.contentAsString)!!.groupValues[1]

        // Sign out
        mockMvc.post("/api/auth/sign-out") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isOk() }
            cookie { maxAge(SessionTokenAuthenticationFilter.COOKIE_NAME, 0) }
        }

        // Immediate retry with the same token must fail with 401
        mockMvc.get("/api/auth/session") {
            header("Authorization", "Bearer $token")
        }.andExpect {
            status { isUnauthorized() }
        }
    }

    @Test
    fun `07 sign up creates new user and session`() {
        val email = "bob_${System.currentTimeMillis()}@bookstore.test"
        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Bob Builder","email":"$email","password":"securePassword123"}"""
        }.andExpect {
            status { isCreated() }
            jsonPath("$.user.email") { value(email) }
            jsonPath("$.user.role") { value("USER") }
            jsonPath("$.token") { isNotEmpty() }
        }

        // Duplicate registration must fail with 409 Conflict
        mockMvc.post("/api/auth/sign-up") {
            contentType = MediaType.APPLICATION_JSON
            content = """{"name":"Bob Again","email":"$email","password":"securePassword123"}"""
        }.andExpect {
            status { isConflict() }
        }
    }
}
