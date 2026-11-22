package exercises.ex02

import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Clock

/*
 * Exercise 02 — REST controllers               (lesson: lessons/02-rest-controllers.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex02*'
 *
 * Build a small authors API on the existing `authors` table (id, name, created_at).
 * Write the SQL yourself with JdbcClient — look at bookstore/books/BookRepository.kt for examples.
 */

data class AuthorDto(val id: Long, val name: String)

data class CreateAuthorRequest(val name: String)

@RestController
@RequestMapping("/exercises/ex02/authors")
class Ex02AuthorController(
    private val jdbc: JdbcClient,
    private val clock: Clock,
) {
    /** 02.1 — GET /exercises/ex02/authors → all authors as [{"id":1,"name":"Frank Herbert"}, …], ordered by id. */
    @GetMapping
    fun list(): List<AuthorDto> = TODO("02.1")

    /**
     * 02.2 — GET /exercises/ex02/authors/{id} → one author, or 404 when it doesn't exist.
     * Hint: throw bookstore.NotFoundException("Author", id) — ApiExceptionHandler turns it into a 404.
     */
    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): AuthorDto = TODO("02.2")

    /**
     * 02.3 — POST /exercises/ex02/authors with {"name": "Ursula K. Le Guin"}
     * → insert (created_at = clock.now().toString()), respond 201 Created with a
     *   Location header ".../exercises/ex02/authors/{newId}" and the new author as body.
     * Hint: see BookController.create for ResponseEntity.created(...).
     */
    @PostMapping
    fun create(@RequestBody request: CreateAuthorRequest): ResponseEntity<AuthorDto> = TODO("02.3")
}
