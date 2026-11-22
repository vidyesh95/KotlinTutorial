package solutions.ex02

import bookstore.NotFoundException
import exercises.ex02.AuthorDto
import exercises.ex02.CreateAuthorRequest
import org.springframework.http.ResponseEntity
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import kotlin.time.Clock

@RestController
@RequestMapping("/solutions/ex02/authors")
class Ex02AuthorSolutionController(
    private val jdbc: JdbcClient,
    private val clock: Clock,
) {
    @GetMapping
    fun list(): List<AuthorDto> =
        jdbc.sql("SELECT id, name FROM authors ORDER BY id")
            .query { rs, _ -> AuthorDto(rs.getLong("id"), rs.getString("name")) }
            .list()

    @GetMapping("/{id}")
    fun get(@PathVariable id: Long): AuthorDto =
        jdbc.sql("SELECT id, name FROM authors WHERE id = :id")
            .param("id", id)
            .query { rs, _ -> AuthorDto(rs.getLong("id"), rs.getString("name")) }
            .optional()
            .orElseThrow { NotFoundException("Author", id) }

    @PostMapping
    fun create(@RequestBody request: CreateAuthorRequest): ResponseEntity<AuthorDto> {
        val id = jdbc.sql("INSERT INTO authors (name, created_at) VALUES (:name, :now) RETURNING id")
            .param("name", request.name)
            .param("now", clock.now().toString())
            .query(Long::class.java)
            .single()
        val location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(id).toUri()
        return ResponseEntity.created(location).body(AuthorDto(id, request.name))
    }
}
