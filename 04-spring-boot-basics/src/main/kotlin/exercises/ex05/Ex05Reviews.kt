package exercises.ex05

import bookstore.NotFoundException
import bookstore.books.BookRepository
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

/*
 * Exercise 05 — JdbcClient and Flyway           (lesson: lessons/05-jdbc-client-and-flyway.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex05*'
 *
 * The table already exists — Flyway created it from db/migration/V3__reviews.sql:
 *   reviews(id INTEGER PK, book_id → books, rating 1..5, text, created_at TEXT)
 * The controller is done. Your job: the SQL in Ex05ReviewRepository.
 */

data class NewReview(val rating: Int, val text: String? = null)

data class ReviewCreated(val id: Long)

/** average = null when the book has no reviews yet. */
data class BookRating(val bookId: Long, val average: Double?, val count: Int)

data class TopRated(val bookId: Long, val title: String, val average: Double, val count: Int)

@Repository
class Ex05ReviewRepository(private val jdbc: JdbcClient) {

    /** 05.1 — INSERT a review (created_at = createdAt) and return its new id. Hint: RETURNING id. */
    fun insert(bookId: Long, rating: Int, text: String?, createdAt: String): Long = TODO("05.1")

    /** 05.2 — Average rating (as Double, null if none) and number of reviews for one book. */
    fun ratingOf(bookId: Long): BookRating = TODO("05.2")

    /**
     * 05.3 — The [limit] best books that HAVE reviews: highest average first; ties → more reviews
     * first; still tied → lower book id first. Include the book title (JOIN books).
     */
    fun topRated(limit: Int): List<TopRated> = TODO("05.3")
}

@RestController
@RequestMapping("/exercises/ex05")
class Ex05ReviewController(
    private val reviews: Ex05ReviewRepository,
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
