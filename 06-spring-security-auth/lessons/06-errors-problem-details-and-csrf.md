# Lesson 06 — Problem Details & CSRF

| | |
|---|---|
| **Goal** | Deliver RFC 9457 Problem Details for 401 Unauthorized and 403 Forbidden errors, and understand when CSRF protection is needed. |
| **Needs** | Lesson 05 |
| **Time** | ~25 min |
| **Next** | [07 — Testing Security](07-testing-security.md) |
| **Exercise** | None (operational & standards) |

---

## §1. The horror of HTML error pages in REST APIs

By default, an unconfigured Spring Security application redirects unauthenticated requests to `/login` (an HTML form) or returns a blank 403 status.

For a REST API consumed by mobile apps or single-page apps (React/Vue/Android/iOS), receiving HTML when expecting JSON breaks JSON parsers with `SyntaxError: Unexpected token <`.

In module 04 lesson 03 we introduced **RFC 9457 Problem Details**. Spring Security provides two interfaces to intercept these errors:
1. `AuthenticationEntryPoint`: triggers when an unauthenticated client tries to access a protected route (HTTP 401).
2. `AccessDeniedHandler`: triggers when an authenticated client lacks the required role or permission (HTTP 403).

In [`src/main/kotlin/bookstore/auth/security/ProblemDetailsHandlers.kt`](../src/main/kotlin/bookstore/auth/security/ProblemDetailsHandlers.kt):

```kotlin
@Component
class ProblemDetailsAuthenticationEntryPoint : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        response.status = HttpStatus.UNAUTHORIZED.value()
        response.contentType = "application/problem+json;charset=UTF-8"
        val message = (authException.message ?: "Authentication required").replace("\"", "\\\"")
        val uri = request.requestURI.replace("\"", "\\\"")
        response.writer.write("""{"type":"about:blank","title":"Unauthorized","status":401,"detail":"$message","instance":"$uri"}""")
    }
}
```

Now, every failed security check returns a clean, standardized JSON response:

```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Full authentication is required to access this resource",
  "instance": "/api/orders"
}
```

---

## §2. CSRF (Cross-Site Request Forgery) demystified

What is CSRF?
If a browser is logged into `bank.com` with a cookie, and the user visits `evil.com`, an evil script on `evil.com` can make a `POST http://bank.com/transfer` request. Because browsers automatically attach cookies on cross-origin requests by default, `bank.com` executes the transfer unless protected!

### When do you need CSRF?
- **Cookie-based sessions in browser forms**: ✅ YES! You need CSRF protection (e.g. `CookieCsrfTokenRepository` + SameSite cookies).
- **Bearer Tokens in `Authorization: Bearer <token>`**: ❌ NO! Evil websites cannot read or forge the `Authorization` header on cross-site requests.
- **Pure APIs**: Disabled with `.csrf { it.disable() }` when clients pass Bearer tokens.

In our bookstore app, we support Bearer tokens for API clients and use `SameSite=Lax` on our session cookies.
