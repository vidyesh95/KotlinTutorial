package solutions.ex05

import bookstore.db.BookstoreDatabase
import exercises.ex05.NewReview
import exercises.ex05.ReviewService
import kotlin.time.Clock

class ReviewServiceSolution(private val db: BookstoreDatabase, private val clock: Clock) : ReviewService {

    override fun addReview(review: NewReview): Long {
        require(review.rating in 1..5) { "rating must be 1..5, was ${review.rating}" }
        db.booksQueries.selectById(review.bookId).executeAsOneOrNull()
            ?: throw NoSuchElementException("Book ${review.bookId} not found")
        return db.reviewsQueries.insert(review.bookId, review.reviewer, review.rating, review.body, clock.now()).executeAsOne()
    }

    override fun importReviews(reviews: List<NewReview>): Int =
        db.transactionWithResult {
            reviews.forEach(::addReview)   // nested call joins this transaction; any exception rolls back all
            reviews.size
        }

    override fun averageRating(bookId: Long): Double? {
        val ratings = db.reviewsQueries.selectForBook(bookId).executeAsList().map { it.rating }
        return if (ratings.isEmpty()) null else ratings.average()
    }
}
