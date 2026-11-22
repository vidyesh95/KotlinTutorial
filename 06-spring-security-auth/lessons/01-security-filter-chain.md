# Lesson 01 — The Security Filter Chain

| | |
|---|---|
| **Goal** | Understand how Spring Security intercepts HTTP requests, the mental model of the Filter Chain, `SecurityContextHolder`, `Authentication`, and `Principal`. |
| **Needs** | Module 04 (REST controllers, Spring Boot basics) |
| **Time** | ~25 min |
| **Next** | [02 — Passwords and Hashing](02-password-hashing.md) |
| **Exercise** | None (foundations) |

---

## §1. Why security is NOT an `if` statement

When developers first write a web backend, they often protect endpoints with manual checks:

```kotlin
@PostMapping("/api/books")
fun createBook(@RequestBody book: Book, request: HttpServletRequest): Book {
    val token = request.getHeader("Authorization")
    if (token == null || !isValidAdmin(token)) {
        throw ResponseStatusException(HttpStatus.UNAUTHORIZED)
    }
    return bookRepository.save(book)
}
```

This breaks down quickly:
1. **Forgetfulness is fatal**: You write 40 endpoints, and on the 41st you forget the `if` check. The endpoint is completely exposed.
2. **Scattered logic**: Changing token formats or authorization rules requires touching every controller.
3. **Audit failure**: There is no central pipeline proving what is public and what is protected.

**Spring Security moves security out of your business logic and into a gauntlet of filters in front of your application.**

> 🧒 **Like I'm 5:**
> Think of an airport.
> You don't have flight attendants at every airplane seat checking your passport and metal-detecting your bags.
> You go through a **security checkpoint** at the airport entrance. By the time you reach the departure gate,
> the gate agent knows you've already been checked, stamped, and given a boarding pass.
> That checkpoint is Spring's **SecurityFilterChain**.

---

## §2. The gauntlet: how requests traverse filters

When an HTTP request enters Tomcat, it passes through a chain of servlet filters before reaching Spring's `DispatcherServlet`:

```
HTTP Request
     │
     ▼
┌────────────────────────────────────────────────────────┐
│ FilterChainProxy                                       │
│                                                        │
│  [1] SecurityContextHolderFilter                       │
│      └─ Prepares the ThreadLocal security context      │
│                                                        │
│  [2] SessionTokenAuthenticationFilter (our filter!)    │
│      ├─ Looks for `Authorization: Bearer <token>`      │
│      ├─ Queries SQLite session + user tables           │
│      └─ Stuffs `UserPrincipal` into SecurityContext    │
│                                                        │
│  [3] AnonymousAuthenticationFilter                     │
│      └─ If nobody logged in, marks user as "anonymous" │
│                                                        │
│  [4] ExceptionTranslationFilter                        │
│      └─ Catches 401 & 403 and invokes custom handlers  │
│                                                        │
│  [5] AuthorizationFilter                               │
│      └─ Matches URL patterns against permitted roles   │
└──────────────────────────┬─────────────────────────────┘
                           │
                           ▼
                    DispatcherServlet
                           │
                           ▼
                   @RestController
```

Every filter can:
- **Pass the request down the chain**: `filterChain.doFilter(request, response)`
- **Reject the request immediately**: return HTTP 401/403 without touching any controller
- **Modify the environment**: enrich the request or populate the security context

---

## §3. `SecurityContextHolder`: the identity badge

Once an identity is verified, how does your controller know *who* made the request without re-parsing the header?

Spring Security uses **`SecurityContextHolder`**:

```kotlin
// In a filter:
val authentication = UsernamePasswordAuthenticationToken(principal, token, principal.authorities)
SecurityContextHolder.getContext().authentication = authentication

// Later, in any service or controller:
val currentAuth = SecurityContextHolder.getContext().authentication
val user = currentAuth.principal as UserPrincipal
```

In standard Spring MVC with virtual or platform threads, `SecurityContextHolder` stores identity in a **`ThreadLocal`**. Because each HTTP request is handled on its thread, the authenticated user is automatically accessible across any service called during that request.

In your controllers, Spring can inject the user directly using `@AuthenticationPrincipal`:

```kotlin
@GetMapping("/api/orders/mine")
fun getMyOrders(@AuthenticationPrincipal user: UserPrincipal): List<OrderResponse> {
    return orderRepository.findOrdersByUserId(user.id)
}
```

---

## §4. The Kotlin configuration DSL

In [`src/main/kotlin/bookstore/auth/security/SecurityConfig.kt`](../src/main/kotlin/bookstore/auth/security/SecurityConfig.kt):

```kotlin
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
class SecurityConfig(
    private val sessionTokenFilter: SessionTokenAuthenticationFilter,
    private val authEntryPoint: ProblemDetailsAuthenticationEntryPoint,
    private val accessDeniedHandler: ProblemDetailsAccessDeniedHandler,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling {
                it.authenticationEntryPoint(authEntryPoint)
                it.accessDeniedHandler(accessDeniedHandler)
            }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.POST, "/api/auth/sign-up", "/api/auth/sign-in").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
                    .anyRequest().authenticated()
            }
            .addFilterBefore(sessionTokenFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}
```

⚠️ **Gotcha:** Circular dependency trap!
If you declare `@Bean fun passwordEncoder(): PasswordEncoder` inside `SecurityConfig`, but `SecurityConfig` injects `SessionTokenAuthenticationFilter`, which injects `AuthService`, which injects `PasswordEncoder`, Spring fails at startup with a circular reference.
Always isolate crypto utilities in a dedicated [`PasswordConfig.kt`](../src/main/kotlin/bookstore/auth/security/PasswordConfig.kt)!
