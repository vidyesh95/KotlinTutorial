# Lesson 02 — REST controllers

| | |
|---|---|
| **Goal** | Understand HTTP well enough to design an API, then build one with `@RestController`: path variables, query parameters, JSON bodies, status codes, `Location` headers. |
| **Needs** | Lesson 01 |
| **Time** | ~40 min |
| **Next** | [03 — Validation and errors](03-validation-and-errors.md) |
| **Exercise** | `src/main/kotlin/exercises/ex02/` |

## §1. HTTP in five minutes

> 🧒 **Like I'm 5:**
> HTTP is a restaurant.
> - The **request** is the order slip: *what* you want (`GET /api/books/1`), plus notes (headers), and
>   sometimes a filled-in form (the body).
> - The **response** is what comes back to your table: the plate (the body) plus a few words from the
>   waiter (the **status code**). "Here you go" (200). "I made the new dish you asked for, it's at table 6"
>   (201 + Location). "We don't have that" (404). "You filled in the slip wrong" (400). "Someone took the
>   last one" (409). "Oops, the kitchen is on fire" (500).
> - The waiter forgets you after every order. Each request has to carry everything needed to understand it:
>   HTTP is **stateless**.

A request is just text:

```http
POST /api/books HTTP/1.1
Host: localhost:8080
Content-Type: application/json

{"title":"Children of Dune","authorId":1,"isbn":"9780593098240","priceCents":1399}
```

### Methods

| Method | Meaning | Safe? (no changes) | Idempotent? (repeat = same result) |
|---|---|---|---|
| `GET` | read | ✅ | ✅ |
| `POST` | create / run an action | ❌ | ❌ (two POSTs = two books) |
| `PUT` | replace the whole resource | ❌ | ✅ |
| `PATCH` | change some fields | ❌ | usually |
| `DELETE` | remove | ❌ | ✅ (deleting twice leaves it deleted) |

*Idempotent* matters because networks fail. A client can safely retry an idempotent request it never got an
answer to. Retrying a POST might create a duplicate (module 08 shows idempotency keys).

### Status codes this API uses

| Code | Name | When |
|---|---|---|
| 200 | OK | successful GET / PATCH |
| 201 | Created | POST created something; the `Location` header says where |
| 204 | No Content | successful DELETE, empty body |
| 400 | Bad Request | invalid JSON or invalid fields (lesson 03) |
| 404 | Not Found | no such book or author |
| 405 | Method Not Allowed | e.g. `PUT /api/books/1`, which we don't support |
| 409 | Conflict | duplicate ISBN, out of stock |
| 500 | Internal Server Error | a bug, which a client should never see by design |

Rule of thumb: **4xx = the client's fault, 5xx = the server's fault.**

## §2. Designing resources

REST APIs are built around **nouns** (resources) with HTTP **verbs**, not around verbs in URLs:

```text
✅ GET    /api/books/1           ❌ GET  /api/getBook?id=1
✅ POST   /api/books             ❌ POST /api/createBook
✅ DELETE /api/books/1           ❌ POST /api/books/1/delete
✅ POST   /api/orders            (placing an order *creates* an order resource)
```

Conventions used here: plural nouns, ids in the path, filters and paging in the query string
(`?page=0&size=20`), JSON in and out, camelCase field names.

## §3. The controller

[`web/BookController.kt`](../src/main/kotlin/bookstore/web/BookController.kt):

```kotlin
@RestController
@RequestMapping("/api/books")
class BookController(
    private val service: BookService,
    private val props: BookstoreProperties,
) {
    @GetMapping
    fun list(
        @RequestParam(defaultValue = "0") @Min(0) page: Int,
        @RequestParam(defaultValue = "20") @Min(1) size: Int,
    ): PageResponse<BookResponse> { … }

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): BookResponse = service.get(id).toDto()

    @PostMapping
    fun create(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse> { … }

    @PatchMapping("/{id}")
    fun update(@PathVariable id: Long, @Valid @RequestBody request: UpdateBookRequest): BookResponse = …

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun delete(@PathVariable id: Long) = service.delete(id)
}
```

| Annotation | Job |
|---|---|
| `@RestController` | `@Controller` + `@ResponseBody`: whatever a method returns is written to the response as JSON. Also a `@Component`, so component scanning finds it. |
| `@RequestMapping("/api/books")` | URL prefix for every method in the class |
| `@GetMapping`, `@PostMapping`, … | the HTTP method plus an optional extra path |
| `@PathVariable id: Long` | the `{id}` in the URL, converted to `Long`. `/api/books/abc` gives a 400. |
| `@RequestParam(defaultValue = "0") page: Int` | `?page=…` from the query string, with a default |
| `@RequestBody request: CreateBookRequest` | the JSON body, turned into a Kotlin object by Jackson |
| `@ResponseStatus(NO_CONTENT)` | the status to return when the method finishes normally |

Notice what the controller does **not** do: no SQL, no business rules. It converts HTTP into a method call
and the result back into HTTP. Thin controllers are easy to read and easy to test.

> 🧒 **Like I'm 5:** the controller is the waiter. It takes the order, hands it to the kitchen (the
> service), and carries the plate back. The waiter doesn't cook.

## §4. Creating: 201 + Location

```kotlin
@PostMapping
fun create(@Valid @RequestBody request: CreateBookRequest): ResponseEntity<BookResponse> {
    val book = service.create(NewBook(request.title.trim(), request.authorId, request.isbn, request.priceCents, request.stock))
    val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(book.id).toUri()
    return ResponseEntity.created(location).body(book.toDto())
}
```

`ResponseEntity` lets you control the **whole** response: status, headers and body. Use it when the default
"200 with this body" isn't what you want.

### Try it

```bash
curl -s -i -X POST 'localhost:8080/api/books' \
  -H 'Content-Type: application/json' \
  -d '{"title":"Children of Dune","authorId":1,"isbn":"9780593098240","priceCents":1399}'
```

Real response:

```http
HTTP/1.1 201
Location: http://localhost:8080/api/books/6
Content-Type: application/json

{"id":6,"title":"Children of Dune","author":"Frank Herbert","isbn":"9780593098240","priceCents":1399,"price":"13.99 EUR","stock":0,"lowStock":true}
```

`stock` was not in the request. It defaulted to `0` because `CreateBookRequest` declares `val stock: Int = 0`.
The Jackson Kotlin module understands Kotlin default values.

## §5. Reading: paging and path variables

```bash
curl -s "localhost:8080/api/books?page=0&size=2"
```

```json
{
  "items": [
    {"id": 1, "title": "Dune", "author": "Frank Herbert", "isbn": "9780441172719",
     "priceCents": 1299, "price": "12.99 EUR", "stock": 12, "lowStock": false},
    {"id": 2, "title": "Dune Messiah", "author": "Frank Herbert", "isbn": "9780593098233",
     "priceCents": 1199, "price": "11.99 EUR", "stock": 2, "lowStock": true}
  ],
  "page": 0,
  "size": 2,
  "totalItems": 5
}
```

The response type is `PageResponse<BookResponse>`, one **generic** wrapper (module 01, lesson 23) shared by
every list endpoint. `size` is capped at `bookstore.max-page-size` (lesson 04), so a client can't ask for a
million rows.

⚠️ **Gotcha:** always quote URLs containing `?` or `&` in your shell. In zsh, `curl localhost:8080/api/books?size=2`
fails with "no matches found", because `?` is a filename wildcard there.

## §6. Updating and deleting

```bash
curl -s -X PATCH 'localhost:8080/api/books/3' -H 'Content-Type: application/json' -d '{"priceCents":999}'
# {"id":3,"title":"Pride and Prejudice","author":"Jane Austen","isbn":"9780141439518","priceCents":999,"price":"9.99 EUR","stock":30,"lowStock":false}

curl -s -i -X DELETE "localhost:8080/api/books/6"
# HTTP/1.1 204

curl -s -i -X PUT "localhost:8080/api/books/1"
# HTTP/1.1 405
# Allow: PATCH, GET, DELETE
```

`PATCH` with `UpdateBookRequest(priceCents: Int? = null, stock: Int? = null)` changes only the fields you
send. In the repository, `null` turns into "keep the old value" via `coalesce(:priceCents, price_cents)`.
Spring answered the `PUT` with 405 by itself, including an `Allow` header listing what *is* supported.

## §7. JSON: Jackson 3 + the Kotlin module

Spring Boot 4 uses **Jackson 3** (package `tools.jackson`) to convert between JSON and objects. We added
`tools.jackson.module:jackson-module-kotlin`, which teaches Jackson three things:

- to create objects through the **primary constructor**, so data classes with `val`s work and there is no
  need for no-arg constructors or setters;
- what **default values** are (missing `stock` → `0`);
- what **nullability** is: a missing non-null field (for example no `title`) is rejected with a 400.

```bash
curl -s -X POST 'localhost:8080/api/books' -H 'Content-Type: application/json' -d '{"authorId":1}'
# {"detail":"Failed to read request","status":400,"title":"Bad Request"}
```

## §8. DTOs vs domain objects

[`web/Dtos.kt`](../src/main/kotlin/bookstore/web/Dtos.kt) defines the JSON shapes: `CreateBookRequest`,
`UpdateBookRequest`, `BookResponse`, `PageResponse<T>`, and the order DTOs. The domain class
[`books/Book.kt`](../src/main/kotlin/bookstore/books/Book.kt) is separate.

Why not return `Book` directly?

- The API is a **public contract**. Clients break when it changes. DTOs let you rename a database column
  without breaking a mobile app.
- Responses can be *shaped for clients*. `BookResponse` adds a formatted `price` ("12.99 EUR") and a
  `lowStock` flag, and drops `createdAt`.
- Requests contain *only what the client may set*. A client can't send `id` or `stock` on a book create and
  sneak values in (a "mass assignment" bug). 🔐

The mapping is a plain extension function, `fun Book.toResponse(currency, lowStockThreshold) = BookResponse(…)`
(module 01, lesson 20). No mapping library is needed.

> 🏗️ **In the real world:** teams often generate an OpenAPI (Swagger) document from controllers, so frontend
> developers can browse the API. The DTO classes are exactly what ends up in that document.

## Recap

- HTTP = method + path + headers + body → status + headers + body. It's stateless.
- Resources are nouns, methods are verbs. Know your status codes: 2xx ok, 4xx client error, 5xx server error.
- `@RestController` + `@GetMapping`/`@PostMapping`/… + `@PathVariable`/`@RequestParam`/`@RequestBody`.
- `ResponseEntity.created(location).body(dto)` for 201 + `Location`; `@ResponseStatus(NO_CONTENT)` for deletes.
- Jackson + the Kotlin module map JSON ⇄ data classes, including defaults and nullability.
- Keep controllers thin, and keep DTOs separate from domain objects.
