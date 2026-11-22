package exercises.ex05

import app.cash.sqldelight.db.SqlDriver
import bookstore.db.BookstoreDatabase
import bookstore.db.inMemoryBookstore
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Tag
import solutions.ex05.ReviewServiceSolution
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.time.Clock
import kotlin.time.Instant

abstract class Ex05Spec {
    private val connection: Pair<SqlDriver, BookstoreDatabase> = inMemoryBookstore()
    protected val db: BookstoreDatabase = connection.second
    protected val clock = object : Clock {
        override fun now() = Instant.parse("2026-07-01T12:00:00Z")
    }

    abstract val service: ReviewService

    private fun reviewCount(bookId: Long) = db.reviewsQueries.selectForBook(bookId).executeAsList().size

    @AfterEach
    fun close() {
        connection.first.close()
    }

    @Test
    fun `05_1 add a review`() {
        val id = service.addReview(NewReview(bookId = 6, reviewer = "ursula-fan", rating = 5, body = "Magic"))
        val saved = db.reviewsQueries.selectForBook(6).executeAsList().single()
        assertEquals(id, saved.id)
        assertEquals(clock.now(), saved.created_at)
    }

    @Test
    fun `05_1 invalid reviews are rejected`() {
        assertFailsWith<IllegalArgumentException> { service.addReview(NewReview(6, "x", rating = 6)) }
        assertFailsWith<NoSuchElementException> { service.addReview(NewReview(bookId = 999, reviewer = "x", rating = 3)) }
    }

    @Test
    fun `05_2 import is atomic`() {
        assertEquals(2, service.importReviews(listOf(NewReview(6, "a", 4), NewReview(2, "b", 3))))
        assertFails {
            service.importReviews(listOf(NewReview(6, "c", 5), NewReview(6, "a", 1)))   // "a" already reviewed book 6
        }
        assertEquals(1, reviewCount(6), "the review by c must have been rolled back")
    }

    @Test
    fun `05_3 average rating`() {
        assertEquals(4.5, service.averageRating(1))
        assertNull(service.averageRating(6))
    }
}

class Ex05SolutionTest : Ex05Spec() {
    override val service: ReviewService = ReviewServiceSolution(db, clock)
}

@Tag("exercise")
class Ex05ExerciseTest : Ex05Spec() {
    override val service: ReviewService = ReviewServiceExercise(db, clock)
}
