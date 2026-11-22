# Lesson 04 — Session Management & Tokens

| | |
|---|---|
| **Goal** | Implement database-backed sessions with Better-Auth: issuing cryptographically secure tokens, cookie + bearer header extraction, expiration checks, and instant revocation. |
| **Needs** | Lesson 03 |
| **Time** | ~35 min |
| **Next** | [05 — Roles & Authorization](05-roles-and-authorization.md) |
| **Exercise** | `src/main/kotlin/exercises/ex02/` |

---

## §1. The myth of "stateless JWTs"

Many tutorials advocate for "Stateless JWTs": encode the user ID and role into a signed token, send it to the client, and never touch the database again on requests.

It sounds fast, but it has a fatal security flaw: **you cannot revoke a stateless JWT**.
If a user clicks "Log Out", their laptop is stolen, or an admin bans a malicious account, the JWT remains completely valid until its expiration timestamp.
To fix that, developers end up building "token blocklists" in Redis... which means they are storing sessions in a database anyway, but with all the complexity of JWTs!

Database-backed sessions give you:
1. **Instant revocation**: `DELETE FROM session WHERE token = :token` logs the user out immediately.
2. **Device visibility**: Users can view all logged-in devices and click "Sign out of all other devices".
3. **Audit trails**: Keep IP address and User-Agent per session.

---

## §2. Generating cryptographically secure session tokens

Never use simple sequence numbers (`1, 2, 3`) or standard `UUID.randomUUID()` for session tokens (standard UUIDs have lower entropy and predictable formats).

Use `java.security.SecureRandom` with 32 bytes (256 bits of entropy), encoded with URL-safe Base64:

```kotlin
val secureRandom = SecureRandom()
val tokenBytes = ByteArray(32)
secureRandom.nextBytes(tokenBytes)
val token = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes)
```

This produces a string like:
`5d8WqL0kE19mZ4nF6u8P1x7C0k0eP9yNKi2mYjF3gC2`
Brute-forcing this is computationally impossible ($2^{256}$ possibilities).

---

## §3. The `SessionTokenAuthenticationFilter`

In [`src/main/kotlin/bookstore/auth/security/SessionTokenAuthenticationFilter.kt`](../src/main/kotlin/bookstore/auth/security/SessionTokenAuthenticationFilter.kt):

```kotlin
@Component
class SessionTokenAuthenticationFilter(
    private val authService: AuthService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val token = extractToken(request)
        if (token != null) {
            val sessionWithUser = authService.validateSession(token)
            if (sessionWithUser != null) {
                val principal = UserPrincipal(
                    id = sessionWithUser.user.id,
                    name = sessionWithUser.user.name,
                    email = sessionWithUser.user.email,
                    role = sessionWithUser.user.role,
                )
                val auth = UsernamePasswordAuthenticationToken(principal, token, principal.authorities)
                SecurityContextHolder.getContext().authentication = auth
                request.setAttribute("SESSION_TOKEN", token)
            }
        }
        filterChain.doFilter(request, response)
    }

    private fun extractToken(request: HttpServletRequest): String? {
        // 1. Check Authorization: Bearer <token>
        val authHeader = request.getHeader("Authorization")
        if (!authHeader.isNullOrBlank() && authHeader.startsWith("Bearer ", ignoreCase = true)) {
            return authHeader.substring(7).trim()
        }
        // 2. Check Cookie: better-auth.session_token=<token>
        request.cookies?.firstOrNull { it.name == "better-auth.session_token" }?.let {
            return it.value
        }
        return null
    }
}
```

### Both Bearer tokens and HTTP-Only Cookies supported!
- **Mobile apps and CLI tools**: send `Authorization: Bearer <token>`
- **Browsers / Web frontends**: send HTTP-only cookies (`better-auth.session_token`) that cannot be stolen by malicious JavaScript (XSS defense).

---

## §4. Expiration and sliding windows

In [`src/main/kotlin/bookstore/auth/domain/Models.kt`](../src/main/kotlin/bookstore/auth/domain/Models.kt):

```kotlin
data class Session(
    val id: String,
    val userId: String,
    val token: String,
    val expiresAt: Instant,
    ...
) {
    fun isExpired(now: Instant = Instant.now()): Boolean = now.isAfter(expiresAt)
}
```

When validating:
```kotlin
fun validateSession(token: String): SessionWithUser? {
    val session = sessionRepository.findByToken(token) ?: return null
    if (session.isExpired()) {
        sessionRepository.deleteByToken(token) // clean up expired token
        return null
    }
    val user = userRepository.findById(session.userId) ?: return null
    return SessionWithUser(session, user)
}
```

Try exercise 02:
```bash
./gradlew :06-spring-security-auth:exercises --tests 'exercises.ex02*'
```
