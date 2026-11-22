package solutions.ex05

import bookstore.NotFoundException
import bookstore.books.BookRepository
import exercises.ex05.BookRating
import exercises.ex05.NewReview
import exercises.ex05.ReviewCreated
import exercises.ex05.TopRated
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.simple.JdbcClient
import org.springframework.stereotype.Repository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import kotlin.time.Clock

@Repository
class Ex05ReviewSolutionRepository(private val jdbc: JdbcClient) {

    fun insert(bookId: Long, rating: Int, text: String?, createdAt: String): Long =
        jdbc.sql("INSERT INTO reviews (book_id, rating, text, created_at) VALUES (:bookId, :rating, :text, :createdAt) RETURNING id")
            .param("bookId", bookId)
            .param("rating", rating)
            .param("text", text)
            .param("createdAt", createdAt)
            .query(Long::class.java)
            .single()

    fun ratingOf(bookId: Long): BookRating =
        jdbc.sql("SELECT avg(rating) AS average, count(*) AS n FROM reviews WHERE book_id = :bookId")
            .param("bookId", bookId)
            .query { rs, _ ->
                val average = rs.getDouble("average").takeUnless { rs.wasNull() }   // SQL NULL → Kotlin null
                BookRating(bookId, average, rs.getInt("n"))
            }
            .single()

    fun topRated(limit: Int): List<TopRated> =
        jdbc.sql(
            """
            SELECT b.id, b.title, avg(r.rating) AS average, count(*) AS n
            FROM reviews r
            JOIN books b ON b.id = r.book_id
            GROUP BY b.id, b.title
            ORDER BY average DESC, n DESC, b.id
            LIMIT :limit
            """.trimIndent(),
        )
            .param("limit", limit)
            .query { rs, _ -> TopRated(rs.getLong("id"), rs.getString("title"), rs.getDouble("average"), rs.getInt("n")) }
            .list()
}

@RestController
@RequestMapping("/solutions/ex05")
class Ex05ReviewSolutionController(
    private val reviews: Ex05ReviewSolutionRepository,
    private val books: BookRepository,
    private val clock: Clock,
) {
    @PostMapping("/books/{bookId}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun add(@PathVariable bookId: Long, @RequestBody review: NewReview): ReviewCreated {
        books.findById(bookId) ?: throw NotFoundException("Book", bookId)
        return ReviewCreated(reviews.insert(bookId, review.rating, review.text, clock.now().toString()))
    }

    @GetMapping("/books/{bookId}/rating")
    fun rating(@PathVariable bookId: Long): BookRating = reviews.ratingOf(bookId)

    @GetMapping("/top-rated")
    fun topRated(@RequestParam(defaultValue = "3") limit: Int): List<TopRated> = reviews.topRated(limit)
}
