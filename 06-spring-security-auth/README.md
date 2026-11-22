# 06 — Spring Security & Better-Auth

> 🧒 If Spring Boot is a furnished apartment, Spring Security is the **smart lock and digital keycard system**:
> it checks who is knocking, checks which doors they're allowed through, and throws out anyone without a badge.

In module 04 we built a REST API, and in module 05 we gave it compile-checked SQL with SQLDelight.
Now we protect it: **sessions, BCrypt password hashing, the Spring Security Filter Chain, and the modern Better-Auth database schema**.

Instead of Spring Security's 2005-era default schema (`users` table where `username` is the primary key and sessions are in-memory binary blobs), we implement the industry standard **Better-Auth architecture**:
`user` (identity), `account` (credentials & OAuth providers), `session` (database-tracked revocable sessions), and `verification` (tokens).

```
HTTP Request ──▶ SecurityFilterChain (SessionTokenAuthenticationFilter)
                     ├── Validates Bearer token or Cookie against SQLite `session` + `user`
                     ├── Sets SecurityContextHolder.context.authentication
                     ├── Route authorization (.hasRole("ADMIN"))
                     └── RFC 9457 Problem Details for 401 Unauthorized & 403 Forbidden
```

```bash
./gradlew :06-spring-security-auth:bootRun          # starts API on http://localhost:8080
./gradlew :06-spring-security-auth:test             # integration tests + reference solutions (always green)
./gradlew :06-spring-security-auth:exercises        # your exercises (red until solved)
```

Requests to try: [`http/auth.http`](http/auth.http) (IntelliJ HTTP Client) or `curl`.

| ✓ | # | Lesson | Main code | Exercise |
|---|---|--------|-----------|----------|
| ☐ | 01 | [The Security Filter Chain](lessons/01-security-filter-chain.md) | [SecurityConfig.kt](src/main/kotlin/bookstore/auth/security/SecurityConfig.kt) | — |
| ☐ | 02 | [Passwords and Hashing](lessons/02-password-hashing.md) | [PasswordConfig.kt](src/main/kotlin/bookstore/auth/security/PasswordConfig.kt) | [ex01](src/main/kotlin/exercises/ex01) — BCrypt hashing & verify |
| ☐ | 03 | [The Better-Auth Schema](lessons/03-better-auth-schema.md) | [V1__better_auth_tables.sql](src/main/resources/db/migration/V1__better_auth_tables.sql) | — |
| ☐ | 04 | [Session Management & Tokens](lessons/04-session-management-and-tokens.md) | [SessionTokenAuthenticationFilter.kt](src/main/kotlin/bookstore/auth/security/SessionTokenAuthenticationFilter.kt), [AuthService.kt](src/main/kotlin/bookstore/auth/service/AuthService.kt) | [ex02](src/main/kotlin/exercises/ex02) — validation & extension |
| ☐ | 05 | [Roles & Authorization](lessons/05-roles-and-authorization.md) | [BookController.kt](src/main/kotlin/bookstore/books/BookController.kt), [OrderController.kt](src/main/kotlin/bookstore/orders/OrderController.kt) | [ex03](src/main/kotlin/exercises/ex03) — `@PreAuthorize` guards |
| ☐ | 06 | [Problem Details & CSRF](lessons/06-errors-problem-details-and-csrf.md) | [ProblemDetailsHandlers.kt](src/main/kotlin/bookstore/auth/security/ProblemDetailsHandlers.kt) | — |
| ☐ | 07 | [Testing Security](lessons/07-testing-security.md) | [AuthApiIntegrationTest.kt](src/test/kotlin/bookstore/AuthApiIntegrationTest.kt) | [ex04](src/main/kotlin/exercises/ex04) — session revocation |

Next: `07-spring-ai` — Ollama, structured outputs, tool calling, and RAG.
