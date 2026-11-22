package bookstore

import bookstore.auth.security.SessionTokenAuthenticationFilter
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get

@SpringBootTest(classes = [BookstoreSecurityApplication::class])
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class SecurityFilterChainTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Test
    fun `cookie based session token authenticates user`() {
        // Pre-seeded token from V2 migration
        val seedToken = "seed-alice-session-token-12345"

        mockMvc.get("/api/auth/session") {
            cookie(Cookie(SessionTokenAuthenticationFilter.COOKIE_NAME, seedToken))
        }.andExpect {
            status { isOk() }
            jsonPath("$.user.email") { value("alice@bookstore.test") }
        }
    }

    @Test
    fun `invalid token returns 401 Unauthorized`() {
        mockMvc.get("/api/auth/session") {
            header("Authorization", "Bearer invalid-token-value-does-not-exist")
        }.andExpect {
            status { isUnauthorized() }
            header { string("Content-Type", "application/problem+json;charset=UTF-8") }
            jsonPath("$.status") { value(401) }
        }
    }
}
