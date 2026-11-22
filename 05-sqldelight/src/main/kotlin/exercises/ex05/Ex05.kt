package exercises.ex05

import bookstore.db.BookstoreDatabase
import kotlin.time.Clock

/*
 * Exercise 05 — a service on top of SQLDelight          (lesson: lesson05_spring_integration)
 * In the Spring app you'd annotate methods with @Transactional; here (no Spring) you use
 * SQLDelight's own `db.transaction {}` — same idea: all or nothing.
 * Use the existing queries: db.reviewsQueries (insert, selectForBook) and db.booksQueries.selectById.
 * Check: ./gradlew :05-sqldelight:exercises --tests 'exercises.ex05*'
 */

data class NewReview(val bookId: Long, val reviewer: String, val rating: Int, val body: String? = null)

interface ReviewService {
    /**
     * 05.1 — Save a review (created_at = clock.now()) and return its id.
     * rating outside 1..5 → IllegalArgumentException; unknown bookId → NoSuchElementException.
     */
    fun addReview(review: NewReview): Long

    /**
     * 05.2 — Save all reviews atomically and return how many were saved. If ANY of them fails
     * (invalid, unknown book, or the same reviewer twice for one book), save NONE and rethrow.
     */
    fun importReviews(reviews: List<NewReview>): Int

    /** 05.3 — The average rating of a book, or null when it has no reviews. */
    fun averageRating(bookId: Long): Double?
}

class ReviewServiceExercise(private val db: BookstoreDatabase, private val clock: Clock) : ReviewService {
    override fun addReview(review: NewReview): Long = TODO("05.1")
    override fun importReviews(reviews: List<NewReview>): Int = TODO("05.2")
    override fun averageRating(bookId: Long): Double? = TODO("05.3")
}
