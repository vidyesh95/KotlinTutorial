# Lesson 03 — Validation and error handling

| | |
|---|---|
| **Goal** | Reject bad input before it reaches your business logic, and turn every error into a consistent, useful JSON response. |
| **Needs** | Lesson 02 |
| **Time** | ~35 min |
| **Next** | [04 — Configuration](04-configuration.md) |
| **Exercise** | `src/main/kotlin/exercises/ex03/` |

## §1. Never trust input

Anyone can send anything to your API: a negative price, a 5 MB title, an ISBN like `"hello"`. Your code
should check input **once, at the edge**, so services can assume the data is sane.

> 🧒 **Like I'm 5:**
> A nightclub bouncer checks everybody at the door: ID, age, dress code. Inside, the bartender doesn't
> re-check ages. Validation is the bouncer, and the service is the bartender.

## §2. Jakarta Validation annotations

[`web/Dtos.kt`](../src/main/kotlin/bookstore/web/Dtos.kt):

```kotlin
data class CreateBookRequest(
    @field:NotBlank @field:Size(max = 200)
    val title: String,
    @field:Positive
    val authorId: Long,
    @field:Pattern(regexp = "\\d{13}", message = "must be exactly 13 digits")
    val isbn: String,
    @field:PositiveOrZero
    val priceCents: Int,
    @field:PositiveOrZero
    val stock: Int = 0,
)
```

| Annotation | Rule |
|---|---|
| `@NotBlank` | not null, and not empty or only spaces (strings) |
| `@NotEmpty` | not null and not empty (strings, collections) |
| `@Size(min, max)` | length or size limits |
| `@Positive` / `@PositiveOrZero` / `@Min` / `@Max` | number ranges |
| `@Pattern(regexp)` | must match a regex (module 01, lesson 33) |
| `@Email` | looks like an email address |
| `@Valid` | "also validate the object inside this field", e.g. each element of a list |

The rules run when a parameter is marked `@Valid`:

```kotlin
@PostMapping
fun create(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse>
```

Nested validation, from the order DTOs:

```kotlin
data class PlaceOrderRequest(
    @field:Email @field:NotBlank val customerEmail: String,
    @field:NotEmpty @field:Valid val lines: List<OrderLineRequest>,   // @Valid → validate each line too
)
data class OrderLineRequest(
    @field:Positive val bookId: Long,
    @field:Positive @field:Max(100) val quantity: Int,
)
```

## §3. Why `@field:`?

In Kotlin, `val title: String` inside a constructor declares several things at once: a **constructor
parameter**, a **property**, a backing **field**, and a **getter**. An annotation written there could belong
to any of them. Jakarta Validation reads annotations on **fields** (or getters). If the annotation lands on
the constructor parameter only, the validator never sees it and the rule silently does nothing.

`@field:NotBlank` uses a *use-site target* (module 01, lesson 17 / 22) to say exactly where the annotation
goes.

🆕 **Kotlin 2.4:** the rules for where an annotation *without* a target goes on a constructor property were
revised and are now stable (look for "annotation use-site targets" in the Kotlin 2.2/2.4 "What's new" pages).
Kotlin 2.4 also stabilised the `@all:` meta-target, which applies an annotation to every applicable place
(`@all:NotBlank val title: String`). We still write `@field:` everywhere: it is explicit, behaves the same on
every Kotlin version you'll meet in real projects, and is what most Spring + Kotlin code uses.

⚠️ **Gotcha:** if validation "doesn't work", check the target first. Then check that the parameter has
`@Valid`, and that `spring-boot-starter-validation` is on the classpath.

## §4. What a validation failure looks like

```bash
curl -s -i -X POST 'localhost:8080/api/books' -H 'Content-Type: application/json' \
  -d '{"title":" ","authorId":1,"isbn":"123","priceCents":-5}'
```

Real response:

```http
HTTP/1.1 400
Content-Type: application/problem+json
```
```json
{
  "detail": "Validation failed",
  "status": 400,
  "title": "Bad Request",
  "errors": {
    "title": "must not be blank",
    "isbn": "must be exactly 13 digits",
    "priceCents": "must be greater than or equal to 0"
  }
}
```

The same works for nested lists. Field names show the path:

```bash
curl -s -X POST 'localhost:8080/api/orders' -H 'Content-Type: application/json' \
  -d '{"customerEmail":"nope","lines":[{"bookId":0,"quantity":500}]}'
```
```json
{
  "detail": "Validation failed", "status": 400, "title": "Bad Request",
  "errors": {
    "lines[0].bookId": "must be greater than 0",
    "customerEmail": "must be a well-formed email address",
    "lines[0].quantity": "must be less than or equal to 100"
  }
}
```

## §5. Validating `@RequestParam`s

Constraints can sit directly on controller method parameters:

```kotlin
@GetMapping
fun list(
    @RequestParam(defaultValue = "0") @Min(0) page: Int,
    @RequestParam(defaultValue = "20") @Min(1) size: Int,
): PageResponse<BookResponse>
```

Spring Framework 6.1+ validates these automatically ("method validation"). You don't need `@Validated` on the
class.

```bash
curl -s "localhost:8080/api/books?page=-1"
# {"detail":"Validation failure","status":400,"title":"Bad Request"}
```

## §6. Problem Details: one error format for everything

Every error from this API, whatever caused it, has the same shape. That shape is **Problem Details**
(RFC 9457), served as `application/problem+json`:

```json
{ "detail": "Book 999 not found", "status": 404, "title": "Not Found" }
```

| Field | Meaning |
|---|---|
| `status` | the HTTP status, repeated for convenience |
| `title` | a short, generic summary ("Not Found", "Out of stock") |
| `detail` | what happened *this time* ("Book 999 not found") |
| `type` | a URI naming the error kind; defaults to `about:blank` and is then left out |
| `instance` | the specific occurrence, often the request path (optional) |
| anything else | extra machine-readable fields, e.g. `errors`, `bookId` |

Clients can write ONE piece of error-handling code for your whole API. That's the point of a standard.

Two settings make Spring itself use this format:
- `spring.mvc.problemdetails.enabled: true` in [`application.yml`](../src/main/resources/application.yml)
- our handler extends `ResponseEntityExceptionHandler` (next section)

So even errors we never wrote code for come back as Problem Details:

```bash
curl -s -X POST 'localhost:8080/api/books' -H 'Content-Type: application/json' -d 'not json'
# {"detail":"Failed to read request","status":400,"title":"Bad Request"}

curl -s -X PUT "localhost:8080/api/books/1"
# {"detail":"Method 'PUT' is not supported.","status":405,"title":"Method Not Allowed"}
```

## §7. Domain exceptions → HTTP: `ApiExceptionHandler`

Services must not know about HTTP. They don't return status codes; they throw **domain exceptions**
([`BookstoreExceptions.kt`](../src/main/kotlin/bookstore/BookstoreExceptions.kt)):

```kotlin
sealed class BookstoreException(message: String) : RuntimeException(message)
class NotFoundException(what: String, id: Any) : BookstoreException("$what $id not found")
class ConflictException(message: String) : BookstoreException(message)
class OutOfStockException(val bookId: Long, requested: Int, available: Int) : BookstoreException(…)
```

A `sealed` hierarchy (module 01, lesson 18) gives a closed, known set of error kinds. They extend
`RuntimeException` on purpose, because lesson 06 explains that only those trigger a rollback by default.

ONE class translates them into HTTP:
[`web/ApiExceptionHandler.kt`](../src/main/kotlin/bookstore/web/ApiExceptionHandler.kt)

```kotlin
@RestControllerAdvice
class ApiExceptionHandler : ResponseEntityExceptionHandler() {

    @ExceptionHandler
    fun notFound(e: NotFoundException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.message)

    @ExceptionHandler
    fun outOfStock(e: OutOfStockException): ProblemDetail =
        ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.message).apply {
            title = "Out of stock"
            setProperty("bookId", e.bookId)
        }

    override fun handleMethodArgumentNotValid(…): ResponseEntity<Any>? {
        val problem = ex.body.apply {
            detail = "Validation failed"
            setProperty("errors", ex.bindingResult.fieldErrors.associate { it.field to it.defaultMessage })
        }
        return handleExceptionInternal(ex, problem, headers, status, request)
    }
    …
}
```

- `@RestControllerAdvice` means "applies to all controllers". It's a `@Component`, so component scanning
  picks it up.
- `@ExceptionHandler` methods are chosen **by the parameter type**: a `NotFoundException` thrown anywhere
  during a request lands in `notFound`.
- Overriding `handleMethodArgumentNotValid` adds the `errors` map. Out of the box Spring only says
  "Invalid request content.", which isn't helpful for a client showing errors next to form fields.

> 🧒 **Like I'm 5:** the kitchen (service) shouts "we're out of Emma!" The head waiter (exception handler)
> turns that into a polite, standard note for the customer: "409 — Out of stock — Book 4". Cooks don't
> talk to customers.

```bash
curl -s -X POST 'localhost:8080/api/orders' -H 'Content-Type: application/json' \
  -d '{"customerEmail":"bob@example.com","lines":[{"bookId":1,"quantity":1},{"bookId":4,"quantity":1}]}'
# {"detail":"Book 4: requested 1, only 0 in stock","status":409,"title":"Out of stock","bookId":4}
```

## §8. 🔐 Don't leak internals

What an error response must **never** contain:
- stack traces (they reveal library versions, class names and file paths);
- SQL text or database error messages (they reveal your schema and help with SQL injection);
- whether a user exists, in auth flows ("wrong password" vs "no such user" helps attackers find valid
  accounts; module 06).

Log the details server-side and send the client a safe summary. Spring Boot's defaults already hide stack
traces (`server.error.include-stacktrace: never`). Keep it that way in production.

⚠️ **Gotcha:** a "catch-all" `@ExceptionHandler(Exception::class)` that returns `e.message` can leak exactly
those things. If you add one, return a generic "Internal error" and log the exception.

> 🏗️ **In the real world:** frontends map `errors` keys to form fields. Mobile apps switch on `status` or
> `type`. Keep the format stable: it's part of your API contract, just like the DTOs.

## Recap

- Validate at the edge with Jakarta annotations plus `@Valid`. Write `@field:` on Kotlin constructor properties.
- Nested objects and lists need `@Valid` on the field; `@RequestParam` constraints are validated automatically.
- Use one error format, Problem Details (`application/problem+json`).
- Services throw domain exceptions, and one `@RestControllerAdvice` maps them to HTTP.
- Never leak stack traces, SQL or account-existence hints in error responses.
