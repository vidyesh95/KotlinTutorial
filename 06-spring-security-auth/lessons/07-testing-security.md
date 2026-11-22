# Lesson 07 — Testing Security

| | |
|---|---|
| **Goal** | Write comprehensive automated tests for secured endpoints using `MockMvc`, `@WithMockUser`, testing real database sessions, and verifying status codes (200, 201, 401, 403). |
| **Needs** | Lesson 06, Module 04 lesson 07 (Testing) |
| **Time** | ~30 min |
| **Next** | Module 07 — Spring AI |
| **Exercise** | `src/main/kotlin/exercises/ex04/` |

---

## §1. The three levels of security testing

1. **Unit tests**: Test password encoders, token expiration, and domain models directly in memory.
2. **MockMvc slice tests with `@WithMockUser`**: Quickly test `@PreAuthorize` annotations and controller responses with synthetic users without touching database tables.
3. **End-to-End integration tests**: Real HTTP requests traversing the actual `SecurityFilterChain`, querying SQLite session tables, and verifying complete sign-up / sign-in / sign-out lifecycles.

---

## §2. Using MockMvc with `SecurityMockMvcRequestPostProcessors`

Spring Security provides `user(...)` to populate the `SecurityContext` for an individual `MockMvc` request:

```kotlin
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user

@Test
fun `admin can ping admin endpoint`() {
    val adminPrincipal = UserPrincipal(id = "usr_admin", name = "Admin", email = "admin@test.com", role = Role.ADMIN)

    mockMvc.get("/exercises/ex03/admin-ping") {
        with(user(adminPrincipal))
    }.andExpect {
        status { isOk() }
        jsonPath("$.status") { value("admin-ok") }
    }
}
```

---

## §3. Testing real database sessions and token lifecycle

In [`src/test/kotlin/bookstore/AuthApiIntegrationTest.kt`](../src/test/kotlin/bookstore/AuthApiIntegrationTest.kt):

```kotlin
@Test
fun `signing out revokes token immediately`() {
    // 1. Sign in to obtain token
    val signInResult = mockMvc.post("/api/auth/sign-in") {
        contentType = MediaType.APPLICATION_JSON
        content = """{"email":"alice@bookstore.test","password":"password123"}"""
    }.andReturn()

    val token = extractToken(signInResult)

    // 2. Sign out
    mockMvc.post("/api/auth/sign-out") {
        header("Authorization", "Bearer $token")
    }.andExpect {
        status { isOk() }
    }

    // 3. Verify subsequent request with that token fails with 401
    mockMvc.get("/api/auth/session") {
        header("Authorization", "Bearer $token")
    }.andExpect {
        status { isUnauthorized() }
    }
}
```

Try exercise 04 (Multi-session revocation):
```bash
./gradlew :06-spring-security-auth:exercises --tests 'exercises.ex04*'
```

---

## Congratulations! 🎉

You have completed module **06 — Spring Security & Better-Auth**.
You now understand:
- The Security Filter Chain and `SecurityContextHolder`
- BCrypt, adaptive hashing, salt, and timing attack defenses
- The Better-Auth 4-table schema (`user`, `session`, `account`, `verification`)
- Opaque session token generation, validation, and instant database revocation
- Route matchers, `@PreAuthorize`, SpEL, and role hierarchies
- RFC 9457 Problem Details for 401 & 403 errors
- Complete integration testing for modern secured backends
