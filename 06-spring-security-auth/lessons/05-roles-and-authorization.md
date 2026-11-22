# Lesson 05 — Roles & Authorization

| | |
|---|---|
| **Goal** | Distinguish authentication from authorization, enforce route-level rules in `SecurityFilterChain`, and use method security (`@PreAuthorize`) with SpEL. |
| **Needs** | Lesson 04 |
| **Time** | ~25 min |
| **Next** | [06 — Problem Details & CSRF](06-errors-problem-details-and-csrf.md) |
| **Exercise** | `src/main/kotlin/exercises/ex03/` |

---

## §1. Authentication vs Authorization

- **Authentication (401)**: "Who are you?" (Verifying identity via password or session token).
- **Authorization (403)**: "Are you allowed to do this?" (Checking if the authenticated user has permission).

---

## §2. Route-level authorization (`requestMatchers`)

Route-level rules are configured centrally in [`SecurityConfig.kt`](../src/main/kotlin/bookstore/auth/security/SecurityConfig.kt):

```kotlin
.authorizeHttpRequests { auth ->
    auth
        .requestMatchers(HttpMethod.POST, "/api/auth/sign-up", "/api/auth/sign-in").permitAll()
        .requestMatchers(HttpMethod.GET, "/api/books", "/api/books/**").permitAll()
        .requestMatchers("/api/admin/**").hasRole("ADMIN")
        .requestMatchers(HttpMethod.POST, "/api/books").hasRole("ADMIN")
        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasRole("ADMIN")
        .anyRequest().authenticated()
}
```

⚠️ **Gotcha: The `ROLE_` prefix in Spring Security**
When you write:
```kotlin
.hasRole("ADMIN")
```
Spring Security internally checks for the authority `"ROLE_ADMIN"`.
In [`Models.kt`](../src/main/kotlin/bookstore/auth/domain/Models.kt), our `UserPrincipal` returns:
```kotlin
override fun getAuthorities(): Collection<GrantedAuthority> =
    listOf(SimpleGrantedAuthority("ROLE_${role.name}"))
```
If you omit the `ROLE_` prefix in `GrantedAuthority`, `.hasRole("ADMIN")` will fail!

---

## §3. Method security (`@PreAuthorize`)

Route-level matching is great for URL patterns, but business logic often requires finer checks:
- "Only the user who owns this order can view it"
- "Only users with role ADMIN can trigger this action"

Enable it with `@EnableMethodSecurity` on a `@Configuration` class:

```kotlin
@RestController
@RequestMapping("/api/books")
class BookController(private val bookRepository: BookRepository) {

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    fun createBook(@RequestBody request: CreateBookRequest): ResponseEntity<Book> {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookRepository.create(request))
    }
}
```

### SpEL: Checking resource ownership
You can reference method arguments and the authenticated principal using Spring Expression Language (SpEL):

```kotlin
@GetMapping("/user-data/{userId}")
@PreAuthorize("hasRole('ADMIN') or (principal instanceof T(bookstore.auth.domain.UserPrincipal) and principal.id == #userId)")
fun getUserData(@PathVariable userId: String): UserData {
    return userService.getDataFor(userId)
}
```

Try exercise 03:
```bash
./gradlew :06-spring-security-auth:exercises --tests 'exercises.ex03*'
```
