package exercises.ex03

import org.springframework.http.HttpStatus
import org.springframework.http.ProblemDetail
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.RestControllerAdvice
import bookstore.books.BookRepository
import jakarta.validation.Valid

/*
 * Exercise 03 — Validation and errors          (lesson: lessons/03-validation-and-errors.md)
 * Run: ./gradlew :04-spring-boot-basics:exercises --tests 'exercises.ex03*'
 */

/**
 * 03.1 — Add Jakarta Validation annotations (remember the `@field:` prefix!) so that:
 *   bookId  must be positive                        → errors.bookId
 *   rating  must be between 1 and 5                 → errors.rating   (@Min/@Max)
 *   email   must be a non-blank, valid email        → errors.email
 *   text    may be null, but at most 500 characters → errors.text
 * The existing ApiExceptionHandler already turns failures into 400 + "errors": {field: message}.
 */
data class ReviewRequest(
    val bookId: Long,
    val rating: Int,
    val email: String,
    val text: String? = null,
)

data class ReviewAccepted(val bookId: Long, val rating: Int, val accepted: Boolean)

/** Thrown when a review looks like spam. */
class SpamReviewException : RuntimeException("Review looks like spam")

@RestController
@RequestMapping("/exercises/ex03")
class Ex03ReviewController(private val books: BookRepository) {

    /**
     * 03.2 — POST /exercises/ex03/reviews
     *  • book doesn't exist → throw bookstore.NotFoundException("Book", id)        (→ 404)
     *  • text contains "spam" (any letter case) → throw SpamReviewException()       (→ 422, see 03.3)
     *  • otherwise → 201 with ReviewAccepted(bookId, rating, accepted = true)
     */
    @PostMapping("/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    fun submit(@Valid @RequestBody request: ReviewRequest): ReviewAccepted = TODO("03.2")
}

/**
 * An advice that only applies to Ex03ReviewController (assignableTypes), so it doesn't
 * affect the rest of the app.
 */
@RestControllerAdvice(assignableTypes = [Ex03ReviewController::class])
class Ex03ExceptionHandler {

    /**
     * 03.3 — Turn SpamReviewException into status 422 (UNPROCESSABLE_CONTENT) Problem Details
     * with title "Spam detected" and the exception message as detail.
     */
    @ExceptionHandler
    fun spam(e: SpamReviewException): ProblemDetail = TODO("03.3")
}
